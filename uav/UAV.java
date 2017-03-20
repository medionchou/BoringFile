package uav;

import java.util.Random;

import thesis.Grid;
import thesis.Point;
import thesis.Terminal;
import utility.SequenceGenerator;
import utility.StepCounting;

public abstract class UAV implements StepCounting {

    public static int ID = 0; //for automatically increment id
    
    private int id;
    private double x;
    private double y;
    private double z;
    private boolean isOpen;
    
    public static final SequenceGenerator SEQUENCE_GENERATOR = new SequenceGenerator() {

        @Override
        public int[] sequence(int uavNum) {
            int[] a = new int[uavNum];
            int n = a.length;

            for (int i = 0; i < uavNum; i++)
                a[i] = i;
            Random random = new Random();
            for (int i = 0; i < n; i++) {
                int r = i + random.nextInt(n - i); // between i and n-1
                int temp = a[i];
                a[i] = a[r];
                a[r] = temp;
            }
            return a;
        }

    };
    
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
        if (sir < 0.1) return 0;
        else if (sir >= 15) return 4;
        else return Math.log10(sir + 1) / Math.log10(2);
    }
    
    protected void moveByPoint(Point pt, int boundary) {
        move(pt.x, pt.y, pt.z, boundary);
    }

    private void move(double x, double y, double z, int boundary) {
        if (checkBoundary(this.x + x, boundary)) this.x += x;
        if (checkBoundary(this.y + y, boundary)) this.y += y;
        if (checkBoundary(this.z + z, boundary)) this.z += z; 
    }
    
    public boolean checkBoundary(Point pt, int grid_size, int maxHeight) {
        if ((this.x + pt.x) < 0 || (this.x + pt.x) > grid_size) return false;
        if ((this.y + pt.y) < 0 || (this.y + pt.y) > grid_size) return false;
        if ((this.z + pt.z) < 0 || (this.z + pt.z) > maxHeight) return false;
        else return true;
    }
    
    private boolean checkBoundary(double coordinate, int boundary) {
        if (coordinate < 0 || coordinate > boundary) return false;
        else return true;
    }
    
    public String toString() {
        return "x: " + x + " y:" + y + " z:" + z;
    }
    
    public double[] getSpectrumAndTerms(Grid[][] grid) {
        int grid_size = grid.length;
        int served_terminal = 0;
        double[] result = new double[3];
        double spectal_efficiency = 0.0d;
        double served_term_distance = 0.d;
        
        
        for (int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                double sir = 0.0d;
                int termNum = grid[i][j].getTermNum();
                if (termNum == 0)
                    continue;

                Terminal t = grid[i][j].getTerminal();
                sir = t.getSIR(getID());
                
                if (t.isServed(this)) {
                	t.setCovered();
                    served_terminal += termNum;
                    served_term_distance += t.distance(this) * termNum;
                }
                double tmp = ld(sir);
                if (tmp > 0) spectal_efficiency += tmp * termNum; 
            }
        }

        result[0] = spectal_efficiency;
        result[1] = served_terminal;
        result[2] = served_term_distance;
        
        return result;
    }
    
    public abstract void run(Grid[][] grid);
    
    public abstract boolean isStable();
    
    public abstract Iterable<Point> movements();
    
    public static void main(String[] args) {
        BioInspired a = new BioInspired(0.1, 0.1, 0.1, true);
        
        System.out.println(a.ld(0));
    }
}
