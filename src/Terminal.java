
public class Terminal {

    private int x;
    private int y;
    
    private int weight;
    private int coverage;
    
    public Terminal(int x, int y, int weight) {
        this.x = x;
        this.y = y;
        this.weight = weight;
        coverage = 0;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public int getCoverage() {
        return coverage;
    }
    
    
}
