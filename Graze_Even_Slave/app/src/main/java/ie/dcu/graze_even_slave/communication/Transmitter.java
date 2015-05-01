package ie.dcu.graze_even_slave.communication;

import android.app.Activity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Transmitter {
    private String appId;//name of this application instance, ie. the name of the horse
    public static final int SLAVE_PORT = 4001;//Port on which slaves transmit
	private DatagramSocket dSocket = null;//Datagram socket used to transmit udp
    //the host device ip address and the broadcast ip address
    private String sourceIP;
    private InetAddress broadcastAddress = null;
    public Transmitter(Activity caller)throws TransmissionException
    {
    	try{
            sourceIP = IPFetcher.getDeviceIp(caller).toString();
	    	broadcastAddress = IPFetcher.getBroadcastIp(caller);
    	}
    	catch(Exception e){
    		throw new TransmissionException(e);
    	}
    }
    //getter and setter for appId
    public String getAppId()
    {
        return appId;
    }
    public void setAppId(String newID)
    {
        appId = newID;
    }
    /*
    Function Name: transmit
    Return Type: void
    Access Type: public
    Purpose:
    To transmit a message on the slave port using UDP.
    Parameters:
    String message, the message to be transmitted
    PreConditions: It is required to transmit a message on the slave port using UDP.
    PostConditions: The specified message is transmitted on the slave port using UDP
     */
    public void transmit(String message) throws TransmissionException
    {
        String nonce = System.currentTimeMillis()+"";//Incremental nonce to ensure freshness
        String fullMessage = appId+":::"+sourceIP+":::"+message+":::"+nonce;//compile the full message to be transmitted
        byte [] messageBytes = fullMessage.getBytes();
        try {
            //if the udp datagram socket is null initialise it
        	if(dSocket==null)
        	{
        		dSocket = new DatagramSocket(SLAVE_PORT);
        		dSocket.setBroadcast(true);
        	}
            //send a datagram packet containing the message through the socket
            DatagramPacket packet = new DatagramPacket(messageBytes,messageBytes.length,broadcastAddress,SLAVE_PORT);
            dSocket.send(packet);
        }
        catch (Exception e)
        {
        	throw new TransmissionException(e);
        }
    }
}
