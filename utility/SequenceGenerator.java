package utility;

public interface SequenceGenerator {
    
	
	/**
	 * The ID of UAV in the lowest index of array moves first. For example, 
	 * the returned array A contains element {3, 1, 2, 0, 4} which are the IDs of each UAV.
	 * In this case, UAV with ID 3 (A[0]) moves first. UAV with ID 1 (A[1]) is the next one to move, and so on.    
	 * 
	 * @param uavNUm - the number of UAVs in the environment.
	 * @return integer array contains the IDs of each UAV. 
	 */
    public int[] sequence(int uavNUm);
}
