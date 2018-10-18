/*
 * We run a Fitness test on both:
 * 	- the function value obtained by the PSO
 * 	- how far in the solution space that value is 
 * 	  from the location of the optimum 
 * This class just allows us to package FitnessData objects 
 * for both.
 */


public class FitnessResult {
	
	private FitnessData functionValue;
	private FitnessData distGlobalOpt;
	
	public FitnessResult() {
		functionValue = null;
		distGlobalOpt = null;
	}
	
	public FitnessResult(FitnessData functionValue, FitnessData distGlobalOpt) {
		this.functionValue = functionValue;
		this.distGlobalOpt = distGlobalOpt;
	}

	// getters and setters
	
	public FitnessData getFunctionValue() {
		return functionValue;
	}

	public void setFunctionValue(FitnessData functionValue) {
		this.functionValue = functionValue;
	}

	public FitnessData getDistGlobalOpt() {
		return distGlobalOpt;
	}

	public void setDistGlobalOpt(FitnessData distGlobalOpt) {
		this.distGlobalOpt = distGlobalOpt;
	}
	
	
	
	
}