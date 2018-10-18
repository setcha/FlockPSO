import java.util.Random;

// often need random numbers in ranges in rest of code, so put methods
// that do this in a Utilities class

public class Utilities {

	public static final Random rand = new Random();
	
	// to get random value in range
	public static int nextInt(int minVal, int maxVal) {
		return minVal + Utilities.rand.nextInt((maxVal - minVal));
	}
	
	public static double nextDouble(double minVal, double maxVal) {
		return minVal + (Utilities.rand.nextDouble() * (maxVal - minVal));
	}
	
	public static void setConsistentSeed(int seed) {
		Utilities.rand.setSeed(seed); //can be set to any other seed as well
	}
	
}
