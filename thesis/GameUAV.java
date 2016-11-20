package thesis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class GameUAV extends UAV {
    
    // 14 is the theoretical maximal distance which signal is able to travel based on 46dBm.
    private static final int BOUNDARY = 14 + (int)Math.ceil(Environment.STEP);
    
    private UAV[] uav;
    private HashMap<Terminal, Integer> potentialTerms;
    private Point rp;
    private Point vector;
    
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

    public GameUAV(double x, double y, double z, boolean isOpen) {
        super(x, y, z, isOpen);
        potentialTerms = new HashMap<>();
    }

    @Override
    public double[] getSpectrumAndTerms(Grid[][] grid) {

        int grid_size = grid.length;
        collectTerms(grid, grid_size);
        double[] res = new double[2];
        int si_deno = 0;
        double si_no = 0.0d;
        
        for (Terminal t : potentialTerms.keySet()) {
            
            if (t.withinRange(this)) {
                double sir;

                sir = t.getSIR(getID());
                
                if (sir != 0.0d) {
                    double tmp = UAV.ld(sir);

                    if (tmp > 0) {
                        si_no += tmp * potentialTerms.get(t);
                        si_deno += potentialTerms.get(t);
                    }
                }
            }
        }
        System.out.println("Terms: " + si_deno);
        if (si_deno == 0)
            si_deno = 1;
        
        res[0] = si_no;
        res[1] = si_deno;
        
        return res;
    }

    @Override
    public void run(Grid[][] grid) {
        int grid_size = grid.length;
        collectTerms(grid, grid_size);
        
        if (potentialTerms.size() == 0) { // random walk
            moveByPoint(randomPoint(grid_size), grid_size);                        
        } else {
            Point p = bestStrategy();
            if (p == null) {
                moveByPoint(randomPoint(grid_size), grid_size);
                return;
            }
            else moveByPoint(p, grid_size);

            for (Iterator<Entry<Terminal, Integer>> it =  potentialTerms.entrySet().iterator(); it.hasNext(); ) {
                Entry<Terminal, Integer> map = it.next();
                if (!map.getKey().withinRange(this, 0, 0, 0)) it.remove();
            }
        }
    }
    
    private Point bestStrategy() {
        
        Point move = new Point(0, 0, 0);
        HashSet<Terminal> owned = new HashSet<>();
        HashSet<Terminal> nonowned = new HashSet<>();
        double payoff = 0.0d;
        boolean hasTerm = false;
        
        for (Strategy st : Strategy.values()) {
            Point tmp = Util.getPointByStrategy(st);
            double each_payoff;
            
            for (Terminal t : potentialTerms.keySet()) {
                if (t.withinRange(this)) {
                    double sir = t.peekSIR(getID(), tmp.x, tmp.y, tmp.z);
                    
                    if (sir != 0.0d) owned.add(t); 
                    else nonowned.add(t);
                    hasTerm = true;
                }
            }                    

            if (getID() == 1) {
                System.out.println("\nOwned: " + owned.size());
                System.out.println("Nonowned: " + nonowned.size());
                System.out.println("Potential: " + potentialTerms.size());
                System.out.println("Position: " + toString());
                System.out.println("PM: " + tmp);
            }
            each_payoff = owned.size() * Math.pow(Math.E, payoff(owned, tmp, true)) +
                    0.1 * nonowned.size() * Math.pow(Math.E, payoff(nonowned, tmp, false));
            if (getID() == 1)  System.out.println("\nStrategy: " + st + " Payoff: " + each_payoff);
           
            if (each_payoff > payoff) {
                payoff = each_payoff;
                move = tmp;
            }
            owned.clear();
            nonowned.clear();
        }
        if (getID() == 1) System.out.println("\nSelected Move: " + move );

        if (hasTerm) return move;
        else return null;
    }
    
    private double payoff(HashSet<Terminal> termSet, Point pt, boolean isOwned) {
        double distance = 0.0;
        
        for (Terminal t : termSet) {
            distance += t.distance(this, pt);
        }
        
        if (getID() == 1)  System.out.print("distance: " + distance + " ");
        if (distance == 0) return 0.0;
        return termSet.size() / distance;
    }
    
    private Point randomPoint(int grid_size) {
        if (rp == null || rp.isClose(x(), y(), z())) {
            int x = getCoordinate(grid_size, (int)x());
            int y = getCoordinate(grid_size, (int)y());
            
            rp = new Point(x, y, 0);
            double distance = rp.distance(x(), y(), 0);
            vector = new Point((rp.x - x()) / distance, (rp.y -y()) / distance, 0);
        }
        return vector;
    }
    
    private int getCoordinate(int grid_size, int axis) {
        int tmp;
        Random r = new Random();
        do {
            tmp = r.nextInt(grid_size);

        } while (tmp >= (axis - BOUNDARY) && tmp <= (axis + BOUNDARY));
        
        return tmp;
    }
   
    
    private void collectTerms(Grid[][] grid, int grid_size) {
        int x = (int) x();
        int y = (int) y();
        
        for (int i = x - BOUNDARY; i <= x + BOUNDARY; i++) {
            if (i < 0 || i >= grid_size) continue;
            for (int j = y - BOUNDARY; j <= y + BOUNDARY; j++) {
                if (j < 0 || j >= grid_size) continue;
                int terms = grid[i][j].getTermNum();
                if (terms == 0) continue;
                
                Terminal t = grid[i][j].getTerminal();
                
                if (!potentialTerms.containsKey(t)) potentialTerms.put(t, terms);
            }
        }
    }

    @Override
    public void setPartnetUAV(UAV[] uav) {
        this.uav = uav;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("id:" + getID() + " x: " + String.format("%.2f", x()) + " y:" + String.format("%.2f", y()) + " z:"
                + String.format("%.2f", z()));

        return sb.toString();
    }
    
    
    public static void main(String[] args) {
        GameUAV g1 = new GameUAV(38, 30, 3, true);
       GameUAV g = new GameUAV(30, 30, 3, true);
       Grid[][] gd = new Grid[60][60];
//       Terminal.DB_THRESHOLD = true;
       
       for (int i = 0; i < gd.length; i++) {
           for (int j = 0; j < gd.length; j++) {
               gd[i][j] = new Grid();
           }
       }
       UAV[] uav = new UAV[2];
       uav[0] = g;
       uav[1] = g1;
       Terminal t1 = new Terminal(26, 30, 1);
       Terminal t2 = new Terminal(34, 30, 1);
       Terminal t3 = new Terminal(26, 36, 1);
       Terminal t4 = new Terminal(34, 33, 1);
       Terminal t5 = new Terminal(26, 24, 1);
       Terminal t6 = new Terminal(34, 34, 1);
       t1.setUAV(uav);
       t2.setUAV(uav);
       t3.setUAV(uav);
       t4.setUAV(uav);
       t5.setUAV(uav);
       t6.setUAV(uav);
       gd[26][30].addTerminal(t1);
       gd[34][30].addTerminal(t2);
       gd[26][36].addTerminal(t3);
       gd[34][33].addTerminal(t4);
       gd[26][24].addTerminal(t5);
       gd[34][34].addTerminal(t6);
               
       
       g.run(gd);
       System.out.println();
       g1.run(gd);
       
       System.out.println(g.potentialTerms);
    }
}
