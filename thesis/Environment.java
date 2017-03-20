package thesis;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdRandom;
import uav.BioInspired;
import uav.MAD2T;
import uav.PIA;
import uav.UAV;
import uav.UAVType;
import utility.SequenceGenerator;
import utility.Util;

public class Environment {

	public static boolean PAUSE = false;
	public static boolean DRAW = true;
	public static boolean UAV_OFFSET = true;
	public static int UAV_NUM = 20;
	public static final int TERMINAL_NUM = 360;
	public static final int GRID_SIZE = 60; 
	public static final int ITERATION = 10000;
	public static final int MAX_WEIGHT = 5;
	public static final int MAX_HEIGHT = 5;
	public static final double TRANSMIT_POWER = 46;
	public static final double STEP = 0.1;
	public static final String UNIFORM_DISTRIBUTION = "UNIFORM_DISTRIBUTION";
	public static final String POISSON_DISTRIBUTION = "POISSON_DISTRIBUTION";
	public static final String UAV_RANDOM = "UAV_RANDOM";

	public static final double MDSP = -100.0;

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
	 *            - which type of terminal should be generate(uniform, poisson, or read from file).
	 * @param uavDistri
	 *            - the file specify initial location for UAV.
	 * @param uavType
	 *            - which type of UAV should be created (MAD2T, PIA, or BioInspired).
	 */
	public Environment(String type, String uavDistri, UAVType uavType) {
		grid_size = GRID_SIZE;
		terminal_num = TERMINAL_NUM;
		initTerms(type);
		initUAV(uavDistri, uavType);
		bindUAVtoTerm();
	}

	/**
	 * Control minimal detectable signal power toggle. Set false to disable MDSP and vice versa.
	 * @param val - toggle value
	 */
	public void setMDSPThreshold(boolean val) {
		Terminal.MDSP_THRESHOLD = val;
	}

	/**
	 * Generate terminals information file for this simulation.
	 * @param filename specify output filename
	 */
	public void exportFile(String filename) {
		if (filename == null) throw new NullPointerException("File name can't be null.");
		if (filename.length() == 0) throw new NullPointerException("File name can't be empty.");

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

	private void draw() {
		StdDraw.enableDoubleBuffering();

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

	
	/**
	 * Return results after simulation finished.
	 * @return array of double containing results
	 */
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
			if (DRAW) draw();
			if (PAUSE) while (!StdDraw.hasNextKeyTyped());
			
			int[] seq = sg.sequence(uav.length);

			leave = keyControl();
			if (leave)
				break;

			for (int j = 0; j < seq.length; j++) {
				uav[seq[j]].run(grid);
				stable &= uav[seq[j]].isStable();
			}
			if (stable) count++;
			else count = 0;

			if (count == 10) {
				count = 0;
				break;
			}
		}

		return printConsoleResult(iterations);
	}

	private double[] printConsoleResult(int iteration) {

		double spectral_efficiency = 0d;
		double result[];
		double travel_distance = 0.0;
		double aud = 0d;
		int[] served_term = new int[uav.length];
		double[] served_term_distance = new double[uav.length];

		setMDSPThreshold(false);
		for (int i = 0; i < uav.length; i++) {
			result = uav[i].getSpectrumAndTerms(grid);
			spectral_efficiency += result[0];
			served_term[i] = (int) result[1];
			served_term_distance[i] = result[2];
			travel_distance += uav[i].travel_distance();
		}
		draw(); //last draw to show served terminals in red color 

		for (int i = 0; i < uav.length; i++) {
			for (int j = i + 1; j < uav.length; j++) {
				aud += Point.distance(uav[i].x(), uav[i].y(), uav[i].z(), uav[j].x(), uav[j].y(), uav[j].z());
			}
		}
		System.out.println("Average Spectral Efficiency: " + spectral_efficiency / terminal_num);
		System.out.println("Total Terminal num: " + terminal_num);
		System.out.printf("Served terminal num: %d\n", IntStream.of(served_term).sum());
		System.out.printf("Served terminal num (%%): %.2f%%\n", (float) IntStream.of(served_term).sum() / terminal_num * 100);
		System.out.printf("Served terminals per UAV: %.2f\n", (float) IntStream.of(served_term).sum() / uav.length);
		System.out.println("Average UAV-Terminal distance: " + DoubleStream.of(served_term_distance).sum() / IntStream.of(served_term).sum());
		System.out.println("Total travel distance: " + travel_distance);
		System.out.println("UAV average distance: " + aud / (uav.length * (uav.length - 1) / 2));
		System.out.println("Total spectral efficiency: " + spectral_efficiency);
		System.out.println("Iterations: " + iteration);

		int total_served_term = IntStream.of(served_term).sum();

		double[] a = { total_served_term > 0 ? spectral_efficiency / terminal_num : 0, // average spectral efficiency
				total_served_term, // # of served terminals
				travel_distance, // cost
				aud / (uav.length * (uav.length - 1) / 2), // average UAV distance
				DoubleStream.of(served_term_distance).sum() / total_served_term, // average UAV-terminal distance
				(float) total_served_term / uav.length, // served terminals per UAV
				total_served_term > 0 ? spectral_efficiency : 0, 
				iteration };
		return a;
	}

