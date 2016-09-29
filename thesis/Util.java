package thesis;

import java.util.HashMap;
import java.util.Random;

public class Util {
	
	private static HashMap<Strategy, Point> map;
	private Util() {}
	
	static {
		map = new HashMap<>();
		
		for (Strategy st:Strategy.values()) {
			switch (st) {
			case STILL:
				map.put(st, new Point(0, 0, 0));
				break;
			case UP:
				map.put(st, new Point(0, 0, Environment.STEP * Environment.Z_WEIGHT));
				break;
			case DOWN:
				map.put(st, new Point(0, 0, -Environment.STEP * Environment.Z_WEIGHT));
				break;
			case FORWARD:
				map.put(st, new Point(Environment.STEP, 0, 0));
				break;
			case BACKWARD:
				map.put(st, new Point(-Environment.STEP, 0, 0));
				break;
			case RIGHT:
				map.put(st, new Point(0, Environment.STEP, 0));
				break;
			case LEFT:
				map.put(st, new Point(0, -Environment.STEP, 0));
				break;
			}
		}
	}
	
	
	public static HashMap<Strategy, Point> enumerate() {
		return map;		
	}
	
	public static Point randomPoint() {
		Strategy st = Strategy.randomStrategy();
		
		switch(st) {
		case UP:
			return new Point(0, 0, Environment.STEP * Environment.Z_WEIGHT);
		case DOWN:
			return new Point(0, 0, -Environment.STEP * Environment.Z_WEIGHT);
		case FORWARD:
			return new Point(Environment.STEP, 0, 0);
		case BACKWARD:
			return new Point(-Environment.STEP, 0, 0);
		case RIGHT:
			return new Point(0, Environment.STEP, 0);
		case LEFT:
			return new Point(0, -Environment.STEP, 0);
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
			return new Point(Environment.STEP, 0, 0);
		case BACKWARD:
			return new Point(-Environment.STEP, 0, 0);
		case RIGHT:
			return new Point(0, Environment.STEP, 0);
		case LEFT:
			return new Point(0, -Environment.STEP, 0);
		default:
			return new Point(0, 0, 0);
		}
	}
	
	
	public static void main(String[] args) {
		
		enumerate();
	}
}
