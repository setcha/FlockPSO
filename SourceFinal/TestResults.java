/*
 * there is one TestResults object for a run, holding information
 * on every individual in every generation
 * this can then be used to generate desired statistics:
 * 	- mean fitness for each generation
 * 	- median fitness for each generation
 * 	- minimum fitness for each generation
 * 	- maximum fitness for each generation
 * AND, for each individual:
 * 	- generation in which max fitness achieved
 * 	- the value of that fitness
 */


import java.util.Arrays;
import java.io.PrintWriter;

public class TestResults {

	private int numGenerations;
	private int populationSize;
	private double[][] fitnesses;
	private double[] generationMin;
	private double[] generationMax;
	private double[] generationMean;
	private double[] generationMedian;
	
	private int[] individMaxGenNum;
	private double[] individMaxGenFitness;

	public TestResults(int numGenerations, int populationSize) {
		this.numGenerations = numGenerations;
		this.populationSize = populationSize;
		fitnesses = new double[numGenerations][populationSize];
		generationMin = new double[numGenerations];
		generationMax = new double[numGenerations];
		generationMean = new double[numGenerations];
		generationMedian = new double[numGenerations];
		
		individMaxGenNum = new int[populationSize];
		individMaxGenFitness = new double[populationSize];
	}

	
	public void calcStats() {
		calcMeans();
		calcMediansMinMax();
		calcIndividMaxGen();
	}
	

	
	// for each generation
	private void calcMeans() {
		
		for (int i = 0; i < numGenerations; ++i) {
			double genSum = 0.0;
			for (int j = 0; j < populationSize; j++) {
				genSum += fitnesses[i][j];
			}
			generationMean[i] = genSum / Parameters.numTrees;
		}
	}

	
	// for each generation
	private void calcMediansMinMax() {
		
		// need copy so sorting does not permanently affect order of
		// individuals in a generation
		double[][] fitnessesCopy = new double[numGenerations][populationSize];
		for (int i = 0; i < numGenerations; ++i) {
			for (int j = 0; j < populationSize; j++) {
				fitnessesCopy[i][j] = fitnesses[i][j];
			}
		}

		// once sorted, median, min, and max are easy
		for (int i = 0; i < numGenerations; ++i) {
			double[] genFitnesses = fitnessesCopy[i];
			Arrays.sort(genFitnesses);
			double median;
			// if there are an even number of fitnesses, there is not a single median;
			// there are several ways to handle this -- one frequently used one is to take
			// the average of the two "center" items, as is done here
			if (genFitnesses.length % 2 == 0) {
				median = (genFitnesses[populationSize/2] + genFitnesses[populationSize/2 - 1]) / 2.0;
			}
			else {
				median = genFitnesses[populationSize/2];
			}
			generationMedian[i] = median;

			generationMin[i] = genFitnesses[0];
			generationMax[i] = genFitnesses[populationSize-1];
		}
	}
	
	// for each individual, calculate gen in which max fitness attained, and the value of the max
	private void calcIndividMaxGen() {
		

		for (int j = 0; j < populationSize; j++) {
			int maxGenNum = 0;
			double maxGenFitness = fitnesses[0][j];
			for (int i = 1; i < numGenerations; ++i) {
				if (fitnesses[i][j] > maxGenFitness) {
					maxGenNum = i;
					maxGenFitness = fitnesses[i][j];
				}
			}
			individMaxGenNum[j] = maxGenNum;
			individMaxGenFitness[j] = maxGenFitness;
		}
		
	}
	
	
	// can output to screen or file, depending on the PrintWriter sent to it
	public void outputData(PrintWriter dataFile) {
		
		dataFile.println("# Fitnesses");
		dataFile.println("# row = generations, col = individuals");
		dataFile.println("# ");
		
		dataFile.print("# ");
		dataFile.print("           ");
		for (int j = 0; j < populationSize; j++) {
			dataFile.printf("  %5d  ", j);
		}
		dataFile.printf("    Min  ");
		dataFile.printf("    Max  ");
		dataFile.printf("   Mean  ");
		dataFile.printf("  Median ");
		dataFile.println();
		
		for (int i = 0; i < numGenerations; ++i) {
			dataFile.printf("        %5d", i);
			for (int j = 0; j < populationSize; j++) {
				dataFile.printf("  %7.3f", fitnesses[i][j]);
			}
			dataFile.printf("  %7.3f", generationMin[i]);
			dataFile.printf("  %7.3f", generationMax[i]);
			dataFile.printf("  %7.3f", generationMean[i]);
			dataFile.printf("  %7.3f", generationMedian[i]);
			dataFile.println();
		}

		dataFile.print("# Max Fitness");
		for (int j = 0; j < populationSize; j++) {
			dataFile.printf("  %7.3f", individMaxGenFitness[j]);
		}
		dataFile.println();
		
		dataFile.println("# ");
		dataFile.print("# Boid ID    ");
		for (int j = 0; j < populationSize; j++) {
			dataFile.printf("  %7d", individMaxGenNum[j]);
		}
		dataFile.println();
		
	}
	
	// getters and setters
	
	public int getNumGenerations() {
		return numGenerations;
	}

	public void setNumGenerations(int numGenerations) {
		this.numGenerations = numGenerations;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public double getFitness(int generation, int individual) {
		return fitnesses[generation][individual];
	}

	public void setFitness(int generation, int individual, double fitness) {
		fitnesses[generation][individual] = fitness;
	}


	
}
