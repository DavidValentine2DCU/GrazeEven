package ie.dcu.graze_even_master.location;

public interface Map {
    /*
    Function Name: update
    Return Type: void
    Access Type: public
    Purpose:
    To update the area at location (x,y) within the map given the value to update it by.
    Parameters:
    int x, representing the x coordinate location of the area within the map.
    int y, representing the y coordinate location of the area within the map.
    int addValue, representing the value with which to update the area.
    PreConditions: A specified area within the map needs to be updated by the specified amount.
    PostConditions: The specified area within the map has been updated by the specified amount.
     */
    public void update(int x, int y, int addValue) throws OutOfBoundsException;
    public Area get(int x, int y);
    public void set(int x, int y, Area newValue);
    /*
    Function Name: toString
    Return Type: String
    Access Type: public
    Purpose:
    To return a formatted String representation of this Map.
    Parameters:
    None
    PreConditions: It is required to acquire a String representation of this Map.
    PostConditions: The String representation of this Map is returned.
     */
    public String toString();
}
