package ie.dcu.graze_even_master.communication;

public interface MessageHandler extends Runnable {
    /*
    Function Name: setMessage
    Return Type: void
    Access Type: public
    Purpose:
    To allow the MessageHandler instance to store the message it is meant to handle.
    Parameters:
    byte [] message, the message the MessageHandler is meant to handle, represented by and array of bytes
    PreConditions: It is required that the MessageHandler store the message it is to handle.
    PostConditions: The MessageHandler stores the message it is to handle.
     */
    public void setMessage(byte [] message);
}
