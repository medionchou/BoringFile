package thesis;
import java.util.ArrayList;
import java.util.Iterator;

public class Grid {
    
    private int terminal_num;
    
    private ArrayList<Terminal> terminalList;
    
    public Grid() {
        terminal_num = 0;
        terminalList = new ArrayList<>();
    }
    
    
    public void addTerminal(Terminal termi) {
        terminalList.add(termi);
        terminal_num++;
    }
    
    public Iterator<Terminal> getTerminals() {
        return terminalList.iterator();
    }
    
    public int getTermNum() {
        
        return terminal_num;
    }
    
    public static void main(String[] args) {
    }
}
