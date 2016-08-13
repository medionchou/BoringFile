package thesis;


/**
 * 
 * @author Medion-PC
 *  M2016 prototype UAV.
 */
public class RawUAV extends UAV {
    
    private double last_profit;
    private Strategy last_move;

    public RawUAV(double x, double y, double z, boolean isOpen) {
        super(x, y, z, isOpen);
        last_profit = 0.0;
        last_move = null;
    }

    
    private double utility() {
        return 0;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

}
