package thesis;

import java.util.LinkedList;

public class Terminal {

    private int x;
    private int y;
    
    private int weight;
    private LinkedList<UAV> uavList;
    
    public Terminal(int x, int y, int weight) {
        this.x = x;
        this.y = y;
        this.weight = weight;
        uavList = new LinkedList<>();
    }
    
    public void addUAV(UAV uav) {
        uavList.add(uav);
    }
    
    public int getWeight() {
        return weight;
    }
    
    public int getCoverage() {
        return uavList.size();
    }
    
    public double getSIR(int uavID) {
        return 0.0;
    }
    
}
