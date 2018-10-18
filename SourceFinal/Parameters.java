/* 
 * There are a ton of parameters:
 * 	- for the Genetic Program
 * 	- for the trees that the GP is operating on
 * 	- for the PSO
 * This provides one location for all of them so that we don't have to hint
 * through the code to find them when we want to change them.
 * 
 */

import java.io.PrintWriter;

public class Parameters {

	// GP Parameters
	public static int numGens = 10; // 100; //2
	public static int numTrees = 10; // 20; //5
	
	// the raw fitness interval is 0.0 to 2.0, so differences between fitnesses will
	// be small; this expands the interval to 0.0 to 200.0 to allow bigger
	// fitness differences 
	public static double fitnessInterval = 200.0;
	
	// self-explanatory
	public static int numPSORuns = 50; //50;//20         // number of runs in PSO when evaluating fitness
	public static double probCrossover = 0.7; 
	public static int maxCrossoverTries = 10; // 37; 	// max number of times to try to find compatible nodes for crossover
	public static double probMutation = 0.05; 
	public static int eliteNumber = 4; //how many of the top performing individuals pass on unchanged to the next generation
	
	// Tree Creation Parameters
	// constants are restricted to a given interval, but there doesn't seem to be any good reason to do so
	public static double minConstant = 0.0;
	public static double maxConstant = 100.0;
	
	// we don't want huge trees, so limit the depth; another way to do this would be to allow any
	// depth put lower the fitness of large trees
	public static int maxTreeDepth = 10; //5 to 10?
	
	// some tree nodes represent a sequence of operations, a child for each one, e.g.
	// increase the value of variable1, then decrease the value of variable2;
	// a sequence of length 1 is pointless, but we want to restrict the length to avoid large trees
	public static int minSequenceLength = 2;
	public static int maxSequenceLength = 6;
	public static int headerLength = 8; //8 variables to initialize
	

	// PSO Parameters
	// for testing random parameter changes; hopefully they are worse than our evolved "programs"
	public static boolean doRandomTree = false;
	
	// right now, we can only test one function at a time; may want to change this
	public static int functionNum = TestFunctions.ACKLEY_FUNCTION_NUM;//ACKLEY_FUNCTION_NUM;//RASTRIGIN_FUNCTION_NUM;
	
	// ditto for topology, etc. of the PSO
	public static PSO.Topology topology = PSO.Topology.GBEST;//MOORE;//GBEST;////
	public static PSO.SelfModel selfModel = PSO.SelfModel.INCLUDE_SELF;
	public static PSO.InfluenceModel influenceModel = PSO.InfluenceModel.NEIGH_BEST;
	
	// number dimensions and particles will likely remain fixed at 30 and 30, which are
	// standard enough values for PSO tests that we can just test them
	public static int numDimensions = 30;
	public static int numParticles = 30;
	
	public static int FLOCK_SPACE_DIM_LENGTH = 300;
	
	//how many dummy particles do we want per real particle, to help with flocking behavior
	public static int dummyParticles = 0; 
	//the total number of particles is used sometimes
	public static int totalParticles = numParticles + dummyParticles;
	
	// need to know how to set up a von Neumann or Moore neighborhood
	public static int numRowsVonNeumannAndMoore = 5;
	public static int numColsVonNeumannAndMoore = 6;
	
	public static int numIterations = 100;
    
	//Parameters has also become a sort of information sharing platform between different parts of the program
	
    //initial PSO run data
    public static boolean ranRegularPSO = false;
    public static double regularPSOValue = 100000; //just a large number, should probably be max_int or similar
    public static double regularDistanceValue = 100000; //just a large number, should probably be max_int or similar
    
    //used to store the best trees and 
    public static double bestFitness = -100;
    public static double bestValue = 100000;
    public static GPTree bestTree;
    
    //A random integer used to differentiate between different runs, in case they are started at the exact same time. 
    //We don't want to overwrite a file, so this makes the files different. Also used to tell flock
    public static int runKey = 0;
    
    //determines how often we save a flock swarm to csv, so that we can look at it later (used by PSO.java)
    public static double printFlockProb = 0.0002; 
    
    public static FlockTracker currFlockData = new FlockTracker();
    
    //public static int globalSeed = 0;
    
    
	public Parameters() {
		
	}
	
	public static void changeGPParameters(int numGens, int numTrees, int numPSORuns, double fitnessInterval, double probCrossover, 
										int maxCrossoverTries, double probMutation) {
		Parameters.numGens = numGens;
		Parameters.numTrees = numTrees;

		Parameters.numPSORuns = numPSORuns;

		Parameters.fitnessInterval = fitnessInterval;
		Parameters.probCrossover = probCrossover;
		Parameters.maxCrossoverTries = maxCrossoverTries;
		Parameters.probMutation = probMutation;

	}

