package ie.dcu.graze_even_master.drone;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

class NumberParser {

    /*
    Function Name: parseFromFloat
    Return Type: byte []
    Access Type: private static
    Purpose:
    To parse the given integer value to a float and return an array of bytes representing this float value
    Parameters:
    int number, representing the integer value to be converted to a float and then to a byte array.
    PreConditions: It is required that a byte array representing the float value of an integer be generated.
    PostConditions: The required byte array is generated and returned.
     */
    public static byte [] parseFromFloat(int number)
    {
        return parseFromFloat((double)number);
    }
    /*
    Function Name: parseFromFloat
    Return Type: byte []
    Access Type: private static
    Purpose:
    To parse the given double value to a float and return an array of bytes representing this float value.
    Parameters:
    double number, representing the double value to be converted to a float and then to a byte array.
    PreConditions: It is required that a byte array representing the float value of a double be generated.
    PostConditions: The required byte array is generated and returned.
     */
    public static byte [] parseFromFloat(double number){
        float floatNumber = new BigDecimal(number).floatValue();
        byte [] bytes = ByteBuffer.allocate(4).putFloat(floatNumber).array();
        //This produces the array we want but backwards so swap the values
        byte temp = bytes[0];
        bytes[0] = bytes[3];
        bytes[3] = temp;
        temp = bytes[1];
        bytes[1] = bytes[2];
        bytes[2] = temp;
        return bytes;
    }
    /*
    Function Name: parseFrom16BitInteger
    Return Type: byte []
    Access Type: private static
    Purpose:
    To parse the given integer value to a 16-bit short integer value and return an array of bytes representing this short value.
    Parameters:
    double number, representing the integer value to be converted to a short and then to a byte array.
    PreConditions: It is required that a byte array representing the short value of a integer be generated.
    PostConditions: The required byte array is generated and returned.
     */
    public static byte[] parseFrom16BitInteger(int number)
    {
        short shortNumber = new BigDecimal(number).shortValue();
        byte [] ret = new byte[2];
        int temp = 0x00FF;
        ret[0] = (byte)(shortNumber & temp);
        ret[1] = (byte)((shortNumber >> 8) & temp);
        return ret;
    }
    /*
    Function Name: parseTo32BitNumber
    Return Type: long
    Access Type: public static
    Purpose:
    To convert the given array of bytes(which should be of size 4) to a 32-bit long value and return this value.
    Parameters:
    byte [] bytes, representing the bytes to be converted into the long value.
    PreConditions: It is required that an array of 4 bytes be converted to a 32-bit long value.
    PostConditions: A 32-bit long value is calculated from the bytes and returned.
     */
    public static long parseTo32BitNumber(byte [] bytes)
    {
        long value = bytes[0] & 0xFF;
        value |= (bytes[1] << 8) & 0xFFFF;
        value |= (bytes[2] << 16) & 0xFFFFFF;
        value |= (bytes[3] << 24);
        return value;
    }
    /*
    Function Name: parseTo16BitInteger
    Return Type: int
    Access Type: public static
    Purpose:
    To convert the given array of bytes(which should be of size 2) to a 16-bit integer value and return this value.
    Parameters:
    byte [] bytes, representing the bytes to be converted into the integer value.
    PreConditions: It is required that an array of 2 bytes be converted to a 16-bit integer value.
    PostConditions: A 16-bit integer value is calculated from the bytes and returned.
     */
    public static int parseTo16BitInteger(byte[]bytes)
    {
        short int16 = (short)(((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF));
        return ((int)int16)%255;
    }
}
