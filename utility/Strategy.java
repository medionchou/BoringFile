package utility;

import java.util.Random;

public enum Strategy {
    STILL,
    FORWARD,
    BACKWARD,
    RIGHT,
    LEFT;
    
    private static final Strategy[] STRATEGY = Strategy.values();
    private static final Random r = new Random();
    
    
    public static Strategy randomStrategy() {
        return STRATEGY[r.nextInt(STRATEGY.length)];
    }
}


