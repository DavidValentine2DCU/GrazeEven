package ie.dcu.graze_even_master;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import ie.dcu.graze_even_master.masterutils.MasterUtils;
import ie.dcu.graze_even_master.masterutils.Polygon;

public class FieldActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Implementation of Google's LatLng class using BigDecimals rather than doubles for accuracy of operations
    private class BigDecimalLatLng{
        public BigDecimal longitude;
        public BigDecimal latitude;
        public BigDecimalLatLng(BigDecimal latitude, BigDecimal longitude)
        {
            this.longitude = longitude;
            this.latitude = latitude;
        }
        public BigDecimalLatLng(double latitude, double longitude)
        {
            this(new BigDecimal(latitude).setScale(MasterUtils.MAX_DECIMAL_PLACES,RoundingMode.FLOOR),new BigDecimal(longitude).setScale(MasterUtils.MAX_DECIMAL_PLACES, RoundingMode.FLOOR));
        }
        public String toString() {
            return longitude.toString()+","+latitude;
        }
    }
    private Bundle resultBundle = new Bundle();
    private PolygonOptions fieldOutline = new PolygonOptions().strokeColor(Color.BLACK).strokeWidth(5).visible(true);
    private Polygon polygon = new Polygon();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient googleApiClient = null;
    private boolean hasPoints = false;
    private SlaveDevice [] slaves;

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //set the map type to be hybrid ie. satilite imagery overlayed with road maps
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //set a listener to see if the user presses down on the map(long click rather than short click to help avoid mistakes)
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //get the round the latlng object to the max number of decimal places
                LatLng newLatLng = new LatLng(roundToMax(latLng.latitude).doubleValue(), roundToMax(latLng.longitude).doubleValue());
                //add the new latlng object to the field outline
                fieldOutline.add(newLatLng);
                polygon.addPoint(newLatLng.longitude,newLatLng.latitude);
                //clear the current visual of the field outline
                mMap.clear();
                if (fieldOutline.getPoints().size() <= 1) {
                    //if theres only one point then make that point visible by surrounding it in a circle
                   mMap.addCircle(new CircleOptions().strokeWidth(5).radius(5).fillColor(Color.BLACK).center(newLatLng).strokeColor(Color.BLACK));
                }
                addSlaveMarkers();
                //add and display teh field outline
                mMap.addPolygon(fieldOutline);
            }
        });
        if(hasPoints) {
            mMap.addPolygon(fieldOutline);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getCenterPoint(), mMap.getMaxZoomLevel() - 3));
                }
            });
        }
        else {
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(), mMap.getMaxZoomLevel() - 3));
                }
            });
        }
        addSlaveMarkers();
    }
    /*
    Function Name: addSlaveMarkers
    Return Type: void
    Access Type: private
    Purpose:
    To add markers to the map at the last known location of slave devices with additional data about the slave.
    Parameters:
    long time, a time, the distance to which is to be calculated
    PreConditions: It is required a string representing the time elapsed between the current system time and a specified time be known.
    PostConditions: A string representing the time elapsed between the current system time and the specified time is returned.
     */
    private void addSlaveMarkers()
    {
        try{
            for(SlaveDevice slave:slaves)
            {
                mMap.addMarker(new MarkerOptions().position(slave.getLatLng()).title(slave.name+"'s last known location("+getTime(slave.lastUpdateTime)+" ago)"));
            }
        }
        catch(Exception e)
        {
            Log.e("Setting animal Markers",e.toString(),e);
        }
    }
    /*
    Function Name: getTime
    Return Type: String
    Access Type: private
    Purpose:
    To return a text string which represents the amount of time that has passed since the specified time.
    Parameters:
    long time, a time, the distance to which is to be calculated
    PreConditions: It is required a string representing the time elapsed between the current system time and a specified time be known.
    PostConditions: A string representing the time elapsed between the current system time and the specified time is returned.
     */
    private String getTime(long time)
    {
        long difference = Math.abs(System.currentTimeMillis() - time);
        long numMinutes = difference/60000;
        if(numMinutes == 0)
            return "Less than 1 minute";
        if(numMinutes == 1)
            return "1 minute";
        else if(numMinutes>0&&numMinutes<60)
            return numMinutes+" minutes";
        else if(numMinutes>=60)
        {
            long numHours = numMinutes/60;
            if(numHours == 1)
                return "1 hour";
            else if(numHours>0&&numHours<24)
                return numHours+" hours";
            else if(numHours>=24)
            {
                long numDays = numHours/24;
                if(numDays==1)
                    return "1 day";
                else if(numDays>0)
                    return numDays+" days";
            }
        }
        return "An indeterminate amount of time";
    }
    /*
    Function Name: getCenterPoint
    Return Type: LatLng
    Access Type: private
    Purpose:
    To return a LatLng which represents the center of the field outline.
    Parameters:
    None
    PreConditions: It is required that the center of the field outline be known.
    PostConditions: The LatLng representing the center of the field outline is returned.
     */
    private LatLng getCenterPoint()
    {
        List<LatLng> points = fieldOutline.getPoints();
        LatLng first = points.get(0);
        //get the maximum points of the heatmap
        double northmost = first.latitude, southmost = first.latitude, eastmost = first.longitude, westmost = first.longitude;
        for (int index = 1; index < points.size(); index++) {
            LatLng current = points.get(index);
            double latitude = current.latitude, longitude = current.longitude;
            if (latitude < southmost)
                southmost = latitude;
            if (latitude > northmost)
                northmost = latitude;
            if (longitude < eastmost)
                eastmost = longitude;
            if (longitude > westmost)
                westmost = longitude;
        }
        //get the middle of the outline
        double middleY = westmost +((eastmost-westmost)/2);
        double middleX = southmost +((northmost-southmost)/2);
        return new LatLng(middleX,middleY);
    }
    /*
    Function Name: roundToMax
    Return Type: BigDecimal
    Access Type: private
    Purpose:
    To return a BigDecimal representing the specified double value rounded to the maximum number of decimal places allowed by the app.
    Parameters:
    double startingValue, the double value to be rounded
    PreConditions: It is required to round a double value to the maximum number of decimal places allowed by the app.
    PostConditions: The BigDecimal representing the rounded double value is returned.
     */
    private BigDecimal roundToMax(double startingValue) {
        BigDecimal decimal = new BigDecimal(startingValue);
        return decimal.setScale(MasterUtils.MAX_DECIMAL_PLACES,RoundingMode.FLOOR);
    }

    /*
    Function Name: getMyLocation
    Return Type: LatLng
    Access Type: private
    Purpose:
    To return the last known location of the device as a LatLng object.
    Parameters:
    None
    PreConditions: It is required to find the last known location of the device.
    PostConditions: The LatLng representing the last known location of the device is returned.
     */
    private LatLng getMyLocation() {
        Location myLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        return new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
    }

    /*
    Function Name: saveData
    Return Type: void
    Access Type: private
    Purpose:
    To save the outline data to the result bundle and finish the current activity.
    Parameters:
    None
    PreConditions: It is required that a outline data be saved to a bundle.
    PostConditions: The outline data has been saved to the result bundle and the activity has finished.
     */
    private void saveData() {
        doHeatmapCalculations();
        Intent result = new Intent();
        result.putExtras(resultBundle);
        setResult(RESULT_OK, result);
        finish();
    }

    /*
    Function Name: doHeatmapCalculations
    Return Type: void
    Access Type: private
    Purpose:
    To calculate the vertices of the heatmap both as world coordinates and as heatmap coordinates, store these
    in the result bundle along with the height and width of teh heatmap and the north- and east-most points of the heatmap.
    Parameters:
    None
    PreConditions: It is required that the vertices, width, height and start coordinates of the heatmap be calculated.
    PostConditions: The vertices, width, height and start coordinates of the
    heatmap have been calculated and put in the result bundle.
     */
    private void doHeatmapCalculations() {
        //Get the max and min of longitude and latitude
        List<LatLng> points = fieldOutline.getPoints();
        LatLng first = points.get(0);
        //get the maximum points of the heatmap
        double northmost = first.latitude, southmost = first.latitude, eastmost = first.longitude, westmost = first.longitude;
        for (int index = 1; index < points.size(); index++) {
            LatLng current = points.get(index);
            double latitude = current.latitude, longitude = current.longitude;
            if (latitude < southmost)
                southmost = latitude;
            if (latitude > northmost)
                northmost = latitude;
            if (longitude > eastmost)
                eastmost = longitude;
            if (longitude < westmost)
                westmost = longitude;
        }
        //get the northeast most and southwest most corners of the outline
        BigDecimalLatLng topRight = new BigDecimalLatLng(northmost, eastmost), bottomLeft = new BigDecimalLatLng(southmost, westmost);
        Log.d("TopRight",topRight.toString());Log.d("BottomLeft",bottomLeft.toString());
        //using these get the width and height
        int width = Math.abs(topRight.longitude.subtract(bottomLeft.longitude).movePointRight(MasterUtils.MAX_DECIMAL_PLACES).intValue()) + 1, length = Math.abs(topRight.latitude.subtract(bottomLeft.latitude).movePointRight(MasterUtils.MAX_DECIMAL_PLACES).intValue()) + 1;
        generateShapedHeatmap(topRight.latitude, topRight.longitude);
        //put the northeast most and southwest most points of the outline,
        //the length and width of the outline and the polygon of vertices in the bundle
        resultBundle.putDouble("Top", northmost);
        resultBundle.putDouble("Left", eastmost);
        resultBundle.putInt("Length", length);
        resultBundle.putInt("Width", width);
        resultBundle.putString("Vertices",polygon.toString());
    }
    /*
    Function Name: generateShapedHeatmap
    Return Type: void
    Access Type: private
    Purpose:
    To translate the polygon vertices from world coordinates to heatmap indexes and put them in the result bundle
    Parameters:
    BigDecimal top, the north-most point in the heatmap
    BigDecimal right, the east-most point in the heatmap
    PreConditions: It is required that the vertices, width, height and start coordinates of the heatmap be calculated.
    PostConditions: The vertices have been translated from world coordinates to heatmap indexes and put in the result bundle.
     */
    private void generateShapedHeatmap(BigDecimal top, BigDecimal right) {
        Polygon newPolygon = new Polygon();
        for (int index = 0; index < polygon.getNumVertices(); index++) {
            newPolygon.addPoint(right.subtract(new BigDecimal(polygon.getXPoints()[index])).movePointRight(MasterUtils.MAX_DECIMAL_PLACES).intValue(), top.subtract(new BigDecimal(polygon.getYPoints()[index])).movePointRight(MasterUtils.MAX_DECIMAL_PLACES).intValue());
        }
        Log.d("VerticesAsInts", newPolygon.toString());
        resultBundle.putString("VerticesAsInts", newPolygon.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle startBundle = getIntent().getExtras();
        try{
            Polygon polygon = new Polygon(startBundle.getString("Polygon"));
            double [] xPoints = polygon.getXPoints();
            double [] yPoints = polygon.getYPoints();
            for(int index = 0;index<polygon.getNumVertices();index++)
            {
                Log.d("PassedPolygon",yPoints[index]+","+xPoints[index]);
                fieldOutline.add(new LatLng(yPoints[index],xPoints[index]));
            }
            hasPoints = polygon.getNumVertices()>0;
        }
        catch(Exception e)
        {
            Log.e("Getting Polygon",e.toString(),e);
            hasPoints = false;
        }
        try{
            String [] slaveDevices = startBundle.getStringArray("Animals");
            slaves = new SlaveDevice[slaveDevices.length];
            for(int index = 0;index<slaveDevices.length;index++){
                Log.d("Getting animals",slaveDevices[index]);
                slaves[index] = new SlaveDevice(slaveDevices[index]);
            }
        }
        catch(Exception e){
            Log.e("Getting animals",e.toString(),e);
        }
        setContentView(R.layout.activity_field);
        //to get out location we will need to connect to google play services so do that now
        this.googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        //set up the map if needed
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    //method to inflate a menu when the settings button is clicked
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.field_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //in the case that the user selects the clear outline option
            case R.id.clearFieldOutline:
                //clears the visual of the field outline, the points of the fieldoutline and adds a marker to the devices location
                mMap.clear();
                fieldOutline = new PolygonOptions().strokeColor(Color.BLACK).strokeWidth(5).visible(true);
                polygon = new Polygon();
                mMap.addMarker(new MarkerOptions().position(getMyLocation()).title("My Location"));
                addSlaveMarkers();
                break;
            case R.id.saveFieldOutline:
                //save the field outline data
                saveData();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //when the google services client connects
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Location", "Connected, Getting location now");
        //get the devices current location, set a marker on it, move the camera to it and zoom in to the max level
        LatLng myLocationPointer = getMyLocation();
        mMap.addMarker(new MarkerOptions().position(myLocationPointer).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocationPointer));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getMaxZoomLevel()));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