	private void initUAV(String uavConfig, UAVType uavType) {
		if (uavConfig == null || uavType == null) throw new NullPointerException("Arguments can't be null");
		
		Point[] pt = getUAVLocs(uavConfig);
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
		case UNIFORM_DISTRIBUTION:
			uniform_distribution();
			break;
		case POISSON_DISTRIBUTION:
			poisson_distrbution();
			break;
		default:
			read_terms_config(type);
			break;
		}
		System.out.println("Terminal Initialization Finished");
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("GRID_SIZE: " + grid_size + "\tTERM_NUM: " + terminal_num + "\tUAV_NUM: " + uav.length + "\n\n");
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

	/**
	 * Read UAVs' coordinates from configuration file. Since UAVs' initial location is based on area size 60 x 60 km,
	 * we use <b>UAV_OFFSET</b> flag to control UAVs' location relative to area size 60 x 60 km.
	 * @param uavConfig UAV configuration filename
	 * @return UAVs' location array
	 */
	private Point[] getUAVLocs(String uavConfig) {
		Point[] pt = null;
		int offset = (grid_size - 60) / 2;
		
		if (!UAV_OFFSET) offset = 0; 
		
		switch (uavConfig) {
		case UAV_RANDOM:
			Random r = new Random();
			pt = new Point[UAV_NUM];

			for (int i = 0; i < UAV_NUM; i++) {
				pt[i] = new Point(r.nextDouble() * grid_size, r.nextDouble() * grid_size, r.nextDouble() * MAX_HEIGHT);
			}
			break;
		default:
			try {
				Scanner sc = new Scanner(new File(uavConfig));
				pt = new Point[sc.nextInt()];
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

	private void read_terms_config(String filename) {
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

	/**
	 * Output the trails UAVs have traveled
	 * @throws Exception
	 */
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

	/**
	 * Generate java process to invoke python script. Be sure the python script path and environment variable both exist.
	 * You can directly execute python script instead of this function.
	 * @param filename Output terminals file name generated by python script.
	 * @param num_term The number of terminals
	 * @param center Cluster center (It is String or Integer type. String type indicates the exact location of each 
	 * cluster centers while Integer type indicates the number of randomly generated cluster centers) 
	 * @param std
	 * @param grid_size area size
	 * @param randomCenter boolean to control whether the type of <i>center</i> is String or Integer.
	 * @throws Exception
	 */
	public static void generateClusterDataFromPython(String filename, int num_term, Object center, float std,
			int grid_size, boolean randomCenter) throws Exception {

		String[] cmd = { "python", "./cluster_gen/cluster_gen.py", 
				String.valueOf(num_term),
				String.valueOf(grid_size), 
				String.valueOf(5),
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
		String termConfig = "cluster_with_grid20-100/cluster_grid20_term360.txt";
		String uavConfig = "uavConfig_height300m.txt";
		UAVType type = UAVType.BioInspired;

		Environment e = new Environment(termConfig, uavConfig, type);
		e.setMDSPThreshold(true);
		Environment.DRAW = true;

		System.out.println("UAV num:\t" + e.uav.length);
		System.out.println("Terminal num:\t" + e.terminal_num);
		System.out.println("Grid size:\t" + e.grid_size);
		System.out.println("\nStart simulation");
		System.out.println("Terminal configure file name:\t" + termConfig);
		System.out.println("UAV configure file name:\t" + uavConfig);
		System.out.println("Selected UAV Type:\t\t" + type);
		System.out.println("PThreash -100 dBm is set to:\t" + Terminal.MDSP_THRESHOLD);
		System.out.println();

		e.simulate();
	}

	public static void repetitiveSimulation() throws Exception {
		String uavConfig = "uavConfig_height300m.txt";
		UAVType type = UAVType.MAD2T;
		int TIMES = 10;

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

					e.setMDSPThreshold(true);
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
//		 oneTimeSimulation();
//		repetitiveSimulation();
	}

}
