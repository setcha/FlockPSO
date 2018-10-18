
/*
 * This is the class for a PSO particle, BUT it also contains information for the 
 * particle's flock dynamics, i.e.
 * each particle belongs to the PSO swarm and also to the flock that is being used to
 * calculate neighborhoods. So, the code here keeps track of both -- see the comments
 * in the instance variable section.
 * 
 * DummyParticles are determined by the boolean isDummy. These particles can be added 
 * in addition to PSO particles to help flock movement. They dont have neighbors or 
 * corresponding positions in PSO function space. If statements are used to differentiate 
 * between dummy and "real" particles.
 */


public class Particle {

	private int particleID;

	// PSO DATA

	// location and velocity of the particle in PSO SOLUTION SPACE
	private DoubleVector position;  
	private DoubleVector velocity;

	private Solution currSolution;
	private Solution personalBest;

	// the neighborhood that this particle is in
	private Neighborhood neighborhood;


	// FLOCK DATA

	// flocks are in a 500 x 500 2D space
	private static final int FLOCK_SPACE_NUM_DIM = 2;
	private static final int FLOCK_SPACE_DIM_LENGTH = Parameters.FLOCK_SPACE_DIM_LENGTH;

	// make components of initial velocity very small 
	private static final double FLOCK_INIT_VELOCITY_DIMENSION_MAGNITUDE = 1.0;
	// if a random component is added to the motion, keep it small
	private static final double FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE = 1.0;
	// how close to the boundary does a particle need to be before it "bounces back"?
	private static final int FLOCK_BOUDARY_SENSING_THRESHOLD = 5;

	// flock parameters
	private double maxSpeed;                // speed limit
	private double normalSpeed;             // when pacekeeping (see below) used, tries to keep particle to this speed
	private double neighborRadius;          // determines which other particles are neighbors of a given particle    
	private double separationWeight;        // how strongly do particles move away from every neighbor: range = [0.0, 100.0]
	private double cohesionWeight;          // how strongly do particles move toward the average position of their neighbors: range = [0.0, 1.0]
	private double alignmentWeight;         // how strongly do particles align match average velocity of their neighbors: range = [0.0, 1.0]
	private double pacekeepingWeight;       // how strongly do particles stick to the normalSpeed: range = [0.0, 1.0]
	private double randomMotionProb;        // probability that a small random component is introduced into the motion (see particle class for more info)

	// motion of particles in flock depends on the number of neighbors
	private double numNeighborsOwnFlock = 0;
	// this is not used, since there is only one flock, but we might introduce more flocks later, so leave it
	private double numNeighborsAllFlocks = 0;

	// location and velocity of the particle in FLOCK SPACE
	private DoubleVector currFlockLocation;  
	private DoubleVector currFlockVelocity;
	private boolean isDummy;
	
	private int internalNeighRadius;

