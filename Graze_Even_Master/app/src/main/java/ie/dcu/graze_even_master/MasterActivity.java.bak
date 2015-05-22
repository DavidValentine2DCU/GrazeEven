package ie.dcu.graze_even_master;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import ie.dcu.graze_even_master.communication.MessageHandler;
import ie.dcu.graze_even_master.communication.ReceiverThread;
import ie.dcu.graze_even_master.masterutils.ColorIterator;
import ie.dcu.graze_even_master.display.DisplayActivity;
import ie.dcu.graze_even_master.masterutils.MasterUtils;
import ie.dcu.graze_even_master.masterutils.Polygon;
import ie.dcu.graze_even_master.drone.DroneHandler;
import ie.dcu.graze_even_master.location.Map;
import ie.dcu.graze_even_master.location.MapFactory;

public class MasterActivity extends ActionBarActivity {
    private BigDecimal[] fieldStartCoordinates = new BigDecimal[2];
    private Bitmap heatmapDisplay = null;
    private static final int BOOST = 100;
    private int width,height;
    private DroneHandler handler = null;
    private boolean writtenOut = true;
    private Polygon polygon = null;
    private Map heatmap = null;
    private ColorIterator colorIterator = null;
    private final List<SlaveDevice> slaves = new LinkedList<>();
    //implementation of receiver thread, updates the location given a valid message
    private ReceiverThread receiverThread = new ReceiverThread(new MessageHandler() {
        String message;
        @Override
        public void setMessage(byte [] bytes) {
            message = new String(bytes);
        }
        @Override
        public void run() {
            try{
                Log.d("Message", message);
               //if the message is a name update
                if(message.contains("NAME_UPDATE")){
                    //get the old and new names
                    String[] messageComponents = message.split(":::");
                    String newName = messageComponents[2].split(":")[1];
                    String oldName = messageComponents[0];
                    updateSlaveName(oldName,newName);
                }
                else {
                    //get and process the location data
                    String[] messageComponents = message.split(":::");
                    String messageCoords = messageComponents[2];
                    BigDecimal lati, longi;
                    String[] coordinates = messageCoords.split(",");
                    lati = new BigDecimal(coordinates[0]);
                    longi = new BigDecimal(coordinates[1]);
                    long updateTime = System.currentTimeMillis();
                    processUpdate(messageComponents[0],lati,longi,updateTime);
                }
            }
            catch(Exception e){
                Log.e("Communication", "Attempt to process data from socket failed: " + e.toString(),e);
            }
        }
    });

    /*
    Function Name: editField
    Return Type: void
    Access Type: private
    Purpose:
    To launch the FieldActivity to allow the user to specify their field outline.
    Parameters:
    None
    PreConditions: It is required to launch the FieldActivity.
    PostConditions: The FieldActivity has been launched.
     */
    private void editField()
    {
        Intent thisIntent = new Intent(this,FieldActivity.class);
        Bundle extras = new Bundle();
        try {
            Polygon polygon1 = new Polygon();
            double [] xPoints = polygon.getXPoints();
            double [] yPoints = polygon.getYPoints();
            for(int index = 0;index<polygon.getNumVertices();index++)
            {
                polygon1.addPoint(parseToWorld((int)xPoints[index]/BOOST,true).doubleValue(),parseToWorld((int)yPoints[index]/BOOST,false).doubleValue());
            }
            extras.putString("Polygon",polygon1.toString());
        }
        catch(Exception e){
            Log.e("SettingPolygon",e.toString(),e);
        }
        try{
            extras.putStringArray("Animals",getSlavesAsStrings());
        }catch(Exception e){
            Log.e("Setting animals",e.toString(),e);
        }
        thisIntent.putExtras(extras);
        startActivityForResult(thisIntent, 0);
    }

