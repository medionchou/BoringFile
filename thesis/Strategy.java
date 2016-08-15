package thesis;

import java.util.Random;

public enum Strategy {
    UP,
    DOWN,
    FORWARD,
    BACKWARD,
    RIGHT,
    LEFT;
    
    private static final Strategy[] STRATEGY = Strategy.values();
    private static final Random r = new Random();
    
    public static class Point {
        public double x;
        public double y;
        public double z;
        
        public Point(double x, double y, double z) {
            
        }
    }
    
    public static Strategy randomStrategy() {
        return STRATEGY[r.nextInt(STRATEGY.length)];
    }
    
}
