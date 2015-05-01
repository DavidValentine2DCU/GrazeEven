package ie.dcu.graze_even_master.drone;

import android.util.Log;

import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DroneHandler {
    private static final BigDecimal DEFAULT_ALTITUDE = new BigDecimal(10.0);
    private static final int MAVLINK_CHANNEL = 14550;
    private BigDecimal [] homeCoordinates = new BigDecimal[2];
    private DatagramSocket droneSocket = null;

    /*
    Function Name: setHomeCoordinates
    Return Type: void
    Access Type: private
    Purpose:
    To set the home coordinates of the drone to be the drones current location.
    Parameters:
    None
    PreConditions: The home coordinates of the drone need to be set to the drone's current location
    PostConditions: The home coordinates of the drone have been set to the drone's current location
     */
    private void setHomeCoordinates()
    {
        //set the current coordinates of the drone to be the home coordinates
        try{
            byte [] messageBytes = receivePacket(33);
            Log.d("Received Packet","Received GPS Packet");
            long latitude = NumberParser.parseTo32BitNumber(new byte[]{messageBytes[10],messageBytes[11],messageBytes[12],messageBytes[13]});
            long longitude = NumberParser.parseTo32BitNumber(new byte[]{messageBytes[14],messageBytes[15],messageBytes[16],messageBytes[17]});
            homeCoordinates[0] = new BigDecimal(latitude).movePointLeft(7);
            homeCoordinates[1] = new BigDecimal(longitude).movePointLeft(7);
            if(homeCoordinates[0].equals(BigDecimal.ZERO)&&homeCoordinates[1].equals(BigDecimal.ZERO))
                setHomeCoordinates();
            Log.d("Home Coordinates","Coordinates set to "+homeCoordinates[0]+","+homeCoordinates[1]);
        }
        catch(Exception e)
        {
            Log.e("Getting Home Location",e.toString(),e);
        }
    }
    /*
    Function Name: receiveDroneAck
    Return Type: void
    Access Type: private
    Purpose:
    To wait for a Mission Ack message from the drone.
    Parameters:
    None
    PreConditions: A Mission Ack message needs to be received from the drone.
    PostConditions: A Mission Ack message has been received from the drone.
     */
    private void receiveDroneACK(){
        Log.d("Drone Communication","Waiting on Mission ACK packet to arrive");
        receivePacket(47);
        Log.d("Received Packet","Received Mission ACK packet");
    }
    /*
    Function Name: receiveWaypointRequestNumber
    Return Type: int
    Access Type: private
    Purpose:
    To wait for a Waypoint Request message from the drone, extract the waypoint number requested and return it.
    Parameters:
    None
    PreConditions: A Waypoint Request number needs to be received from the drone.
    PostConditions: A Waypoint Request number has been received from the drone and returned as  an integer.
     */
    private int receiveWaypointRequestNumber()
    {
        Log.d("Drone Communication","Waiting on Drone Request Number packet to arrive");
        byte [] messageBytes = receivePacket(40);
        if(messageBytes!=null) {
            int retValue = NumberParser.parseTo16BitInteger(new byte[]{messageBytes[6], messageBytes[7]});
            Log.d("Received Packet", "Received Mission Request "+retValue+" packet");
            return retValue;
        }
        return 0;
    }
    /*
    Function Name: receivePacket
    Return Type: byte []
    Access Type: private
    Purpose:
    To wait for a specific message from the drone and return that message's bytes.
    Parameters:
    int messageNumber, the MAVLink message id to wait for.
    PreConditions: A specific message needs to be received from the drone.
    PostConditions: The message has been received from the drone and returned as an array of the message's bytes.
     */
    private byte [] receivePacket(int messageNumber)
    {
        try {
            byte [] dataBytes = new byte[1024];
            DatagramPacket packet = new DatagramPacket(dataBytes,dataBytes.length);
            while (true) {
                droneSocket.receive(packet);
                byte [] messageBytes = packet.getData();
                //Check if the first symbol is the message start symbol
                if(messageBytes[0]==(byte)0xfe) {
                    //get the message id
                    int messageId = (((int) messageBytes[5])+256)%256;
                    if (messageId == messageNumber) {
                        return messageBytes;
                    }
                }
            }
        }
        catch(Exception e)
        {
            Log.e("Reading Packet",e.toString(),e);
        }
        return null;
    }
    /*
    Function Name: transmitWaypoint
    Return Type: void
    Access Type: private
    Purpose:
    To transmit a MAVLink Mission Item message containing a waypoint to the drone.
    Parameters:
    BigDecimal latitude, representing the latitude of the waypoint.
    BigDecimal longitude, representing the longitude of the waypoint.
    int sequence, representing the numerical order within the flight-plan this waypoint occurs.
    PreConditions: A waypoint needs to be sent to the drone.
    PostConditions: The waypoint has been sent to the drone.
     */
    private void transmitWaypoint(BigDecimal latitude, BigDecimal longitude,int sequence){
        Log.d("Drone Communication","Sending waypoint");
        sendToDrone(DroneCommandGenerator.addMissionItem(latitude, longitude, DEFAULT_ALTITUDE, 16, sequence));
    }
    /*
    Function Name: transmitWaypointCount
    Return Type: void
    Access Type: private
    Purpose:
    To transmit a MAVLink Mission Item Count message containing a specified waypoint count to the drone.
    Parameters:
    int sequence, representing the number of waypoints in the flight-plan.
    PreConditions: A Mission Item Count message needs to be sent to the drone.
    PostConditions: The Mission Item Count message has been sent to the drone.
     */
    private void transmitWaypointCount(int numWaypoints){
        Log.d("Drone Communication","Sending mission count message");
        sendToDrone(DroneCommandGenerator.missionCount((short) numWaypoints));
    }
    /*
    Function Name: takeOff
    Return Type: void
    Access Type: private
    Purpose:
    To transmit a MAVLink Command Long message to the drone telling it to take off.
    Parameters:
    None
    PreConditions: A take off message needs to be sent to the drone.
    PostConditions: The take off message has been sent to the drone.
     */
    private void takeOff(){
        Log.d("Drone Communication","Sending takeoff message");
        sendToDrone(DroneCommandGenerator.takeOff(0));
    }
    /*
    Function Name: land
    Return Type: void
    Access Type: private
    Purpose:
    To transmit a MAVLink Mission Item message to the drone telling it where to land.
    Parameters:
    BigDecimal latitude, representing the latitude of where the drone should land.
    BigDecimal longitude, representing the longitude of where the drone should land.
    int sequence, representing the numerical order within the flight-plan this instruction occurs.
    PreConditions: A Mission Item message telling the drone where to land needs to be sent to the drone.
    PostConditions: The message telling the drone where to land has been sent to the drone.
     */
    private void land(BigDecimal latitude, BigDecimal longitude,int sequence){
        Log.d("Drone Communication","Sending land message");
        sendToDrone(DroneCommandGenerator.addMissionItem(latitude, longitude, DEFAULT_ALTITUDE, 21, sequence));
    }
    /*
    Function Name: transmitBeginCommand
    Return Type: void
    Access Type: private
    Purpose:
    To transmit a MAVLink Command Long message to the drone telling it to begin following the flight-plan.
    Parameters:
    int lastIndex, the last instruction in the flight-plan in numerical order that the drone should execute.
    PreConditions: A Command Long message telling the drone to begin following the flight-plan needs to be sent to the drone.
    PostConditions: The message telling the drone to begin following the flight-plan has been sent to the drone.
     */
    private void transmitBeginCommand(int lastIndex){
        Log.d("Drone Communication","Sending start message");
        sendToDrone(DroneCommandGenerator.beginMission(0,lastIndex));
    }
    /*
    Function Name: transmitFaceNorth
    Return Type: void
    Access Type: private
    Purpose:
    To transmit a MAVLink Command Long message to the drone telling it spin until it faces north.
    Parameters:
    None
    PreConditions: A Command Long message needs to be sent to the drone telling it to face north.
    PostConditions: The message has been sent to the drone telling it to face north.
     */
    private void transmitFaceNorth(){
        Log.d("Drone Communication","Telling the drone to face north");
        sendToDrone(DroneCommandGenerator.faceNorth(0));
    }
    /*
    Function Name: clearWaypoints
    Return Type: void
    Access Type: private
    Purpose:
    To issue a command to the drone telling it to clear its current flightplan and
    wait for a Mission Ack message from the drone to confirm it has done so.
    Parameters:
    None
    PreConditions: Any existing flightplan needs to be cleared from the drone.
    PostConditions: Any existing flightplan on the drone is deleted. The drone no longer has a flightplan.
     */
    private void clearWaypoints()
    {
        sendToDrone(DroneCommandGenerator.missionClearAll());
        receiveDroneACK();
    }
    /*
    Function Name: sendToDrone
    Return Type: void
    Access Type: private
    Purpose:
    To send a message as an array of bytes to the drone.
    Parameters:
    byte [] instructions, representing the message to be sent to the drone.
    PreConditions: A message needs to be sent to the drone using UDP.
    PostConditions: The message has been sent to the drone sing UDP.
     */
    private void sendToDrone(byte [] instructions){
        try {
            DatagramPacket packet = new DatagramPacket(instructions, instructions.length, InetAddress.getByAddress(new byte[]{(byte)192,(byte)168,(byte)1,(byte)1}),MAVLINK_CHANNEL+1);
            droneSocket.send(packet);
            Log.d("Sending to drone","Message sent");
        }
        catch (Exception e){
            Log.e("Error Sending to drone",e.toString(),e);
        }
    }
    public DroneHandler(){
        try {
            //initialise the udp sockets
            droneSocket = new DatagramSocket(MAVLINK_CHANNEL);//receive navdata from and send messages to the drone using udp
            droneSocket.setReuseAddress(true);
            droneSocket.setBroadcast(true);
        }
        catch(Exception e){
            Log.e("Drone Communication Initialisation",e.toString(),e);
        }
    }
    /*
    Function Name: close
    Return Type: void
    Access Type: public
    Purpose:
    To close the UDP socket associated with this DroneHandler object.
    Parameters:
    None
    PreConditions: The socket is open and need to be closed
    PostConditions: The socket is closed.
     */
    public void close(){
        droneSocket.close();
    }
    /*
    Function Name: travelThroughWaypointsAndHome
    Return Type: void
    Access Type: public final
    Purpose:
    To issue a series of commands to the drone forcing it to fly a given route and then land back at its start position.
    Parameters:
    BigDecimal [] waypoints, representing the coordinates of each waypoint the drone should fly to. Each even number(and 0)
    represent the latitude of the coordinate and each odd number represents the longitude of the coordinate.
    PreConditions: The drone needs to be flown through a specific route and back to its start position.
    PostConditions: Any flightplan existing on the drone is cleared, the required waypoints(including the
    start position in order to return to it) are added to the drones flight-plan and commands telling the drone
    to takeoff and follow the flight-plan are issued to it.
     */
    public final void travelThroughWaypointsAndHome(BigDecimal [] waypoints){
        if(waypoints.length%2==0)
        {
            setHomeCoordinates();
            clearWaypoints();
            transmitWaypointCount((waypoints.length/2)+2);
            int waypointRequestNumber = receiveWaypointRequestNumber();
            transmitWaypoint(homeCoordinates[0],homeCoordinates[1],waypointRequestNumber);
            while(waypointRequestNumber<(waypoints.length/2)) {
                waypointRequestNumber = receiveWaypointRequestNumber();
                int index = (waypointRequestNumber-1)*2;
                transmitWaypoint(waypoints[index],waypoints[index+1],waypointRequestNumber);
            }
            waypointRequestNumber = receiveWaypointRequestNumber();
            land(homeCoordinates[0], homeCoordinates[1], waypointRequestNumber);
            receiveDroneACK();
            takeOff();
            transmitFaceNorth();
            transmitBeginCommand((waypoints.length/2)+2);
        }
    }
}
