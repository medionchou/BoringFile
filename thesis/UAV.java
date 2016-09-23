package thesis;

import java.util.Iterator;
import java.util.Random;

import edu.princeton.cs.algs4.StdRandom;

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
        if (sir < 0.1) return 0;
        else if (sir >= 15) return 4;
        else return Math.log10(sir + 1) / Math.log10(2);
    }
    
    protected void moveByPoint(Point pt, int grid_size) {
        move(pt.x, 0, 0, grid_size);
        move(0, pt.y , 0, grid_size);
        move(0, 0, pt.z, Environment.MAX_HEIGHT);
    }

    protected Point randomPoint(int grid_size, int maxHeight, double zweight) {
        Point pt = new Point(0, 0, 0);
        
        while (true) {
            pt.x = (StdRandom.uniform(3) - 1) * Environment.STEP;
            pt.y = (StdRandom.uniform(3) - 1) * Environment.STEP;
            pt.z = (StdRandom.uniform(3) - 1) * Environment.STEP * zweight;
            
            if (checkBoundary(pt, grid_size, maxHeight)) return pt;           
        }
    }

    private void move(double x, double y, double z, int boundary) {
        if (checkBoundary(this.x + x, boundary)) this.x += x;
        if (checkBoundary(this.y + y, boundary)) this.y += y;
        if (checkBoundary(this.z + z, boundary)) this.z += z; 
    }
    
    public boolean checkBoundary(Point pt, int grid_size, int maxHeight) {
        if ((this.x + pt.x) < 0 || (this.x + pt.x) > grid_size) return false;
        if ((this.y + pt.y) < 0 || (this.y + pt.y) > grid_size) return false;
        if ((this.z + pt.z) < 0 || (this.z + pt.z) > maxHeight) return false;
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
