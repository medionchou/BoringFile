package thesis;

public class Point {

    public double x;
    public double y;
    public double z;
    
    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }
    
    public void set(double x, double y, double z) {
    	this.x = x;
    	this.y = y;
    	this.z = z;
    }
    
    public void set(Point pt) {
    	this.x = pt.x;
    	this.y = pt.y;
    	this.z = pt.z;
    }
     
    
    public String toString() {
        return "x:" + x + " y:" + y + " z:" + z  ;
    }
}
