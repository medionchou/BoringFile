package thesis;

import java.util.Iterator;
import java.util.Random;

import thesis.Strategy.Point;

/**
 * 
 * @author Medion-PC
 *  M2016 prototype UAV.
 */
public class RawUAV extends UAV {

    private double last_profit;
    private Strategy last_move;

    public RawUAV(double x, double y, double z, boolean isOpen) {
        super(x, y, z, isOpen);
        last_profit = 0.0;
        last_move = Strategy.randomStrategy();
    }

    @Override
    public void run(Grid[][] grid) {
        int len = grid.length;
        int si_deno = 0;
        double si_no = 0.0d;
        
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                int termNum = grid[i][j].getTermNum();
                double sir = 0.0d;
                
                if (termNum == 0) continue;
                
                Terminal t = grid[i][j].getTerminal();
                Point p = strategyToPoint(last_move);
                sir = t.getSIR(getID(), p.x, p.y, p.z);
                
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
            } else last_move = Strategy.randomStrategy();
        }       
        moveByStrategy(last_move, len);
    }
    
    
    
    private double utility() {
        return 0;
    }

    @Override
    public int[] sequence(int uavNum) {
        int[] seq = new int[uavNum];
        for (int i = 0; i < uavNum; i++) seq[i] = i;
        
        shuffle(seq);
        
        return seq;
    }
    
    private void shuffle(int[] a) {
        if (a == null) throw new NullPointerException("argument array is null");
        int n = a.length;
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            int r = i + random.nextInt(n-i);     // between i and n-1
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    public static void main(String[] args) {
        RawUAV u = new RawUAV(0, 1, 2, false);
        
        int[] a = u.sequence(20);
        
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ");
        }
    }

}
