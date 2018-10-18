
//import java.util.Random;


public class Swarm {

	public static Particle[] particles;  
	// holds the neighborhoods, one for each particle
	private static Neighborhood[] allNeighs;
	public static Solution globalBest;
	//FlockTracker to keep track of where every particle is in flock space at every iteration
	public static FlockTracker flockData;
	public static int numParticles;
	// the same GPTree is used by each particle to change the flock parameters,
	// so it is a static variable here that is referenced at the end of the 
	// moveFlockAndGetNeighbors method in the Particle class
	public static GPTree gpTree;

	//add a new parameter, numDummy
	public Swarm (int numParticles, 
			int functionNum, 
			int numDimensions, 	
			PSO.Topology currentPSOTopology, 
			PSO.SelfModel currentPSOSelfModel, 
			PSO.InfluenceModel currentPSOInfluenceModel,
			GPTree gpTree,
			int numDummy) {
		
		
		
		this.gpTree = gpTree;
		this.numParticles = numParticles;
		// create arrays to hold particle and neighborhoods
		particles = new Particle[numParticles + numDummy];
		//dummyParticles = new Particle[numParticles];
		
		allNeighs = new Neighborhood[numParticles];		

		// array needed to get back function evaluation results from Particle constructor
		double[] initParticleData = new double[2];    

		//Parameters.___ should be temporary until I can pass it in from PSO (same with 11 flocking parameters)
		flockData = new FlockTracker(Parameters.getNumIterations(), numParticles, Parameters.dummyParticles, 11, gpTree.generation);
		
		//Parameters.globalSeed = (int) gpTree.getRoot().getConstValue();
		//System.out.println(Parameters.globalSeed);
		
		// create the first particle, using randomly generated flock parameters
		
		double[] randParameters = generateRandomParameters();
		particles[0] = new Particle(functionNum, numDimensions, 0, 
				randParameters[0], randParameters[1], randParameters[2], randParameters[3],
				randParameters[4], randParameters[5], randParameters[6], randParameters[7],
				initParticleData, false); //false means it is not a dummy particle
		
		
		
		// first one is the current global best
		int globalBestParticleNum = 0;
		double globalBestValue = initParticleData[TestFunctions.VAL_INDEX];
		double globalBestError = initParticleData[TestFunctions.ERR_INDEX];

		// getPosition does not return a copy, but that's okay because Solution constructor makes 
		// a copy of the DoubleVector sent in
		// NOTE: 0 is the iteration found
		globalBest = new Solution(particles[0].getPosition(), globalBestValue, globalBestError, globalBestParticleNum);

		boolean isDummy = false;
		// now make the rest of the particles
		for (int particleID = 1 ; particleID < particles.length ; particleID++) {

			// NOTE: Every particle starts out with the same random flocking parameters,
			// but the GPTree program is called by each particle separately and will change
			// the parameters for a given particle in ways that are different from the
			// other particles
			
			if (particleID >= numParticles) {
				isDummy = true;
			}
			else {
				isDummy = false;
			}
					
			particles[particleID] = new Particle(functionNum, numDimensions, particleID, 
					randParameters[0], randParameters[1], randParameters[2], randParameters[3],
					randParameters[4], randParameters[5], randParameters[6], randParameters[7],
					initParticleData, isDummy);
			
			
			// reset global best, if necessary
			
			if(!isDummy) { //if it is a regular particle, set the particle best error and value and such
				double particleValue = initParticleData[TestFunctions.VAL_INDEX];
				double particleError = initParticleData[TestFunctions.ERR_INDEX];
				if (particleValue < globalBest.getFunctionValue()) {  
					globalBest.copyFromPosition(particles[particleID].getPosition());
					globalBest.setFunctionValue(particleValue);
					globalBest.setError(particleError);
					globalBest.setParticleID(particleID);
				}
			}
		}

		// if we are using S-PSO, the neighborhoods do not change, so we will create 
		// them once and for all now
		if (!PSO.usingFLOCKPSO) {
			createNeighborhoods(currentPSOTopology, currentPSOSelfModel, currentPSOInfluenceModel);
		}

		/////////
		if (PSO.usingFLOCKPSO) {
			createNeighborhoods(PSO.Topology.FLOCK, currentPSOSelfModel, currentPSOInfluenceModel);
		}
		//create neighborhoods for FlockPSO here too?

	}


