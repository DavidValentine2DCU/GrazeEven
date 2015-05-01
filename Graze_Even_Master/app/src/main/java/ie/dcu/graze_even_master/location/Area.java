package ie.dcu.graze_even_master.location;

public interface Area {
    /*
    Function Name: update
    Return Type: void
    Access Type: public
    Purpose:
    To update this area given the value to update it by.
    Parameters:
    int addValue, representing the value with which to update the area.
    PreConditions: This area needs to be updated by the specified amount.
    PostConditions: This area has been updated by the specified amount.
     */
    public void update(int addValue);
    public int getArea();
    /*
    Function Name: combine
    Return Type: Area
    Access Type: public
    Purpose:
    To return an Area whose value is the sum of this area's value and the value of the provided area.
    Parameters:
    Area other, representing the area to combine this area with.
    PreConditions: It is required to acquire an Area representing the combination of this area and another area.
    PostConditions: The Area representing the combination of this area and another area is returned.
     */
    public Area combine(Area other);
    /*
    Function Name: toString
    Return Type: String
    Access Type: public
    Purpose:
    To return a formatted String representation of this Area.
    Parameters:
    None
    PreConditions: It is required to acquire a String representation of this Area.
    PostConditions: The String representation of this Area is returned.
     */
    public String toString();
}
