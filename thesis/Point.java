package thesis;

import java.math.BigDecimal;

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
    
    public boolean isZero() {
        return x == 0.0 && y == 0.0 && z == 0.0;
    }
    
    public boolean isClose(double x, double y, double z) {
        
        double distance = distance(x, y, z);
        
        if (Math.sqrt(distance) < Environment.STEP + 1) return true;
        else return false;
    }   
    
    public double distance(double x, double y, double z) {
        double rx = Math.pow(this.x - x, 2);
        double ry = Math.pow(this.y - y, 2);
        double rz = Math.pow(this.z - z, 2);
        
        return Math.sqrt(rx + ry + rz);
    }
    
    public String toString() {
        return "x:" + x + " y:" + y + " z:" + z  ;
    }
    
    public static void main(String[] args) {
        Point p = new Point(1, 1, 1);
        
        System.out.println(p.distance(0, 0, 0));
    }
    
}
