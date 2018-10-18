
/*
 * There is a neighborhood object for each particle. It contains an ID and
 * an array of all the particles in that neighborhood, including the particle
 * whose neighborhood it is.
 * 
 */

public class Neighborhood {


	private int neighID;
	private Particle[] neighParticles;  
	private int nextNeighIndex = 0;   // only used when adding neighborhoods; keeps track of index of next one


	// this constructor just creates an array big enough to hold all the neighbors -- 
	// neighbors are added using the addNeighbor method

	public Neighborhood(int size, int particleID) {
		this.neighID = particleID;
		nextNeighIndex = 0;
		neighParticles = new Particle[size];
	}

	
	// after the particles are created in the Swarm constructor, the constructor calls the createNeighborhoods method, 
	// which calls this constructor for each particle to create the Neighborhood in that particle.
	public Neighborhood (Particle[] particles, int particleID, 
			PSO.Topology currentTopology, PSO.SelfModel currentSelfModel, PSO.InfluenceModel currentInfluenceModel) {
		
		this.neighID = particleID;
		nextNeighIndex = 0;

		// this is wasteful, since every particle will have a Neighborhood object containing an array
		// of all the particles; however, since they are just references to particles, it's not so bad
		if (currentTopology == PSO.Topology.GBEST) {

			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[particles.length];
				for (int partID = 0 ; partID < particles.length ; partID++) {
					neighParticles[partID] = particles[partID];
				}
			}

			else {
				int nextParticleIndex = 0;  // need this to keep track when self is not included and that partID is skipped
				neighParticles = new Particle[particles.length-1];
				for (int partID = 0 ; partID < particles.length ; partID++) {
					if (partID != particleID)
						neighParticles[nextParticleIndex++] = particles[partID];
				}

			}


		}

		else if (currentTopology == PSO.Topology.RING) {

			// the non-self particles are always the first two in the list; 
			// if self is included, make the array size 3 and put the self at the end
			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[3];
				neighParticles[2] = particles[particleID];
			}
			else {
				neighParticles = new Particle[2];
			}

			int leftIndex = particleID == 0? particles.length - 1: particleID - 1;				
			int rightIndex = particleID == particles.length - 1? 0: particleID + 1;

