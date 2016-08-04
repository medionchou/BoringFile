package thesis;

import java.util.Random;

public class RandomWrapper implements Wrapper {

    private Random r;
    
    public RandomWrapper(Random r) {
        this.r = r;
    }
    
    @Override
    public int nextInt(int param) {
        return r.nextInt(param);
    }
}