	public Particle(int functionNum, 
			int numDimensions, 
			int particleID, 
			double maxSpeed, 
			double normalSpeed,
			double neighborRadius, 
			double separationWeight,
			double cohesionWeight, 
			double alignmentWeight,
			double pacekeepingWeight, 
			double randomMotionProb,
			double[] sendBackResults,
			boolean isDummy) {

		this.particleID = particleID;

		//here, if statement separating dummy particles from full particles
		if(!isDummy) {

			// start with random position
			//dummy particles don't have a position in function space
			//if (particleID > numParticles)
			position = new DoubleVector(numDimensions);
			
			
			//testing this for random initialization
			//Utilities.setConsistentSeed((int) System.nanoTime() + particleID);
			
			for(int i = 0 ; i < position.size() ; ++i) {
				position.set(i, TestFunctions.INIT_MIN_VALS[functionNum] + (TestFunctions.INIT_RANGES[functionNum] * Utilities.rand.nextDouble()));
			}

			// evaluate the initial position 

			double[] results = TestFunctions.evalWithError(position, functionNum);
			// need to get the function value and error back to the Swarm constructor,
			// so we can determine the initial global best
			sendBackResults[TestFunctions.VAL_INDEX] =  results[TestFunctions.VAL_INDEX];
			sendBackResults[TestFunctions.ERR_INDEX] =  results[TestFunctions.ERR_INDEX];

			// NOTE 1: can send position itself because the Solution constructor makes a copy of the position DoubleVector sent in
			// NOTE 2: 0 is the iteration found
			currSolution = new Solution(position, results[TestFunctions.VAL_INDEX], results[TestFunctions.ERR_INDEX], particleID);
			this.personalBest = currSolution.getCopy();
			//System.out.println("Function value of particle " + particleID + ": " + personalBest.getFunctionValue());
			
			// start with small random velocity
			velocity = new DoubleVector(numDimensions);
			double minSpeed = 0.0;
			double speedRange = 0.0;
			// don't let the initial speed be greater than a small amount
			if (TestFunctions.UNIVERSAL_SPEED_RANGE < TestFunctions.SPEED_RANGES[functionNum]) {
				minSpeed = TestFunctions.UNIVERSAL_MIN_INIT_SPEED;
				speedRange = TestFunctions.UNIVERSAL_SPEED_RANGE;
			}
			else {
				minSpeed = TestFunctions.SPEED_MIN_VALS[functionNum];
				speedRange = TestFunctions.SPEED_RANGES[functionNum];			
			}
			for(int i = 0 ; i < velocity.size() ; ++i) {
				velocity.set(i, minSpeed + (speedRange * Utilities.rand.nextDouble()));
			}

			//a getFlockNeighbors() function would be good to set initial neighborhoods
		
		}

		// set flock parameters
		this.maxSpeed = maxSpeed;
		this.normalSpeed = normalSpeed;
		this.neighborRadius = neighborRadius;
		this.separationWeight = separationWeight;
		this.cohesionWeight = cohesionWeight;
		this.alignmentWeight = alignmentWeight;
		this.pacekeepingWeight = pacekeepingWeight;
		this.randomMotionProb = randomMotionProb;
		
		//used only in testFlockParameters
		this.internalNeighRadius = 20;
		
		
		//Utilities.setConsistentSeed(Parameters.globalSeed + particleID); //just set the flock location and velocity to a consistent starting place, but different for each particle
		
		// create random location in flock space
		currFlockLocation = DoubleVector.randomVector(FLOCK_SPACE_NUM_DIM, FLOCK_SPACE_DIM_LENGTH/2);
		//currFlockLocation.addScalar(FLOCK_SPACE_DIM_LENGTH/2); to confine to positive quadrant

		// start with small random velocity in flock space
		currFlockVelocity = DoubleVector.randomVector(FLOCK_SPACE_NUM_DIM, FLOCK_INIT_VELOCITY_DIMENSION_MAGNITUDE);
		
		//Utilities.setConsistentSeed((int) System.nanoTime()); //re-randomize the seed

	}


//make sure swarm doesn't call this on dummy particles, instead it just calls a "move flock" function, or returns null for moveFlockGetNeighbors
	// Update the the velocity and position IN THE PSO SOLUTION SPACE
	public Solution update(PSO.Topology currentPSOTopology, PSO.SelfModel currentPSOSelfModel, 
			PSO.InfluenceModel currentPSOInfluenceModel) {

		//Utilities.setConsistentSeed(Parameters.globalSeed + particleID);// continue using the same every run
		
		// acceleration starts at 0.0
		DoubleVector acceleration = new DoubleVector(position.size(), 0.0);

		// if using the NEIGHBORHOOD BEST influence model
		if (currentPSOInfluenceModel == PSO.InfluenceModel.NEIGH_BEST) {

			DoubleVector neighBestComponent;

			// if we're doing S-PSO, just get the standard vector to the neighborhood best
			if (!PSO.usingFLOCKPSO) {
				neighBestComponent = neighborhood.getVectorToNeighBestPosition(this, currentPSOTopology, currentPSOSelfModel);
			}
			// if we're doing FLOCK-PSO, we need to move the particles IN FLOCK SPACE and use the resulting
			// positions to determine the particle's neighbors in PSO SOLUTION SPACE; then get a vector to the
			// neighborhood best, as we do in the S-PSO case
			else {
				Neighborhood flockNeigh = moveFlockAndGetNeighbors();
				neighBestComponent = flockNeigh.getVectorToNeighBestPosition(this, currentPSOTopology, currentPSOSelfModel);
			}

			// neighborhood best component of acceleration
			neighBestComponent.multRandomScalar(0.0, PSO.nBestTheta);
			acceleration.addVector(neighBestComponent);

			// personal best component of acceleration
			DoubleVector pBestComponent = DoubleVector.sub(personalBest.getPosition(), position);
			pBestComponent.multRandomScalar(0.0, PSO.pBestTheta);
			acceleration.addVector(pBestComponent);
		}

		// if using the FIPS influence model, in which *every* particle in the neighborhood influences
		// the acceleration
		else if (currentPSOInfluenceModel == PSO.InfluenceModel.FIPS) {

			// S-PSO
			if (!PSO.usingFLOCKPSO) {
				acceleration = neighborhood.getFIPSAcceleration (this);
			}
			// if FLOCK-PSO, find neighbors first
			else {
				Neighborhood flockNeigh = moveFlockAndGetNeighbors();
				acceleration = flockNeigh.getFIPSAcceleration (this);
			}
		}

		// update the velocity and apply the constriction factor
		velocity.addVector(acceleration);
		velocity.multScalar(PSO.constrictionFactor);

		// bound velocity; should not be necessary with the constriction factor, but people
		// often do both
		for (int i = 0 ; i < velocity.size() ; ++i) {
			if (velocity.get(i) < TestFunctions.SPEED_MIN_VALS[Parameters.functionNum])
				velocity.set(i, TestFunctions.SPEED_MIN_VALS[Parameters.functionNum]);
			else if (velocity.get(i) > TestFunctions.SPEED_MAX_VALS[Parameters.functionNum])
				velocity.set(i, TestFunctions.SPEED_MAX_VALS[Parameters.functionNum]);
		}


		// move the particle 
		position.addVector(velocity); 


		// evaluate the new position
		double[] results = TestFunctions.evalWithError(position, Parameters.functionNum);
		double newPositionValue = results[TestFunctions.VAL_INDEX];
		double newPositionError = results[TestFunctions.ERR_INDEX];

		// reset the current solution
		currSolution.copyFromPosition(position);
		currSolution.setFunctionValue(newPositionValue);
		currSolution.setError(newPositionError);

		// update the personal best, if necessary
		if (newPositionValue < personalBest.getFunctionValue()) {
			personalBest.copyFromPosition(position);
			personalBest.setFunctionValue(newPositionValue);
			personalBest.setError(newPositionError);
		}
		
		//Utilities.setConsistentSeed((int) System.nanoTime()); //re-randomize the seed
		return currSolution;
	}		

