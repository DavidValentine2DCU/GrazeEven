package ie.dcu.graze_even_master.location;

class AreaFactory {
    //All NullAreas are the same, no need to clog the system with lots of instances
    private static final NullArea nullInstance = new NullArea();
    /*
    Function Name: getInstance
    Return Type: Area
    Access Type: public static
    Purpose:
    To return an Area instance with the specified value.
    Parameters:
    String value, representing the value of the Area
    PreConditions: It is required to acquire an Area with a specified value.
    PostConditions: The an Area instance with the specified value is returned.
     */
    public static Area getInstance(String value){
        switch(value) {
            case "null":
            case "n":
            case "":
            case "-1":
                return nullInstance;
            default:
                return new MapArea(Integer.parseInt(value));
        }
    }
}
