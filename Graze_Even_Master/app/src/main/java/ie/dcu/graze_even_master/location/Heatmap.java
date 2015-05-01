package ie.dcu.graze_even_master.location;

import android.util.Log;

public class Heatmap implements Map {
    //the width length and areas of the heatmap
    private Area[] map;
    private int length;
    private int width;
    Heatmap(int width,int length)
    {
        //set the width and height and fill the map with empty areas
        this.length = length;
        this.width = width;
        this.map = new Area[width*length];
        for(int yCoordinate = 0;yCoordinate<length;yCoordinate++)
        {
            for(int xCoordinate = 0;xCoordinate<width;xCoordinate++)
            {
                map[xCoordinate+(yCoordinate*width)] = AreaFactory.getInstance("0");
            }
        }
    }
    //update the area at a given location with a given value
    @Override
    public void update(int x, int y, int addValue) throws OutOfBoundsException {
        //if its not in the map or null throw an outofbounds exception
        if(x<0||y<0||x>width||y>length)
                throw new OutOfBoundsException();
        try {
            map[x+(y*width)].update(addValue);
        }
        catch(UnsupportedOperationException uoe) {
            throw new OutOfBoundsException();
        }
    }

    //method to return the area object at a given location
    @Override
    public Area get(int x, int y) {
        return map[x+(y*width)];
    }

    //method to set the area at a given location to a given area
    @Override
    public void set(int x, int y, Area newValue) {
        map[x+(y*width)] = newValue;
    }

    //method to return a string representation of this heatmap instance
    @Override
    public String toString()
    {
        String mapString = width+";;"+length+";;";
        for(Area area:map)
        {
                mapString += area.toString()+",";
        }
        mapString.substring(0,mapString.length()-1);
        return mapString;
    }
}