    /*
    Function Name: updateSlaveName
    Return Type: void
    Access Type: private synchronized
    Purpose:
    To update the name on record of a named slave device.
    Parameters:
    String oldName, the current name of the slave device.
    String oldName, the new name for the slave device.
    PreConditions: It is required to change the recorded name of a slave device.
    PostConditions: The name of the specified slave device has been changed.
     */
    private synchronized void updateSlaveName(String oldName, String newName)
    {
        Log.d("Changing slave name","Changing from "+oldName+" to "+newName);
        for(SlaveDevice slave:slaves)
        {
            if(slave.name.equals(oldName))
            {
                slaves.remove(slave);
                slave.name = newName;
                slaves.add(slave);
                break;
            }
        }
    }
    /*
    Function Name: getSlavesAsStrings
    Return Type: String []
    Access Type: private synchronized
    Purpose:
    To return all recorded slave devices as Strings.
    Parameters:
    None
    PreConditions: It is required to get all recorded slave devices as strings.
    PostConditions: All recorded slave devices have been returned as strings.
     */
    private synchronized String[] getSlavesAsStrings(){
        int slaveNumber = slaves.size();
        String [] slaveArray = new String [slaveNumber];
        for (int index = 0;index<slaveNumber;index++){
            Log.d("Getting Slave String", slaves.get(index).toString());
            slaveArray[index] = slaves.get(index).toString();
        }
        return slaveArray;
    }
    /*
    Function Name: processUpdate
    Return Type: void
    Access Type: private synchronized
    Purpose:
    To write the heatmap data to a file for persistent storage.
    Parameters:
    String appName, the name of the slave device which transmitted the update.
    BigDecimal lati, a BigDecimal representing the latitude of the slave device
    BigDecimal longi, a BigDecimal representing the longitude of the slave device
    long updateTime, the time at which teh update was received
    PreConditions: An update to the field has arrived and needs to be applied.
    PostConditions: The update has been applied to the heatmap.
     */
    private synchronized void processUpdate(String appName, BigDecimal lati, BigDecimal longi, long updateTime){

        boolean contains = false;
        for(SlaveDevice slave:slaves)
        {
            if(slave.name.equals(appName))
            {
                contains = true;
                slaves.remove(slave);
                slave.latitude = lati;
                slave.longitude = longi;
                slave.lastUpdateTime = updateTime;
                slaves.add(slave);
                break;
            }
        }
        if(!contains)
            slaves.add(new SlaveDevice(appName, lati, longi, updateTime));
        processLocation(lati, longi);
    }
    /*
    Function Name: saveData
    Return Type: void
    Access Type: private
    Purpose:
    To write the heatmap data to a file for persistent storage.
    Parameters:
    None
    PreConditions: It is required to save the heatmap data to a file.
    PostConditions: The heatmap data has been saved to a file.
     */
    private void saveData()
    {
        if(!writtenOut) {
            try {
                Log.d("Destruction", "Attempting to save heatmap and location data");
                PrintWriter writer = new PrintWriter(openFileOutput("InitDetails.txt", Context.MODE_PRIVATE));
                writer.println(fieldStartCoordinates[0] + "," + fieldStartCoordinates[1]);
                writer.println(height);
                writer.println(width);
                writer.println(polygon);
                Log.d("Writing Heatmap","Writing the heatmap to file");
                writer.println(heatmap.toString());
                Log.d("Writing Heatmap","Heatmap written to file");
                writer.close();
                try {
                    Log.d("Heatmap Writing","Writing Heatmap Bitmap");
                    FileOutputStream fos = openFileOutput("HeatmapStorage.png", Context.MODE_PRIVATE);
                    heatmapDisplay.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                    Log.d("Heatmap Writing","Heatmap Bitmap written out");
                }
                catch(Exception e)
                {
                    Log.e("Heatmap Writing","Error Writing heatmap to file: "+e.toString(),e);
                }
                writtenOut = true;
                Log.d("Saving","Heatmap and location written out");
            } catch (Exception e) {
                Log.e("Destruction", "Attempt to save data failed",e);
            }
        }
    }

