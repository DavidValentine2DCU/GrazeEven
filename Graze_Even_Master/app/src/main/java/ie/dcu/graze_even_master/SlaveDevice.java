package ie.dcu.graze_even_master;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;

public class SlaveDevice{
    public String name;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public long lastUpdateTime;
    public SlaveDevice(String name, BigDecimal latitude, BigDecimal longitude,long lastUpdateTime)
    {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdateTime = lastUpdateTime;
    }
    public SlaveDevice(String device)
    {
        String [] values = device.split(":");
        this.name = values[0];
        this.latitude = new BigDecimal(values[1]);
        this.longitude = new BigDecimal(values[2]);
        this.lastUpdateTime = Long.parseLong(values[3]);
    }
    /*
    Function Name: getLatLng
    Return Type: LatLng
    Access Type: public
    Purpose:
    To return the latitude and longitude of this slave as a latLng object.
    Parameters:
    None
    PreConditions: It is required that a LatLng object representing the latitude and longitude of this slave be acquired.
    PostConditions: The required LatLng object has been returned.
     */
    public LatLng getLatLng()
    {
        return new LatLng(latitude.doubleValue(),longitude.doubleValue());
    }
    //method to return a string representation of this SlaveDevice
    public String toString()
    {
        return name+":"+latitude+":"+longitude+":"+lastUpdateTime;
    }
}
