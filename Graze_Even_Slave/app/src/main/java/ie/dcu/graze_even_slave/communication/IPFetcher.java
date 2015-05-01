package ie.dcu.graze_even_slave.communication;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

public class IPFetcher {
    /*
    Function Name: getIp
    Return Type: InetAddress
    Access Type: private static
    Purpose:
    To return an InetAddress object referring to either the host device ip address or the broadcast ip address.
    Parameters:
    Activity caller, representing the activity which has called this method, to be used in getting access to system services
    booelan hostOrBroadcast, representing whether to calculate the host device's ip address or the network broadcast ip address
    PreConditions: It is required to acquire an InetAddress object containing the host device's ip address,
    Or It is required to acquire an InetAddress object containing the network broadcast address.
    PostConditions: The InetAddress object containing the network host device's ip is returned,
    Or The InetAddress object containing the network broadcast address is returned
     */
    //method to return either the host device ip address or the broadcast ip address
    private static InetAddress getIp(Activity caller, boolean hostOrBroadcast) throws IOException{
        //get the dhcp info object from the wifi system service
        WifiManager wifi = (WifiManager) caller.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null)
            Log.e("WifiManagement", "Error getting dhcp info");
        //get the address as an int
        int broadcast = dhcp.ipAddress;
        if(!hostOrBroadcast)
            broadcast = (broadcast & dhcp.netmask) | ~dhcp.netmask;
        //get the address as a byte array
        int numBytes = 4;
        byte[] quads = new byte[numBytes];
        for (int index = 0; index < 4; index++)
            quads[index] = (byte) ((broadcast >> index * 8) & 0xFF);
        //return the address as an InetAddresss object
        return InetAddress.getByAddress(quads);
    }
    /*
    Function Name: getBroadcastIp
    Return Type: InetAddress
    Access Type: public static
    Purpose:
    To return the InetAddress object referring to the network broadcast address.
    Parameters:
    Activity caller, representing the activity which has called this method, to be used in getting access to system services
    PreConditions: It is required to acquire an InetAddress object containing the network broadcast address.
    PostConditions: The InetAddress object containing the network broadcast address is returned.
     */
    public static InetAddress getBroadcastIp(Activity caller)throws IOException {
        return getIp(caller,false);
    }
    /*
    Function Name: getDeviceIp
    Return Type: InetAddress
    Access Type: public static
    Purpose:
    To return the InetAddress object referring to the host device's ip address.
    Parameters:
    Activity caller, representing the activity which has called this method, to be used in getting access to system services
    PreConditions: It is required to acquire an InetAddress object containing the host device's ip address.
    PostConditions: The InetAddress object containing the network host device's ip is returned.
     */
    public static InetAddress getDeviceIp(Activity caller) throws IOException {
        return getIp(caller,true);
    }
}