    /*
    Function Name: processLocation
    Return Type: void
    Access Type: private
    Purpose:
    To parse world coordinates to heatmap coordinates and update the map given these heatmap coordinates.
    Parameters:
    BigDecimal latitude representing the world coordinate latitude to be translated.
    BigDecimal longitude representing the world coordinate longitude to be translated.
    PreConditions: It is required to parse world coordinates to heatmap coordinates and update the heatmap at these coordinates.
    PostConditions: The world coordinates have been parsed to heatmap coordinates and the heatmap updated at these coordinates.
     */
    private void processLocation(BigDecimal latitude, BigDecimal longitude)
    {
        try {
            Log.d("Processing Location",latitude+","+longitude);
            Log.d("Processing Location",fieldStartCoordinates[1]+","+fieldStartCoordinates[0]);
            int xCoordinate = Math.abs(longitude.movePointRight(MasterUtils.MAX_DECIMAL_PLACES).intValue() - (fieldStartCoordinates[0]).movePointRight(MasterUtils.MAX_DECIMAL_PLACES).intValue())*BOOST;
            int yCoordinate = Math.abs(latitude.movePointRight(MasterUtils.MAX_DECIMAL_PLACES).intValue() - (fieldStartCoordinates[1]).movePointRight(MasterUtils.MAX_DECIMAL_PLACES).intValue())*BOOST;
            Log.d("Processing Location",yCoordinate+","+xCoordinate+":::"+polygon);
            if(polygon.contains(xCoordinate,yCoordinate)) {
                updateMap(xCoordinate, yCoordinate);
            }
        }
        catch(Exception e) {
            Log.e("Update Error",e.toString(),e);
        }
    }

    /*
    Function Name: updateMap
    Return Type: void
    Access Type: private synchronized
    Purpose:
    To update the map st the specified coordinates.
    Parameters:
    int xCoordinate, representing the x coordinate of the point in the heatmap to be updated.
    int yCoordinate, representing the y coordinate of the point in the heatmap to be updated.
    PreConditions: It is required to update the map at a specified point.
    PostConditions: The map has been updated at the specified point.
     */
    private synchronized void updateMap(int xCoordinate,int yCoordinate)
    {
        this.writtenOut = false;
        try {
            if(this.heatmapDisplay!=null) {
                setBitmapColor(xCoordinate, yCoordinate);
                this.writtenOut = false;
            }
        }
        catch(Exception obe){
            Log.e("Updating Map:",obe.toString(),obe);

        }
    }

