package thesis;

import java.util.HashMap;
import java.util.Random;

public class OptimalGameModelUAV extends UAV {

	private double last_profit;

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

	public OptimalGameModelUAV(double x, double y, double z, boolean isOpen) {
		super(x, y, z, isOpen);
		last_profit = 0;
	}

	@Override
	public void run(Grid[][] grid) {
		int grid_size = grid.length;
		Point pt = new Point(0, 0, 0);
		Strategy bestStrategy = Strategy.BACKWARD;
		double bestPayoff = Double.MIN_VALUE;
		
		for (Strategy st : Strategy.values()) {
			pt.set(Util.getPointByStrategy(st));
				
			if (checkBoundary(pt, grid_size, Environment.MAX_HEIGHT)) {
				BestPayoffPair bpp = payoff(st, pt, grid, grid_size);
				
				
				if (Double.compare(bestPayoff, bpp.bestPayoff) < 0) {
					bestPayoff = bpp.bestPayoff;
					bestStrategy = bpp.bestStrategy;
				}
			}
		}
		
		
		if (Double.compare(bestPayoff, Double.MIN_VALUE) != 0) pt = Util.getPointByStrategy(bestStrategy);
		else pt = Util.randomPoint();
		
		
		if (pt.z != 0) moveByPoint(pt, Environment.MAX_HEIGHT);
		else moveByPoint(pt, grid_size);
	}
	
	private BestPayoffPair payoff(Strategy st, Point pt, Grid[][] grid, int grid_size) {
		int si_deno = 0;
		double si_no = 0.0d;
		
		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				int termNum = grid[i][j].getTermNum();
				if (termNum == 0)
					continue;
				double sir = 0.0d;

				Terminal t = grid[i][j].getTerminal();
				sir = t.peekSIR(getID(), pt.x, pt.y, pt.z);
				
				double tmp = UAV.ld(sir);
				if (tmp > 0) {
					si_no += tmp * termNum;
					si_deno += termNum;
				} 
			}
		}
		

		if (si_deno > 0) return new BestPayoffPair(st, (si_no / si_deno) - cost(st));
		else return new BestPayoffPair(st, 0 - cost(st));
	}

	private double cost(Strategy st) {
		switch (st) {
//		case UP:
//			return 0.0d;
//		case DOWN:
//			return 0.0d;
		case FORWARD:
			return 0.0d;
		case BACKWARD:
			return 0.0d;
		case RIGHT:
			return 0.0d;
		case LEFT:
			return 0.0d;
		default:
			return 0.0d;
		}

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
		System.out.println("Terms: " + si_deno);
		if (si_deno == 0)
			si_deno = 1;

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
	
	private static class BestPayoffPair {
		Strategy bestStrategy;
		Double bestPayoff;
		BestPayoffPair(Strategy st, Double payoff) {
			bestStrategy = st;
			bestPayoff = payoff;
		}
	}
	
	public static void main(String[] args) {
		
		System.out.println(Double.compare(Double.MIN_VALUE, Double.MIN_VALUE));
	}

    @Override
    public double steps() {
        throw new UnsupportedOperationException("Steps Operation is not supported for OriginalUAV");
    }

    @Override
    public void setOriginGridSize(int ogs) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isStable() {
        // TODO Auto-generated method stub
        return false;
    }

}