	//pretty sure we can call this instead of update for Swarm.java
	public void updateDummyFlock() {
		moveFlockAndGetNeighbors();
	}
	

	// this moves the particles IN FLOCK SPACE, determines the particle's neighbors, and
	// returns a neighborhood containing those particles
	private Neighborhood moveFlockAndGetNeighbors () {
		
		//uncomment this to run testFlockPattern, a function where you can define how parameters behave (function is below)
		//testFlockPattern();
		
		// need all the particles to update flock positions
		Particle[] particles = Swarm.particles; 
		//dummy particles array here

		// new acceleration in flock space
		DoubleVector acceleration = new DoubleVector(FLOCK_SPACE_NUM_DIM, 0.0);

		// need sum of locations of particles in the neighborhood for acceleration due to cohesion,
		// since cohesion = acceleration toward the average location of particles in the neighborhood
		DoubleVector sumNeighborLocations = new DoubleVector(FLOCK_SPACE_NUM_DIM, 0.0);

		// need sum of velocities of particles in the neighborhood for acceleration due to alignment,
		// since alignment = acceleration toward the average velocity of particles in the neighborhood
		DoubleVector sumNeighborVelocities = new DoubleVector(FLOCK_SPACE_NUM_DIM, 0.0);

		// this is where we will put particles that are in this particle's neighborhood
		Particle[] neighParticles= new Particle[particles.length];
		// keep track of where the next neighbor should go in the neighParticles array
		//why didn't we just use an arraylist?
		int neighborsIndex = 0;

		numNeighborsOwnFlock = 0;
		numNeighborsAllFlocks = 0;

		double prob = 0;
		double selectValue = Utilities.rand.nextDouble();

		if (selectValue < prob) {
			double flockX = currFlockLocation.get(0);
			double flockY = currFlockLocation.get(1);

			System.out.println();
			System.out.println("Particle ID: " + particleID + " Location: (" + flockX + ", "+ flockY + ")" + " Radius: " + neighborRadius);
			System.out.println();
		}

		// process all particles
		for (int i = 0 ; i < particles.length; i++) {

			// a particle is not in its own neighborhood
			if (i == particleID) {
				continue;				
			}

			// get the distance to the other particle
			Particle otherParticle = particles[i];
			
			//normal distance
			double dist = currFlockLocation.distance(otherParticle.currFlockLocation);
			
			
			//for toroidal space, need to check differently too
			//dist = currFlockLocation.minDistance()
			
			//double dist = currFlockLocation.torusMinDistance(otherParticle.currFlockLocation, FLOCK_SPACE_DIM_LENGTH);
			
			
			
			if (selectValue < prob) {
				double flockX = otherParticle.currFlockLocation.get(0);
				double flockY = otherParticle.currFlockLocation.get(1);			
				System.out.println("i: " + i + " Location: (" + flockX + ", "+ flockY + ")" + " Dist: " + dist + " Radius: " + neighborRadius);
			}
			// is it in the neighborhood?
			// NOTE: check to make sure not zero, in case of underflow 
			
			
			
			//test flock parameters here, from music swarm
			
			
			
			
			if (dist > 0.0 && dist <= neighborRadius) {

				//dont want any dummy particles in neighborhoods
				if (!otherParticle.isDummy) {// if it is not a dummy particle
					++numNeighborsOwnFlock;
					neighParticles[neighborsIndex] = particles[i];		//++ was next to neighbors index before, moved it outside of the if		
				}
				
				neighborsIndex++;

				// sum locations for cohesion calculation after all neighbors have been processed
				
				//sumNeighborLocations.addVector(currFlockLocation.minTorusVect(otherParticle.currFlockLocation, FLOCK_SPACE_DIM_LENGTH));
				sumNeighborLocations.addVector(otherParticle.currFlockLocation); 

				// sum velocities for alignment calculation after all neighbors have been processed
				sumNeighborVelocities.addVector(otherParticle.currFlockVelocity);   ///error???? was CurrFlockLocation

				
				
				// for separation:
				// calculate and weight vector pointing away from neighbor; add to acceleration
				DoubleVector vectorToThisParticle = DoubleVector.sub(currFlockLocation, otherParticle.currFlockLocation);
				vectorToThisParticle.divScalar(dist*dist);  
				// separation force is inversely proportional to the square of the distance, 
				// but some experiments indicate to me that we might want to consider reducing the
				// denominator to dist^1.5, or possibly even dist. using dist^2 sometimes weakens the 
				// separation force to an extent that makes it very difficult for separation to have 
				// any impact when the cohesion is at its max; I would think that even when cohesion 
				// is high, if the separation weight is >50, it should loosen tight clusters significantly,
				// which does not happen currently  
				vectorToThisParticle.multScalar(separationWeight);
				acceleration.addVector(vectorToThisParticle);  

			}	
		} 

		//////
		if (selectValue < prob) {
			//System.out.println("Particle ID: " + particleID);

			//particles typically have between 0 and 3 neighbors
			for(int i = 0; i < neighParticles.length; i++) {
				System.out.println(" " + neighParticles[i]);
			}
		}
		//update something here? isn't neighParticles.length still 30? should we be using ArrayLists?
		// convert the array of neighbor particles to a Neighborhood object
		Neighborhood flockNeigh = new Neighborhood(neighParticles.length, particleID);

		for (int i = 0 ; i < neighParticles.length; i++) {
			flockNeigh.addNeighbor(neighParticles[i]);

		}

		//added this 7/25, before we weren't setting the particle neighborhood!
		setNeighborhood(flockNeigh);


		if (selectValue < prob) {
			flockNeigh.print();
		}

		// only do the following if there were neighbors; otherwise, division by zero!
		// NOTE: neighborsIndex will also be the number of neighbors
		if (neighborsIndex > 0) {
			// cohesion steering: steer in the direction of the average location of your neighbors
			DoubleVector cohesionVector = DoubleVector.divVectorScalar(sumNeighborLocations, neighborsIndex);        
			cohesionVector.subVector(currFlockLocation);
			cohesionVector.multScalar(cohesionWeight);
			acceleration.addVector(cohesionVector);

			// alignment steering: steer so as to align your velocity with the average velocity of your neighbors
			DoubleVector alignmentVector = DoubleVector.divVectorScalar(sumNeighborVelocities, neighborsIndex);
			alignmentVector.subVector(currFlockVelocity);
			alignmentVector.multScalar(alignmentWeight);
			acceleration.addVector(alignmentVector);
		}


		// with the probability specified by the parameter randomMotionProbability, introduce a small
		// random perturbation (magnitude defined by RANDOM_MOTION_DIMENSION_MAGNITUDE) into each 
		// acceleration component
		if (Utilities.rand.nextFloat() < randomMotionProb) {
			acceleration.addRandomScalarMagnitude(FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE);
		} 


		// update velocity
		currFlockVelocity.addVector(acceleration);


		// make sure we don't exceed maxSpeed
		if (currFlockVelocity.mag() > maxSpeed) {
			currFlockVelocity.multScalar(maxSpeed / currFlockVelocity.mag());
		}

		// pacekeeping (stick to normalSpeed to the extent indicated by pacekeepingWeight)
		DoubleVector pacekeeping = 
				DoubleVector.multVectorScalar(currFlockVelocity, ((normalSpeed - currFlockVelocity.mag()) / currFlockVelocity.mag() * pacekeepingWeight));
		currFlockVelocity.addVector(pacekeeping);

		double distance = 0;
		
		// bounce back from the boundaries of the space
		DoubleVector boundaryAcceleration = new DoubleVector(FLOCK_SPACE_NUM_DIM, 0.0);
		for (int d = 0 ; d < FLOCK_SPACE_NUM_DIM ; ++d) {
			//normal(square):
			if (currFlockLocation.get(d) < (FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE/2) + FLOCK_BOUDARY_SENSING_THRESHOLD - FLOCK_SPACE_DIM_LENGTH/2)         
				boundaryAcceleration.set(d, maxSpeed);	
			else if (currFlockLocation.get(d) > FLOCK_SPACE_DIM_LENGTH/2 - FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE/2 - FLOCK_BOUDARY_SENSING_THRESHOLD) 
				boundaryAcceleration.set(d, -maxSpeed);
			
			//circular:
			//distance += currFlockLocation.get(d) * currFlockLocation.get(d);
			
			//toroidal:			
			//if (currFlockLocation.get(d) < -FLOCK_SPACE_DIM_LENGTH/2)         
			//	currFlockLocation.set(d, currFlockLocation.get(d) + FLOCK_SPACE_DIM_LENGTH);	
			//else if (currFlockLocation.get(d) > FLOCK_SPACE_DIM_LENGTH/2) 
			//	currFlockLocation.set(d, currFlockLocation.get(d) - FLOCK_SPACE_DIM_LENGTH);
			
		}
		
//		double boundary = FLOCK_SPACE_DIM_LENGTH/2 - FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE/2 - FLOCK_BOUDARY_SENSING_THRESHOLD;
//		//normalize the current flock location to get a vector pointing away from the origin, then multiply it by maxspeed to go directly toward it
//		if (distance > boundary * boundary){
//			boundaryAcceleration = currFlockLocation.getCopy();
//			boundaryAcceleration.normalize();
//			boundaryAcceleration.multScalar(-maxSpeed);
//		}
		
		//try setting it?
		currFlockVelocity.addVector(boundaryAcceleration);   
		//currFlockVelocity.setVector(boundaryAcceleration);


		// move the particle
		currFlockLocation.addVector(currFlockVelocity);


		// this is where the GP program tree is executed
		// The tree program is a static variable in the Swarm class. We must send it a reference to
		// this Particle so it can call methods in here to get/change variable values FOR THIS
		// PARTICEL (as necessary).
		if (PSO.usingFLOCKPSO) {
			Swarm.gpTree.run(this);
			//			if (particleID == 0) {
			//				System.out.println("HERE");
			//				printFlockParameters();
			//				System.out.println();
			//				Swarm.gpTree.printTree();
			//			}
		}

		// for the test case where random changes are made, just regenerate random values
		else if (Parameters.doRandomTree){
			double[] newRandParams = Swarm.generateRandomParameters();
			this.maxSpeed = newRandParams[0];
			this.normalSpeed = newRandParams[1];
			this.neighborRadius = newRandParams[2];
			this.separationWeight = newRandParams[3];
			this.cohesionWeight = newRandParams[4];
			this.alignmentWeight = newRandParams[5];
			this.pacekeepingWeight = newRandParams[6];
			this.randomMotionProb = newRandParams[7];
		}

		return flockNeigh;

	}



