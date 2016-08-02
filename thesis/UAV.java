package thesis;

public class UAV {

    public final static int MAX_HEIGHT = 100;
    public final static double MOVE_MOMENT = 0.5;

    private double x;
    private double y;
    private double z;
    private boolean isOpen;
    private Heuristic heuristic;

    
    public UAV(double x, double y, double z, boolean isOpen, Heuristic heuristic) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isOpen = isOpen;
        this.heuristic = heuristic;
    }
    
    public void run() {
        
    }
    
    private double utility() {
        
        return 0.0;
    }
    

    
}
