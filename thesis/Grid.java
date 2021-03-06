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
        if (terminal_num == 0) throw new NullPointerException("No terminal within this grid");
        else return terminalList.iterator();
    }
    
    public Terminal getTerminal() {
        if (terminal_num == 0) throw new NullPointerException("No terminal within this grid");
        else return terminalList.get(0);
    }
    
    public int getTermNum() {
        
        return terminal_num;
    }
    
    public static void main(String[] args) {
    }
}
