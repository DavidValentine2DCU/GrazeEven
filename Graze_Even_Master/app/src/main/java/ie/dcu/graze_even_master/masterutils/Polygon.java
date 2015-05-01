package ie.dcu.graze_even_master.masterutils;

import android.util.Log;

public class Polygon {
    //variables holding the x and y coordinates of the vertices and the number of each
    private int numVertices = 0;
    private double [] xPoints = new double[10];
    private double [] yPoints = new double[10];
    /*
    Function Name: pointInPolygon
    Return Type: boolean
    Access Type: private static
    Purpose:
    To determine whether the specified point is inside a specified polygon using ray casting.
    Parameters:
    int verticesNumber, representing the number of vertices in the polygon.
    double [] verticesX, representing the x coordinates of the vertices in the polygon.
    double [] verticesY, representing the y coordinates of the vertices in the polygon.
    double pointX, representing the x coordinate of the point.
    double pointY, representing the y coordinate of the point.
    PreConditions: It is required to know if a specified point is in a specified polygon.
    PostConditions: The value indicating whether or not the point is in the polygon is returned.
     */
    private static boolean pointInPolygon (int verticesNumber, double[] verticesX, double[] verticesY, double pointX, double pointY){
        int index, nextIndex;
        boolean inside = false;
        for (index=0,nextIndex=verticesNumber-1;index<verticesNumber;nextIndex=index++){
            if(((verticesY[index]>pointY) != (verticesY[nextIndex]>pointY)) &&
                    (pointX<(verticesX[nextIndex]-verticesX[index]) * (pointY-verticesY[index]) / (verticesY[nextIndex]-      verticesY[index]) + verticesX[index]))
                inside=!inside;
        }
        return inside;
    }

    public Polygon(){}
    //costructor decodes a formatted string to a polygon
    public Polygon(String polygonString){
        try {
            String[] vertices = polygonString.split(":");
            for (String vertexString : vertices) {
                String vertex[] = vertexString.split(",");
                addPoint(Double.parseDouble(vertex[0]), Double.parseDouble(vertex[1]));
            }
        }
        catch(Exception e){
            Log.e("Polygon Creation",e.toString(),e);
        }
    }
    /*
    Function Name: addPoint
    Return Type: void
    Access Type: public
    Purpose:
    To add a vertex given by the specified point to this polygon.
    Parameters:
    int x, representing the x coordinate of the point.
    int y, representing the y coordinate of the point.
    PreConditions: It is required that a point be added to this polygon.
    PostConditions: The specified point has been added to the polygon.
     */
    public void addPoint(int x, int y){
        this.addPoint((double)x,(double)y);
    }
    /*
    Function Name: addPoint
    Return Type: void
    Access Type: public
    Purpose:
    To add a vertex given by the specified point to this polygon.
    Parameters:
    double x, representing the x coordinate of the point.
    double y, representing the y coordinate of the point.
    PreConditions: It is required that a point be added to this polygon.
    PostConditions: The specified point has been added to the polygon.
     */
    public void addPoint(double x, double y){
        //add the point to the polygon
        xPoints[numVertices] = x;
        yPoints[numVertices] = y;
        numVertices++;
        //if we've run out of space in the internal arrays resize the arrays
        if(numVertices>=xPoints.length)
        {
            int length = xPoints.length;
            double [] newXPoints = new double [length*2];
            double [] newYPoints = new double [length*2];
            System.arraycopy(xPoints,0,newXPoints,0,length);
            System.arraycopy(yPoints,0,newYPoints,0,length);
            xPoints = newXPoints;
            yPoints = newYPoints;
        }
    }
    /*
    Function Name: contains
    Return Type: boolean
    Access Type: public
    Purpose:
    To determine whether the specified point is inside this polygon.
    Parameters:
    double x, representing the x coordinate of the point.
    double y, representing the y coordinate of the point.
    PreConditions: It is required to know if a point is in this polygon.
    PostConditions: The value indicating whether or not the point is in this polygon is returned.
     */
    public boolean contains(double x, double y){
        return pointInPolygon(numVertices,xPoints,yPoints,x,y);
    }
    //methods to return the x and y coordinates of the polygon vertices and the number of vertices
    public int getNumVertices()
    {
        return numVertices;
    }
    public double [] getXPoints()
    {
        return xPoints;
    }
    public double [] getYPoints()
    {
        return yPoints;
    }
    /*
    Function Name: toString
    Return Type: String
    Access Type: public
    Purpose:
    To return a formatted String representation of this polygon.
    Parameters:
    None
    PreConditions: It is required to acquire a String representation of this polygon.
    PostConditions: The String representation of this polygon is returned.
     */
    public String toString(){
        String polyString = "";
        for(int index = 0;index<numVertices;index++)
        {
            polyString+=xPoints[index]+","+yPoints[index]+":";
        }
        return polyString;
    }
}