package ie.dcu.graze_even_master.location;

import android.util.Log;

import java.security.InvalidParameterException;

public class MapFactory {
    /*
    Function Name: getInstance
    Return Type: Map
    Access Type: public static
    Purpose:
    To return a Map instance with the specified width and height with all area values set to 0.
    Parameters:
    int width, representing the required width of the Map
    int height, representing the required height of the Map
    PreConditions: It is required to acquire a blank Map of a specified size.
    PostConditions: The a blank Map with the specified size is returned.
     */
    public static Map getInstance(int width, int height){
        return new Heatmap(width,height);
    }
    /*
    Function Name: getInstance
    Return Type: Map
    Access Type: public static
    Purpose:
    To decode the specified Map String value to a valid Map instance and return it.
    Parameters:
    String mapString, the String representation of the Map
    PreConditions: It is required to acquire a Map instance defined by a specified String value.
    PostConditions: The Map instance defined by the specified String value is returned.
     */
    public static Map getInstance(String mapString){
        String [] breakdown = mapString.split(";;");
        if(breakdown.length<3)
            throw new InvalidParameterException();
        int width = Integer.parseInt(breakdown[0]);
        int length = Integer.parseInt(breakdown[1]);
        //get a heatmap object of the correct size
        Map map = MapFactory.getInstance(width, length);
        //fill in the values of the heatmap object from the values in the string
        String [] individualValues = breakdown[2].split(",");
        Log.d("Creating heatmap",width+","+length);
        for(int index = 0;index<width*length;index++)
        {
            Log.d("Heatmap index",(index/length)+","+(index%length));
            map.set(index/length,index%length, AreaFactory.getInstance(individualValues[index]));
        }
        return map;
    }
}
