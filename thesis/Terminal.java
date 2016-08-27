package thesis;

import java.util.Arrays;
import java.util.LinkedList;

public class Terminal {
    
    public final static double DEFAULT_VAL = 0.0d;

    private double x;
    private double y;
    private int weight;
    private UAV[] uav;
    private double[] net_power; //cache power value for speeding up calculation.
    private int associatedUAVID; //for debug.
    
    public Terminal(double x, double y, int weight, UAV[] uav) {
        this.x = x;
        this.y = y;
        this.weight = weight;
        this.uav = uav;
        associatedUAVID = -1;
        net_power = new double[uav.length];
        Arrays.fill(net_power, -1);
    }
    
    public int getWeight() {
        return weight;
    }
    
    /**
     * 
     * @param uavID - uav identity
     * @param x - the intended move value in x direction 
     * @param y - the intended move value in y direction
     * @param z - the intended move value in z direction
     * @return not really sure if signal / interference or signal - interference (dBm)
     */
    @Deprecated
    public double getSIR(int uavID, double x, double y, double z) {
        double interference = collectITF(uavID);
        net_power[uavID] = getNetPower(uav[uavID], uav[uavID].x(), uav[uavID].y(), uav[uavID].z()); // what the fuck
        
        if (indexOfLargestPower() == uavID) {
            associatedUAVID = uavID;
            double tmp = net_power[uavID];
            net_power[uavID] = -1; // reset power of uavID because we don't know if UAV really take this move.
            return tmp / interference ;
        }
        else {
            net_power[uavID] = -1; // reset power of uavID because we don't know if UAV really take this move.
            return 0.0d;
        }
    }      
    
    public int associatedUAV() {
        return associatedUAVID;
    }
    
    private int indexOfLargestPower() {
        double largest = Double.MIN_VALUE;
        int index = net_power.length - 1;
        
        for (int i = net_power.length - 1; i >= 0; i--) {
            if (net_power[i] >= largest) {
                largest = net_power[i];
                index = i;
            }
        }

        return index;        
    }
    
    /**
     * Collect all interference except uavID.
     * @param uavID
     * @return
     */
    private double collectITF(int uavID) {
        double itf = 0.0;
        
        for (int i = 0; i < uav.length; i++) {
            if (i == uavID) continue;
            
            if (net_power[i] == -1) net_power[i] = getNetPower(uav[i], uav[i].x(), uav[i].y(), uav[i].z());
            
            itf += net_power[i];
        }
        
        return itf;
    }
    
    // power unit in miliWatt
    private double getNetPower(UAV uav, double x, double y, double z) {
        double uavX = uav.x() + x;
        double uavY = uav.y() + y;
        double uavZ = uav.z() + z;
        double degree = angleToUAV(uavX, uavY, uavZ);
        
        if (degree < 0) System.out.println("Unexpected negative degree");
        
        double distance2D = Math.hypot(x - uavX, y - uavY);
        double distance = Math.hypot(distance2D, uavZ);
        
        /**
         * pathloss = Pt / Pr = log_10(Pt) - log_10(Pr)
         * therefore, log_10(Pr)(dBm) = log_10(Pt)(dBm) - pathloss
         */
        return dBmToMiliWatt(UAV.TRANSMIT_POWER - pathLoss(degree, distance));
    }
    
    private double dBmToMiliWatt(double dBm) {
        return Math.pow(10, dBm / 10);
    }
    
    private double pathLoss(double degree, double distance) {
        if (degree >= 0 || degree < 10) 
            return 98.4 + Math.log(distance) + ((2.55 + degree) / (0.0594 + 0.0406 * degree));
        else 
            return 98.4 + Math.log(distance) + ((-94.2 + degree) / (-3.44 + 0.0318 * degree)); 
    }
    
    private double angleToUAV(double uavX, double uavY, double uavZ) {
        
        if (Double.compare(x, uavX) == 0 && Double.compare(y, uavY) == 0) {
            return 90d;
        } else {
            double distance2D = Math.hypot(x - uavX, y - uavY);
            return Math.toDegrees(Math.atan2(uavZ, distance2D)); 
        }
    }
    
    public static void main(String[] args) {
        Terminal t = new Terminal(0, 0, 0, null);
        
        System.out.println(t.dBmToMiliWatt(-0.1));
    }
}