	public void printFlockParameters() {

		System.out.printf("NS = %5.3f  NR = %5.3f  SEP = %5.3f  COH = %5.3f  ALI = %5.3f  PACE = %5.3f  RAND = %5.3f NNOWN = %5.3f NNALL = %5.3f \n",
				normalSpeed, //why not include max speed?
				neighborRadius, 
				separationWeight,
				cohesionWeight, 
				alignmentWeight,
				pacekeepingWeight, 
				randomMotionProb,
				numNeighborsOwnFlock,
				numNeighborsAllFlocks);

	}


	// called by a GPNode when it needs the value of a variable in the Particle object
	public double getVarValue(String varName) {

		if (varName.equals("maxSpeed")) {
			return maxSpeed;
		}

		else if (varName.equals("normalSpeed")) {
			return normalSpeed;
		}

		else if (varName.equals("neighborRadius")) {
			return neighborRadius;
		}

		else if (varName.equals("separationWeight")) {
			return separationWeight;
		}

		else if (varName.equals("alignmentWeight")) {
			return alignmentWeight;
		}

		else if (varName.equals("cohesionWeight")) {
			return cohesionWeight;
		}

		else if (varName.equals("pacekeepingWeight")) {
			return pacekeepingWeight;
		}

		else if (varName.equals("randomMotionProbability")) {
			return randomMotionProb;
		}

		else if (varName.equals("numNeighborsOwnFlock")) {
			return numNeighborsOwnFlock;
		}

		else if (varName.equals("numNeighborsAllFlocks")) {
			return numNeighborsAllFlocks;
		}

		else {
			System.out.println("error: unknown variable name in Particle.getVarValue: \"" + varName + "\"");
			System.exit(0);
		}

		return 0.0f;

	}

