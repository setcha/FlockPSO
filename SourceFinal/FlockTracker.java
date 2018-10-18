//

//This class stores the x position, y position, and radius, in flocking space, of every particle.
//xData, yData, and radiusData are 2D arrays of doubles that, for every iteration, have the particle's data 
//for that dimension (i.e. xData[iteration][particleID] gives an x-positions)

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class FlockTracker {

	//stores the positional data for a particle
	//format is [iterations][particleID]
	private double[][] xData;
	private double[][] yData;
	
	//currently, flockParameters just stores the 11 flock parameters for a SINGLE particle, not every parameter for every particle as it probably should
	private double[][] flockParameters;
	
	//stores the radius for every particle at every iteration
	private double[][] radiusData;
	
	//Also stores flockParameters as discrete values, temporarily
	private int numParticles;
	private int dummyParticles;
	private int totalParticles;
	private int iterations;
	private int currentIteration;
	private int generation;
	private int numFlockParameters = 11;

	//basic constructor, everything is null however
	public FlockTracker(){
		xData = null;
		yData = null;
		flockParameters = null;
		radiusData = null;
		numParticles = 0;
		dummyParticles = 0;
		iterations = 0;
		currentIteration = 0;
		generation = 0;
	}

	//Full constructor
	public FlockTracker(int iterations, int numParticles, int dummyParticles, int numFlockParameters, int generation){

		xData = new double[iterations][numParticles + dummyParticles];
		yData = new double[iterations][numParticles + dummyParticles];
		radiusData = new double [iterations][numParticles + dummyParticles];
		flockParameters = new double[iterations][numFlockParameters];
		this.numParticles = numParticles;
		this.dummyParticles = dummyParticles;
		this.totalParticles = numParticles + dummyParticles;
		this.iterations = iterations;
		this.currentIteration = 0;
		this.generation = generation;
		this.numFlockParameters = numFlockParameters;

	}
	
	
	//Add a particles data. Used totalParticles times every iteration of flock movement
	public void addParticle(Particle particle) {													//should probably be just regular radius
		addPosition(particle.getCurrLocation().get(0), particle.getCurrLocation().get(1), particle.getRadius(), particle.getParticleID());
		double[] flockParam = particle.getParameters();	
		addFlockParameters(flockParam[0], flockParam[1], flockParam[2], flockParam[3], flockParam[4], flockParam[5], flockParam[6], flockParam[7], flockParam[8], flockParam[9]);
	}

	
	//Add the position given to particleID's data for this current iteration. Called every time addParticle is called
	public void addPosition(double x, double y, double radius, int particleID) {
		xData[currentIteration][particleID] = x;
		yData[currentIteration][particleID] = y;
		radiusData[currentIteration][particleID] = radius;
	}
	
	
	//If we need to change the radius of a particle in the current iteration, we can use this
	public void changeRadius(double radius, int particleID) {
		radiusData[currentIteration][particleID] = radius;
	}
	
	
	//add Flock Parameters the tedious way: one at a time. Called every time add particle is called.
	public void addFlockParameters(
			double maxSpeed,                
			double normalSpeed,         
			double neighborRadius,             
			double separationWeight,       
			double cohesionWeight,         
			double alignmentWeight,         
			double pacekeepingWeight,       
			double randomMotionProb,
			double numNeighborsOwnFlock,
			double numNeighborsAllFlocks) {

		flockParameters[currentIteration][0] = maxSpeed;
		flockParameters[currentIteration][1] = normalSpeed;
		flockParameters[currentIteration][2] = neighborRadius;
		flockParameters[currentIteration][3] = separationWeight;
		flockParameters[currentIteration][4] = cohesionWeight;
		flockParameters[currentIteration][5] = alignmentWeight;
		flockParameters[currentIteration][6] = pacekeepingWeight;
		flockParameters[currentIteration][7] = randomMotionProb;
		flockParameters[currentIteration][8] = numNeighborsOwnFlock;
		flockParameters[currentIteration][9] = numNeighborsAllFlocks;

	}

	
	//avgNeighbors isn't a flock parameter that the tree changes, but it might be something we want to monitor. 
	//This is called in Flock.java, since that is where we can see all of the neighbors for every particle.
	public void addAvgNeighbors(double avgNeighbors) {
		flockParameters[currentIteration][10] = avgNeighbors;
	}

	
	public void incrementIteration() {
		currentIteration++;
	}

	
	
	//Creates a new .csv file, BUT ONLY IF THERE IS A flockPositionData FOLDER IN THE SAME DIRECTORY.
	//Outputs xData first, the yData on the same line, then radiusData on the same line
	//then it moves to the next line to print the next iteration data
	public void outputDataCSV() {

		//PrintWriter outputWindow = new PrintWriter(System.out);
		try {
			//System.out.println("## Made it to FlockTracker.java");
			// create date string for screen output and output file names
			SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd--hh.mm.ss.SSS-a");
			Calendar date = Calendar.getInstance();
			String dateString = dateformatter.format(date.getTime());

			//outputWindow.print("STARTING RUN ON " + dateString + "\n\n");

			// create a PrintWriter that sends things to a file
			// NOTE: there needs to be "flockPositionData" folder on the same level as the "src" folder
			// before the program is run; Java will not create it
			//PrintWriter outputWindow = new PrintWriter(System.out);
			PrintWriter flockFile = new PrintWriter(new FileWriter("flockPositionData/" + dateString + "K" + Parameters.runKey + ".csv"));
			
			//flockFile.printf("0, 0, 0, 0, 0, 0, 0, 0, 0, 0, %5d, %5d, %5d, %5d\n",
					//iterations,
					//numParticles,
					//dummyParticles,
					//generation);
			//
			//output the data for every iteration
			for (int i = 0; i < iterations; i++) {

				//output parameters here
				//for(int k = 0; k < numFlockParameters; k++) {
					//flockFile.printf("%.4f,", flockParameters[i][k]);
					//outputWindow.printf("%.4f,", flockParameters[i][k]);
				//}

				//output x position first
				for(int j = 0; j < totalParticles; j++) {
					flockFile.printf("%.4f,", xData[i][j]);	
					//outputWindow.printf("%.4f,", xData[i][j]);	
				}

				//then output y data
				for(int m = 0; m < totalParticles; m++) {
					flockFile.printf("%.4f,", yData[i][m]);
					//outputWindow.printf("%.4f,", yData[i][m]);
				}
				
				for(int n = 0; n < totalParticles; n++) {
					flockFile.printf("%.4f,", radiusData[i][n]);
					//outputWindow.printf("%.4f,", yData[i][m]);
				}
				//outputWindow.print("\n");
				flockFile.print("\n");	
			}



			date = Calendar.getInstance();
			dateString = dateformatter.format(date.getTime());
			//outputWindow.print("\nFINISHED OUTPUT ON " + dateString + "\n");	
			//outputWindow.flush();

			//flockFile.println("# \n# FINISHED RUN ON " + dateString + "\n");	
			flockFile.flush();
			flockFile.close();
		}
		catch (Exception e) {
			System.out.println("Error in outputDataCSV(), FlockTracker.java");
			e.printStackTrace();
		}
	}
	
}
