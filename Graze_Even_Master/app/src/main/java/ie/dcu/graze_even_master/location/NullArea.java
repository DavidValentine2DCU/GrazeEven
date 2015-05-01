package ie.dcu.graze_even_master.location;

public class NullArea implements Area {
    //Is null and cant be updated so throw new unsupportedoperation exception
    @Override
    public void update(int addValue) {
        throw new UnsupportedOperationException();
    }

    //has to return an int to comply with interface, returns negative 1
    @Override
    public int getArea() {
        return -1;
    }

    //Combining a null area with another null or non null area returns a null area
    @Override
    public Area combine(Area other) {
        return this;
    }
    //method to return a string representation of this null area object
    public String toString()
    {
        return "n";
    }
}
