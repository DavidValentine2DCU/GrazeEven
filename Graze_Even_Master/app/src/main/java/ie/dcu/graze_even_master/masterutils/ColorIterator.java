package ie.dcu.graze_even_master.masterutils;

import android.app.Activity;
import ie.dcu.graze_even_master.R;

public class ColorIterator {
    //array of hex colors and the last color in the array
    private int [] colors;
    private int lastColor = -1;
    //This is a singleton because externally it is read only and there is no need to clog the system with multiple instances
    private static ColorIterator instance = null;
    private ColorIterator(Activity caller)
    {
        colors = caller.getResources().getIntArray(R.array.colorsArray);
        lastColor = colors[colors.length-1];
    }
    /*
    Function Name: getInstance
    Return Type: ColorIterator
    Access Type: public static
    Purpose:
    To return a ColorIterator instance.
    Parameters:
    Activity caller, needed for the ColorIterator constructor if no ColorIterator instance currently exists.
    PreConditions: It is required that a ColorIterator instance be acquired.
    PostConditions: A ColorIterator instance is returned.
     */
    public static ColorIterator getInstance(Activity caller)
    {
        if(instance==null)
            instance = new ColorIterator(caller);
        return instance;
    }
    /*
    Function Name: getWhite
    Return Type: int
    Access Type: public
    Purpose:
    To return the integer value that corresponds to the color white.
    Parameters:
    None
    PreConditions: It is required that the integer value representing white be acquired.
    PostConditions: The integer value representing white is returned.
     */
    public int getWhite(){
        return colors[1];
    }
    /*
    Function Name: getBlack
    Return Type: int
    Access Type: public
    Purpose:
    To return the integer value that corresponds to the color black.
    Parameters:
    None
    PreConditions: It is required that the integer value representing black be acquired.
    PostConditions: The integer value representing black is returned.
     */
    public int getBlack() {
        return colors[0];
    }
    /*
    Function Name: getColor
    Return Type: int
    Access Type: public
    Purpose:
    To return the integer value that corresponds to the color at the specified index.
    Parameters:
    int index, the index of the color required.
    PreConditions: It is required that the integer value representing a specified index in the color array be acquired.
    PostConditions: The integer value representing the specified index in the color array is returned.
     */
    public int getColor(int index)
    {
        int color;
        if(index<0)
            return colors[0];
        try{
            color = colors[index];
        }
        catch (ArrayIndexOutOfBoundsException ae)
        {
            color = lastColor;
        }
        return color;
    }
}
