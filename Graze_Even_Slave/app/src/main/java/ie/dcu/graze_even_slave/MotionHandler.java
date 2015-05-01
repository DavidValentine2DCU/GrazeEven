package ie.dcu.graze_even_slave;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MotionHandler implements SensorEventListener{

    private Updatable updatable;
    private float accelerationLessGravity = 0.00f, accelerationWithGravity = SensorManager.GRAVITY_EARTH;
    private float [] geoMagnetic = null;
    /*
    Function Name: isInEatingRange
    Return Type: boolean
    Access Type: private
    Purpose:
    To calculate if the device is in eating range ie. has tilted into a certain range of angles.
    Parameters:
    float azimuth, the tilt of the device about the z-axis
    float pitch, the tilt of the device about the x-axis
    float roll, the tilt of the device about the y-axis
    PreConditions: It is required calculate whether or not the device is in eating range.
    PostConditions: A boolean value indicating whether the device is in eating range is returned
     */
    private boolean isInEatingRange(float azimuth, float pitch, float roll){
        //if the top of the device is tilted towards the ground at a certain angle
        return (1.10 <= pitch || pitch <= -1.10)&&(1.10 <= roll || roll <= -1.10);
    }

    /*
    Function Name: isInBumpRange
    Return Type: boolean
    Access Type: private
    Purpose:
    To calculate if the device has shaken ie. is has gained acceleration.
    Parameters:
    float accelerationLessGravity, representing the current device acceleration without gravity acting on it
    PreConditions: It is required calculate whether or not the device has been shaken or bumped.
    PostConditions: A boolean value indicating whether the device has been shaken or bumped is returned
     */
    private boolean isInBumpRange(float accelerationLessGravity) {
        return accelerationLessGravity>0&&accelerationLessGravity<7;
    }

    public MotionHandler(Updatable updatable)
    {
        this.updatable = updatable;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD)
        {
            geoMagnetic = event.values.clone();
        }
        else if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER&&geoMagnetic!=null)
        {
            //get the orientation of the device to see if the animal has lowered its head to the ground
            float [] values = new float[3];
            float [] r = new float[9];
            float [] i = new float [9];
            SensorManager.getRotationMatrix(r,i,event.values.clone(),geoMagnetic);
            SensorManager.getOrientation(r,values);
            float azimuth = values[0];//Azimuth.(0 to 359). 0=North, 90=East, 180=South, 270=West
            //The pitch value is what were interested in!!!!
            //Pitch. Rotation around x-axis (-180 to 180), with positive values when the z-axis moves toward the y-axis.
            float pitch = values[1];
            float roll = values[2];//Roll. Rotation around the x-axis (-90 to 90) increasing as the device moves clockwise.
            if(isInEatingRange(azimuth,pitch,roll))
            {
                //check to see if the device is receiving bump events which would indicate that the animal is probably eating
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float lastAccelerationWithGravity = accelerationWithGravity;
                accelerationWithGravity = (float) Math.sqrt((double) (x*x + y*y + z*z));
                float delta = accelerationWithGravity - lastAccelerationWithGravity;
                accelerationLessGravity = accelerationLessGravity * 0.9f + delta;
                Log.d("Motion Detector","The horses head is tilted towards the ground");
                if(isInBumpRange(accelerationLessGravity))//random guessing value here
                {
                    //we're pretty sure its eating so mark it.
                    Log.d("Motion Detector:","Horse Is Eating");
                    updatable.update();
                }
            }
        }
    }
    //has to be overridden but we dont really care about it
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
