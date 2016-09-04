package thesis;

import java.util.Iterator;

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
        double result = Math.log(sir + 1) / Math.log(2);
        
        if (result < 0.1) return 0;
        else if (result > 4) return 4;
        else return result;
    }
    
    protected void moveByStrategy(Strategy stgy, int grid_size) {
        switch (stgy) {
        case STILL:
            break;
        case UP:
            move(0, 0, Environment.STEP * 0.1, Environment.MAX_HEIGHT);
            break;
        case DOWN:
            move(0, 0, -Environment.STEP * 0.1, Environment.MAX_HEIGHT);
            break;
        case FORWARD:
            move(0, -Environment.STEP, 0, grid_size);
            break;
        case BACKWARD:
            move(0, Environment.STEP, 0, grid_size);
            break;
        case RIGHT:
            move(Environment.STEP, 0, 0, grid_size);
            break;
        case LEFT:
            move(-Environment.STEP, 0, 0, grid_size);
            break;
        }
    }
    
    public static Point strategyToPoint(Strategy stgy) {
        switch (stgy) {
//        case STILL:
//            return new Point(0, 0, 0);
        case UP:
            return new Point(0, 0, Environment.STEP * 0.1);
        case DOWN:
            return new Point(0, 0, -Environment.STEP * 0.1);
        case FORWARD:
            return new Point(0, -Environment.STEP, 0);
        case BACKWARD:
            return new Point(0, Environment.STEP, 0);
        case RIGHT:
            return new Point(Environment.STEP, 0, 0);
        case LEFT:
            return new Point(-Environment.STEP, 0, 0);
        }
        return new Point(0, 0, 0);
    }

    private void move(double x, double y, double z, int boundary) {
        if (checkBoundary(this.x + x, boundary)) this.x += x;
        if (checkBoundary(this.y + y, boundary)) this.y += y;
        if (checkBoundary(this.z + z, boundary)) this.z += z; 
    }
    
    public boolean checkBoundary(Point pt, int boundary) {
        if (pt.x < 0 || pt.x > boundary) return false;
        if (pt.y < 0 || pt.y > boundary) return false;
        if (pt.z < 0 || pt.z > boundary) return false;
        else return true;
    }
    
    private boolean checkBoundary(double coordinate, int boundary) {
        if (coordinate < 0 || coordinate > boundary) return false;
        else return true;
    }
    
    public String toString() {
        return "x: " + x + " y:" + y + " z:" + z;
    }
    
    public abstract double getSpectrum(Grid[][] grid);
    
    public abstract void run(Grid[][] grid);
}
