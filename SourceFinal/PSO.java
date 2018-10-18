
/*
 * An individual in the population is a GP tree that changes the values of the flock
 * parameters on each iteration of a PSO run. The fitness of an individual is calculated by
 * creating a PSO object and calling the evalGPTree method on that object. This happens in 
 * the calcFitness method in Population.java. evalGPTree executes the number of PSO 
 * runs specified in Parameters.java, using the GP tree to control the flock parameters. 
 * S-PSO (standard PSO) is also run for the same number of iterations and a Mann-Whitney 
 * test is performed to evaluate the results. Those results are returned by evalGPTree.
 * 
 */


//import jsc.independentsamples.MannWhitneyTest;
//import org.apache.commons.math3.stat.inference.MannWhitneyUTest;



public class PSO {


	// topologies considered
	public static enum Topology {
		GBEST, RING, vonNEUMANN, MOORE, FLOCK
	}

	// include self?
	public static enum SelfModel {
		INCLUDE_SELF, NOT_INCLUDE_SELF
	}

	// who has influence in the neighborhood?
	// just the neighborhood best? or all particles
	// in the neighborhood?s
	public static enum InfluenceModel {
		NEIGH_BEST, FIPS
	}

	// the usual PSO parameters
	public static double nBestTheta = 2.05;
	public static double pBestTheta = 2.05;
	public static double theta = nBestTheta + pBestTheta;
	public static double constrictionFactor = 2.0 / (theta - 2.0 + Math.sqrt(theta*theta - 4.0*theta));

	public static boolean usingFLOCKPSO = false;
	// see the mannWhitney method for an explanation of the use of these
	private static final int NON_FLOCK_PSO_DATA_INDEX = 0;
	private static final int FLOCK_PSO_DATA_INDEX = 1;

	// the tree that is being evaluated
	private GPTree gpTree;


	// create a PSO object with a reference to the GPTree being tested 
	public PSO(GPTree gpTree) {
		this.gpTree = gpTree;
	}