	// called by a GPNode when it needs to assign a value to variable in the Particle object
	public void assignVariable(String varName, double value) {

		if(varName == null) {
			System.out.println("################################### tried to assign something that wasn't a variable");
		}

		if (varName.equals("maxSpeed")) {
			maxSpeed = value;
		}

		else if (varName.equals("normalSpeed")) {
			normalSpeed = value;
		}

		else if (varName.equals("neighborRadius")) {
			neighborRadius = value;
		}

		else if (varName.equals("separationWeight")) {
			separationWeight = value;
		}

		else if (varName.equals("alignmentWeight")) {
			alignmentWeight = value;
		}

		else if (varName.equals("cohesionWeight")) {
			cohesionWeight = value;
		}

		else if (varName.equals("pacekeepingWeight")) {
			pacekeepingWeight = value;
		}

		else if (varName.equals("randomMotionProbability")) {
			randomMotionProb = value;
		}

		else if (varName.equals("numNeighborsOwnFlock")) {
			numNeighborsOwnFlock = value;
		}

		else if (varName.equals("numNeighborsAllFlocks")) {
			numNeighborsAllFlocks = value;
		}

		else {
			if (particleID == 0) System.out.println("error: unknown variable name in Particle.assignVariable: \"" + varName + "\"");
			System.exit(0);
		}

	}