	// call update on every particle and reset global best, if necessary
	public void update (int currentFunctionNum, PSO.Topology currentPSOTopology, PSO.SelfModel currentPSOSelfModel, 
			PSO.InfluenceModel currentPSOInfluenceModel) {

		for (int particleID = 0 ; particleID < particles.length ; particleID++) {

			//have to update the dummy swarm separately because dummy particles don't have neighborhoods
			if (particles[particleID].getIsDummy() || particleID >= numParticles) {
				//particles[particleID].testFlockPattern();
				particles[particleID].updateDummyFlock();
			}
			else {
				//particles[particleID].testFlockPattern();
				//Solution nextPBestFuncValSol = particles[particleID].getPersonalBest();
				
				//test
				//System.out.println("Before: " + nextPBestFuncValSol.getFunctionValue());
				Solution newSolution = particles[particleID].update(currentPSOTopology, currentPSOSelfModel, currentPSOInfluenceModel);  
				//System.out.println("After: " + nextPBestFuncValSol.getFunctionValue());
				
				double newParticleValue = newSolution.getFunctionValue();      
				//update neighborhood here too
				allNeighs[particleID] = particles[particleID].getNeighborhood();


				if (newParticleValue < globalBest.getFunctionValue()) {
					globalBest.copyFromPosition(newSolution.getPosition());
					globalBest.setFunctionValue(newParticleValue);
					globalBest.setError(newSolution.getError());
					globalBest.setParticleID(particleID);
				} 
			}
			flockData.addParticle(particles[particleID]);
			//flockData.changeRadius(particles[particleID].internalNeighRadius, particleID);
		}

		//insert "add avgNeighbors here"
		flockData.addAvgNeighbors(avgNeighborhoods());
		//increment iteration of flockTracker here
		flockData.incrementIteration();
	}
	//}   


	// generate random flock parameters
	public static double[] generateRandomParameters() {
		double maxSpeed = Utilities.nextDouble(2.0, 10.0);         		// range = [2.0, 10.0]
		double normalSpeed = Utilities.nextDouble(1.0, maxSpeed);  		// range = [1.0, maxSpeed]
		double neighborRadius = Utilities.nextDouble(10.0, 100.0); 		// range = [10.0, 100.0]
		double separationWeight = Utilities.nextDouble(0.0, 100.0);		// range = [0.0, 100.0]
		double alignmentWeight = Utilities.nextDouble(0.0, 1.0);   		// range = [0.0, 1.0]
		double cohesionWeight = Utilities.nextDouble(0.0, 1.0);    		// range = [0.0, 1.0]
		double pacekeepingWeight = Utilities.nextDouble(0.0, 1.0);		// range = [0.0, 1.0]
		double randomMotionProbability = Utilities.nextDouble(0.0, 5.0);// range = [0.0, 0.5]

		double[] parameters =
			{ maxSpeed, 
					normalSpeed,
					neighborRadius, 
					separationWeight,
					cohesionWeight, 
					alignmentWeight,
					pacekeepingWeight, 
					randomMotionProbability };

		return parameters;
	}



	// create a neighborhood for each particle	
	public void createNeighborhoods(PSO.Topology currentTopology, PSO.SelfModel currentSelfModel, PSO.InfluenceModel currentInfluenceModel) {

		// this is bad for GBEST, because it's going to create a distinct Neighborhood
		// object for every particle, in spite of the fact that there is only one neighborhood...
		// but go with it for now

		for (int particleID = 0 ; particleID < particles.length ; particleID++) {

			//here maybe?
			// create the actual neighborhood containing list of references to Particles in neighborhood
			Neighborhood neigh = new Neighborhood(particles, particleID, currentTopology, currentSelfModel, currentInfluenceModel);
			
			if(!particles[particleID].getIsDummy()) {// if it is not a dummy particle
				particles[particleID].setNeighborhood(neigh);
				//System.out.println("Neighborhood size: " + particles[particleID].getNeighborhood().getNeighParticles().length);
			}
			
			
		}

	}



	// show the IDs of the particles in each neighborhood
	public void showAllNeighborhoods () {
		for (int n = 0 ; n < allNeighs.length ; ++n) {
			showNeighborhood(allNeighs[n]);
		}
	}

	// show  the IDs of the particles in a given neighborhood
	public static void showNeighborhood (Neighborhood neigh) {
		if(neigh.getNeighParticles() == null) {
			System.out.println("neighborhoodID = " + neigh.getNeighID() + " includes particles:");
			System.out.println("null");
		}
		else {
			Particle[] particlesInNeighborhood = neigh.getNeighParticles();

			System.out.println("neighborhoodID = " + neigh.getNeighID() + " includes particles:");
			for (int i = 0 ; i < particlesInNeighborhood.length ; i++) {
				if(particlesInNeighborhood[i] == null) {
					System.out.println("null");
				}
				else {
					System.out.println("particleID = " + particlesInNeighborhood[i].getParticleID());
				}
			}
		}
	}

	//Get the average neighbors for all particles. Can be helpful to compare to what other papers have found is the optimal number of neighbors
	public double avgNeighborhoods() {
		double sum = 0;
		for(int i = 0; i < numParticles; i++) {
			sum += particles[i].getNumNeighborsOwnFlock();
		}
		return sum/particles.length;
	}

	public void printFlockData() {
		flockData.outputDataCSV();
	}


}




