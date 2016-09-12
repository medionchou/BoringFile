package thesis;

import java.util.Iterator;
import java.util.Random;

public abstract class UAV {

    private static int ID = 0; //for automatically increment id
    
    private int id;
    private double x;
    private double y;
    private double z;
    private boolean isOpen;
    
    

    
    public UAV(double x, double y, double z, boolean isOpen) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isOpen = isOpen;
        id = ID++;
    }
    
    public void open(boolean isOpen) {
        this.isOpen = isOpen; 
    }
    
    public int getID() {
        return id;
    }
    
    public double x() {
        return x;
    }
    
    public double y() {
        return y;
    }
    
    public double z() {
        return z;
    }
    
    public static double ld(double sir) {
        double result = Math.log10(sir + 1) / Math.log10(2);
        
        if (result < 0.1) return 0;
        else if (result > 4) return 4;
        else return result;
    }
    
    protected void moveByPoint(Point pt, int grid_size) {
        move(pt.x, 0, 0, grid_size);
        move(0, pt.y , 0, grid_size);
        move(0, 0, pt.z, Environment.MAX_HEIGHT);
    }

    protected Point randomPoint(int grid, int height, double weight) {
        Random r = new Random();
        Point pt = new Point(0, 0, 0);
        while (true) {
            pt.x = (r.nextInt(3) - 1) * Environment.STEP;
            pt.y = (r.nextInt(3) - 1) * Environment.STEP;
            pt.z = (r.nextInt(3) - 1) * Environment.STEP * weight;
            
            if (checkBoundary(pt, grid, height)) return pt;           
        }
    }

    private void move(double x, double y, double z, int boundary) {
        if (checkBoundary(this.x + x, boundary)) this.x += x;
        if (checkBoundary(this.y + y, boundary)) this.y += y;
        if (checkBoundary(this.z + z, boundary)) this.z += z; 
    }
    
    public boolean checkBoundary(Point pt, int grid, int height) {
        if ((this.x + pt.x) < 0 || (this.x + pt.x) > grid) return false;
        if ((this.y + pt.y) < 0 || (this.y + pt.y) > grid) return false;
        if ((this.z + pt.z) < 0 || (this.z + pt.z) > height) return false;
        else return true;
    }
    
    private boolean checkBoundary(double coordinate, int boundary) {
        if (coordinate < 0 || coordinate > boundary) return false;
        else return true;
    }
    
    public String toString() {
        return "x: " + x + " y:" + y + " z:" + z;
    }
    
    public abstract double[] getSpectrumAndTerms(Grid[][] grid);
    
    public abstract void run(Grid[][] grid);
    
    public static void main(String[] args) {
        
    }
}
