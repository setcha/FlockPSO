/*
 * We run a Mann-Whitney test on both:
 * 	- the function value obtained by the PSO
 * 	- how far in the solution space that value is 
 * 	  from the location of the optimum 
 * This class just allows us to package MannWhitneyData objects 
 * for both.
 */


public class MannWhitneyResult {
	
	private MannWhitneyData functionValue;
	private MannWhitneyData distGlobalOpt;
	
	public MannWhitneyResult() {
		functionValue = null;
		distGlobalOpt = null;
	}
	
	public MannWhitneyResult(MannWhitneyData functionValue, MannWhitneyData distGlobalOpt) {
		this.functionValue = functionValue;
		this.distGlobalOpt = distGlobalOpt;
	}

	// getters and setters
	
	public MannWhitneyData getFunctionValue() {
		return functionValue;
	}

	public void setFunctionValue(MannWhitneyData functionValue) {
		this.functionValue = functionValue;
	}

	public MannWhitneyData getDistGlobalOpt() {
		return distGlobalOpt;
	}

	public void setDistGlobalOpt(MannWhitneyData distGlobalOpt) {
		this.distGlobalOpt = distGlobalOpt;
	}
	
	
	
	
}