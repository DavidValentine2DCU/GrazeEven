package ie.dcu.graze_even_slave;

import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.FileOutputStream;
import java.util.Scanner;

import ie.dcu.graze_even_slave.communication.Transmitter;


public class SlaveActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, ie.dcu.graze_even_slave.Updatable,LocationListener{
    private static final int SLEEP_TIME = 10000;//How often we want to transmit the update if there is one
    private GoogleApiClient googleApiClient = null;//client to access google play services
    private boolean transmitUpdate = false;//flag indicating whether we want to transmit an update or not
    private Location lastLocation = null;//Record of the last known host device location
    private Transmitter transmitter = null;//The transmitter object to transmit to the master
    private boolean playServicesEnabled = true;//by default we expect google play services to be enabled
    private MotionHandler motionHandler = new MotionHandler(this);//the motion handler object for this application

    /*
    Function Name: setAppName
    Return Type: void
    Access Type: private
    Purpose:
    To set the name of this application instance ie. the name of the horse.
    Parameters:
    String newName, representing the name to set the application instance name to.
    PreConditions: It is required to set the name of this application instance to a specified String.
    PostConditions: The name of this application instance has been set to the specified String.
     */
    private void setAppName(String newName)
    {
        EditText editText = (EditText)findViewById(R.id.nameBar);
        editText.setText(newName);
        try{
            if(transmitter.getAppId()!=null){
                Log.d("Name Update","Transmitting name update");
                transmitter.transmit("NAME_UPDATE:"+newName);
            }
        }
        catch(Exception e){
            Log.e("Transmitting name update",e.toString(),e);
        }
        transmitter.setAppId(newName);
    }
    /*
    Function Name: writeToFile
    Return Type: void
    Access Type: private
    Purpose:
    To save the name of this application instance ie. the name of the horse by writing it to a file.
    Parameters:
    None
    PreConditions: It is required to write out the application name to a file.
    PostConditions: The application name has been written out to a file.
     */
    private void writeToFile()
    {
        try
        {
            FileOutputStream fos = openFileOutput("App_Data.txt", Context.MODE_PRIVATE);
            String dataString = transmitter.getAppId();
            fos.write(dataString.getBytes());
            fos.close();
            Dialog dialog = new Dialog(this);
            dialog.setTitle("Saved!");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
        catch(Exception e)
        {
            Log.e("ERROR:Writing",e.getMessage(),e);
        }
    }


    /*
    Function Name: setUpdate
    Return Type: void
    Access Type: private synchronized
    Purpose:
    To set the transmit update value to the given value, true if we want to transmit an update false otherwise.
    Parameters:
    boolean value, representing whether or not we wish to transmit an update
    PreConditions: It is required to indicate whether or not we wish to transmit an update.
    PostConditions: The boolean value indicating whether or not we wish to transmit an update is set to the specified value.
     */
    private synchronized void setUpdate(boolean value) {
        transmitUpdate = value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_slave);
        //cheack if google play services are enabled
        playServicesEnabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())==ConnectionResult.SUCCESS;
        //if they are and the client isnt initialised yet initialise it
        if(googleApiClient==null&& playServicesEnabled) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        //set the onclick listener to the save button which sets the contents of the edittext to be the app name and writes it to file
        ImageButton iButton = (ImageButton)findViewById(R.id.save);
        iButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText)findViewById(R.id.nameBar);
                setAppName(editText.getText().toString());
                writeToFile();
            }
        });
        //initialise the transmitter
        try {
            this.transmitter = new Transmitter(this);
        }
        catch(Exception e) {
            Log.e("Transmission Init",e.toString(),e);
        }
        //Get a previously saved app name and set it or get a new app name and set it if no previous one exists
        String appName = "";
        try
        {
            Scanner reader = new Scanner(openFileInput("App_Data.txt"));
            appName += reader.nextLine();
            reader.close();
        }catch (Exception e){
            Log.e("ERROR:Reading",e.toString(),e);
            appName+=("Slave "+Math.random()*10000);
        }
        setAppName(appName);
        //create and start new thread to transmit to the master
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Transmitting","Beginning Transmitting");
                while(true) {
                    //is we want to transmit an update
                    if(transmitUpdate) {
                        //if we have our location data
                        if (lastLocation != null) {
                            try {
                                Log.d("Transmission","Transmitted update to master");
                                //transmit out location and set that we dont want ti transmit any more updates
                                transmitter.transmit(lastLocation.getLatitude() + "," + lastLocation.getLongitude());
                                setUpdate(false);
                            } catch (Exception obe) {
                                Log.e("Transmission Error: Update()", obe.toString(), obe);
                            }
                        } else {
                            Log.e("Update", "Location information not available");
                        }
                    }
                    else
                        Log.d("Transmission","No update to transmit this time");
                    //sleep for a while
                    try{
                        Thread.sleep(SLEEP_TIME);
                    }
                    catch (Exception e){
                        Log.e("Update Sleeping error", e.toString(), e);
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        //when the app is not running we dont want to trigger event listeners so unregister them
        try {
            if (motionHandler != null) {
                Log.d("Motion:", "Unregistering listener");
                SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
                sm.unregisterListener(motionHandler, sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0));
                sm.unregisterListener(motionHandler, sm.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0));
            }
            //if google play services are enabled use them otherwise use native android
            if (googleApiClient != null)
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            else if(!playServicesEnabled)
            {
                LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                lm.removeUpdates(this);
            }
        }
        catch(Exception e){
            Log.e("Pause Error:",e.toString(),e);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        //when the app is showing we do want to trigger event listeners so enable them
        if(motionHandler!=null) {
            Log.d("Motion:", "Registering listener");
            SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            sm.registerListener(motionHandler, sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
            sm.registerListener(motionHandler, sm.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
        //if google play services are enabled use them otherwise use native android
        if(googleApiClient!=null)
            googleApiClient.connect();
        else if(!playServicesEnabled){
            Log.d("Location:", "Registering native listener");
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Criteria crits = new Criteria();
            crits.setAccuracy(Criteria.ACCURACY_FINE);
            lm.requestLocationUpdates(lm.getBestProvider(crits,true),1000,1,this);
            lastLocation = lm.getLastKnownLocation(lm.getBestProvider(crits,true));
        }
        super.onResume();
    }

    //method which simply registers that we want to transmit an update
    @Override
    public void update() {
        setUpdate(true);
    }
    //if the device location is changed update the record of the device location
    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            Log.d("Location:","Setting Location Handler");
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, this);
            Log.d("Location:","Location Handler Set");
        }
        catch(Exception e){
            Log.e("Setting Handler Error:",e.toString(),e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            Log.d("Location:","Location Handler Removed");
        }
        catch(Exception e){
            Log.e("Removing Handler Error:",e.toString(),e);
        }
    }

    //The following methods must be overridden but we're not too bothered about them
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Method which usually inflates an options menu when the settings button is clicked. This implementation suppresses that.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}