
/*
 * holds a candidate PSO solution, including:
 * 	- the position in the solution space
 * 	- the function value and error at that point
 * 	- which particle found that solution
 * 
 * 
 */


public class Solution {

	
	private DoubleVector position;
	private double value;
	private double error;
	private int particleID;

	
	
	public Solution () {
		this.position = null;
		this.value = 0.0;
		this.error = 0.0;
		this.particleID = 0;


	}

	public Solution (DoubleVector position, double value, double error, int particleID) {
		this.position = position.getCopy();
		this.value = value;
		this.error = error;
		this.particleID = particleID;	

	}

	


	
	public Solution getCopy() {	
		Solution retSol = new Solution(position, value, error, particleID);
//		retSol.setMostRecIterUsed(mostRecIterUsedOnPBestsList);
//		retSol.setTimesUsed(timesUsedOnPBestsList);
		
		return retSol;
		
	}

	
	public void copyFrom(Solution s) {
		this.position = s.getPositionCopy();
		this.value = s.getFunctionValue();
		this.error = s.getError();
		this.particleID = s.getParticleID();
//		this.mostRecIterUsedOnPBestsList = s.getMostRecIterUsed();

	}
	
	
	public double distance (Solution s) {
		return position.distance(s.getPosition());
	}
	

	
	public int getParticleID() {
		return particleID;
	}

	public void setParticleID(int particleID) {
		this.particleID = particleID;
	}


	// return the actual position
	public DoubleVector getPosition() {
		return position;
	}

	// return a copy of the position
	public DoubleVector getPositionCopy() {
		return position.getCopy();
	}

	// copy from a given position to this position
	public void copyFromPosition(DoubleVector inPosition) {
		this.position.copyFrom(inPosition);
	}

//	}

	public double getFunctionValue() {
		return value;
	}

	public void setFunctionValue(double value) {
		this.value = value;
	}

	
	public double getError() {
		return error;
	}

	public double getAbsValError() {
		return Math.abs(error);
	}

	public void setError(double error) {
		this.error = error;
	}


	public double calcSolutionValue (int functionNum) {
		double[] results = TestFunctions.evalWithError(position , functionNum);
		return results[TestFunctions.VAL_INDEX];
	}

	public double calcSolutionError (int functionNum) {
		double[] results = TestFunctions.evalWithError(position , functionNum);
		return results[TestFunctions.ERR_INDEX];
	}



	public void print() {
		position.print();
		System.out.printf("%s%2d%s%.8e%s%.8e", "  particleID = ", particleID, "  val = ", value,"  err = ", error);
	}

	public void println() {
		print();
		System.out.println();
	}




}


