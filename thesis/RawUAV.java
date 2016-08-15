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
        last_profit = Double.MIN_VALUE;
        last_move = Strategy.randomStrategy();
    }

    @Override
    public void run(Grid[][] grid) {
        int grid_size = grid.length;
        int si_deno = 0;
        double si_no = 0.0d;
        
        for (int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                int termNum = grid[i][j].getTermNum();
                double sir = 0.0d;
                
                if (termNum == 0) continue;
                
                Terminal t = grid[i][j].getTerminal();
                Point p = strategyToPoint(last_move);
                sir = t.getSIR(getID(), p.x + x(), p.y + y(), p.z + z());
                
                if (sir != 0) {
                    si_no += UAV.ld(sir) * termNum;
                    si_deno += termNum;
                }
            }
        }
        if (si_deno != 0) {
            double si = si_no / si_deno;
            
            if (last_profit < si) {
                if (si > 4.0d) last_profit = 4;
                else last_profit = si;
            } else {
                last_move = randomStrategy(grid_size);
                last_profit = si;
            }
        } else {
            last_move = Strategy.randomStrategy();
        }
        
        moveByStrategy(last_move, grid_size);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("x: " + x() + " y:" + y() + " z:" + z() + "\n");
        sb.append("Profit: " + last_profit);
        
        return sb.toString();
    }
    
    
    private double utility() {
        return 0;
    }
    
    /**
     * Only return available Strategy which doesn't cause fly out of bound.
     * @param grid_size
     * @return one strategy from Enum Strategy
     */
    private Strategy randomStrategy(int grid_size) {
        ArrayList<Strategy> tmp = new ArrayList<>();
        Random r = new Random();
        
        for (Strategy stgy : Strategy.values()) {
            Point pt = strategyToPoint(stgy);
            
            pt.add(x(), y(), z());
            if (checkBoundary(pt, grid_size))  tmp.add(stgy);
        }
        
        return tmp.get(r.nextInt(tmp.size()));
    }
    
    
    public static void main(String[] args) {
    }

}
