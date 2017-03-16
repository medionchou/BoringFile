package thesis;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import edu.princeton.cs.algs4.Merge;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;

public class Environment {

	public static boolean PAUSE = false;
	public static boolean DRAW = true;
	public static int UAV_NUM = 20;
	public static int TERMINAL_NUM = 350;
	public static int GRID_SIZE = 60;
	public static int ITERATION = 10000;
	public static final int MAX_WEIGHT = 5;
	public static final int MAX_HEIGHT = 5;
	public static final double TRANSMIT_POWER = 46;
	public static final double STEP = 0.1;
	public static final double Z_WEIGHT = 0; // whether UAV should consider
												// moving z-coordination.
	public static final String NORMAL_DISTRIBUTION = "UNIFORM_DISTRIBUTION";
	public static final String POISSON_DISTRIBUTION = "POISSON_DISTRIBUTION";
	public static final String UAV_RANDOM = "UAV_RANDOM";

	public final static double MDSP = -100.0;

	private Grid[][] grid;
	private UAV[] uav;
	private int[] x;
	private int[] y;
	private int[] weight;
	private int terminal_num;
	private int grid_size;
	private SequenceGenerator sg;

	/**
	 * 
	 * @param type
	 *            - which type of Terminal should be generate(uniform, poisson,
	 *            random, or read from file);
	 * @param uavDistri
	 *            - the file specify initial location for UAV.
	 * @param uavType
	 *            - which type of UAV should be created.
	 */
	public Environment(String type, String uavDistri, UAVType uavType) {
		grid_size = GRID_SIZE;
		terminal_num = TERMINAL_NUM;
		initTerms(type);
		initUAV(uavDistri, uavType);
		bindUAVtoTerm();
	}

	public void setPowerThreshold(boolean val) {
		Terminal.DB_THRESHOLD = val;
	}

	public void exportFile(String filename) {
		if (filename == null)
			throw new NullPointerException("File name can't be null.");
		if (filename.length() == 0)
			return;

		try {
			File file = new File(filename);
			PrintWriter pw = new PrintWriter(file);

			pw.write(terminal_num + " " + grid_size + "\n");

			for (int i = 0; i < x.length; i++) {
				pw.write(x[i] + " " + y[i] + " " + weight[i] + "\n");
			}

			pw.close();

		} catch (FileNotFoundException e) {
			System.out.println(e.toString());
		}
	}

