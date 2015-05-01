package ie.dcu.graze_even_master.location;

public class MapArea implements Area {
    private int value;
    MapArea(int value){
        this.value = value;
    }
    //method to additively update this area object given the value to update by
    @Override
    public void update(int addValue) {
        value+=addValue;
    }

    //getter for the areas value
    @Override
    public int getArea() {
        return value;
    }

    //method to create and return a new area object whose value is the sum of this areas value and the given areas value
    @Override
    public Area combine(Area other) {
        if(other.getArea()==-1)
            return new NullArea();
        else
            return new MapArea(value+other.getArea());
    }
    //method to return a string representation of this area
    public String toString()
    {
        return value+"";
    }
}
