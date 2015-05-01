package ie.dcu.graze_even_master.drone;

import java.math.BigDecimal;

class DroneCommandGenerator {
    private static final int START_OF_PAYLOAD = 6;//fixed position for the start of the payload
    private static final byte TARGET_SYSTEM_ID = (byte)1,TARGET_COMPONENT_ID=(byte)190,START_SYMBOL=(byte)0xfe,SOURCE_SYSTEM_ID=(byte)2,SOURCE_COMPONENT_ID = (byte)1,FRAME=(byte)0,AUTOCONTINUE=(byte)1;
    private static final byte [] ZERO_FLOAT = NumberParser.parseFromFloat(0.0),TAKEOFF_COMMAND = NumberParser.parseFrom16BitInteger(22),SPIN_COMMAND = NumberParser.parseFrom16BitInteger(115),BEGIN_COMMAND = NumberParser.parseFrom16BitInteger(300);
    private static final char[] MAVLINK_MESSAGE_CRCS = {
            50, 124, 137, 0, 237, 217, 104, 119, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 214, 159, 220, 168, 24,
            23, 170, 144, 67, 115, 39, 246, 185, 104, 237, 244, 222, 212, 9, 254, 230, 28, 28, 132, 221, 232, 11, 153, 41, 39,
            214, 223, 141, 33, 15, 3, 100, 24, 239, 238, 30, 240, 183, 130, 130, 0, 148, 21, 0, 243, 124, 0, 0, 0, 20,
            0, 152, 143, 0, 0, 127, 106, 0, 0, 0, 0, 0, 0, 0, 231, 183, 63, 54, 0, 0, 0, 0, 0, 0, 0,
            175, 102, 158, 208, 56, 93, 211, 108, 32, 185, 235, 93, 124, 124, 119, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 241, 15,
            134, 219, 208, 188, 84, 22, 19, 21, 134, 0, 78, 68, 189, 127, 154, 21, 21, 144, 1, 234, 73, 181, 22, 83, 167,
            138, 234, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 204,
            49, 170, 44, 83, 46, 0};//Default message crcs used in generating a message checksum
    private static int sequenceNumber = 0;//counter for the number of messages transmitted

