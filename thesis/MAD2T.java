package thesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import edu.princeton.cs.algs4.StdRandom;

public class MAD2T extends UAV {
    
    public static double COST_COEF = 0.1;
    public static double THRESHOLD_IMPROVEMENT = 1.0;
    // 14 is the theoretical maximal distance which signal is able to travel based on transmission power 46dBm.
    private static final int BOUNDARY = 14 + (int)Math.ceil(Environment.STEP);
    
    private HashMap<Terminal, Integer> potentialTerms;
    private Point rp;
    private Point vector;
    private double step;
    private Point move;
    private HashSet<Point> trails;
    
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

    public MAD2T(double x, double y, double z, boolean isOpen) {
        super(x, y, z, isOpen);
        potentialTerms = new HashMap<>();
        trails = new HashSet<>();
        step = 0.0;
    }

    @Override
    public double[] getSpectrumAndTerms(Grid[][] grid) {

        int grid_size = grid.length;
        double[] res = new double[4];
        int si_deno = 0;
        int served_terminal = 0;
        double si_no = 0.0d;
        double st_avd = 0.d;
        
        
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
                    st_avd += t.distance(this) * termNum;
                }
                double tmp = UAV.ld(sir);
                if (tmp > 0) {
                    si_no += tmp * termNum;
                    si_deno += termNum;
                }
            }
        }

        res[0] = si_no;
        res[1] = si_deno;
        res[2] = served_terminal;
        res[3] = st_avd;
        
        return res;
    }

    @Override
    public void run(Grid[][] grid) {
        int grid_size = grid.length;
        
        trails.add(new Point(this.x(), this.y(), this.z()));      

        collectTerms(grid, grid.length);
        
        if (potentialTerms.size() == 0) { // random walk
            moveByPoint(randomPoint(grid_size), grid_size);
            step += Environment.STEP;
        } else {
            Point p = bestStrategy();
            if (p == null) {
                moveByPoint(randomPoint(grid_size), grid_size);
                step += Environment.STEP;
            }
            else {
                moveByPoint(p, grid_size);         
                if (!p.isZero()) step += Environment.STEP;
                move = p;
            }
            for (Iterator<Entry<Terminal, Integer>> it =  potentialTerms.entrySet().iterator(); it.hasNext(); ) {
                Entry<Terminal, Integer> map = it.next();
                Terminal tmp = map.getKey();
                if (!tmp.withinRange(this)) it.remove();
            }
        }
    }
    
    private Point bestStrategy() {
        Point move = new Point(0, 0, 0);
        HashSet<Terminal> owned = new HashSet<>();
        HashSet<Terminal> nonowned = new HashSet<>();
        List<Point> bestLoc = new ArrayList<>();
        double payoff = 0.0d;
        boolean hasTerm = false;
//        System.out.println("\n *********** Who I am ********** : " + getID());
//        System.out.println("\n *********** Where I am ********** : " + toString());
        for (Strategy st : Strategy.values()) {
            Point tmp = Util.getPointByStrategy(st);
            double each_payoff;
            
//            for (Terminal t : potentialTerms.keySet()) {
//                if (t.withinRange(this, tmp.x, tmp.y, tmp.z)) {
//                    boolean isClosest = t.isClosest(this);//
//                    if (isClosest) {
//                      owned.add(t);
//                      hasTerm = true;
//                    }
//                    else nonowned.add(t);
//                }
//            }  
            for (Terminal t : potentialTerms.keySet()) {
                if (t.withinRange(this)) {
                    double sir = t.peekSIR(getID(), 0, 0, 0);
                    
                    if (sir != 0.0d) {
                        owned.add(t);
                        hasTerm = true;
                    }
                    else nonowned.add(t);
                  
                }
            }    


//            System.out.println("\nOwned: " + owned.size() + " " + owned);
//            System.out.println("Nonowned: " + nonowned.size());
//            System.out.println("Potential: " + potentialTerms.size());
//            System.out.println("Position: " + toString());
//            System.out.println("PM: " + tmp);
            
//            each_payoff = payoff(owned, tmp);
//            each_payoff = payoff(nonowned, tmp, true) * nonowned.size() + payoff(owned, tmp, true) * owned.size() * 10;
            each_payoff = Math.pow(Math.E, payoff(owned, tmp, true)) * owned.size();// - (cost(st) / owned.size()) * COST_COEF;
            /* +
              0.1 * nonowned.size() * Math.pow(Math.E, payoff(nonowned, tmp, false));*/
//            System.out.println("\nStrategy: " + st + " Payoff: " + each_payoff);
           
            if (each_payoff > payoff) {
                payoff = each_payoff;
                move = tmp;
                bestLoc.clear();
                bestLoc.add(tmp);
            } else if (each_payoff == payoff) {
                bestLoc.add(tmp);
            }
            owned.clear();
            nonowned.clear();
        }
        
        
        if (hasTerm) {
            if (bestLoc.size() == 1) {
//                System.out.println("move: " + move);
                return move;
            }
            else {
                int index = StdRandom.uniform(bestLoc.size());
//                System.out.println("index: " + index + " size: " + bestLoc.size());
//                System.out.println("move: " + bestLoc.get(index));
                return bestLoc.get(index);
            }
        }
        else return null;
    }
    
    private double cost(Strategy stg) {
        switch (stg) {
        case STILL:
            return 0.0;
        default:
            return Environment.STEP;
        }  
    }
    
    private double payoff(HashSet<Terminal> termSet, Point pt) {
        double distance = 0.0;
        double r;
        
        for (Terminal t : termSet) {
            distance += t.peekSIR(this.getID(), pt.x, pt.y, pt.z);
        }
        
        double tmp = UAV.ld(distance);
        
        return tmp/termSet.size();
    }
    
    private double payoff(HashSet<Terminal> termSet, Point pt, boolean isOwned) {
        double distance = 0.0;
        
        for (Terminal t : termSet) {
            distance += t.distance(this, pt);
        }
        
        if (distance == 0) return 0.0;
        return termSet.size() / distance;
    }
    
    private Point randomPoint(int grid_size) {
        if (rp == null || rp.isClose(x(), y(), 0)) {
            int x = getCoordinate(grid_size, (int)x());
            int y = getCoordinate(grid_size, (int)y());
            
            rp = new Point(x, y, 0);
            double distance = rp.distance(x(), y(), 0);
            
            if (Double.compare(distance, 0.0) == 0) distance = Environment.STEP;
            vector = new Point(Environment.STEP * (rp.x - x()) / distance, Environment.STEP * (rp.y - y()) / distance, 0);
            
        }
        return vector;
    }
    
    private int getCoordinate(int grid_size, int axis) {
        int tmp;
        Random r = new Random();
        do {
            tmp = r.nextInt(grid_size);
        } while (tmp >= (axis - BOUNDARY) && tmp <= (axis + BOUNDARY) && grid_size >= 50);
        
        return tmp;
    }
    
    public Point tmp() {
        return rp;
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
    
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("id:" + getID() + " x: " + String.format("%.2f", x()) + " y:" + String.format("%.2f", y()) + " z:"
                + String.format("%.2f", z()));

        return sb.toString();
    }
    
    
    public static void main(String[] args) {
        Point p = new Point(0, 0, 0);
        
        System.out.println(p.isZero());
    }

    @Override
    public double steps() {
        return step;
    }

    @Override
    public boolean isStable() {
        if (move == null) return false;
        if (move.x == 0 && move.y == 0) return true;
        return false;
    }

    @Override
    public Iterable<Point> movements() {
        return trails;
    }
    
	@Override
	public void inspect(Grid[][] grid) {
		// TODO Auto-generated method stub
        int grid_size = grid.length;
        
        for (int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                double sir = 0.0d;
                int termNum = grid[i][j].getTermNum();
                if (termNum == 0)
                    continue;

                for (Iterator<Terminal> t = grid[i][j].getTerminals(); t.hasNext();) {
                	Terminal term = t.next();
                	
                	if (term.withinRange(this)) 
                		term.setCovered();
                }
            }
        }   
	}

}
