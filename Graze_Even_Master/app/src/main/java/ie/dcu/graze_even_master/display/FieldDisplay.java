package ie.dcu.graze_even_master.display;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class FieldDisplay extends View {
    private Bitmap bitmap = null;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //if the bitmap exists draw it to the canvas
        if(bitmap!=null)
        {
            canvas.drawBitmap(bitmap,0,0,null);
        }
    }
    public FieldDisplay(Context context) {
        super(context);
        //if the version is greater than 11 disable hardware acceleration
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    //setter for the bitmap object
    public void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
    }

}