    /*
    Function Name: getMessageStart
    Return Type: byte []
    Access Type: private static
    Purpose:
    To generate and return a partially filled byte array of the correct size for the specified message identifier and with
    the correct initial(packets 0-5 inclusive) packets filled in.
    Parameters:
    int messageNumber, representing the numeric MAVLink message identifier.
    PreConditions: It is required that an array of bytes be generated whose size is that required of the given message and
    which has the starting packet data filled in
    PostConditions: A byte array of the required size is generated, the values required are filled in and it is returned.
     */
    private static byte [] getMessageStart(int messageNumber)
    {
        byte length = 0;
        switch(messageNumber)
        {
            case 39://Mission item message has payload length of 37
                length = 37;
                break;
            case 44://Mission count message has payload length of 4
                length = 4;
                break;
            case 45://Mission clear all message has payload length of 2
                length = 2;
                break;
            case 76://Command long message has payload length of 33
                length = 33;
                break;
        }
        byte [] bytes = new byte [(int)length+8];/*Every message has 8 bytes in addition to the payload*/
        bytes[0] = START_SYMBOL;/*start of message character*/
        bytes[1] = length;/*Length of the message payload*/
        bytes[2] = (byte)sequenceNumber;/*sequence number*/
        bytes[3] = SOURCE_SYSTEM_ID;/*system id*/
        bytes[4] = SOURCE_COMPONENT_ID;/*component id*/
        bytes[5] = (byte)messageNumber;/*message id*/
        return bytes;
    }
    /*
    Function Name: addFloatZeros
    Return Type: int
    Access Type: private static
    Purpose:
    To fill the given byte array with byte representations of floats with value 0 beginning at the specified offset
    and continuing the specified number of times
    Parameters:
    byte [] messageBytes, representing the array that is being filled
    int offset, representing the index at which to start to fill the array.
    int numTimes, representing the number of  zero floats to add to the array(each float is 4 bytes)
    PreConditions: It is required that floats with value 0 be added to a byte array beginning at a specified index.
    PostConditions: The floats are added as bytes and the new
    offset is returned(this is equal to the specified offset+(numTimes*4)).
     */
    private static int addFloatZeros(byte [] messageBytes, int offset, int numTimes)
    {
        for(int index = 0;index<numTimes;index++)
            offset = addByteArray(messageBytes,offset,ZERO_FLOAT);
        return offset;
    }
    /*
    Function Name: addSystemAndComponentBytes
    Return Type: int
    Access Type: private static
    Purpose:
    To add the target system id and target component id, the values of which
    are constant, to the specified byte array at the specified index.
    Parameters:
    byte [] messageBytes, representing the array that is being filled
    int offset, representing the index at which to add the system and component ids.
    PreConditions: It is required that the target system and component ids be added to a byte array representing a MAVLink message.
    PostConditions: The system and component ids are added to the array and the new
    offset is returned(this is equal to the specified offset+2).
     */
    private static int addSystemAndComponentBytes(byte [] messageBytes, int offset)
    {
        offset = addByte(messageBytes,offset,TARGET_SYSTEM_ID);
        return addByte(messageBytes,offset,TARGET_COMPONENT_ID);
    }
    /*
    Function Name: endMessage
    Return Type: byte []
    Access Type: private static
    Purpose:
    To add the checksum to the end of the specified message given by the specified offset,
    increment the sequence counter and return the completed message.
    Parameters:
    byte [] messageBytes, representing the array that is being filled
    int offset, representing the index at which the payload ends and so the checksum starts.
    PreConditions: It is required that a MAVLink message be completed, the sequence number incremented and the message returned.
    PostConditions: The MAVLink message is completed, the sequence number incremented and the message returned.
     */
    private static byte [] endMessage(byte [] messageBytes, int offset)
    {
        addByteArray(messageBytes,offset,calculateEncodeChecksum(messageBytes));/*The message checksum*/
        sequenceNumber++;
        return messageBytes;
    }
    /*
    Function Name: addByteArray
    Return Type: int
    Access Type: private static
    Purpose:
    To add the values from a source byte array to a target byte array at a given offset and return the new offset.
    Parameters:
    byte [] target, representing the byte array the values are to be copied to.
    int offset, representing the index at which the values are to be added.
    byte [] source, representing the array of values which are to be copied.
    PreConditions: It is required that an array of byte values be copied into another byte array at a specified index.
    PostConditions: The values are copied at the specified index and the new offset is returned.
     */
    private static int addByteArray(byte [] target, int offset, byte [] source)
    {
        System.arraycopy(source,0,target,offset,source.length);
        return offset+source.length;
    }
    /*
    Function Name: addByte
    Return Type: int
    Access Type: private static
    Purpose:
    To add the given byte value to a target byte array at a given offset and return the new offset.
    Parameters:
    byte [] target, representing the byte array the values are to be copied to.
    int offset, representing the index at which the values are to be added.
    byte source, representing the value which is to be copied.
    PreConditions: It is required that a byte value be copied into a byte array at a specified index.
    PostConditions: The value is copied at the specified index and the new offset is returned.
     */
    private static int addByte(byte [] target, int offset, byte source)
    {
        target[offset] = source;
        return offset+1;
    }
    /*
    Function Name: accumulate
    Return Type: int
    Access Type: private static
    Purpose:
    To accumulate the X.25 integer checksum for a MAVLink message by adding the hash of a
    byte value to the existing 16bit checksum.
    Parameters:
    byte data, representing the next byte in the message to be added to the checksum.
    int crc, indicating the currently calculated checksum value.
    PreConditions: It is required that checksum be accumulated for a byte value.
    PostConditions: The checksum is accumulated and the new checksum value returned.
     */
    private static int accumulate(byte data, int crc) {
        int tmp, tmpdata;
        int temp1 = 0x000000ff;
        int temp2 = 0x0000ffff;
        int crcaccum = crc & temp1;
        tmpdata = data & temp1;
        tmp = tmpdata ^ crcaccum;
        tmp &= temp1;
        int tmp4 = tmp << 4;
        tmp4 &= temp1;
        tmp ^= tmp4;
        tmp &= temp1;
        int crch = crc >> 8;
        crch &= temp2;
        int tmp8 = tmp << 8;
        tmp8 &= temp2;
        int tmp3 = tmp << 3;
        tmp3 &= temp2;
        tmp4 = tmp >> 4;
        tmp4 &= temp2;
        int tmpa = crch ^ tmp8;
        tmpa &= temp2;
        int tmpb = tmp3 ^ tmp4;
        tmpb &= temp2;
        crc = tmpa ^ tmpb;
        crc &= temp2;
        return crc;
    }