	public static void changeTreeCreatonParameters(double minConstant, double maxConstant, int maxTreeDepth, int minSequenceLength, int maxSequenceLength) {

		Parameters.minConstant = minConstant;
		Parameters.maxConstant = maxConstant;
		Parameters.maxTreeDepth = maxTreeDepth;
		Parameters.minSequenceLength = minSequenceLength;
		Parameters.maxSequenceLength = maxSequenceLength;
	}


	public static void changePSOParameters(boolean doRandomTree, int functionNum, PSO.Topology topology, PSO.SelfModel selfModel, PSO.InfluenceModel influenceModel,
											int numDimensions, int numParticles, int numRowsVonNeumannAndMoore, int numColsVonNeumannAndMoore, int numIterations) {

		Parameters.doRandomTree = doRandomTree;
		Parameters.functionNum = functionNum;
		Parameters.topology = topology;
		Parameters.selfModel = selfModel;
		Parameters.influenceModel = influenceModel;
		Parameters.numDimensions = numDimensions;
		Parameters.numParticles = numParticles;
		Parameters.numRowsVonNeumannAndMoore = numRowsVonNeumannAndMoore;
		Parameters.numColsVonNeumannAndMoore = numColsVonNeumannAndMoore;
		Parameters.numIterations = numIterations;
	}
	
	public void outputParameters(PrintWriter pw, boolean isFile) {
		
		// if it's a file, we want to start each of these lines with a # sign so that
		// gnuplot will ignore them
		if (isFile) {
			pw.println("# GP Parameters");
			pw.println("# numGens: " + numGens);
			pw.println("# numTrees: " + numTrees);
			pw.println("# numPSORuns: " + numPSORuns);
			pw.println("# fitnessInterval:" + fitnessInterval);
			pw.println("# probCrossover: " + probCrossover);
			pw.println("# maxCrossoverTries: " + maxCrossoverTries);
			pw.println("# probMutation: " + probMutation);
			pw.println("#");

			pw.println("# Tree Creation Parameters");
			pw.println("# minConstant: " + minConstant);
			pw.println("# maxConstant: " + maxConstant);
			pw.println("# maxTreeDepth: " + maxTreeDepth);
			pw.println("# minSequenceLength: " + minSequenceLength);
			pw.println("# maxSequenceLength: " + maxSequenceLength);
			pw.println("#");

			pw.println("# PSO Parameters");
			pw.println("# doRandomTree: " + doRandomTree);
			pw.println("# functionNum: " + functionNum);
			pw.println("# topology: " + topology);
			pw.println("# selfModel: " + selfModel);
			pw.println("# influenceModel: " + influenceModel);
			pw.println("# numDimensions: " + numDimensions);
			pw.println("# numParticles: " + numParticles);
			pw.println("# dummyParticles: " + dummyParticles);
			pw.println("# FLOCK_SPACE_DIM_LENGTH: " + FLOCK_SPACE_DIM_LENGTH);
			pw.println("# numRowsVonNeumannAndMoore: " + numRowsVonNeumannAndMoore);
			pw.println("# numColsVonNeumannAndMoore: " + numColsVonNeumannAndMoore);
			pw.println("# numIterations: " + numIterations);
			pw.println("#");
		}

		// otherwise it's outputting to the screen and we'd rather not have to look
		// at the # signs
		else {
			pw.println("GP Parameters");
			pw.println("numGens: " + numGens);
			pw.println("numTrees: " + numTrees);
			pw.println("numPSORuns: " + numPSORuns);
			pw.println("fitnessInterval:" + fitnessInterval);
			pw.println("probCrossover: " + probCrossover);
			pw.println("maxCrossoverTries: " + maxCrossoverTries);
			pw.println("probMutation: " + probMutation);
			pw.println("");

			pw.println("Tree Creation Parameters");
			pw.println("minConstant: " + minConstant);
			pw.println("maxConstant: " + maxConstant);
			pw.println("maxTreeDepth: " + maxTreeDepth);
			pw.println("minSequenceLength: " + minSequenceLength);
			pw.println("maxSequenceLength: " + maxSequenceLength);
			pw.println("");

			pw.println("PSO Parameters");
			pw.println("doRandomTree: " + doRandomTree);
			pw.println("functionNum: " + functionNum);
			pw.println("topology: " + topology);
			pw.println("selfModel: " + selfModel);
			pw.println("influenceModel: " + influenceModel);
			pw.println("numDimensions: " + numDimensions);
			pw.println("numParticles: " + numParticles);
			pw.println("dummyParticles: " + dummyParticles);
			pw.println("FLOCK_SPACE_DIM_LENGTH: " + FLOCK_SPACE_DIM_LENGTH);
			pw.println("numRowsVonNeumannAndMoore: " + numRowsVonNeumannAndMoore);
			pw.println("numColsVonNeumannAndMoore: " + numColsVonNeumannAndMoore);
			pw.println("numIterations: " + numIterations);
			pw.println("");
		}
		
		pw.flush();		
	}
	