	// called by a GPNode when it needs to increment a variable in the Particle object
	public void increment(String varName) {

		if(varName == null) {
			System.out.println("################################### tried to assign something that wasn't a variable");
		}

		if (varName.equals("maxSpeed")) {
			++maxSpeed;
		}

		else if (varName.equals("normalSpeed")) {
			++normalSpeed;
		}

		else if (varName.equals("neighborRadius")) {
			++neighborRadius;
		}

		else if (varName.equals("separationWeight")) {
			++separationWeight;
		}

		else if (varName.equals("alignmentWeight")) {
			++alignmentWeight;
		}

		else if (varName.equals("cohesionWeight")) {
			++cohesionWeight;
		}

		else if (varName.equals("pacekeepingWeight")) {
			++pacekeepingWeight;
		}

		else if (varName.equals("randomMotionProbability")) {
			++randomMotionProb;
		}

		else if (varName.equals("numNeighborsOwnFlock")) {
			++numNeighborsOwnFlock;
		}

		else if (varName.equals("numNeighborsAllFlocks")) {
			++numNeighborsAllFlocks;
		}

		else {
			System.out.println("error: unknown variable name in Particle.increment: \"" + varName + "\"");
			System.exit(0);
		}

	}


	// called by a GPNode when it needs to decrement a variable in the Particle object
	public void decrement(String varName) {

		if(varName == null) {
			System.out.println("################################### tried to assign something that wasn't a variable");
		}

		if (varName.equals("maxSpeed")) {
			--maxSpeed;
		}

		else if (varName.equals("normalSpeed")) {
			--normalSpeed;
		}

		else if (varName.equals("neighborRadius")) {
			--neighborRadius;
		}

		else if (varName.equals("separationWeight")) {
			--separationWeight;
		}

		else if (varName.equals("alignmentWeight")) {
			--alignmentWeight;
		}

		else if (varName.equals("cohesionWeight")) {
			--cohesionWeight;
		}

		else if (varName.equals("pacekeepingWeight")) {
			--pacekeepingWeight;
		}

		else if (varName.equals("randomMotionProbability")) {
			--randomMotionProb;
		}

		else if (varName.equals("numNeighborsOwnFlock")) {
			--numNeighborsOwnFlock;
		}

		else if (varName.equals("numNeighborsAllFlocks")) {
			--numNeighborsAllFlocks;
		}

		else {
			System.out.println("error: unknown variable name in Particle.decrement: \"" + varName + "\"");
			System.exit(0);
		}

	}


