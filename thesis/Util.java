package thesis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Random;

public class Util {
	
	private Util() {}
	
	public static Point randomPoint() {
		Strategy st = Strategy.randomStrategy();
		
		switch(st) {
		case UP:
			return new Point(0, 0, Environment.STEP * Environment.Z_WEIGHT);
		case DOWN:
			return new Point(0, 0, -Environment.STEP * Environment.Z_WEIGHT);
		case FORWARD:
		    return new Point(0, Environment.STEP, 0);
		case BACKWARD:
		    return new Point(0, -Environment.STEP, 0);
		case RIGHT:
		    return new Point(Environment.STEP, 0, 0);
		case LEFT:
		    return new Point(-Environment.STEP, 0, 0);
		default:
			return new Point(0, 0, 0);
		}
	}
	
	public static Point getPointByStrategy(Strategy st) {
		switch(st) {
		case UP:
			return new Point(0, 0, Environment.STEP * Environment.Z_WEIGHT);
		case DOWN:
			return new Point(0, 0, -Environment.STEP * Environment.Z_WEIGHT);
		case FORWARD:
            return new Point(0, Environment.STEP, 0);
        case BACKWARD:
            return new Point(0, -Environment.STEP, 0);
        case RIGHT:
            return new Point(Environment.STEP, 0, 0);
        case LEFT:
            return new Point(-Environment.STEP, 0, 0);
        default:
            return new Point(0, 0, 0);
		}
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static void main(String[] args) {
		

	}
}
