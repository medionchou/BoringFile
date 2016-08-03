package thesis;

public abstract class UAV {

    public final static int MAX_HEIGHT = 100;
    public final static double MOVE_MOMENT = 0.5;
    public final static double TRANSMIT_POWER = 46;

    private static int ID; //for automatically increment id
    
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
    
    public void move(double x, double y, double z, boolean isOpen) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.isOpen = isOpen;
    }
    
    public int getID() {
        return id;
    }
    
    public abstract void run();
}
