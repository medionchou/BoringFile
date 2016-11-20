package thesis;

import java.util.Arrays;

import edu.princeton.cs.algs4.StdStats;

public class Terminal {
    
    public static boolean DB_THRESHOLD = false;
    private final static double DB_BOUNDARY = -100.0;
    private final static int NOT_INITIALIZED = -1;

    private double x;
    private double y;
    private int weight;
    private int avail_uav;
    private UAV[] uav;
    private double[] net_power; //cache uav net power value for speeding up calculation.
    
    public Terminal(double x, double y, int weight) {
        this.x = x;
        this.y = y;
        this.weight = weight;
        avail_uav = 1; // avoid divide by zero
    }
    
    public void setUAV(UAV[] uav) {
        this.uav = uav;
        net_power = new double[uav.length];
        Arrays.fill(net_power, NOT_INITIALIZED);
    }
    
    public int getWeight() {
        return weight;
    }
    
    public int getCoveringUAV() {
        return avail_uav;
    }
    
    public void setCoveringUAV(int num) {
        avail_uav += num;
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
        net_power[uavID] = getNetPower(uav[uavID], mx, my, mz); 
       
        if (indexOfLargestPower() == uavID) {
            double tmp = net_power[uavID];
            net_power[uavID] = -1.0; // reset power of uavID because we don't know if UAV really takes this move.
            
            if (DB_THRESHOLD && tmp == 0.0) return 0.0d;
            
            if (interference == 0) return 15.0;
            else return tmp / interference;
        }
        else {
            net_power[uavID] = -1.0; // reset power of uavID because we don't know if UAV really takes this move.
            return 0.0d;
        }
    }
    
    public double getSIR(int uavID) {
        double interference = collectITF(uavID);
        net_power[uavID] = getNetPower(uav[uavID], 0, 0, 0); // what the fuck
        
        if (indexOfLargestPower() == uavID) {
            if (DB_THRESHOLD && net_power[uavID] == 0.0) return 0.0d;  
            if (interference == 0) return 15.0;
            else return net_power[uavID] / interference;
        }
        else {
            return 0.0d;
        }
    }
    
    public double distance(UAV uav, Point pt) {
        double rx = Math.pow(this.x - (uav.x() + pt.x), 2);
        double ry = Math.pow(this.y - (uav.y() + pt.y), 2);
        double rz = Math.pow(0 - (uav.z() + pt.z), 2);
        
        return Math.sqrt(rx + ry + rz);     
    }
    
    public boolean withinRange(UAV uav, double mx, double my, double mz) {
        
        double result = getNetPower(uav, mx, my, mz);
        
        
        if (result == 0.0) return false;
        else return true;
    }
    
    public boolean withinRange(UAV uav) {
        
        double result = getNetPower(uav, 0, 0, 0);

        
        if (result == 0.0) return false;
        else return true;
    }
    
    public String toString() {
        return "x: " + x + " y: " + y;        
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
     * @return summation of interferences.
     */
    private double collectITF(int uavID) {
        double itf = 0.0;
        
        for (int i = 0; i < uav.length; i++) {
            if (i == uavID) continue;
            if (net_power[i] == -1) net_power[i] = getNetPower(uav[i], 0, 0, 0);
            
            itf += net_power[i];
        }
        return itf;
    }

    /**
     * @param uav - the target used to calculate power with respect to this terminal.
     * @param mx - the intended moving value in x direction 
     * @param my - the intended moving value in y direction
     * @param mz - the intended moving value in z direction
     * @return net power which is the trasmitted power substracting path loss in MiliWatt.
     */
    private double getNetPower(UAV uav, double mx, double my, double mz) {
        double uavX = uav.x() + mx;
        double uavY = uav.y() + my;
        double uavZ = (uav.z() + mz) < 0 ? 0 : uav.z() + mz;
        double degree = angleToUAV(uavX, uavY, uavZ);
        
        if (degree < 0) {
            System.out.println(uavX + " " + uavY + " " + uavZ + " " + x + " " + y);
            System.out.println("Unexpected negative degree " + uav.getID() + " :" +  degree);
        }
        
        double distance2D = Math.hypot(x - uavX, y - uavY);
        double distance = Math.hypot(distance2D, uavZ);
        /**
         * pathloss = Pt / Pr = log_10(Pt) - log_10(Pr)
         * therefore, log_10(Pr)(dBm) = log_10(Pt)(dBm) - pathloss
         */
        double power = Environment.TRANSMIT_POWER - pathLoss(degree, distance);
        
        
        if (DB_THRESHOLD && power < DB_BOUNDARY) return 0.0; 
        return dBmToMiliWatt(power);
    }
    
    private double dBmToMiliWatt(double dBm) {
        return Math.pow(10, dBm / 10);
    }
    
    private double pathLoss(double degree, double distance) {
        if (degree > 90 || degree < 0) throw new IllegalArgumentException("degree outside of desired range 0 - 90");
        
        if (degree >= 0 && degree < 10) 
            return 98.4 + 20 * Math.log10(distance) + ((2.55 + degree) / (0.0594 + 0.0406 * degree));
        else if (degree >= 10 && degree <= 90)
            return 98.4 + 20 * Math.log10(distance) + ((-94.2 + degree) / (-3.44 + 0.0318 * degree));
        else
            throw new IllegalArgumentException("Invalid argument");
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
        
        Terminal t = new Terminal(0, 0, 0);
        
        System.out.println(t.angleToUAV(1, 0, Math.sqrt(3.0)));
    }
}
