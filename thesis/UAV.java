package thesis;

import java.util.Iterator;

public abstract class UAV {

    public static final int MAX_HEIGHT = 100;
    public static final double STEP = 0.5;
    public static final double TRANSMIT_POWER = 46;

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
        
        return result < 0.1 ? 0 : result;
    }
    
    protected void moveByStrategy(Strategy stgy, int grid_size) {
        switch (stgy) {
        case UP:
            move(0, 0, STEP, grid_size);
            break;
        case DOWN:
            move(0, 0, -STEP, grid_size);
            break;
        case FORWARD:
            move(STEP, 0, 0, grid_size);
            break;
        case BACKWARD:
            move(-STEP, 0, 0, grid_size);
            break;
        case RIGHT:
            move(0, STEP, 0, grid_size);
            break;
        case LEFT:
            move(0, -STEP, 0, grid_size);
            break;
        }
    }
    
    public static Point strategyToPoint(Strategy stgy) {
        switch (stgy) {
        case UP:
            return new Point(0, 0, STEP);
        case DOWN:
            return new Point(0, 0, -STEP);
        case FORWARD:
            return new Point(STEP, 0, 0);
        case BACKWARD:
            return new Point(-STEP, 0, 0);
        case RIGHT:
            return new Point(0, STEP, 0);
        case LEFT:
            return new Point(0, -STEP, 0);
        }
        return new Point(0, 0, 0);
    }

    private void move(double x, double y, double z, int grid_size) {
        if (checkBoundary(this.x + x, grid_size)) this.x += x;
        if (checkBoundary(this.y + y, grid_size)) this.y += y;
        if (checkBoundary(this.z + z, grid_size)) this.z += z; 
    }
    
    public boolean checkBoundary(Point pt, int grid_size) {
        if (pt.x < 0 || pt.x > grid_size) return false;
        else if (pt.y < 0 || pt.y > grid_size) return false;
        else if (pt.z < 0 || pt.z > grid_size) return false;
        else return true;
    }
    
    private boolean checkBoundary(double coordinate, int grid_size) {
        if (coordinate < 0 || coordinate > grid_size) return false;
        else return true;
    }
    
    public String toString() {
        return "x: " + x + " y:" + y + " z:" + z;
    }
    
    public abstract void run(Grid[][] grid);
}
