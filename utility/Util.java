package utility;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Random;

import thesis.Environment;
import thesis.Point;

public class Util {
	
	private Util() {}
	
	public static Point randomPoint() {
		Strategy st = Strategy.randomStrategy();
		
		switch(st) {
		case FORWARD:
		    return new Point(0, Environment.STEP, 0);
		case BACKWARD:
		    return new Point(0, -Environment.STEP, 0);
		case RIGHT:
		    return new Point(Environment.STEP, 0, 0);
		case LEFT:
		    return new Point(-Environment.STEP, 0, 0);
		default:
			return new Point(0, 0, 0);
		}
	}
	
	public static Point getPointByStrategy(Strategy st) {
		switch(st) {
		case FORWARD:
            return new Point(0, Environment.STEP, 0);
        case BACKWARD:
            return new Point(0, -Environment.STEP, 0);
        case RIGHT:
            return new Point(Environment.STEP, 0, 0);
        case LEFT:
            return new Point(-Environment.STEP, 0, 0);
        default:
            return new Point(0, 0, 0);
		}
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

	/**
	 * Generate file for regular terminals distribution where each terminal has a distance <i>interval</i> 
	 * from its neighboring terminals.
	 * 
	 * @param interval - distance between terminals
	 * @param grid_size - area size
	 * @param filename - output filename
	 * 
	 */
    public static void fixedDistanceTerms(int interval, int grid_size, String filename) {
	    if (interval <= 0) return;
	    
	    int center = grid_size / 2;	    
	    int[][] g = new int[grid_size][grid_size];
	    int term = 0;
	    
	    for (int i = 0; i < grid_size; i++) {
	        if (i % interval != 0) continue;
	        for (int j = 0; j < grid_size; j++) {
	            if (j % interval != 0) continue;
	        
	            if (j >= (center - 8) && j <= (center + 8) && i >= (center - 8) && i <= (center + 8)) continue;
	            g[i][j] = 1;
	            term ++;
	        }
	    }
        try {
            File f = new File(filename);
            PrintWriter pw = new PrintWriter(f);
            pw.println(term + " " + grid_size);
            for (int i = 0; i < grid_size; i++) {
                for (int j = 0; j < grid_size; j++) {
                    if (g[i][j] > 0)
                        pw.println(i + " " + j + " " + 1);
                }
            }
            pw.close();
        } catch (Exception e) {

        }
	}

	public static void main(String[] args) {
	}
}
