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
	private int origin_grid_size;
	private SequenceGenerator sg;

	/**
	 * 
	 * @param type - which type of Terminal should be generate(uniform, poisson, random, or read from file);
	 * @param uavDistri - the file specify initial location for UAV.
	 * @param uavType - which type of UAV should be created. 
	 */
	public Environment(String type, String uavDistri, UAVType uavType) {
	    origin_grid_size = -1;
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
	    StdDraw.clear();
	}
	
	private boolean keyControl() {
	    
        if (StdDraw.hasNextKeyTyped()) {
            char d = StdDraw.nextKeyTyped();
            
            switch(d) {
            case 'K':
            case 'k':
                return true;
            case 'P':
            case 'p':
                while (true) {
                    if (StdDraw.hasNextKeyTyped()) break;
                }
                break;
            }
        }
        
        return false;
	}
	
	public double[] simulate() { 
	    if (DRAW) {
	        StdDraw.setCanvasSize(720, 720);
	        StdDraw.setScale(0, grid_size);
	        StdDraw.setPenRadius(0.02);
	    }
	    boolean leave = false;
	    boolean stable;
	    int count = 0;
		for (int i = 0; i < ITERATION; i++) {
		    stable = true;
	        if (DRAW) draw(-1);
//	        while (!StdDraw.hasNextKeyTyped());
//	        System.out.println(uav[0]);
			int[] seq = sg.sequence(UAV_NUM);

			leave = keyControl();
			if (leave) break;
			
			if ((i + 1) % 1000 == 0) {
				System.out.println("Iteration: " + (i + 1));
			}

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
		return printConsoleResult();
	}
	
	private double[] printConsoleResult() {
	  double avs = 0d;
      double res[];
      double step = 0.0;
      int[] term_dist = new int[uav.length];
      for (int i = 0; i < uav.length; i++) {
//            System.out.println(uav[i]);
          res = uav[i].getSpectrumAndTerms(grid);
//            System.out.println("Spectrum: " + res[0] + "\n");
          avs += res[0];
          term_dist[i] = (int) res[1];
          step += uav[i].steps();
      }
      double std = StdStats.stddev(term_dist);
      System.out.println("Standard deviation: " + std);
      System.out.println("Total Average Spectral Efficiency: " + avs / terminal_num);
      System.out.println("Total Terminal num: " + terminal_num);
      System.out.println("Terminal num: " + IntStream.of(term_dist).sum());
      System.out.println("Total steps: " + step);
      
      double[] a = {avs/terminal_num, IntStream.of(term_dist).sum(), step};
      return a;
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
				if (origin_grid_size != -1) uav[i].setOriginGridSize(origin_grid_size);
			}
			sg = ReferenceUAV.SEQUENCE_GENERATOR;
			break;
		case OriginalUAV:
			for (int i = 0; i < uav.length; i++) {
				uav[i] = new OriginalUAV(pt[i].x, pt[i].y, pt[i].z, true);
				if (origin_grid_size != -1) uav[i].setOriginGridSize(origin_grid_size);
			}
			sg = OriginalUAV.SEQUENCE_GENERATOR;
			break; 
		case OptimalGameModelUAV:
			for (int i = 0; i < uav.length; i++) {
				uav[i] = new OptimalGameModelUAV(pt[i].x, pt[i].y, pt[i].z, true);
				if (origin_grid_size != -1) uav[i].setOriginGridSize(origin_grid_size);
			}
			sg = OptimalGameModelUAV.SEQUENCE_GENERATOR;
			break;
		case GameUAV:
		    for (int i = 0; i < uav.length; i++) {
                uav[i] = new GameUAV(pt[i].x, pt[i].y, pt[i].z, true);
                if (origin_grid_size != -1) uav[i].setOriginGridSize(origin_grid_size);
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
		int offset = (grid_size - 60) / 2;
		offset = 0;
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
			
			if (config.length == 2) {
			    terminal_num = Integer.parseInt(config[0]);
			    grid_size = Integer.parseInt(config[1]);
			    
			} 
			else if (config.length == 3) {
			    terminal_num = Integer.parseInt(config[0]);
	            grid_size = Integer.parseInt(config[1]); // show be bigger than origin size
	            origin_grid_size = Integer.parseInt(config[2]); // origin
	            
	            if (grid_size < origin_grid_size) grid_size = origin_grid_size;
			}

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
	
	  public static void generateClusterDataFromPython(String filename, int num_term, Object center, float std, int grid_size, boolean isCenterLoc) throws Exception {
	      String[] cmd = {
	        "python",
	        "./cluster_gen/cluster_gen_sp.py",
	        String.valueOf(num_term),
	        String.valueOf(grid_size),
	        String.valueOf(5),
	        isCenterLoc ? (String) center : String.valueOf((int) center),
	        String.valueOf(std),
	        filename
	      };

	      ProcessBuilder pb = new ProcessBuilder(cmd);
	      Map<String, String>env = pb.environment();
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
        String termConfig = "test.txt";
        String uavConfig = "uavConfig_height300m.txt";
//	    String termConfig = "test/test_term.txt";
//	    String uavConfig = "test/test_uav.txt";
        UAVType type = UAVType.GameUAV;
        
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

        e.outFinalLoc("./result/final_loc_" + type + ".txt");
//        generateClusterDataFromPython("testtttt.txt", 3600, 5, 60);
	}
	
	public static void repetitiveSimulation() throws Exception {
        String termConfig = "cluster5_n383.txt";
        String uavConfig = "uavConfig_height300m.txt";
        UAVType type = UAVType.GameUAV;
        int TIMES = 10;

        UAV_NUM = 20;
        TERMINAL_NUM = 360;
        GRID_SIZE = 60;
        int std = 1;
        double[][][] mean = new double[2][10][3];
        for (int l = 0; l < TIMES; l++) {
            for (int j = 0; j < 2; j++) {
                if (j == 0) type = UAVType.GameUAV;
                else type = UAVType.ReferenceUAV;
                for (int i = 1, t = 0; i <= 10; i += 1 , t++) {
//                    TERMINAL_NUM = i * 360;
//                    GRID_SIZE = i;
                    std = i;
                    Environment e;
                    System.out.println("Experiment num: " +  (l+1));
                    String filename = "cluster_with_grid60_term360/cluster_grid" + 60 + "_term" + 360 + "_std" + i + ".txt";
                    if (j == 0)
                        e = new Environment(filename, uavConfig, type);
                    else
                        e = new Environment(filename, uavConfig, type);
                    UAV.ID = 0;
                    System.out.println("Configfile: " + filename);
                    System.out.println("Type " + type);
                    System.out.println("Grid_size " + GRID_SIZE);
                    System.out.println("Terminal_size " + TERMINAL_NUM);
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
                }
            }
        }
        
        for (int i = 0; i < 2; i++) {
            if (i == 0) System.out.println("\nType: " + UAVType.GameUAV + "\n");
            if (i == 1) System.out.println("\nType: " + UAVType.ReferenceUAV + "\n");
            for (int j = 0; j < mean[0].length; j++) {
                System.out.println("Simulation" + i + ":");
                System.out.print("ASF: " + mean[i][j][0] / TIMES + " ");
                System.out.print("Terminal: " + mean[i][j][1] / TIMES+ " ");
                System.out.println("MM: " + mean[i][j][2] / TIMES);
            }
        }

	}

	public static void main(String[] args) throws Exception {
	    
//	    oneTimeSimulation();
	    repetitiveSimulation();
	    
//	    generateClusterDataFromPython("test.txt", 360, "12-56 47-33 50-56 2-13 43-46", (float)4, 60, true);
//	    for (int i = 1; i <= 10; i += 1) {
//	        generateClusterDataFromPython("cluster_grid60_term360_std" + i + ".txt"
//                                           , 360, "12-56 47-33 50-56 2-13 43-46", (float)i, 60, true);
//	    }
	    //"20-93 78-55 83-93 3-21 71-76"
//	    
//	    for (int i = 0; i < 5; i++) {
//	        int x = StdRandom.uniform(60);
//	        int y = StdRandom.uniform(60);
//	        
//	        System.out.print(x + "-" + y + " ");
//	    }
	    
	}

}
