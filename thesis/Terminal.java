package thesis;

import java.util.Arrays;

import edu.princeton.cs.algs4.StdStats;
import uav.MAD2T;
import uav.UAV;

public class Terminal {
    
    public static boolean MDSP_THRESHOLD = false;
    private final static double NOT_INITIALIZED = -1;

    private double x;
    private double y;
    private int weight;
    private UAV[] uav;
    private double[] signal_strength; //cache received signal power from each UAV to speed up runnig time.
    private boolean isCovered;
    
    public Terminal(double x, double y, int weight) {
        this.x = x;
        this.y = y;
        this.weight = weight;
        isCovered = false;
    }
    
    public void setUAV(UAV[] uav) {
        this.uav = uav;
        signal_strength = new double[uav.length];
        Arrays.fill(signal_strength, NOT_INITIALIZED);
    }
    
    public int getWeight() {
        return weight;
    }
    
    
    /**
     * 
     * @param uavID - the uav which takes movement.
     * @param mx - the intended moving value in x direction 
     * @param my - the intended moving value in y direction
     * @param mz - the intended moving value in z direction
     * @return not really sure if signal / interference or signal - interference (dBm)
     */
    public double peekSIR(int uavID, double mx, double my, double mz) {
        double interference = collectITF(uavID);
        signal_strength[uavID] = getSignalStrenth(uav[uavID], mx, my, mz);
        double tmp = signal_strength[uavID];
       
        if (indexOfLargestPower() == uavID) {
            signal_strength[uavID] = NOT_INITIALIZED; // reset power of uavID because we don't know if UAV really takes this move.
            
            if (MDSP_THRESHOLD && tmp == 0.0) return 0.0d;
            
            if (interference == 0) return 15.0;
            else return tmp / interference;
        }
        else {
            signal_strength[uavID] = NOT_INITIALIZED; // reset power of uavID because we don't know if UAV really takes this move.
            return 0.0d;
        }
    }
    
    public double getSIR(int uavID) {
        
        for (int i = 0; i < signal_strength.length; i++) signal_strength[i] = NOT_INITIALIZED; // reset all UAV cached net_power.
        
        double interference = collectITF(uavID);
        signal_strength[uavID] = getSignalStrenth(uav[uavID], 0, 0, 0); // what the fuck
        
        if (indexOfLargestPower() == uavID) {
            if (MDSP_THRESHOLD && signal_strength[uavID] == 0.0) return 0.0d;  
            
            if (interference == 0) return 15.0;
            else return signal_strength[uavID] / interference;
        }
        else {
            return 0.0d;
        }
    }
    
    public double distance(UAV theuav, Point pt) {
        double rx = Math.pow(this.x - (theuav.x() + pt.x), 2);
        double ry = Math.pow(this.y - (theuav.y() + pt.y), 2);
        double rz = Math.pow(0 - (theuav.z() + pt.z), 2);
        
        return Math.sqrt(rx + ry + rz);     
    }
    
    public double distance(UAV theuav) {
        double rx = Math.pow(this.x - theuav.x(), 2);
        double ry = Math.pow(this.y - theuav.y(), 2);
        double rz = Math.pow(0 - theuav.z(), 2);
        
        return Math.sqrt(rx + ry + rz);
    }
    
    public boolean withinRange(UAV theuav, double mx, double my, double mz) {
        
        double result = getSignalStrenth(theuav, mx, my, mz);
        
        
        if (result == 0.0) return false;
        else return true;
    }
    
    public boolean withinRange(UAV theuav) {
        
        double result = getSignalStrenth(theuav, 0, 0, 0);

        if (result == 0.0) return false;
        else return true;
    }
    
    public boolean isServed(UAV theuav) {
        boolean tmp = MDSP_THRESHOLD;
        double result;
        		
        MDSP_THRESHOLD = true;
        result = getSIR(theuav.getID());
        MDSP_THRESHOLD = tmp;
        
        if (result == 0.0) return false;
        else return true;
    }
    
    public boolean isCovered() {
    	return isCovered;
    }
    
    public void unsetCovered() {
    	isCovered = false;
    }
    
    public void setCovered() {
    	isCovered = true;
    }
    
    public String toString() {
        return "x: " + x + " y: " + y;        
    }
    
    private int indexOfLargestPower() {
        double largest = Double.MIN_VALUE;
        int index = signal_strength.length - 1;
        
        for (int i = signal_strength.length - 1; i >= 0; i--) {
            if (signal_strength[i] >= largest) {
                largest = signal_strength[i];
                index = i;
            }
        }

        return index;        
    }
    
    /**
     * Collect all interference except uavID.
     * @param uavID
     * @return summation of interferences.
     */
    private double collectITF(int uavID) {
        double itf = 0.0;
        
        for (int i = 0; i < uav.length; i++) {
            if (i == uavID) continue;
            if (signal_strength[i] == NOT_INITIALIZED) signal_strength[i] = getSignalStrenth(uav[i], 0, 0, 0);
            
            itf += signal_strength[i];
        }
        return itf;
    }

    /**
     * @param uav - the target used to calculate power with respect to this terminal.
     * @param mx - the intended moving direction in x direction 
     * @param my - the intended moving direction in y direction
     * @param mz - the intended moving direction in z direction
     * @return received signal strength in MiliWatt.
     */
    private double getSignalStrenth(UAV uav, double mx, double my, double mz) {
        double uavX = uav.x() + mx;
        double uavY = uav.y() + my;
        double uavZ = (uav.z() + mz) < 0 ? 0 : uav.z() + mz;
        double degree = angleToUAV(uavX, uavY, uavZ);
        
        if (degree < 0) throw new IllegalArgumentException("degree outside of desired range 0 - 90");
        
        double distance2D = Math.hypot(x - uavX, y - uavY);
        double distance = Math.hypot(distance2D, uavZ);
        /**
         * pathloss = Pt / Pr = log_10(Pt) - log_10(Pr)
         * therefore, log_10(Pr)(dBm) = log_10(Pt)(dBm) - pathloss
         */
        double power = Environment.TRANSMIT_POWER - pathLoss(degree, distance, uav);
        
        
        if (MDSP_THRESHOLD && (power < Environment.MDSP)) return 0.0; 
        return dBmToMiliWatt(power);
    }
    
    private double dBmToMiliWatt(double dBm) {
        return Math.pow(10, dBm / 10);
    }
    
    private double pathLoss(double degree, double distance, UAV uav) {
        if (degree > 90 || degree < 0) throw new IllegalArgumentException("degree outside of desired range 0 - 90");
        
        if (degree >= 0 && degree < 10) 
            return 98.4 + 20 * Math.log10(distance) + ((2.55 + degree) / (0.0594 + 0.0406 * degree));
        else if (degree >= 10 && degree <= 90)
            return 98.4 + 20 * Math.log10(distance) + ((-94.2 + degree) / (-3.44 + 0.0318 * degree));
        else {
            throw new IllegalArgumentException("Invalid argument");
        }
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
        
        Terminal t = new Terminal(26.0, 30.0, 0);
        UAV a = new MAD2T(26.0, 30.0, -1, false);
        
  
    }
}
