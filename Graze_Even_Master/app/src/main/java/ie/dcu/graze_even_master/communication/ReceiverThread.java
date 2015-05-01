package ie.dcu.graze_even_master.communication;

import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class ReceiverThread implements Runnable {
    private MessageHandler action;
    private static final int SLAVE_PORT = 4001;
    private DatagramSocket dSocket = null;
    /*
    Function Name: receive
    Return Type: void
    Access Type: public
    Purpose:
    To receive UDP packets on the port of this receiver and apply a MessageHandler action to the messages received.
    Parameters:
    MessageHandler action, the action to apply to the messages received.
    PreConditions: A Command Long message needs to be sent to the drone telling it to face north.
    PostConditions: The message has been sent to the drone telling it to face north.
     */
    private void receive(MessageHandler action)
    {
        //try read the message from the socket, then start a new thread to run the messagehandler action on it
        try {
            if(dSocket==null) {
                dSocket = new DatagramSocket(SLAVE_PORT);
            }
            byte [] dataBytes = new byte[1024];
            DatagramPacket packet;
            while (true) {
                packet = new DatagramPacket(dataBytes,dataBytes.length);
                dSocket.receive(packet);
                action.setMessage(packet.getData());
                new Thread(action).start();
            }
        }
        catch(Exception e){
            Log.e("Error Receiving data from Slaves", e.toString(),e);
        }
    }
    //class to receive and process udp
    public ReceiverThread(MessageHandler action)
    {
        this.action = action;
    }
    @Override
    public void run() {
        receive(action);
    }
}