    /*
    Function Name: startDisplayActivity
    Return Type: void
    Access Type: private
    Purpose:
    To start the DisplayActivity and ensure it has the information it needs to display the bitmap to the user.
    Parameters:
    None
    PreConditions: It is required that the DisplayActivity be started to display the heatmap to the user.
    PostConditions: The DisplayActivity has been started to display the heatmap to the user.
     */
    private void startDisplayActivity()
    {
        //scale the bitmap to screen size and write it out to a file
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap bitmap = this.createScaledBitmap(Bitmap.createBitmap(heatmapDisplay, 0, 0, heatmapDisplay.getWidth(), heatmapDisplay.getHeight(), m, false));
        //m.preScale(-1,-1);
        //bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,false);
        try {
            FileOutputStream fos = openFileOutput("HeatmapBitmap.png", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Log.d("Heatmap Writing", "Heatmap written to file");
        }
        catch(Exception e)
        {
            Log.e("Heatmap Writing","Error Writing heatmap to file: "+e.toString());
        }
        startActivity(new Intent(this,DisplayActivity.class));
    }

    /*
    Function Name: createScaledBitmap
    Return Type: Bitmap
    Access Type: private
    Purpose:
    To return the bitmap representing the given bitmap scaled to window size.
    Parameters:
    Bitmap heatmapDisplay, the bitmap to be scaled
    PreConditions: It is required that a representation of a given bitmap scaled to window size be acquired.
    PostConditions: The representation of the given bitmap scaled to window size is returned.
     */
    private Bitmap createScaledBitmap(Bitmap heatmapDisplay)
    {
        if(heatmapDisplay==null) {
            heatmapDisplay = Bitmap.createBitmap(width * BOOST, height * BOOST, Bitmap.Config.ARGB_8888);
            createInitialBitmap(heatmapDisplay, BOOST);
        }
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;
        int screenWidth = displaymetrics.widthPixels;
        return Bitmap.createScaledBitmap(heatmapDisplay, screenWidth, screenHeight, false);
    }

    /*
    Function Name: createInitialBitmap
    Return Type: void
    Access Type: private
    Purpose:
    To create an initial bitmap using the bitmap object passed in and the specified boost.
    Parameters:
    Bitmap bitmap, the bitmap to use to create the initial bitmap
    int boost, the number of pixels assigned to each heatmap point
    PreConditions: It is required to create an initial bitmap.
    PostConditions: The initial bitmap is created using the bitmap object passed in and the specified boost.
     */
    private void createInitialBitmap(Bitmap bitmap, int boost)
    {
        for(int widthCounter = 0;widthCounter<width*boost;widthCounter++)
        {
            for(int heightCounter = 0;heightCounter<height*boost;heightCounter++)
            {
                //if the point is within the field outline color it in white otherwise color it in black
                if(polygon.contains(widthCounter,heightCounter)) {
                    Log.d("Bitmap coloring","Coloring in white: "+widthCounter+","+heightCounter);
                    bitmap.setPixel(widthCounter, heightCounter, colorIterator.getWhite());
                    if(widthCounter%boost==0&&heightCounter%boost==0)
                        updateHeatmap(widthCounter / boost, heightCounter / boost, 10,true);
                }
                else {
                    Log.d("Bitmap coloring","Coloring in black: "+widthCounter+","+heightCounter);
                    bitmap.setPixel(widthCounter, heightCounter, colorIterator.getBlack());
                }
            }
        }
    }

    /*
    Function Name: setBitmapColor
    Return Type: void
    Access Type: private synchronized
    Purpose:
    To update the color of the bitmap representation of the heatmap at a specified heatmap point.
    Parameters:
    int xCo, the x coordinate of the point to be updated with regards to the heatmap
    int yCo, the y coordinate of the point to be updated with regards to the heatmap
    PreConditions: It is required to update the bitmap representation of the heatmap at a specified point on the heatmap.
    PostConditions: The bitmap representation of the heatmap is updated at the specified point on the heatmap.
     */
    private synchronized void setBitmapColor(int xCo, int yCo) {
        try {
            int currentWidth = -1;
            int currentHeight = -1;
            boolean updated = false;
            for (int widthCounter = 0; widthCounter < BOOST; widthCounter++) {
                for (int heightCounter = 0; heightCounter < BOOST; heightCounter++) {
                    if (polygon.contains(widthCounter + xCo, heightCounter + yCo)) {
                        try {
                            int currentPixel = heatmap.get((widthCounter + xCo)/BOOST, (heightCounter + yCo)/BOOST).getArea()/10;
                            Log.d("Changing color", currentPixel + "->" + colorIterator.getColor(currentPixel));
                            heatmapDisplay.setPixel(widthCounter + xCo, heightCounter + yCo, colorIterator.getColor(currentPixel));
                            if(!updated) {
                                //update to the next color
                                currentWidth = (widthCounter+xCo)/BOOST;currentHeight = (heightCounter+yCo)/BOOST;
                                updateHeatmap(currentWidth, currentHeight, 1,false);
                                updated = true;
                            }
                            int thisWidth = (widthCounter+xCo)/BOOST,thisHeight = (heightCounter+yCo)/BOOST;
                            if(thisWidth!=currentWidth&&thisHeight!=currentHeight)
                                updated = false;
                        }
                        catch(Exception e)
                        {
                            Log.e("Coloring map",e.toString(),e);
                        }
                    } else {
                        Log.d("Bitmap coloring", "Coloring in black: " + (widthCounter + xCo) + "," + (heightCounter + yCo));
                    }
                }
            }
        }
        catch(Exception e){
            Log.e("Update Error",e.toString(),e);
        }
    }

    /*
    Function Name: updateHeatmap
    Return Type: void
    Access Type: private synchronized
    Purpose:
    To update the heatmap.
    Parameters:
    int x, the x coordinate of the point to be updated
    int y, the y coordinate of the point to be updated
    int addition, the amount to update the value of the heatmap at (x,y) with
    PreConditions: It is required to update the heatmap at a specified point.
    PostConditions: The heatmap is updated at the specified point.
     */
    private void updateHeatmap(int x, int y, int addition,boolean override)
    {
        try {
            heatmap.update(x, y, addition);
            if(!override)
                makeDroneDecision(new int []{x,y},heatmap.get(x,y).getArea());
        }
        catch(Exception e){
            Log.e("Updating map",e.toString(),e);
        }
    }

    /*
    Function Name: makeDroneDecision
    Return Type: void
    Access Type: private
    Purpose:
    To decide whether to send the drone out and where to send the drone.
    Parameters:
    int [] currentLocation, the location on the heatmap that was updated
    int currentValue, the new value of the heatmap at the current location
    PreConditions: It is required to decide whether or not to send the drone out and if so where to send it.
    PostConditions: Either:
    The drone is not sent out.
    The drone is sent out and moves through the current location to
    the location on the heatmap which has the smallest value then back to its start position.
     */
    private void makeDroneDecision(int [] currentLocation,int currentValue){
        int [] minLocation = new int [] {0,0};
        int minValue = heatmap.get(0,0).getArea();
        boolean broken = false;
        for(int heatmapWidth = 1;heatmapWidth<width&&!broken;heatmapWidth++)
        {
            for(int heatmapHeight = 0;heatmapHeight<height&&!broken;heatmapHeight++)
            {
                int currentLoopValue = heatmap.get(heatmapWidth,heatmapHeight).getArea();
                if((minValue<0&&currentLoopValue>=0)||(minValue>currentLoopValue))
                {
                    minLocation[0] = heatmapWidth;
                    minLocation[1] = heatmapWidth;
                    minValue = currentLoopValue;
                    //cant get a lower value than zero so break
                    if(minValue==0)
                        broken = true;
                }
            }
        }
        //if the difference exceeds a certain amount
        if(currentValue-minValue>0)
        {
            //get the world coordinates of the min location and the current location and pass them to the drone as waypoints
            final BigDecimal [] waypoints = new BigDecimal[] {parseToWorld(currentLocation[1],false),parseToWorld(currentLocation[0],true),parseToWorld(minLocation[1],false),parseToWorld(minLocation[0],true)};
            //send out the drone
            Log.d("Waypoints:","Waypoint 1: "+waypoints[0]+","+waypoints[1]+"\nWaypoint 2: "+waypoints[2]+","+waypoints[3]);
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.travelThroughWaypointsAndHome(waypoints);
                    }
                }).start();
            }
            catch(Exception npe)
            {
                Log.e("Drone Communication",npe.toString(),npe);
            }
        }
    }

    /*
    Function Name: parseToWorld
    Return Type: BigDecimal
    Access Type: private
    Purpose:
    To parse a heatmap coordinate to a world coordinate.
    Parameters:
    int currentValue, the heatmap coordinate to be parsed
    boolean latFlag, representing whether the coordinate should be parsed to a latitude or a longitude
    PreConditions: It is required to convert a map coordinate to a world coordinate.
    PostConditions: The world coordinate corresponding to the map coordinate is returned
     */
    private BigDecimal parseToWorld(int mapCoord,boolean latFlag)
    {
        int startIndex = 0;
        if(!latFlag)
            startIndex++;
        return fieldStartCoordinates[startIndex].movePointRight(MasterUtils.MAX_DECIMAL_PLACES).subtract(new BigDecimal(mapCoord)).movePointLeft(MasterUtils.MAX_DECIMAL_PLACES);
    }

    //method to get the new field outline from the Field Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Getting result:","Child activity has returned result");
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            //display a saving message
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Creating the heat-map, Please wait...");
            dialog.show();
            Bundle resultBundle = data.getExtras();
            Log.d("Getting result:", "Getting the heatmap and field start coordinates from the bundle");
            fieldStartCoordinates[0] = new BigDecimal(resultBundle.getDouble("Left"));
            fieldStartCoordinates[1] = new BigDecimal(resultBundle.getDouble("Top"));
            width = resultBundle.getInt("Width");
            height = resultBundle.getInt("Length");
            Log.d("Bundle Results",width+","+height);
            heatmap = MapFactory.getInstance(width,height);
            polygon = new Polygon(resultBundle.getString("VerticesAsInts"));
            Polygon newPolygon = new Polygon();
            for(int index = 0;index < polygon.getNumVertices();index++)
            {
                newPolygon.addPoint(polygon.getXPoints()[index]*BOOST,polygon.getYPoints()[index]*BOOST);
            }
            polygon = newPolygon;
            writtenOut = false;
            heatmapDisplay = Bitmap.createBitmap(width * BOOST, height * BOOST, Bitmap.Config.RGB_565);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    createInitialBitmap(heatmapDisplay,BOOST);
                    saveData();
                    Looper.prepare();
                    try {
                        dialog.hide();
                    }
                    catch(Exception e){
                        Log.e("Showing loading dialog",e.toString(),e);
                    }
                }
            }).start();
            Button showButton = (Button)findViewById(R.id.showHeatmap);
            showButton.setEnabled(true);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_master);
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler = new DroneHandler();
            }
        }).start();
        colorIterator = ColorIterator.getInstance(this);
        //set onclick listeners for buttons
        Button editButton = (Button)findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editField();
            }
        });
        Button displayHeatmap = (Button)findViewById(R.id.showHeatmap);
        displayHeatmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDisplayActivity();
            }
        });
        //attempt to read previously stored data
        try{
            Log.d("Initialisation","Attempting to read previously saved data");
            Scanner scanner = new Scanner(openFileInput("InitDetails.txt"));
            String [] coords = scanner.nextLine().split(",");
            fieldStartCoordinates [0] = new BigDecimal(coords[0]);
            fieldStartCoordinates [1] = new BigDecimal(coords[1]);
            height = Integer.parseInt(scanner.nextLine());
            width = Integer.parseInt(scanner.nextLine());
            String [] vertices = scanner.nextLine().split(":");
            polygon = new Polygon();
            for(String point:vertices)
            {
                polygon.addPoint(Double.parseDouble(point.split(",")[0]),Double.parseDouble(point.split(",")[1]));
            }
            heatmap = MapFactory.getInstance(scanner.nextLine());
            scanner.close();
            heatmapDisplay = BitmapFactory.decodeStream(openFileInput("HeatmapStorage.png")).copy(Bitmap.Config.RGB_565,true);
        }catch(Exception e)
        {
            Log.e("Initialisation", "Attempt to read previously saved data failed: " + e.toString(),e);
            displayHeatmap.setEnabled(false);
        }
        new Thread((receiverThread)).start();
    }


    //These methods save data on certain events such as the application being closed, a new activity taking focus etc.
    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.close();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        saveData();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus)
            saveData();
    }

    //Method which usually inflates a menu when the settings button is clicked, this version suppresses that
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
