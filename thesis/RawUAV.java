package thesis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * 
 * @author Medion-PC
 *  M2016 prototype UAV.
 */
public class RawUAV extends UAV {

    private double last_profit;
    private Strategy last_move;
    private static final double BIAS = 1; 
    
    public static final SequenceGenerator SEQUENCE_GENERATOR = new SequenceGenerator() {

        @Override
        public int[] sequence(int uavNum) {
            int[] a = new int[uavNum];
            int n = a.length;
            
            for (int i = 0; i < uavNum; i++) a[i] = i;
            Random random = new Random();
            for (int i = 0; i < n; i++) {
                int r = i + random.nextInt(n-i);     // between i and n-1
                int temp = a[i];
                a[i] = a[r];
                a[r] = temp;
            }
            return a;
        }
        
    };

    public RawUAV(double x, double y, double z, boolean isOpen) {
        super(x, y, z, isOpen);
        last_profit = 0;
        last_move = Strategy.randomStrategy();
    }

    @Override
    public void run(Grid[][] grid) {
        int grid_size = grid.length;
        int si_deno = 0;
        double si_no = 0.0d;
        
        for (int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                double sir = 0.0d;
                int termNum = grid[i][j].getTermNum();
                if (termNum == 0) continue;
                
                Terminal t = grid[i][j].getTerminal();
                Point p = strategyToPoint(last_move, BIAS);
                sir = t.peekSIR(getID(), p.x, p.y, p.z);
                
                if (sir > 0) {
                    si_no += UAV.ld(sir) * termNum;
                    si_deno += termNum;
                }
            }
        }
        
        if (si_deno != 0) {
            double si = si_no / si_deno;
            if (last_profit >= si) last_move = randomStrategy(grid_size);
            
            last_profit = si;
        } 
        else last_move = Strategy.randomStrategy();
        
        moveByStrategy(last_move, grid_size, BIAS);
    }
    
    @Override
    public double[] getSpectrumAndTerms(Grid[][] grid) {
        int grid_size = grid.length;
        int si_deno = 0;
        double si_no = 0.0d;
        double[] res = new double[2];
        
        for (int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                double sir = 0.0d;
                int termNum = grid[i][j].getTermNum();
                if (termNum == 0) continue;
                
                Terminal t = grid[i][j].getTerminal();
                sir = t.getSIR(getID());
                
                if (sir > 0) {
                    si_no += UAV.ld(sir) * termNum;
                    si_deno += termNum;
                }
            }
        }
        System.out.println("Terms: " + si_deno);
        if (si_deno == 0) si_deno = 1;
        
        res[0] = si_no;
        res[1] = si_deno;
        
        return res;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("id:" + getID() + " x: " + String.format("%.2f", x())  + " y:" + String.format("%.2f", y()) + " z:" + String.format("%.2f", z()));

        return sb.toString();
    }
    
    /**
     * Only return available Strategies which don't cause moving out of bound.
     * @param grid_size
     * @return one strategy from Enum Strategy
     */
    private Strategy randomStrategy(int grid_size) {
        ArrayList<Strategy> tmp = new ArrayList<>();
        Random r = new Random();
        
        for (Strategy stgy : Strategy.values()) {
            Point pt = strategyToPoint(stgy, BIAS);
            
            pt.add(x(), y(), z());
            if (checkBoundary(pt, grid_size))  tmp.add(stgy);
        }
        
        return tmp.get(r.nextInt(tmp.size()));
    }
    
    public static void main(String[] args) {
        System.out.println(Double.MIN_VALUE);
    }
}