	private void draw(int current) {
		StdDraw.enableDoubleBuffering();
		// StdDraw.setPenColor(Color.CYAN);

		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				if (grid[i][j].getTermNum() > 0) {
					Terminal t = grid[i][j].getTerminal();
					if (t.isCovered()) {
						StdDraw.setPenColor(Color.RED);
						StdDraw.point(i, j);
					} else {
						StdDraw.setPenColor(Color.CYAN);
						StdDraw.point(i, j);
					}
				}
			}
		}

		for (int i = 0; i < uav.length; i++) {
			if (i == current)
				StdDraw.setPenColor(Color.RED);
			else if (StdDraw.getPenColor() != Color.BLACK)
				StdDraw.setPenColor(Color.BLACK);
			StdDraw.point(uav[i].x(), uav[i].y());
		}
		StdDraw.show();
		StdDraw.clear();
	}

	private boolean keyControl() {

		if (StdDraw.hasNextKeyTyped()) {
			char d = StdDraw.nextKeyTyped();

			switch (d) {
			case 'K':
			case 'k':
				return true;
			case 'P':
			case 'p':
				while (true) {
					if (StdDraw.hasNextKeyTyped())
						break;
				}
				break;
			}
		}

		return false;
	}

	public double[] simulate() {
		if (DRAW) {
			StdDraw.setCanvasSize(720, 720);
			StdDraw.setScale(0, grid_size - 1);
			StdDraw.setPenRadius(0.02);
		}
		boolean leave = false;
		boolean stable;
		int count = 0;
		int iterations = 0;
		for (int i = 0; i < ITERATION; i++, iterations++) {
			stable = true;
			if (DRAW)
				draw(-1);
			if (PAUSE)
				while (!StdDraw.hasNextKeyTyped())
					;
			int[] seq = sg.sequence(UAV_NUM);

			leave = keyControl();
			if (leave)
				break;

			// if ((i+1) % 1000 == 0) {
			// System.out.println("Iteration: " + (i));
			// for (int j = 0; j < seq.length; j++) {
			// uav[j].inspect(grid);
			// }
			// printConsoleResult();
			// setPowerThreshold(true);
			// int num = coveredTerminals();

			// }

			for (int j = 0; j < seq.length; j++) {
				uav[seq[j]].run(grid);
				stable &= uav[seq[j]].isStable();
			}
			if (stable)
				count++;
			else
				count = 0;

			if (count == 10) {
				count = 0;
				break;
			}
		}

		return printConsoleResult(iterations);
	}

	private int coveredTerminals() {
		int count = 0;
		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				double sir = 0.0d;
				int termNum = grid[i][j].getTermNum();
				if (termNum == 0)
					continue;

				for (Iterator<Terminal> t = grid[i][j].getTerminals(); t.hasNext();) {
					Terminal term = t.next();

					if (term.isCovered())
						count++;

					term.unsetCovered();
				}
			}
		}

		return count;
	}

	private double[] printConsoleResult(int it) {

		double avs = 0d;
		double res[];
		double step = 0.0;
		double avd = 0d;
		int[] term_dist = new int[uav.length];
		int[] served_term = new int[uav.length];
		double[] st_avd = new double[uav.length];

		setPowerThreshold(false);
		for (int i = 0; i < uav.length; i++) {
			res = uav[i].getSpectrumAndTerms(grid);
			avs += res[0];
			term_dist[i] = (int) res[1];
			served_term[i] = (int) res[2];
			st_avd[i] = res[3];
			step += uav[i].steps();
		}
		draw(-1);

		for (int i = 0; i < uav.length; i++) {
			for (int j = i + 1; j < uav.length; j++) {
				avd += Point.distance(uav[i].x(), uav[i].y(), uav[i].z(), uav[j].x(), uav[j].y(), uav[j].z());
			}
		}
		double std = StdStats.stddev(term_dist);
		System.out.println("Standard deviation: " + std);
		System.out.println("Total Average Spectral Efficiency: " + avs / terminal_num);
		System.out.println("Total Terminal num: " + terminal_num);
		System.out.println("Terminal num: " + IntStream.of(term_dist).sum());
		System.out.printf("Served terminal num: %d\n", IntStream.of(served_term).sum(),
				(float) IntStream.of(served_term).sum() / terminal_num * 100);
		System.out.printf("Served terminal num (%%): %.2f%%\n",
				(float) IntStream.of(served_term).sum() / terminal_num * 100);
		System.out.printf("Served terminal num for each UAV: %.2f\n",
				(float) IntStream.of(served_term).sum() / uav.length);
		System.out.println(
				"Average UAV-Terminal distance: " + DoubleStream.of(st_avd).sum() / IntStream.of(served_term).sum());
		System.out.println("Total steps: " + step);
		System.out.println("UAV average distance: " + avd / (uav.length * (uav.length - 1) / 2));
		System.out.println("AVS: " + avs);

		int total_served_term = IntStream.of(served_term).sum();

		double[] a = { total_served_term > 0 ? avs / terminal_num : 0, // total
																		// spectral
																		// efficiency
				IntStream.of(served_term).sum(), // # of served terminals
				step, // cost
				avd / (uav.length * (uav.length - 1) / 2), // average UAV
															// distance
				DoubleStream.of(st_avd).sum() / IntStream.of(served_term).sum(), // averaged
																					// distance
																					// of
																					// served
																					// terminals
				(float) IntStream.of(served_term).sum() / uav.length, // served
																		// terminals
																		// per
																		// UAV
				total_served_term > 0 ? avs : 0, it };
		return a;
	}

	private void initUAV(String uavDistri, UAVType uavType) {
		if (uavDistri == null || uavType == null)
			throw new NullPointerException("Arguments can't be null");
		Point[] pt = getUAVLocs(uavDistri);
		uav = new UAV[pt.length];

		switch (uavType) {
		case BioInspired:
			for (int i = 0; i < uav.length; i++) {
				uav[i] = new BioInspired(pt[i].x, pt[i].y, pt[i].z, true);
			}
			sg = BioInspired.SEQUENCE_GENERATOR;
			break;
		case MAD2T:
			for (int i = 0; i < uav.length; i++) {
				uav[i] = new MAD2T(pt[i].x, pt[i].y, pt[i].z, true);
			}
			sg = MAD2T.SEQUENCE_GENERATOR;
			break;
		case PIA:
			for (int i = 0; i < uav.length; i++) {
				uav[i] = new PIA(pt[i].x, pt[i].y, pt[i].z, true);
			}
			sg = PIA.SEQUENCE_GENERATOR;
			break;
		}
		System.out.println("UAV Initialization Finished");
	}

	private void initTerms(String type) {

		switch (type) {
		case NORMAL_DISTRIBUTION:
			uniform_distribution();
			break;
		case POISSON_DISTRIBUTION:
			poisson_distrbution();
			break;
		default:
			read_file(type);
			break;
		}
		System.out.println("Terminal Initialization Finished");
	}

	public void outFinalLoc(String filename) throws FileNotFoundException {
		File f = new File(filename);
		PrintWriter pw = new PrintWriter(f);

		pw.println(terminal_num + " " + grid_size);
		for (int i = 0; i < terminal_num; i++) {
			pw.println(x[i] + " " + y[i] + " " + weight[i]);
		}
		pw.println("\" Below is uav data");

		for (int i = 0; i < uav.length; i++) {
			pw.println(Util.round(uav[i].x(), 2) + " " + Util.round(uav[i].y(), 2));
		}

		pw.close();

		System.out.println("Generate " + filename);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("GRID_SIZE: " + grid_size + "\tTERM_NUM: " + terminal_num + "\tUAV_NUM: " + UAV_NUM + "\n\n");
		sb.append("TERMINALS:\n");
		sb.append("X\t\tY\t\tWEIGHT\n");

		for (int i = 0; i < terminal_num; i++) {
			sb.append(x[i] + "\t\t" + y[i] + "\t\t" + weight[i] + "\n");
		}

		sb.append("GRID STATUS:\n\n");

		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				sb.append(grid[i][j].getTermNum() + " ");

			}
			sb.append("\n");
		}

		return sb.toString();
	}

	private Point[] getUAVLocs(String uavDistri) {
		Point[] pt = null;
		int offset = (grid_size - 60) / 2;
		// offset = 0;
		switch (uavDistri) {
		case UAV_RANDOM:
			Random r = new Random();
			pt = new Point[UAV_NUM];

			for (int i = 0; i < UAV_NUM; i++) {
				pt[i] = new Point(r.nextDouble() * grid_size, r.nextDouble() * grid_size, r.nextDouble() * MAX_HEIGHT);
			}
			break;
		default:
			try {
				Scanner sc = new Scanner(new File(uavDistri));
				pt = new Point[sc.nextInt()];
				UAV_NUM = pt.length;
				for (int i = 0; i < pt.length; i++) {
					pt[i] = new Point(sc.nextDouble() + offset, sc.nextDouble() + offset, sc.nextDouble());
				}
				sc.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			break;
		}

		return pt;
	}

	private void bindUAVtoTerm() {
		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				if (grid[i][j].getTermNum() == 0)
					continue;

				for (Iterator<Terminal> it = grid[i][j].getTerminals(); it.hasNext();) {
					Terminal t = it.next();
					t.setUAV(uav);
				}
			}
		}
	}

	private void uniform_distribution() {
		Random r = new Random();
		initGrids(grid_size, terminal_num);

		for (int i = 0; i < terminal_num; i++) {
			x[i] = r.nextInt(grid_size);
			y[i] = r.nextInt(grid_size);
			weight[i] = r.nextInt(MAX_HEIGHT);
			Terminal t = new Terminal(x[i], y[i], weight[i]);
			grid[x[i]][y[i]].addTerminal(t);
		}
	}

	private void poisson_distrbution() {
		grid = new Grid[grid_size][grid_size];
		Random r = new Random();
		ArrayList<Integer> xList = new ArrayList<>();
		ArrayList<Integer> yList = new ArrayList<>();
		ArrayList<Integer> weightList = new ArrayList<>();

		terminal_num = 0;
		int tmp = 0;
		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				tmp = StdRandom.poisson(0.1);
				terminal_num += tmp;
				grid[i][j] = new Grid();
				for (int k = 0; k < tmp; k++) {
					int random = r.nextInt(MAX_HEIGHT);
					Terminal t = new Terminal(i, j, random);
					xList.add(i);
					yList.add(j);
					weightList.add(random);
					grid[i][j].addTerminal(t);
				}
			}
		}

		x = new int[terminal_num];
		y = new int[terminal_num];
		weight = new int[terminal_num];
		convertToPrimitive(x, xList);
		convertToPrimitive(y, yList);
		convertToPrimitive(weight, weightList);
	}

	private void convertToPrimitive(int[] arr, ArrayList<Integer> list) {
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i);
		}
	}

	private void initGrids(int grid_size, int terminal_num) {
		grid = new Grid[grid_size][grid_size];
		x = new int[terminal_num];
		y = new int[terminal_num];
		weight = new int[terminal_num];

		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				grid[i][j] = new Grid();
			}
		}
	}

	private void read_file(String filename) {
		try {
			Scanner sc = new Scanner(new File(filename));
			String s = sc.nextLine();
			String[] config = s.split(" ");

			terminal_num = Integer.parseInt(config[0]);
			grid_size = Integer.parseInt(config[1]);

			initGrids(grid_size, terminal_num);

			for (int i = 0; i < terminal_num; i++) {
				x[i] = sc.nextInt();
				y[i] = sc.nextInt();
				weight[i] = sc.nextInt();
				Terminal t = new Terminal(x[i], y[i], weight[i]);
				grid[x[i]][y[i]].addTerminal(t);
			}
			sc.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	public void outputUAVTrail() throws Exception {
		for (int i = 0; i < uav.length; i++) {
			String name = "UAV" + uav[i].getID() + ".txt";
			File f = new File(name);
			PrintWriter pw = new PrintWriter(f);
			for (Point p : uav[i].movements()) {
				pw.printf("%.2f %.2f\n", p.x, p.y);
			}
			pw.close();
		}
	}

	public static void generateClusterDataFromPython(String filename, int num_term, Object center, float std,
			int grid_size, boolean randomCenter) throws Exception {

		String[] cmd = { "python", "./cluster_gen/cluster_gen_sp.py", String.valueOf(num_term),
				String.valueOf(grid_size), String.valueOf(5),
				randomCenter ? String.valueOf((int) center) : (String) center, String.valueOf(std), filename };

		ProcessBuilder pb = new ProcessBuilder(cmd);
		Map<String, String> env = pb.environment();
		env.put("PATH", "/home/medion/anaconda2/bin:" + env.get("PATH"));
		Process proc = pb.start();

		BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		String s = null;
		while ((s = input.readLine()) != null) {
			System.out.println(s);
		}

		while ((s = err.readLine()) != null) {
			System.out.println(s);
		}

		int x = proc.waitFor();
	}

	public static void oneTimeSimulation() throws Exception {
		String termConfig = "cluster_with_grid20-100/cluster_grid60_term360.txt";
		String uavConfig = "uavConfig_height300m.txt";
		UAVType type = UAVType.BioInspired;

		Environment e = new Environment(termConfig, uavConfig, type);
		e.setPowerThreshold(true);
		Environment.ITERATION = 10000;
		Environment.DRAW = true;

		System.out.println("UAV num:\t" + UAV_NUM);
		System.out.println("Terminal num:\t" + e.terminal_num);
		System.out.println("Grid size:\t" + e.grid_size);
		System.out.println("\nStart simulation");
		System.out.println("Terminal configure file name:\t" + termConfig);
		System.out.println("UAV configure file name:\t" + uavConfig);
		System.out.println("Selected UAV Type:\t\t" + type);
		System.out.println("PThreash -100 dBm is set to:\t" + Terminal.DB_THRESHOLD);
		System.out.println();

		e.simulate();

		// e.outputUAVTrail();

		e.outFinalLoc("./result/final_loc_" + type + ".txt");
	}

	public static void repetitiveSimulation() throws Exception {
		String uavConfig = "uavConfig_height300m.txt";
		UAVType type = UAVType.BioInspired;
		int TIMES = 10;

		UAV_NUM = 20;
		TERMINAL_NUM = 360;
		GRID_SIZE = 60;
		double std = 1;
		double[][][] mean = new double[3][10][8];
		for (int l = 0; l < TIMES; l++) {
			for (int j = 0; j < 3; j++) {
				if (j == 0)
					type = UAVType.BioInspired;
				if (j == 1)
					type = UAVType.MAD2T;
				if (j == 2)
					type = UAVType.PIA;

				int t = 0;
				for (int i = 1; i <= 10; i += 1) {
					// TERMINAL_NUM = i * 360;
					// GRID_SIZE = i;
					std = i;
					Environment e;
					System.out.println("Experiment num: " + (l + 1));
					// String filename = "uniform_with_grid60/cluster_grid" + i
					// + "_term" + 360 + ".txt";
					String filename = "cluster_with_grid60_term360/cluster_grid" + 60 + "_term" + 360 + "_std"
							+ (float) i + ".txt";
					// filename = "regular_with_grid60/regular" + i + ".txt";
					if (j == 0)
						e = new Environment(filename, uavConfig, type);
					else
						e = new Environment(filename, uavConfig, type);
					UAV.ID = 0;
					System.out.println("Configfile: " + filename);
					System.out.println("Type " + type);
					System.out.println("Grid_size " + e.grid_size);
					System.out.println("Terminal_size " + e.terminal_num);
					System.out.println("Standard deviation " + std);
					Environment.ITERATION = 10000;
					e.setPowerThreshold(true);
					Environment.DRAW = false;

					double[] a = e.simulate();
					for (int k = 0; k < a.length; k++) {
						mean[j][t][k] += a[k];
						System.out.print(a[k] + " ");
					}
					System.out.println("");
					System.out.println("");
					t++;
				}
			}
		}

		for (int i = 0; i < 3; i++) {
			if (i == 0)
				System.out.println("\nType: " + UAVType.BioInspired + "\n");
			if (i == 1)
				System.out.println("\nType: " + UAVType.MAD2T + "\n");
			if (i == 2)
				System.out.println("\nType: " + UAVType.PIA + "\n");
			for (int j = 0; j < mean[0].length; j++) {
				System.out.printf("ASF: %.3f ", mean[i][j][0] / TIMES);
				System.out.printf("Terminal: %.3f ", mean[i][j][1] / TIMES);
				System.out.printf("MM: %.3f ", mean[i][j][2] / TIMES);
				System.out.printf("AVD: %.3f ", mean[i][j][3] / TIMES); // Average
																		// distance
																		// among
																		// UAVs
				System.out.printf("UTAD: %.3f ", mean[i][j][4] / TIMES); // UAV-Terminal
																			// average
																			// distance
				System.out.printf("Served Terminals per UAVs: %.3f ", mean[i][j][5] / TIMES); // Served
																								// Terminals
																								// for
																								// each
																								// UAVs
				System.out.printf("Effective SIR: %.3f \n", mean[i][j][6] / TIMES); // Served
																					// Terminals
																					// for
																					// each
																					// UAVs
				System.out.printf("Iterations: %.3f \n", mean[i][j][7] / TIMES); // Served
																					// Terminals
																					// for
																					// each
																					// UAVs
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// Environment.PAUSE = true;
		 oneTimeSimulation();
//		repetitiveSimulation();

	}

}