    /*
    Function Name: encodeCRC
    Return Type: int
    Access Type: private static
    Purpose:
    To generate an integer checksum as specified by X.25 for a specified MAVLink message excluding the start symbol.
    Parameters:
    byte [] message, representing the message for which the checksum is to be calculated.
    PreConditions: It is required that checksum be generated for a MAVLink message.
    PostConditions: The checksum is generated and returned.
     */
    private static int encodeCRC(byte[] message) {
        int crc = 0x0000ffff;
        for (int i = 1; i < message.length - 2; i++) {
            crc = accumulate(message[i], crc);
        }
        return crc;
    }
    /*
    Function Name: addMissionItem
    Return Type: byte []
    Access Type: public static
    Purpose:
    To generate and return a byte array representing a MAVLink MISSION_ITEM message
    containing either a land or waypoint command depending on the command number provided
    Parameters:
    BigDecimal latitude, representing the target latitude of the mission item.
    BigDecimal longitude, representing the target longitude of the mission item.
    BigDecimal altitude, representing the target altitude of the mission item.
    int commandNumber, representing the MAVLink CMD-Enum numeric value associated with the command.
    int missionSequence, representing the numeric order position in which this mission item appears within the flight plan.
    PreConditions: It is required that a MISSION_ITEM message containing either a land or waypoint command be generated.
    PostConditions: A byte [] containing the required MISSION_ITEM message is returned.
     */
    public static byte [] addMissionItem(BigDecimal latitude,BigDecimal longitude, BigDecimal altitude,int commandNumber,int missionSequence)
    {
        /*
        Excerpts from MAVLink common.xml specification
            <entry value="16" name="MAV_CMD_NAV_WAYPOINT">
                <description>Navigate to MISSION.</description>
                <param index="1">Hold time in decimal seconds. (ignored by fixed wing, time to stay at MISSION for rotary wing)</param>
                <param index="2">Acceptance radius in meters (if the sphere with this radius is hit, the MISSION counts as reached)</param>
                <param index="3">0 to pass through the WP, if > 0 radius in meters to pass by WP. Positive value for clockwise orbit, negative value for counter-clockwise orbit. Allows trajectory control.</param>
                <param index="4">Desired yaw angle at MISSION (rotary wing)</param>
                <param index="5">Latitude</param>
                <param index="6">Longitude</param>
                <param index="7">Altitude</param>
            </entry>
            <entry value="21" name="MAV_CMD_NAV_LAND">
                <description>Land at location</description>
                <param index="1">Empty</param>
                <param index="2">Empty</param>
                <param index="3">Empty</param>
                <param index="4">Desired yaw angle.</param>
                <param index="5">Latitude</param>
                <param index="6">Longitude</param>
                <param index="7">Altitude</param>
            </entry>
        */
        int offset = START_OF_PAYLOAD;
        byte [] messageBytes = getMessageStart(39);
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(5));/*Hold time in seconds as a float*/
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(2));/*Acceptance radius as a float for waypoint, ignored by land*/
        offset = addFloatZeros(messageBytes,offset,2);/*Unused parameters*/
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(latitude.doubleValue()));/*Latitude as a float*/
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(longitude.doubleValue()));/*Longitude as a float*/
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(altitude.doubleValue()));/*Altitude as a float*/
        offset = addByteArray(messageBytes,offset,NumberParser.parseFrom16BitInteger(missionSequence));/*The mission sequence number as a 16-bit integer*/
        offset = addByteArray(messageBytes,offset,NumberParser.parseFrom16BitInteger(commandNumber));/*The command number as a 16-bit integer*/
        offset = addSystemAndComponentBytes(messageBytes,offset);/*Add the target system and component bytes*/
        offset = addByte(messageBytes,offset,FRAME);/*The frame as an 8-bit integer, 0 for global*/
        offset = addByte(messageBytes,offset,(byte)0);/*Current flag as an 8-bit integer*/
        offset = addByte(messageBytes,offset,AUTOCONTINUE);/*AutoContinue as an 8-bit integer*/
        return endMessage(messageBytes,offset);
    }
    /*
    Function Name: takeOff
    Return Type: byte []
    Access Type: public static
    Purpose:
    To generate and return a byte array representing a MAVLink COMMAND_LONG
    message containing a MAVLink takeoff command
    Parameters:
    int missionSequence, representing the number of times this command has been transmitted.
    PreConditions: It is required that a COMMAND_LONG message containing a takeoff command be generated.
    PostConditions: A byte [] containing the required COMMAND_LONG is returned.
     */
    public static byte [] takeOff(int missionSequence)
    {
        /*
        Excerpt from MAVLink common.xml specification
            <entry value="22" name="MAV_CMD_NAV_TAKEOFF">
                <description>Takeoff from ground / hand</description>
                <param index="1">Minimum pitch (if airspeed sensor present), desired pitch without sensor</param>
                <param index="2">Empty</param>
                <param index="3">Empty</param>
                <param index="4">Yaw angle (if magnetometer present), ignored without magnetometer</param>
                <param index="5">Latitude</param>
                <param index="6">Longitude</param>
                <param index="7">Altitude</param>
            </entry>
        */
        int offset = START_OF_PAYLOAD;
        byte [] messageBytes = getMessageStart(76);
        offset = addFloatZeros(messageBytes,offset,7);/*Unused parameters*/
        offset = addByteArray(messageBytes,offset,TAKEOFF_COMMAND);/*The command number as a 16-bit integer*/
        offset = addSystemAndComponentBytes(messageBytes,offset);/*Add the target system and component bytes*/
        offset = addByte(messageBytes,offset,(byte)missionSequence);/*Mission Sequence as an 8-bit integer*/
        return endMessage(messageBytes,offset);
    }
    /*
    Function Name: faceNorth
    Return Type: byte []
    Access Type: public static
    Purpose:
    To generate and return a byte array representing a MAVLink COMMAND_LONG
    message containing a MAVLink Condition_Yaw command which tells the drone to face north
    Parameters:
    int missionSequence, representing the number of times this command has been transmitted.
    PreConditions: It is required that a COMMAND_LONG message containing a Condition_Yaw command be generated to make the drone face north.
    PostConditions: A byte [] containing the required COMMAND_LONG is returned.
     */
    public static byte [] faceNorth(int missionSequence)
    {
        /*
        Excerpt from MAVLink common.xml specification.
           <entry value="115" name="MAV_CMD_CONDITION_YAW">
                <description>Reach a certain target angle.</description>
                <param index="1">target angle: [0-360], 0 is north</param>
                <param index="2">speed during yaw change:[deg per second]</param>
                <param index="3">direction: negative: counter clockwise, positive: clockwise [-1,1]</param>
                <param index="4">relative offset or absolute angle: [ 1,0]</param>
                <param index="5">Empty</param>
                <param index="6">Empty</param>
                <param index="7">Empty</param>
           </entry>
         */
        int offset = START_OF_PAYLOAD;
        byte [] messageBytes = getMessageStart(76);
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(0));/*The target angle(0 is north) as a float*/
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(60));/*The speed at which to spin in degrees per second as a float*/
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(1));/*The direction in which to spin(positive for clockwise) as a float*/
        offset = addFloatZeros(messageBytes,offset,4);/*Unused parameters*/
        offset = addByteArray(messageBytes,offset,SPIN_COMMAND);/*The command number as a 16-bit integer*/
        offset = addSystemAndComponentBytes(messageBytes,offset);/*Add the target system and component bytes*/
        offset = addByte(messageBytes,offset,(byte)missionSequence);/*The mission sequence number as an 8-bit integer, specifying how many times this command has been sent*/
        return endMessage(messageBytes,offset);
    }
    /*
    Function Name: beginMission
    Return Type: byte []
    Access Type: public static
    Purpose:
    To generate and return a byte array representing a MAVLink COMMAND_LONG
    message containing a MAVLink Condition_Yaw command which tells the drone to face north
    Parameters:
    int missionSequence, representing the number of times this command has been transmitted.
    int lastIndex, representing the index of the last mission item in numerical order to be run
    PreConditions: It is required that a COMMAND_LONG message containing a Mission start command be generated to make the drone fly its course.
    PostConditions: A byte [] containing the required COMMAND_LONG is returned.
     */
    public static byte [] beginMission(int missionSequence, int lastIndex)
    {
        /*
        Excerpt from MAVLink common.xml specification
           <entry value="300" name="MAV_CMD_MISSION_START">
                <description>start running a mission</description>
                <param index="1">first_item: the first mission item to run</param>
                <param index="2">last_item:  the last mission item to run (after this item is run, the mission ends)</param>
           </entry>
         */
        int offset = START_OF_PAYLOAD;
        byte [] messageBytes = getMessageStart(76);
        offset = addFloatZeros(messageBytes,offset,1);//Start index as a float
        offset = addByteArray(messageBytes,offset,NumberParser.parseFromFloat(new BigDecimal(lastIndex).doubleValue()));/*The end index as a float*/
        offset = addFloatZeros(messageBytes,offset,5);/*Unused parameters*/
        offset = addByteArray(messageBytes,offset,BEGIN_COMMAND);/*The command number as a 16-bit integer*/
        offset = addSystemAndComponentBytes(messageBytes,offset);/*Add the target system and component bytes*/
        offset = addByte(messageBytes,offset,(byte)missionSequence);/*The mission sequence number as an 8-bit integer, specifying how many times this command has been sent*/
        return endMessage(messageBytes,offset);
    }
    /*
    Function Name: missionClearAll
    Return Type: byte []
    Access Type: public static
    Purpose:
    To generate and return a byte array representing a MAVLink MISSION_CLEAR_ALL message
    Parameters:
    None
    PreConditions: It is required that a MISSION_CLEAR_ALL message be generated to make the drone clear its currently stored waypoints.
    PostConditions: A byte [] containing the required MISSION_CLEAR_ALL message is returned.
     */
    public static byte [] missionClearAll()
    {
        int offset = START_OF_PAYLOAD;
        byte [] messageBytes = getMessageStart(45);
        return endMessage(messageBytes,addSystemAndComponentBytes(messageBytes,offset));
    }
    /*
    Function Name: missionCount
    Return Type: byte []
    Access Type: public static
    Purpose:
    To generate and return a byte array representing a MAVLink MISSION_COUNT message with the specified number of mission items
    Parameters:
    int number, the number of mission items
    PreConditions: It is required that a MISSION_COUNT message be generated with the specified number of mission items.
    PostConditions: A byte [] containing the required MISSION_COUNT message is returned.
     */
    public static byte [] missionCount(int number)
    {
        int offset = START_OF_PAYLOAD;
        byte [] messageBytes = getMessageStart(44);
        offset = addByteArray(messageBytes,offset,NumberParser.parseFrom16BitInteger(number));/*16-bit integer representing the count*/
        return endMessage(messageBytes,addSystemAndComponentBytes(messageBytes,offset));
    }
    /*
    Function Name: calculateEncodeChecksum
    Return Type: byte []
    Access Type: private static
    Purpose:
    To generate a 2-byte checksum based on the content of a message represented by an array of bytes.
    Parameters:
    byte [] message, representing the message for which the checksum is to be calculated.
    PreConditions: It is required that checksum be generated for and appended to a MAVLink message.
    PostConditions: The checksum is generated and returned.
     */
    public static byte [] calculateEncodeChecksum(byte [] message)
    {
        //get the integer value of the checksum of the message
        int crc = encodeCRC(message);
        // CRC-EXTRA for Mavlink 1.0
        crc = accumulate((byte) MAVLINK_MESSAGE_CRCS[(int)message[5]], crc);
        //Convert to a 16-bit integer represented by an array of bytes and return
        return NumberParser.parseFrom16BitInteger(crc);
    }
}