			neighParticles[0] = particles[leftIndex];
			neighParticles[1] = particles[rightIndex];

		}

		else if (currentTopology == PSO.Topology.vonNEUMANN) {

			// the non-self particles are always the first four in the list; 
			// if self is included, make the array size 5 and put the self at the end
			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[5];
				neighParticles[4] = particles[particleID];
			}
			else {
				neighParticles = new Particle[4];
			}

			// get the dimensions of the torus from PSO, where they are set by hand
			// for each possible # of particles
			int numRowsVonNeumann = Parameters.numRowsVonNeumannAndMoore;
			int numColsVonNeumann = Parameters.numColsVonNeumannAndMoore;

			int row = particleID  / numColsVonNeumann;
			int col = particleID  % numColsVonNeumann;

			int northParticleRow = row - 1 < 0? numRowsVonNeumann - 1: row - 1;
			int northParticleCol = col;
			int particleNum = (northParticleRow * numColsVonNeumann) + northParticleCol;
			neighParticles[0] = particles[particleNum];

			int eastParticleRow = row;
			int eastParticleCol = (col + 1) % numColsVonNeumann;
			particleNum = (eastParticleRow * numColsVonNeumann) + eastParticleCol;
			neighParticles[1] = particles[particleNum];

			int southParticleRow = (row + 1) % numRowsVonNeumann;
			int southParticleCol = col;
			particleNum = (southParticleRow * numColsVonNeumann) + southParticleCol;
			neighParticles[2] = particles[particleNum];

			int westParticleRow = row;
			int westParticleCol = col - 1 < 0? numColsVonNeumann - 1: col - 1;
			particleNum = (westParticleRow * numColsVonNeumann) + westParticleCol;
			neighParticles[3] = particles[particleNum];			

		}

		else if (currentTopology == PSO.Topology.MOORE) {

			// the non-self particles are always the first eight in the list; 
			// if self is included, make the array size 9 and put the self at the end
			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[9];
				neighParticles[8] = particles[particleID];
			}
			else {
				neighParticles = new Particle[8];
			}

			// get the dimensions of the torus from PSO, where they are set by hand
			// for each possible # of particles
			int numRowsMoore = Parameters.numRowsVonNeumannAndMoore;
			int numColsMoore = Parameters.numColsVonNeumannAndMoore;

			int row = particleID  / numColsMoore;
			int col = particleID  % numColsMoore;

			int nextParticleIndex = 0;
			for (int rDelta = -1 ; rDelta <= 1 ; ++rDelta) {
				for (int cDelta = -1 ; cDelta <= 1 ; ++cDelta) {

					// don't do this for the particle itself
					if (rDelta != 0 || cDelta != 0) {
						int neighRow = row + rDelta;
						int neighCol = col + cDelta;

						if (neighRow < 0)
							neighRow = numRowsMoore - 1;
						else if (neighRow == numRowsMoore) {
							neighRow = 0;
						}

						if (neighCol < 0)
							neighCol = numColsMoore - 1;
						else if (neighCol == numColsMoore) {
							neighCol = 0;
						}

						int particleNum = (neighRow * numColsMoore) + neighCol;
						neighParticles[nextParticleIndex++] = particles[particleNum];
					}
				}
			}
		}
		
		else if (currentTopology == PSO.Topology.FLOCK) {
			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[1];
				neighParticles[0] = particles[particleID];
			}
			else {
				neighParticles = new Particle[0];
				
			}
		}
		
	}


	
	// get a vector to the neighborhood best position; this will be used in Particle.java to compute the
	// neighborhood element of the change in velocity
	public DoubleVector getVectorToNeighBestPosition(Particle particle, PSO.Topology currentTopology, PSO.SelfModel currentSelfModel) {

		DoubleVector neighBestPosition = getNeighBestPosition(particle, currentTopology, currentSelfModel);
		DoubleVector vectorToNeighBestPosition = DoubleVector.sub(neighBestPosition, particle.getPosition());

		return vectorToNeighBestPosition;

	}

	
	// determine the neighborhood best and return its position
	public DoubleVector getNeighBestPosition(Particle particle, PSO.Topology currentTopology, PSO.SelfModel currentSelfModel) {

		
		// if it's the standard gbest including self, it's faster to just return the
		// true global best that we're keeping track of in Swarm.java
		if (currentTopology == PSO.Topology.GBEST && currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
			
			return Swarm.globalBest.getPosition(); //This was being called no matter what for all 10 weeks of this project
		}

		// whether the self is included was dealt with when the neighborhood was created;
		// if the self is not supposed to be in the neighborhood, it's not (see constructor for details)
		// so just go through the list of particles and find the best one
		Particle bestParticle = null;
		if(currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
			bestParticle = particle;
		}

		double bestPBestFuncVal = Double.MAX_VALUE;

		//this commented below was how it was originally, consider putting it back 
		//int numPartsInNeigh = neighParticles.length;
		
		int numPartsInNeigh = particle.getNeighborhood().getNeighParticles().length;
		Neighborhood neighborhoodParticles = particle.getNeighborhood();
		
		//neighborhoodParticles.print();
		
		for (int p = 0 ; p < numPartsInNeigh ; ++p) {
			
			//this commented below was how it was originally, consider putting it back 
			//Particle nextParticle = neighParticles[p];
			
			Particle nextParticle = neighborhoodParticles.getNeighParticles()[p];
			
			//test
			//System.out.println("particle ID: " + particle.getParticleID() + " Neighbor: " + nextParticle.getParticleID() + " p: "+ p);
			
			try {
				
				
				if(nextParticle != null) {
					//nullPointerException here (without the above if statement) **************
					Solution nextPBestFuncValSol = nextParticle.getPersonalBest();
					
					double nextPBestFuncVal = nextPBestFuncValSol.getFunctionValue();
					if (nextPBestFuncVal < bestPBestFuncVal) {
						bestParticle = nextParticle;
						bestPBestFuncVal = nextPBestFuncVal;
						//System.out.println("Particle " + (p+1) + " of " + numPartsInNeigh + " Best value found: " + bestPBestFuncVal);
					}
				}
			}
			catch (Exception e){
				System.out.println("Exception in Neighborhood.getNeighBestPosition");
				System.out.println("Number of particles in neighborhood: " + numPartsInNeigh + " p: " + p);
				e.printStackTrace();
			}
			
		}

		//print statements here
		
		return bestParticle.getPersonalBest().getPosition();
		
		
	}
	
	
	// calculates the change in velocity if we are using the FIPS model, i.e. *every*
	// particle in the neighborhood influences the change in velocity
	public DoubleVector getFIPSAcceleration (Particle particle) {

		int numPartsInNeigh = neighParticles.length;
		
		// each particle has equal influence; this is not always the case in FIPS;
		// sometimes they are weighted by fitness
		double componentTheta = PSO.theta / numPartsInNeigh;
		DoubleVector position = particle.getPosition();
		DoubleVector acceleration = new DoubleVector(position.size(), 0.0);

		// whether the self is included was dealt with when the neighborhood was created;
		// if the self is not supposed to be in the neighborhood, it's not (see constructor for details)
		// so just go through the list of particles and do the standard FIPS calculation
		for (int p = 0 ; p < numPartsInNeigh ; ++p) {
			Particle nextParticle = neighParticles[p];
			DoubleVector vectorToNextPBest = DoubleVector.sub(nextParticle.getPosition(), position);
			vectorToNextPBest.multRandomScalar(0.0, componentTheta);
			acceleration.addVector(vectorToNextPBest);
		}

		return acceleration;
	}
	


	// miscellaneous methods 
		
	public void addNeighbor(Particle particle) {
		neighParticles[nextNeighIndex++] = particle;
	}
	
	
	// does the neighborhood contain a particular particle
	public boolean containsParticle (Particle particle) {

		for (int p = 0 ; p < neighParticles.length ; ++p) {
			if (neighParticles[p] == particle)	{
				return true;
			}
		}

		return false;
	}

	
	public Particle[] getNeighParticles() {
		return neighParticles;
	}

	
	public int getNeighID () {
		return neighID;
	}
	
	public void print() {
		System.out.println("neighID: " + neighID + " nextNeighIndex: " + nextNeighIndex);
		for(int i = 0; i < neighParticles.length; i++) {
			if(neighParticles[i] == null) {
				System.out.println("null");
			}
			else {
				System.out.println(neighParticles[i].getParticleID());
			}
		}
	}
	


}