	// evaluate the GPTree
	//	public MannWhitneyResult evalGPTree() {
	public FitnessResult evalGPTree() {
		//
		
		//		// get the parameters from the Parameters object that was created in TestFlockPSO
		int totalNumRuns = Parameters.numPSORuns;
		int functionNum = Parameters.functionNum;
		Topology topologyC = Parameters.topology;
		SelfModel selfModel = Parameters.selfModel;
		InfluenceModel influenceModel = Parameters.influenceModel;
		int numDimensions = Parameters.numDimensions;
		int numParticles = Parameters.numParticles;
		int numDummy = Parameters.dummyParticles;
		int numIterations = Parameters.numIterations;
		double printProb = Parameters.printFlockProb;
		
		
		//		// to save values for Mann Whitney tests, both the function values
		//		// and the distance in solution space from the location of the global optimum
		double[][] functionValues = new double[2][totalNumRuns];
		double[][] distanceFromGlobalOptimum = new double[2][totalNumRuns];
		//
		//
		//
		//
		
		//test
		Topology topology = Topology.GBEST;
		
		//		// run S-PSO and FLOCK-PSO
		for (int i = 0; i < 2; ++i) {
			//
			//			// do S-PSO, then FLOCK-PSO
			if (i == 0) {
				usingFLOCKPSO = false;
				topology = topologyC;
			}
			else if (i == 1) {
				usingFLOCKPSO = true;
				topology = Topology.FLOCK;
			}
			//
			//			// RUNS
			//			// ====
			
			//this double between 0 and 1 determines how if a flockData is output to a csv file
			double selectValue = Utilities.rand.nextDouble();
			
			for (int currentRunNum = 0 ; currentRunNum < totalNumRuns ; ++currentRunNum) {
				//              ////////
				//				// for each run generate a random shift of the location of the optimum in that function's search space
				double shiftVectorAmount = TestFunctions.SHIFT_RANGE[functionNum] * Utilities.rand.nextDouble();
				if (Utilities.rand.nextDouble() < 0.5) {
					shiftVectorAmount *= -1.0;
				}
				//
				//				// set the shift vector in the TestFunctions class
				TestFunctions.setShiftVector(functionNum, numDimensions, shiftVectorAmount);
				//
				//				// the location of the optimum is shifted and that location is stored in this
				//				// vector; it is initially set to the actual optimum location, then  shifted
				DoubleVector shiftedOptimumLocation = new DoubleVector(numDimensions, TestFunctions.OPT_COORD[functionNum]);
				shiftedOptimumLocation.addScalar(shiftVectorAmount);
				//
				//
				//				// create the swarm
				//				// numParticlesIndex is sent because it needs to be used in the creation of the vonNeumann
				//				// and Moore neighborhoods to access the arrays above that indicate the number of rows/cols
				if (!Parameters.ranRegularPSO || usingFLOCKPSO){
//					if(usingFLOCKPSO) {
//						topology = Topology.FLOCK;
//					}
//					else {
//						topology = Parameters.topology;
//					}
					
					//create a new swarm with these parameters ///SHOULD WE CHANGE TOPOLOGY TO FLOCK?????
					Swarm swarm = new Swarm (numParticles, functionNum, numDimensions, topology, selfModel, influenceModel, gpTree, numDummy);
					
					
					//
					//
					//				// ITERATIONS
					//				// ==========
					for (int iter = 0; iter < numIterations ; ++iter) {
						swarm.update(functionNum, topology, selfModel, influenceModel);
						
						//uncomment this to see what the neighborhoods look like in a FlockPSO run, very close to the end of the run
						//if (iter == (numIterations - 10) && currentRunNum == 0 && usingFLOCKPSO) {
							//System.out.println("Iteration: " + iter + "\n");
							//swarm.showAllNeighborhoods();
						//}
						
					}
					//if we select that probability or less, print out just one (in this case the first one) of the runs flocking
					if(selectValue < printProb && currentRunNum == 0) {
						//System.out.println("############## PRINTING FLOCK DATA TO FILE ##############");
						//swarm.printFlockData();
						
					}
					if(currentRunNum == 0) {
						Parameters.currFlockData = swarm.flockData;
					}
				}
				///////
				//
				//				// save final data
				if (!usingFLOCKPSO) { //regular PSO

					//if we've run the initial test
					if (Parameters.ranRegularPSO){
						functionValues[NON_FLOCK_PSO_DATA_INDEX][currentRunNum] = Parameters.regularPSOValue; //check
						distanceFromGlobalOptimum[NON_FLOCK_PSO_DATA_INDEX][currentRunNum] = Parameters.regularDistanceValue;// this hasn't been changed yet for the initial runs!!
						
//						double[] A = functionValues[NON_FLOCK_PSO_DATA_INDEX];
//						//		// get the second row
//						double[] B = functionValues[FLOCK_PSO_DATA_INDEX];
//						
//						double functionFlockSum = 0;
//						double functionNonFlockSum = 0;
//						
//						for(int j = 0; j < totalNumRuns; j++) {
//							functionFlockSum += A[j];
//							functionNonFlockSum += B[j];
//						}
//
//						//get the average values for the function and the distance from
//						//optimum for both flocking and non-flocking
//
//						double functionFlockAvg = functionFlockSum/totalNumRuns;
//						double functionNonFlockAvg = functionNonFlockSum/totalNumRuns;
						
					}
					else {
						functionValues[NON_FLOCK_PSO_DATA_INDEX][currentRunNum] = Swarm.globalBest.getFunctionValue();
						distanceFromGlobalOptimum[NON_FLOCK_PSO_DATA_INDEX][currentRunNum] = Swarm.globalBest.getPosition().distance(shiftedOptimumLocation);
						

					}
				}
				else {
					functionValues[FLOCK_PSO_DATA_INDEX][currentRunNum] = Swarm.globalBest.getFunctionValue();
					distanceFromGlobalOptimum[FLOCK_PSO_DATA_INDEX][currentRunNum] = Swarm.globalBest.getPosition().distance(shiftedOptimumLocation);
				}
				//
				//////////////////
				
				
				
			}  // END RUNS FOR-LOOP 
			//
			
			//most of this if statement is probably unnecessary
			if (!Parameters.ranRegularPSO){
				Parameters.ranRegularPSO = true;
				double[] A = functionValues[NON_FLOCK_PSO_DATA_INDEX];
				//		// get the second row
				double C[] = distanceFromGlobalOptimum[NON_FLOCK_PSO_DATA_INDEX];
				
				double functionNonFlockSum = 0;
				double distanceNonFlockSum = 0;
				
				for(int j = 0; j < totalNumRuns; j++) {
					functionNonFlockSum += A[j];
					distanceNonFlockSum += C[j];
				}

				//get the average values for the function and the distance from
				//optimum for both flocking and non-flocking

				double functionNonFlockAvg = functionNonFlockSum/totalNumRuns;
				double distanceNonFlockAvg = distanceNonFlockSum/totalNumRuns;
				
				Parameters.regularPSOValue = functionNonFlockAvg;
				Parameters.regularDistanceValue = distanceNonFlockAvg;
			}
			
		}
		//if we have run the initial regular PSO once, don't run them again
		//this is now done in FitnessResult
		

		//		// send the function value data and distance from global optimum location data
		//		// the method that does the Mann-Whitney tests; the tests for both function value
		//		// and distance are returned in a MannWhitneyResult object
		//		return mannWhitney(functionValues, distanceFromGlobalOptimum);
		return fitnessTest(functionValues, distanceFromGlobalOptimum);
	}
	
	
	//FitnessResult is basically the same thing as MannWhitneyResult, just with fewer things to keep track of
	//	// each of the arrays in the parameter list have two rows; the first row is results for the
	//	// S-PSO runs, and the second row is  results for the FLOCK-PSO runs
	//	// number of columns = the number of runs
	//	public MannWhitneyResult mannWhitney(double[][] functionValues, double[][] distanceFromGlobalOptimum) {
	public FitnessResult fitnessTest(double[][] functionValues, double[][] distanceFromGlobalOptimum) {
		//
		int totalNumRuns = Parameters.numPSORuns;

		//		// this will hold the results for both Mann-Whitney tests:
		//		//   1) for funciton values
		//		//   2) for distance in solution space from global optimum location
		FitnessResult FitResult = new FitnessResult();
		//		
		//		
		//		// MANN-WHITNEY TEST FOR FUNCTION VALUES
		//		
		//		// NON_FLOCK_PSO_DATA_INDEX = 0
		//		// FLOCK_PSO_DATA_INDEX = 1
		//		// get the first row
		double[] A = functionValues[NON_FLOCK_PSO_DATA_INDEX];
		//		// get the second row
		double[] B = functionValues[FLOCK_PSO_DATA_INDEX];	

		// NON_FLOCK_PSO_DATA_INDEX = 0
		//		// FLOCK_PSO_DATA_INDEX = 1
		//		// get the first row		
		double C[] = distanceFromGlobalOptimum[NON_FLOCK_PSO_DATA_INDEX];
		//		// get the second row
		double D[] = distanceFromGlobalOptimum[FLOCK_PSO_DATA_INDEX];	
		//
		//		// MannWhitneyTest (jsc)
		//		// this class is in the jsc library and holds the results of the
		//		// Mann-Whitney test performed
		//		MannWhitneyTest mwTestFuncVal = new MannWhitneyTest(A, B);

		double functionFlockSum = 0;
		double functionNonFlockSum = 0;
		double distanceFlockSum = 0;
		double distanceNonFlockSum = 0;

		// FitnessTest replaces MannWhitney test, for now it will be the
		// average function value error. Just make sure to set everything 
		// we need into the FitnessData class

		//sum everything up
		for(int i = 0; i < totalNumRuns; i++) {
			functionFlockSum += A[i];
			functionNonFlockSum += B[i];
			distanceFlockSum += C[i];
			distanceNonFlockSum += D[i];
		}

		//get the average values for the function and the distance from
		//optimum for both flocking and non-flocking

		double functionFlockAvg = functionFlockSum/totalNumRuns;
		double functionNonFlockAvg = functionNonFlockSum/totalNumRuns;
		
		

		double distanceFlockAvg = distanceFlockSum/totalNumRuns;
		double distanceNonFlockAvg = distanceNonFlockSum/totalNumRuns;
		
		//if we just ran regular PSO, set the value in parameters
		//if (!Parameters.ranRegularPSO) {
		//	Parameters.regularPSOValue = functionNonFlockAvg;
		//	Parameters.regularDistanceValue = distanceNonFlockAvg;
		//	Parameters.ranRegularPSO = true;
		//}


		//calculate the error of the flock algorithm to the non-flock algorithm
		double functionFlockError =  errorValue(functionNonFlockAvg, functionFlockAvg);
		double distanceFlockError =  errorValue(distanceNonFlockAvg, distanceFlockAvg);

		//make FitnessData objects to hold the error scores
		FitnessData fitDataFuncVal = new FitnessData();
		FitnessData fitDataDistGlobalOpt = new FitnessData();

		fitDataFuncVal.setFitScore(functionFlockError);
		fitDataDistGlobalOpt.setFitScore(distanceFlockError);

		//used for keeping track of best
		fitDataFuncVal.setU(functionNonFlockAvg);

		//replace mean rank with just mean for now
		if (functionNonFlockAvg < functionFlockAvg) {
			fitDataFuncVal.setFlockDidBetter(false);
		}
		else {
			fitDataFuncVal.setFlockDidBetter(true);
		}

		// put it in the FitnessResult object that gets returned
		FitResult.setFunctionValue(fitDataFuncVal);


		//replace mean rank with just mean for now
		if (distanceNonFlockAvg < distanceFlockAvg) {
			fitDataDistGlobalOpt.setFlockDidBetter(false);
		}
		else {
			fitDataDistGlobalOpt.setFlockDidBetter(true);
		}

		// put it in the FitnessResult object that gets returned
		FitResult.setDistGlobalOpt(fitDataDistGlobalOpt);




		// wont need this next section mostly

		//		// save various statistics computed by the MW test
		//		// the p-value is the one we care about
		//		double pValue = mwTestFuncVal.getSP();
		//		double U = mwTestFuncVal.getTestStatistic();
		//		double Z = mwTestFuncVal.getZ();
		//
		//		// put the Mann-Whitney results for the function values in a MannWhitneyData object; 
		//		// unclear why we can't just use the MannWhitneyTest object....
		//		MannWhitneyData mwDataFuncVal = new MannWhitneyData();
		//		mwDataFuncVal.setMWScore(pValue);
		//		mwDataFuncVal.setU(U);
		//		mwDataFuncVal.setZ(Z);
		//
		//		// NOTE: the p-value just tells us how significant the results is; now we need
		//		// to indicate who "won":
		//		// all the function values are sorted (in the jsc library; we don't do it)
		//		// the ranks for each of FLOCK-PSO and S-PSO are then averaged; lower is better
		//		double meanRankSPSOFuncVal = mwTestFuncVal.getRankSumA()/Parameters.numPSORuns;
		//		double meanRankFLOCKPSOFuncVal = mwTestFuncVal.getRankSumB()/Parameters.numPSORuns;
		//		mwDataFuncVal.setMeanRankSPSO(meanRankSPSOFuncVal);
		//		mwDataFuncVal.setMeanRankFLOCKPSO(meanRankFLOCKPSOFuncVal);


		// will need this

		//		if (meanRankSPSOFuncVal < meanRankFLOCKPSOFuncVal) {
		//			mwDataFuncVal.setFlockDidBetter(false);
		//		}
		//		else {
		//			mwDataFuncVal.setFlockDidBetter(true);
		//		}
		//
		//		// put it in the MannWhitneyResult object that gets returned
		//		mwResult.setFunctionValue(mwDataFuncVal);
		//		
		//


		//try to get this up in the first section, without the need for a second test		

		//		// MANN-WHITNEY TEST FOR DISTANCE IN SOLUTION SPACE FROM GLOBAL OPTIMUM LOCATION
		//		
		//		// NON_FLOCK_PSO_DATA_INDEX = 0
		//		// FLOCK_PSO_DATA_INDEX = 1
		//		// get the first row		
		//		A = distanceFromGlobalOptimum[NON_FLOCK_PSO_DATA_INDEX];
		//		// get the second row
		//		B = distanceFromGlobalOptimum[FLOCK_PSO_DATA_INDEX];	
		//
		//		// MannWhitneyTest (jsc)
		//		// this class is in the jsc library and holds the results of the
		//		// Mann-Whitney test performed
		//		MannWhitneyTest mwTestDistGlobalOpt = new MannWhitneyTest(A, B);
		//		// save various statistics computed by the MW test
		//		// the p-value is the one we care about
		//		pValue = mwTestDistGlobalOpt.getSP();
		//		U = mwTestDistGlobalOpt.getTestStatistic();
		//		Z = mwTestDistGlobalOpt.getZ();
		//
		//		// put the Mann-Whitney results for the function values in a MannWhitneyData object; 
		//		// unclear why we can't just use the MannWhitneyTest object....
		//		MannWhitneyData mwDataDistGlobalOpt = new MannWhitneyData();
		//		mwDataDistGlobalOpt.setMWScore(pValue);
		//		mwDataDistGlobalOpt.setU(U);
		//		mwDataDistGlobalOpt.setZ(Z);
		//
		//		// NOTE: the p-value just tells us how significant the results is; now we need
		//		// to indicate who "won":
		//		// all the function values are sorted (in the jsc library; we don't do it)
		//		// the ranks for each of FLOCK-PSO and S-PSO are then averaged; lower is better
		//		double meanRankSPSODistGlobalOpt = mwTestDistGlobalOpt.getRankSumA()/Parameters.numPSORuns;
		//		double meanRankFLOCKPSODistGlobalOpt = mwTestDistGlobalOpt.getRankSumB()/Parameters.numPSORuns;


		//will need this section		

		//		if (meanRankSPSODistGlobalOpt < meanRankFLOCKPSODistGlobalOpt) {
		//			mwDataDistGlobalOpt.setFlockDidBetter(false);
		//		}
		//		else {
		//			mwDataDistGlobalOpt.setFlockDidBetter(true);
		//		}
		//
		//		// put it in the MannWhitneyResult object that gets returned
		//		mwResult.setDistGlobalOpt(mwDataDistGlobalOpt);
		//		
		//		
		return FitResult;
		//
	}
	//



	// returns the percent error of the flocking PSO algorithm against 
	// regular PSO algorithm. Will be between 0 and 1 if the flocking
	// algorithm is better, negative if not. Greater numbers are more fit
	public double errorValue(double FlockAvg, double nonFlockAvg) { //original order: double nonFlockAvg, double FlockAvg
		//nonFlockAvg = 10; //if we want to set consistent fitnesses
		System.out.println("RegularPSO: "+ nonFlockAvg+ " Flock PSO: "+ FlockAvg);
		
		double fitnessError = (nonFlockAvg - FlockAvg)/nonFlockAvg;	
		//double fitnessError = (nonFlockAvg/FlockAvg) - (FlockAvg/nonFlockAvg);
		
		
		
		
		return fitnessError;
	}




}

