

/*
 * Holds the data for a single Fitness (Mann-Whitney) test
 * Much of the Mann Whitney components aren't used anymore, since we changed to function values instead of Mann Whitney tests
 */

public class FitnessData {

	// the p-value; this becomes the fitness when scaled by the "fitness interval"
	private double FitnessScore;
	// other Mann-Whitney statistics that the p-value is based on, but that we
	// do not use directly
	private double U;
	private double Z;
	
	// these appear not to be used, but I'll leave them in, just in case we do at some point
	private double meanRankSPSO;
	private double meanRankFLOCKPSO;
	
	// indicates whether FLOCK-PSO did better (true) or
	// S-PSO did better (false)
	private boolean flockDidBetter = false;

	
	
	public FitnessData(){
		FitnessScore = Double.MAX_VALUE;
		U = Double.MAX_VALUE;
		Z = Double.MAX_VALUE;
		meanRankSPSO = Double.MAX_VALUE;
		meanRankFLOCKPSO = Double.MAX_VALUE;
		flockDidBetter = false;
	}

	
	public FitnessData(double jscMWScore, double U, double Z, double meanRankSPSO, double meanRankFLOCKPSO,
							boolean flockDidBetter) {
		this.FitnessScore = jscMWScore;
		this.U = U;
		this.Z = Z;
		this.meanRankSPSO = meanRankSPSO;
		this.meanRankFLOCKPSO = meanRankFLOCKPSO;
		this.flockDidBetter = flockDidBetter;
	}


	// getters and setters
	
	public double getFitScore() {
		return FitnessScore;
	}


	public void setFitScore(double FitnessScore) {
		this.FitnessScore = FitnessScore;
	}


	public double getU() {
		return U;
	}


	public void setU(double U) {
		this.U = U;
	}


	public double getZ() {
		return Z;
	}


	public void setZ(double Z) {
		this.Z = Z;
	}


	public double getMeanRankSPSO() {
		return meanRankSPSO;
	}


	public void setMeanRankSPSO(double meanRankSPSO) {
		this.meanRankSPSO = meanRankSPSO;
	}


	public double getJscMeanRankFLOCKPSO() {
		return meanRankFLOCKPSO;
	}


	public void setMeanRankFLOCKPSO(double meanRankFLOCKPSO) {
		this.meanRankFLOCKPSO = meanRankFLOCKPSO;
	}


	public boolean flockDidBetter() {
		return flockDidBetter;
	}


	public void setFlockDidBetter(boolean flockDidBetter) {
		this.flockDidBetter = flockDidBetter;
	}

	

}
