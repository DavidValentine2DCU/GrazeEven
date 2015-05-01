package ie.dcu.graze_even_master.display;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;


public class DisplayActivity extends Activity {
    /*
    Function Name: getBitmap
    Return Type: Bitmap
    Access Type: private
    Purpose:
    To get the bitmap to be displayed from a file.
    Parameters:
    None
    PreConditions: It is required to acquire a bitmap to display from a file.
    PostConditions: The bitmap acquired from a file is returned.
     */
    private Bitmap getBitmap()
    {
        Bitmap bitmap = null;
        //decode the bitmap from the file and return it
        try {
            bitmap = BitmapFactory.decodeStream(openFileInput("HeatmapBitmap.png"));
        }
        catch(Exception e){
            Log.e("Bitmap Decoding:","Caught Exception: "+e.toString());
        }
        Log.d("Bitmap Decoding:","Returning decoded bitmap");
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FieldDisplay object used to display bitmap
        FieldDisplay display = new FieldDisplay(this);
        //display the bitmap using the FieldDisplay object
        display.setBitmap(getBitmap());
        setContentView(display);
    }

    //Method which usually inflates a menu when the settings button is clicked, this version suppresses that
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
