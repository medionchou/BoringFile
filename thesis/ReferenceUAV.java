package thesis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import edu.princeton.cs.algs4.StdRandom;

/**
 * 
 * @author Medion-PC.
 * 
 * The UAV that randomly moves one unit choosing from 3 dimensions.  
 */
public class ReferenceUAV extends UAV {

	private double last_profit;
	private Point last_move;
	private double step;
	private int origin_grid_size;
	private Point move;

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

	public ReferenceUAV(double x, double y, double z, boolean isOpen) {
		super(x, y, z, isOpen);
		last_profit = 0;
		last_move = new Point(0.0, 0.0, 0.0);
		step = 0.0;
		origin_grid_size = -1;
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
				if (termNum == 0)
					continue;

				Terminal t = grid[i][j].getTerminal();
				Point p = last_move;
				sir = t.peekSIR(getID(), p.x, p.y, p.z);

				double tmp = UAV.ld(sir);
				if (tmp > 0) {
					si_no += tmp * termNum;
					si_deno += termNum;
				} 
			}
		}
		
//		System.out.println("******* Who I am *********: " + getID());

		if (si_deno > 0) {
			double si = si_no / si_deno;
			if (last_profit >= si) {
				last_move = randomPoint(grid_size);
//				System.out.println("No improvement !!");
			}
//			System.out.println("last_profit: " + last_profit);
			last_profit = si;
//			System.out.println("profit: " + si);
//			System.out.println("move: " + last_move);
		} else {
			last_move = randomPoint(grid_size);
//			System.out.println("no terms random_move: " + last_move);
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
	

	@Override
	public double[] getSpectrumAndTerms(Grid[][] grid) {
		int grid_size;
		int si_deno = 0;
		double si_no = 0.0d;
		double[] res = new double[2];
		 
        grid_size = origin_grid_size == -1 ? grid.length : origin_grid_size;

		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				double sir = 0.0d;
				int termNum = grid[i][j].getTermNum();
				if (termNum == 0)
					continue;

				Terminal t = grid[i][j].getTerminal();
				sir = t.getSIR(getID());

				double tmp = UAV.ld(sir);
				if (tmp > 0) {
					si_no += tmp * termNum;
					si_deno += termNum;
				}
			}
		}
//		System.out.println("Terms: " + si_deno);
		
		res[0] = si_no;
		res[1] = si_deno;

		return res;
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
    public double steps() {
        return step;
    }

    @Override
    public void setOriginGridSize(int ogs) {
        origin_grid_size = ogs;
    }

    @Override
    public boolean isStable() {
        return false;
    }

}
