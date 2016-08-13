package thesis;

import java.util.LinkedList;

public class Terminal {

    private int x;
    private int y;
    private int weight;
    private UAV[] uav;
    
    public Terminal(int x, int y, int weight, UAV[] uav) {
        this.x = x;
        this.y = y;
        this.weight = weight;
        this.uav = uav;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public double getSIR(int uavID, int x, int y, int z) {
        double interference = collect_itf(uavID);
        
        
        
        return 0.0;
    }      
    
    private double collect_itf(int uavID) {
        double itf = 0.0;
        for (int i = 0; i < uav.length; i++) {
            if (i == uavID) continue;
            
            if (uav[i].getCachedSIR() == UAV.SIR_NOT_DEFINED) {
                
            } else {
                itf += uav[i].getCachedSIR();
            }
        }
        
        return itf;
    }
}