	// getters and setters

	public int getParticleID () {
		return particleID;
	}

	public DoubleVector getPosition() {
		return position;
	}

	public Solution getCurrSolution() {
		return currSolution;
	}

	public void setCurrSolution(Solution currSolution) {
		this.currSolution = currSolution;
	}

	public Solution getPersonalBest() {
		return personalBest;
	}


	public void setPersonalBest(Solution personalBest) {
		this.personalBest = personalBest;
	}


	public Neighborhood getNeighborhood() {
		return neighborhood;
	}


	public void setNeighborhood(Neighborhood neighborhood) {
		this.neighborhood = neighborhood;
	}

	public DoubleVector getCurrLocation() {
		return currFlockLocation;
	}
	
	public double getRadius() {
		return neighborRadius;
	}
	
	public int getInternalNeighRadius() {
		return internalNeighRadius;
	}
	
	public double getNumNeighborsOwnFlock() {
		return numNeighborsOwnFlock;
	}
	
	public boolean getIsDummy() {
		return isDummy;
	}
	
	public double[] getParameters() {
		double[] flockParam = new double[10];
		flockParam[0] = maxSpeed;                // speed limit
		flockParam[1] = normalSpeed;             // when pacekeeping (see below) used, tries to keep particle to this speed
		flockParam[2] = neighborRadius;          // determines which other particles are neighbors of a given particle    
		flockParam[3] = separationWeight;        // how strongly do particles move away from every neighbor: range = [0.0, 100.0]
		flockParam[4] = cohesionWeight;          // how strongly do particles move toward the average position of their neighbors: range = [0.0, 1.0]
		flockParam[4] = alignmentWeight;         // how strongly do particles align match average velocity of their neighbors: range = [0.0, 1.0]
		flockParam[6] = pacekeepingWeight;       // how strongly do particles stick to the normalSpeed: range = [0.0, 1.0]
		flockParam[7] = randomMotionProb;        // probability that a small random component is introduced into the motion (see particle class for more info)

		// motion of particles in flock depends on the number of neighbors
		flockParam[8] = numNeighborsOwnFlock;
		// this is not used, since there is only one flock, but we might introduce more flocks later, so leave it
		flockParam[9] = numNeighborsAllFlocks;
		return flockParam;
	}

	public void testFlockPattern() {
		if (numNeighborsOwnFlock < 8) {
			internalNeighRadius += 1;	
			if (internalNeighRadius > 400) {  
				internalNeighRadius = 400;
			}
			cohesionWeight = 0.9;
            separationWeight = 10; 
			normalSpeed = 6;
			maxSpeed = 6;
			
			alignmentWeight = 0.2;
			pacekeepingWeight = 0.3;
			randomMotionProb = 1;
		}
		else if (numNeighborsOwnFlock > 8){
			internalNeighRadius -= 1; 
			if (internalNeighRadius < 0) {
				internalNeighRadius = 0;
			}
			cohesionWeight = 0.1;
            separationWeight = 90; 
			normalSpeed = 24; //12
			maxSpeed = 24;
			
			alignmentWeight = 0.2;
			pacekeepingWeight = 0.2;
			randomMotionProb = 1;
		}
		
		neighborRadius = internalNeighRadius;	
	}
	
	
	
	
	
	
	
}







