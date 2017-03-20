package uav;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.princeton.cs.algs4.StdRandom;
import thesis.Environment;
import thesis.Grid;
import thesis.Point;
import thesis.Terminal;
import utility.SequenceGenerator;
import utility.Util;

/**
 * 
 * @author Medion-PC.
 * 
 * The UAV that randomly moves one unit choosing from 3 dimensions.  
 */
public class BioInspired extends UAV {

	private double last_profit;
	private Point last_move;
	private double step;
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

	public BioInspired(double x, double y, double z, boolean isOpen) {
		super(x, y, z, isOpen);
		last_profit = 0;
		last_move = new Point(0.0, 0.0, 0.0);
		step = 0.0;
		trails = new HashSet<>();
	}

	@Override
	public void run(Grid[][] grid) {
		int grid_size = grid.length;
		int served_term = 0;
		double spectral_efficiency = 0.0d;
		trails.add(new Point(this.x(), this.y(), this.z()));

		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				double sir = 0.0d;
				int termNum = grid[i][j].getTermNum();
				if (termNum == 0)
					continue;

				Terminal t = grid[i][j].getTerminal();
				Point p = last_move;
				sir = t.peekSIR(getID(), p.x, p.y, p.z);

				double tmp = ld(sir);
				if (tmp > 0) {
					spectral_efficiency += tmp * termNum;
					served_term += termNum;
				} 
			}
		}
		

		if (served_term > 0) {
			double se = spectral_efficiency / served_term;
			if (last_profit >= se) {
				last_move = randomPoint(grid_size);
			}
			
			last_profit = se;
		} else {
			last_move = randomPoint(grid_size);
		}
		
		
		if (!last_move.isZero()) step += Environment.STEP;
		
		if (last_move.z != 0) moveByPoint(last_move, Environment.MAX_HEIGHT);
		else moveByPoint(last_move, grid_size);
	}
	
	private Point randomPoint(int grid_size) {
		Point pt = new Point(0, 0, 0);
		while (true) {
			pt.set(Util.randomPoint());
			if (checkBoundary(pt, grid_size, Environment.MAX_HEIGHT)) return pt;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("id:" + getID() + " x: " + String.format("%.2f", x()) + " y:" + String.format("%.2f", y()) + " z:"
				+ String.format("%.2f", z()));

		return sb.toString();
	}

	public static void main(String[] args) {

	}

    @Override
    public double travel_distance() {
        return step;
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public Iterable<Point> movements() {
        return trails;
    }

}
