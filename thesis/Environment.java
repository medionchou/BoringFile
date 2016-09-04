package thesis;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import edu.princeton.cs.algs4.StdRandom;
import javafx.scene.effect.Lighting;


public class Environment {

    public static final int UAV_NUM = 20;
    public static final int TERMINAL_NUM = 350;
    public static final int GRID_SIZE = 60;
    public static final int MAX_WEIGHT = 5;
    public static final int MAX_HEIGHT = 3;
    public static final int TRANSMIT_POWER = 46;
    public static final double STEP = 0.1;
    public static final String NORMAL_DISTRIBUTION = "UNIFORM_DISTRIBUTION";
    public static final String POISSON_DISTRIBUTION = "POISSON_DISTRIBUTION";
    public static final String UAV_RANDOM = "UAV_RANDOM";
    
    
    private Grid[][] grid;
    private UAV[] uav;
    private int[] x;
    private int[] y;
    private int[] weight;
    private int grid_size;
    private int terminal_num;
    private SequenceGenerator sg;
    

    public Environment(String type, String uavDistri, UAVType uavType) {
        grid_size = GRID_SIZE;
        terminal_num = TERMINAL_NUM;
        initTerms(type);
        initUAV(uavDistri, uavType);
        bindUAVtoTerm();
    }
    
    public void exportFile(String filename) {
        if (filename == null) throw new NullPointerException("File name can't be null.");
        if (filename.length() == 0) return;
        
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
    
    public void simulate() {
        for (int i = 0; i < 10000; i++) {
            int[] seq = sg.sequence(UAV_NUM);
            
            for (int j = 0; j < seq.length; j++) {
                uav[seq[j]].run(grid);
            }
        }        
        
        double avs = 0d;
        double tmp;
        for (int i = 0; i < uav.length; i++) {
            System.out.println(uav[i]);
            tmp = uav[i].getSpectrum(grid);
            System.out.println("Spectrum: " + tmp);
            avs += tmp;
        }
        
        System.out.println(avs / TERMINAL_NUM);
    }
    
    private void initUAV(String uavDistri, UAVType uavType) {
        if (uavDistri == null || uavType == null) throw new NullPointerException("Arguments can't be null");
        Point[] pt = getPoints(uavDistri);
        uav = new UAV[pt.length];
        switch (uavType) {
        case RawUAV:
            for (int i = 0; i < uav.length; i++) {
                uav[i] = new RawUAV(pt[i].x, pt[i].y, pt[i].z, true);
            }
            sg = RawUAV.SEQUENCE_GENERATOR;
            break;
        case GameModelUAV:
            break;
        }
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
    }
    
    private Point[] getPoints(String uavDistri) {
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
                if (grid[i][j].getTermNum() == 0) continue;
                
                for (Iterator<Terminal> it = grid[i][j].getTerminals(); it.hasNext(); ) {
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
                tmp = poisson(0.1);
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
            grid_size = sc.nextInt();
            terminal_num = sc.nextInt();
            initGrids(grid_size, terminal_num);
            
            for (int i = 0; i < terminal_num; i++) {
                x[i] = sc.nextInt();
                y[i] = sc.nextInt();
                weight[i] = sc.nextInt();
                Terminal t = new Terminal(x[i], y[i], weight[i]);                
            }
            sc.close();
        } catch(IOException e) {
            System.out.println(e.toString());
        }
    }
    
    public static int poisson(double lambda) {
        if (!(lambda > 0.0))
            throw new IllegalArgumentException("Parameter lambda must be positive");
        if (Double.isInfinite(lambda))
            throw new IllegalArgumentException("Parameter lambda must not be infinite");
        // using algorithm given by Knuth
        // see http://en.wikipedia.org/wiki/Poisson_distribution
        Random r = new Random();
        int k = 0;
        double p = 1.0;
        double L = Math.exp(-lambda);
        do {
            k++;
            p *= r.nextDouble();
        } while (p >= L);
        
        return k-1;
    }

    public static void main(String[] args) {
        
        Environment e = new Environment(POISSON_DISTRIBUTION, "uavConfig.txt", UAVType.RawUAV);
        
        e.simulate();

//        System.out.println(RawUAV.class.getSimpleName());
        
    }
       
}


