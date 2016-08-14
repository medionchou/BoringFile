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
    
    protected void moveByStrategy(Strategy stgy) {
        switch (stgy) {
        case UP:
            move(0, 0, STEP);
            break;
        case DOWN:
            move(0, 0, -STEP);
            break;
        case FORWARD:
            move(STEP, 0, 0);
            break;
        case BACKWARD:
            move(-STEP, 0, 0);
            break;
        case RIGHT:
            move(0, STEP, 0);
            break;
        case LEFT:
            move(0, -STEP, 0);
            break;
        }
    }

    private void move(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }
    
    public String toString() {
        return "x: " + x + " y:" + y + " z:" + z;
    }
    
    public abstract void run();
}