	// getters and setters
	
	public static int getNumGens() {
		return numGens;
	}


	public static void setNumGens(int numGens) {
		Parameters.numGens = numGens;
	}


	public static int getNumTrees() {
		return numTrees;
	}


	public static void setNumTrees(int numTrees) {
		Parameters.numTrees = numTrees;
	}


	public static double getFitnessInterval() {
		return fitnessInterval;
	}


	public static void setFitnessInterval(double fitnessInterval) {
		Parameters.fitnessInterval = fitnessInterval;
	}


	public static int getNumPSORuns() {
		return numPSORuns;
	}


	public static void setNumPSORuns(int numPSORuns) {
		Parameters.numPSORuns = numPSORuns;
	}


	public static double getProbCrossover() {
		return probCrossover;
	}


	public static void setProbCrossover(double probCrossover) {
		Parameters.probCrossover = probCrossover;
	}


	public static int getMaxCrossoverTries() {
		return maxCrossoverTries;
	}


	public static void setMaxCrossoverTries(int maxCrossoverTries) {
		Parameters.maxCrossoverTries = maxCrossoverTries;
	}


	public static double getProbMutation() {
		return probMutation;
	}


	public static void setProbMutation(double probMutation) {
		Parameters.probMutation = probMutation;
	}


	public static double getMinConstant() {
		return minConstant;
	}


	public static void setMinConstant(double minConstant) {
		Parameters.minConstant = minConstant;
	}


	public static double getMaxConstant() {
		return maxConstant;
	}


	public static void setMaxConstant(double maxConstant) {
		Parameters.maxConstant = maxConstant;
	}


	public static int getMaxTreeDepth() {
		return maxTreeDepth;
	}


	public static void setMaxTreeDepth(int maxTreeDepth) {
		Parameters.maxTreeDepth = maxTreeDepth;
	}


	public static int getMinSequenceLength() {
		return minSequenceLength;
	}


	public static void setMinSequenceLength(int minSequenceLength) {
		Parameters.minSequenceLength = minSequenceLength;
	}


	public static int getMaxSequenceLength() {
		return maxSequenceLength;
	}


	public static void setMaxSequenceLength(int maxSequenceLength) {
		Parameters.maxSequenceLength = maxSequenceLength;
	}


	public static int getFunctionNum() {
		return functionNum;
	}


	public static void setFunctionNum(int functionNum) {
		Parameters.functionNum = functionNum;
	}


	public static PSO.Topology getTopology() {
		return topology;
	}


	public static void setTopology(PSO.Topology topology) {
		Parameters.topology = topology;
	}


	public static PSO.SelfModel getSelfModel() {
		return selfModel;
	}


	public static void setSelfModel(PSO.SelfModel selfModel) {
		Parameters.selfModel = selfModel;
	}


	public static PSO.InfluenceModel getInfluenceModel() {
		return influenceModel;
	}


	public static void setInfluenceModel(PSO.InfluenceModel influenceModel) {
		Parameters.influenceModel = influenceModel;
	}


	public static int getNumDimensions() {
		return numDimensions;
	}


	public static void setNumDimensions(int numDimensions) {
		Parameters.numDimensions = numDimensions;
	}


	public static int getNumParticles() {
		return numParticles;
	}


	public static void setNumParticles(int numParticles) {
		Parameters.numParticles = numParticles;
	}


	public static int getNumRowsVonNeumannAndMoore() {
		return numRowsVonNeumannAndMoore;
	}


	public static void setNumRowsVonNeumannAndMoore(int numRowsVonNeumannAndMoore) {
		Parameters.numRowsVonNeumannAndMoore = numRowsVonNeumannAndMoore;
	}


	public static int getNumColsVonNeumannAndMoore() {
		return numColsVonNeumannAndMoore;
	}


	public static void setNumColsVonNeumannAndMoore(int numColsVonNeumannAndMoore) {
		Parameters.numColsVonNeumannAndMoore = numColsVonNeumannAndMoore;
	}


	public static int getNumIterations() {
		return numIterations;
	}


	public static void setNumIterations(int numIterations) {
		Parameters.numIterations = numIterations;
	}



}
