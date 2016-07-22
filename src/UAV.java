
public class UAV {

    private int x;
    private int y;
    private int z;
    private boolean isOpen;
    private Heuristic heuristic;

    
    public UAV(int x, int y, int z, boolean isOpen, Heuristic heuristic) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isOpen = isOpen;
        this.heuristic = heuristic;
    }
    
    public void run() {
        
    }
    
    private int strategy() {
        return 0;
    }
    
    private double utility() {
        
        return 0.0;
    }
    
}
