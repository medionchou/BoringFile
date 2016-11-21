package thesis;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.IntStream;

import edu.princeton.cs.algs4.Merge;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;

public class Environment {

    public static boolean DRAW = true;
	public static int UAV_NUM = 20;
	public static int TERMINAL_NUM = 350;
	public static int GRID_SIZE = 60;
	public static int ITERATION = 10000;
	public static final int MAX_WEIGHT = 5;
	public static final int MAX_HEIGHT = 5;
	public static final double TRANSMIT_POWER = 46;
	public static final double STEP = 0.1;
	public static final double Z_WEIGHT = 0; //whether UAV should consider moving z-coordination.
	public static final String NORMAL_DISTRIBUTION = "UNIFORM_DISTRIBUTION";
	public static final String POISSON_DISTRIBUTION = "POISSON_DISTRIBUTION";
	public static final String UAV_RANDOM = "UAV_RANDOM";


	private Grid[][] grid;
	private UAV[] uav;
	private int[] x;
	private int[] y;
	private int[] weight;
	private int terminal_num;
	private int grid_size;
	private SequenceGenerator sg;

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
	    StdDraw.setPenColor(Color.CYAN);
	    for (int i = 0; i < terminal_num; i++) {
	        StdDraw.point(x[i], y[i]);
	    }
	        
	    
	    for (int i = 0; i < uav.length; i++) {
	        if (i == current) StdDraw.setPenColor(Color.RED);
	        else if (StdDraw.getPenColor() != Color.BLACK) StdDraw.setPenColor(Color.BLACK);
	        StdDraw.point(uav[i].x(), uav[i].y());
	    }
	    StdDraw.show();
//	    while (!StdDraw.hasNextKeyTyped());
//	    StdDraw.nextKeyTyped();
	    StdDraw.clear();
	}
	
	public void simulate() {
//	    Scanner s = new Scanner(System.in);
	    
	    if (DRAW) {
	        StdDraw.setScale(0, grid_size);
	        StdDraw.setPenRadius(0.02);
	    }
	    
		for (int i = 0; i < ITERATION; i++) {
			int[] seq = sg.sequence(UAV_NUM);
			
			if ((i + 1) % 1000 == 0) {
				System.out.println("Iteration: " + (i + 1));
				
			}

			for (int j = 0; j < seq.length; j++) {
//			    if (DRAW) draw(seq[j]);
				uav[seq[j]].run(grid);
//				if (DRAW) draw(seq[j]);
			}
			if (DRAW) draw(-1);
		}

		double avs = 0d;
		double res[];
		int[] term_dist = new int[uav.length];
		for (int i = 0; i < uav.length; i++) {
			System.out.println(uav[i]);
			res = uav[i].getSpectrumAndTerms(grid);
			System.out.println("Spectrum: " + res[0] + "\n");
			avs += res[0];
			term_dist[i] = (int) res[1];
		}
		double std = StdStats.stddev(term_dist);
		System.out.println("Standard deviation: " + std);
		System.out.println("Total Average Spectral Efficiency: " + avs / terminal_num);
		System.out.println("Terminal num: " + terminal_num);
		
	}

	private void initUAV(String uavDistri, UAVType uavType) {
		if (uavDistri == null || uavType == null)
			throw new NullPointerException("Arguments can't be null");
		Point[] pt = getUAVLocs(uavDistri);
		uav = new UAV[pt.length];

		switch (uavType) {
		case ReferenceUAV:
			for (int i = 0; i < uav.length; i++) {
				uav[i] = new ReferenceUAV(pt[i].x, pt[i].y, pt[i].z, true);
			}
			sg = ReferenceUAV.SEQUENCE_GENERATOR;
			break;
		case OriginalUAV:
			for (int i = 0; i < uav.length; i++) {
				uav[i] = new OriginalUAV(pt[i].x, pt[i].y, pt[i].z, true);
			}
			sg = OriginalUAV.SEQUENCE_GENERATOR;
			break; 
		case OptimalGameModelUAV:
			for (int i = 0; i < uav.length; i++) {
				uav[i] = new OptimalGameModelUAV(pt[i].x, pt[i].y, pt[i].z, true);
			}
			sg = OptimalGameModelUAV.SEQUENCE_GENERATOR;
			break;
		case GameUAV:
		    for (int i = 0; i < uav.length; i++) {
                uav[i] = new GameUAV(pt[i].x, pt[i].y, pt[i].z, true);
                uav[i].setPartnetUAV(uav);
            }
            sg = GameUAV.SEQUENCE_GENERATOR;
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
	    
	    System.out.println("Generate " + filename );
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
					pt[i] = new Point(sc.nextDouble(), sc.nextDouble(), sc.nextDouble());
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
			terminal_num = sc.nextInt();
			grid_size = sc.nextInt();
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

	public static void main(String[] args) throws Exception {
	    
	    String termConfig = "cluster3_n383.txt";
	    String uavConfig = "uavConfig_height300m.txt";
	    UAVType type = UAVType.ReferenceUAV;
	    
		Environment e = new Environment(termConfig, uavConfig, type);
		e.setPowerThreshold(true);
		Environment.ITERATION = 1000;
		Environment.DRAW = true;
		
		System.out.println("UAV num:\t" + UAV_NUM);
		System.out.println("Terminal num:\t" + e.terminal_num);
		System.out.println("Grid size:\t" + e.grid_size);
		System.out.println("\nStart simulation");
		System.out.println("Terminal configure file name:\t" + termConfig);
		System.out.println("UAV configure file name:\t" + uavConfig);
		System.out.println("Selected UAV Type:\t\t" + type);
		System.out.println("PThreash -100 dBm is set to:\t" + Terminal.DB_THRESHOLD);
		
		
		e.simulate();

		e.outFinalLoc("./result/final_loc_" + type + ".txt");
	}

}
