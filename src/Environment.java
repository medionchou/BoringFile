import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;


public class Environment {

    public final static int UAV_NUM = 50;
    public final static int TERMINAL_NUM = 50;
    public final static int GRID_SIZE = 50;
    public final static int MAX_WEIGHT = 5;
    
    private Grid[][] grid;
    private int[] x;
    private int[] y;
    private int[] weight;
    private int grid_size;
    private int terminal_num;
    

    public Environment(Type type) {
        grid_size = GRID_SIZE;
        terminal_num = TERMINAL_NUM;
        initSetting(type);
    }
    
    public Environment(String file) {
        read_file(file);
    }

    private void initSetting(Type type) {

        switch (type) {
        case NORMAL_DISTRIBUTION:
            normal_distribution();
            break;
        case POISSON_DISTRIBUTION:
            poisson_distrbution();
            break;
        }
    }
    
    private void initObj(int grid_size, int terminal_num) {
        grid = new Grid[grid_size][grid_size];
        x = new int[terminal_num];
        y = new int[terminal_num];
        weight = new int[terminal_num];
        
        this.grid_size = grid_size;
        this.terminal_num = terminal_num;
        
        for (int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                grid[i][j] = new Grid();
            }
        }
    }

    private void normal_distribution() {
        initObj(grid_size, terminal_num);
        
        Random r = new Random();
        
        for (int i = 0; i < terminal_num; i++) {
            x[i] = r.nextInt(grid_size);
            y[i] = r.nextInt(grid_size);
            weight[i] = r.nextInt(MAX_WEIGHT);
            Terminal t = new Terminal(x[i], y[i], weight[i]);
            
            grid[x[i]][y[i]].addTerminal(t);
        }
    }

    private void poisson_distrbution() {
        initObj(grid_size, terminal_num);
        
    }

    private void read_file(String filename) {
        try {
            Scanner sc = new Scanner(new File(filename));
            
            grid_size = sc.nextInt();
            terminal_num = sc.nextInt();
            initObj(grid_size, terminal_num);
            
            for (int i = 0; i < terminal_num; i++) {
                x[i] = sc.nextInt();
                y[i] = sc.nextInt();
                weight[i] = sc.nextInt();
                Terminal t = new Terminal(x[i], y[i], weight[i]);
                
                grid[x[i]][y[i]].addTerminal(t);
            }
        } catch(IOException e) {
            System.out.println(e.toString());
        }
    }
    
    public void exportFile(String filename) {
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
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("GRID_SIZE: "+grid_size + "\tTERM_NUM: " + terminal_num + "\tUAV_NUM: " + UAV_NUM + "\n\n");
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

    public static void main(String[] args) {
        Environment e = new Environment(Type.NORMAL_DISTRIBUTION);
        
        e.exportFile("text.txt");
        System.out.print(e);        
    }

}
