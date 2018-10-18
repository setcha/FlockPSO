/* 
 * DoubleVector objects are used to store locations and velocities in the
 * PSO solution space.
 * 
 * It contains all the operations that we need to do on these, e.g. adding
 * a scalar value to all elements in the vector.
 */



public class DoubleVector {


	private double[] vector;  


	// CONSTRUCTORS

	public DoubleVector(int size) {
		vector = new double[size];
	}


	public DoubleVector(int size, double initVal) {
		vector = new double[size];
		for(int i = 0 ; i < size ; ++i)
			vector[i] = initVal;
	}



	// creates a DoubleVector that is a copy of the DoubleVector sent in
	public DoubleVector(DoubleVector v) {
		vector = new double[v.size()];
		for(int i = 0 ; i < v.size() ; ++i)
			vector[i] = v.get(i);
	}


	// create and return a DoubleVector with elements in (-maxMagnitude, +maxMagnitude),
	// i.e. not including -maxMagnitude or +maxMagnitude
	public static DoubleVector randomVector(int size, double maxMagnitude) {
		DoubleVector v = new DoubleVector(size);
		for(int i = 0 ; i < size ; ++i) {
			double element = Utilities.rand.nextDouble() * maxMagnitude;
			if (Utilities.rand.nextDouble() < 0.5)
				element *= -1.0;
			v.set(i, element);
		}
		return v;
	}



	// COPIERS

	public DoubleVector getCopy() {

		DoubleVector v = new DoubleVector(vector.length);
		for(int i = 0 ; i < vector.length ; ++i)
			v.set(i, vector[i]);

		return v;

	}


	public void copyFrom(DoubleVector v) {

		if (vector.length != v.size()) {
			System.out.println("error:  vectors not same size in DoubleVector.copyFrom(DoubleVector)");
			System.exit(0);
		}

		for(int i = 0 ; i < vector.length ; ++i)
			vector[i] = v.get(i);

	}


	
	// GETTERS AND SETTERS

	public double get(int i) {
		if (i < 0 || i >= vector.length) {
			System.out.println("error: index out of bounds in DoubleVector.get(int)");
			System.exit(0);
		}
		return vector[i];
	}


	public void set(int i, double value) {
		if (i < 0 || i >= vector.length) {
			System.out.println("error: index out of bounds in DoubleVector.set(int)");
			System.exit(0);
		}
		vector[i] = value;
	}


	public void setAll(double value) {
		for(int i = 0 ; i < vector.length ; ++i) {
			vector[i] = value;
		}
	}

	

	// BASIC VECTOR OPERATIONS

	public int size() {
		return vector.length;
	}


	public double distance(DoubleVector v) {
		double sumSquareDiffs = 0.0;
		for(int i = 0 ; i < vector.length ; ++i) {
			double diff = vector[i] - v.get(i);
			sumSquareDiffs += diff * diff;
		}
		return Math.sqrt(sumSquareDiffs);				
	}
	
	//only works with squares for now
//	public double torusMinDistance(DoubleVector v, double dimLength) {
//		double minDist = dimLength*2; //something large, but i think sqrt(2)*dimLength is max(minDist())
//		double currentDist = 0;
//
//		for (int xDiff = -1; xDiff <= 1; xDiff++) {
//			for (int yDiff = -1; yDiff <= 1; yDiff++) {
//				double sumSquareDiffs = 0.0;
//				for(int i = 0 ; i < vector.length ; ++i) {
//					double diff = vector[i] - v.get(i);
//					sumSquareDiffs += diff * diff;
//				}
//				currentDist = Math.sqrt(sumSquareDiffs);
//				if (currentDist < minDist) {
//					minDist = currentDist;
//				}
//			}
//		}
//
//		return minDist;
//	}
	
	
	//calculates the minimum distance to another point, assuming both points are on a torus. 
	//Only works currently when dimLength for all dimensions is equal
	//helper function so we don't have to type the third argument, the number of dimensions
	
	public double torusMinDistance(DoubleVector v, double dimLength) {
		//initial function call
		double minDist = torusMinDistance(v, dimLength, v.size() - 1); //not sure about - 1. other option is set to 0, have base case if statement be v.size(), and increment instead of decrement
		 
		return minDist;
	}

	
	public double torusMinDistance(DoubleVector v, double dimLength, int currDim) {
		
		if (currDim == -1) { //we've reached the end of the number of dimensions; should it be -1 or 0? 
			//-1 would be zeroeth dimension, cuz doublevectors are zero indexed, but the zeroeth index is actually the first (one) dimension
			return distance(v);
		}
		
		double currDist = Double.MAX_VALUE; //start with something large 
		double minDist = Double.MAX_VALUE;
		DoubleVector currVect = v.getCopy();
		
		for(int direction = -1; direction <= 1; direction++) {
			currVect = v.getCopy();
			//if statement check here if currVect.get(currDim) is in an adjacent quadrant. if not, it wont be the closest vect
			currVect.set(currDim, currVect.get(currDim) + dimLength*direction);
			currDist = torusMinDistance(currVect, dimLength, currDim - 1);
			if(currDist < minDist) {
				minDist = currDist;
			}
		}
		return minDist;
	}
	
	
	public DoubleVector minTorusVect(DoubleVector v, double dimLength){
		//initial function call
		DoubleVector minVect = minTorusVect(v, dimLength, v.size() - 1); //not sure about - 1. other option is set to 0, have base case if statement be v.size(), and increment instead of decrement
				 
		return minVect;
	}
	
	
	public DoubleVector minTorusVect(DoubleVector v, double dimLength, int currDim) {
		
		if (currDim == -1) { //we've reached the end of the number of dimensions; should it be -1 or 0? 
			//-1 would be zeroeth dimension, cuz doublevectors are zero indexed, but the zeroeth index is actually the first (one) dimension
			return v.getCopy();
		}
		
		double currDist = Double.MAX_VALUE; //start with something large 
		double minDist = Double.MAX_VALUE;
		DoubleVector bestVect = v.getCopy();
		DoubleVector currVect = v.getCopy();
		
		for(int direction = -1; direction <= 1; direction++) {
			currVect = v.getCopy();
			currVect.set(currDim, currVect.get(currDim) + dimLength*direction);
			currDist = torusMinDistance(minTorusVect(currVect, dimLength, currDim - 1), dimLength, currDim); //currDim - 1 or not?
			if(currDist < minDist) {
				bestVect = currVect;
				minDist = currDist;
			}
		}
		return bestVect;
	}


	public double mag() {
		double sumSquares = 0.0;
		for(int i = 0 ; i < vector.length ; ++i)
			sumSquares += vector[i] * vector[i];
		return Math.sqrt(sumSquares);		
	}


	public void normalize () {
		divScalar(mag());
	}


	public boolean equal(DoubleVector v, double tolerance) {

		for(int i = 0 ; i < vector.length ; ++i) {	
			if (Math.abs(vector[i] - v.get(i)) > tolerance)
				return false;
		}

		return true;
	}


	
	// ADDITION

	public void addScalar (double scalar) {
		for(int i = 0 ; i < vector.length ; ++i)
			vector[i] += scalar;
	}


	public void addVector (DoubleVector v) {

		if (vector.length != v.size()) {
			System.out.println("error:  vectors not same size in DoubleVector.add(DoubleVector)");
			System.exit(0);
		}

		for(int i = 0 ; i < vector.length ; ++i)
			vector[i] += v.get(i);

	}


	// add two vectors and return a third vector that is the sum
	public static DoubleVector add(DoubleVector v1, DoubleVector v2) {

		if (v1.size() != v2.size()) {
			System.out.println("error:  vectors not same size in DoubleVector.add(DoubleVector, DoubleVector");
			System.exit(0);
		}

		DoubleVector v = new DoubleVector(v1.size());
		for(int i = 0 ; i < v.size() ; ++i)
			v.set(i, v1.get(i) + v2.get(i));

		return v;
	}


	// add a random scalar in (-magnitude, +magnitude) to each element
	// i.e. not including -magnitude or +magnitude
	public void addRandomScalarMagnitude(double magnitude) {

		for(int i = 0 ; i < vector.length ; ++i) {
			double element = Utilities.rand.nextDouble() * magnitude;
			if (Utilities.rand.nextDouble() < 0.5)
				element *= -1.0;
			vector[i] += element;
		}
	}



	// SUBTRACTION

	public void subScalar (double scalar) {
		for(int i = 0 ; i < vector.length ; ++i)
			vector[i] -= scalar;
	}


	public void subVector (DoubleVector v) {

		if (vector.length != v.size()) {
			System.out.println("error:  vectors not same size in DoubleVector.sub(DoubleVector)");
			System.exit(0);
		}
		for(int i = 0 ; i < vector.length ; ++i)
			vector[i] -= v.get(i);

	}

	// subtract two vectors and return a third vector that is the result
	public static DoubleVector sub(DoubleVector v1, DoubleVector v2) {

		if (v1.size() != v2.size()) {
			System.out.println("error:  vectors not same size in DoubleVector.vectorSub");
			System.exit(0);
		}

		DoubleVector v = new DoubleVector(v1.size());
		for(int i = 0 ; i < v.size() ; ++i)
			v.set(i, v1.get(i) - v2.get(i));

		return v;
	}



	// MULTIPLICATION

	public void multScalar (double scalar) {
		for(int i = 0 ; i < vector.length ; ++i)
			vector[i] *= scalar;
	}


	// multiply each element of the vector by a value in [lowVal, highVal)
	public void multRandomScalar(double lowVal, double highVal) {

		double range = highVal - lowVal;
		for(int i = 0 ; i < vector.length ; ++i)
			vector[i] *= lowVal + (Utilities.rand.nextDouble() * range);
	}




	// multiply each item in a vector by a scalar and return a second vector that is the result
	public static DoubleVector multVectorScalar (DoubleVector vectorIn, double scalar) {

		DoubleVector v = new DoubleVector(vectorIn.size());
		for(int i = 0 ; i < v.size() ; ++i)
			v.set(i, vectorIn.get(i) * scalar);

		return v;
	}



	// DIVISION
	
	// divide every item in a vector by a scalar and return a second vector that is the result
	public static DoubleVector divVectorScalar (DoubleVector vectorIn, double scalar) {

		DoubleVector v = new DoubleVector(vectorIn.size());
		for(int i = 0 ; i < v.size() ; ++i)
			v.set(i, vectorIn.get(i) / scalar);

		return v;
	}


	public void divScalar (double scalar) {
		for(int i = 0 ; i < vector.length ; ++i)
			vector[i] /= scalar;
	}



	// PRINTING

	public void println() {
		print();
		System.out.println();
	}


	public void print() {
		System.out.print("("); 
		for(int i = 0 ; i < vector.length ; ++i) {
			if (vector[i] >= 0.0)
				System.out.printf(" %.4e", vector[i]);
			else
				System.out.printf("%.4e", vector[i]);
			if (i < vector.length - 1)
				System.out.print(", ");
		}
		System.out.print(")");
	}



	// *************************************************************************************
	// *************************************************************************************
	// *************************************************************************************
	// OLD STUFF, CURRENTLY NOT USED
	// *************************************************************************************
	// *************************************************************************************
	// *************************************************************************************


	//	public void printlnExp() {
	//		printExp();
	//		System.out.println();
	//	}
	//
	//
	//	public void printExp() {
	//		System.out.print("("); 
	//		for(int i = 0 ; i < vector.length ; ++i) {
	//			if (vector[i] >= 0.0)
	//				System.out.printf(" %e", vector[i]);
	//			else
	//				System.out.printf("%e", vector[i]);
	//			if (i < vector.length - 1)
	//				System.out.print(", ");
	//		}
	//		System.out.print(")");
	//	}


	//	public void addComponent(DoubleVector targetPosition, DoubleVector currentPosition, double weight) {
	//
	//			for (int i = 0 ; i < vector.length ; ++i) {
	//				vector[i] += (targetPosition.get(i) - currentPosition.get(i)) * PSO.rand.nextDouble() * weight;
	//			}
	//
	//		}
	//


	// for each element, take the average of this vector and the one sent in and
	// set the value of this vector 
	//	public void averageIn(DoubleVector v) {
	//
	//		if (v.size() != vector.length) {
	//			System.out.println("error:  vectors not same size in DoubleVector.averageIn(DoubleVector)");
	//			System.exit(0);
	//		}
	//
	//		for(int i = 0 ; i < v.size() ; ++i)
	//			vector[i] = (vector[i] + v.get(i)) / 2.0;
	//
	//	}


	//	public void averageInWeighted(double thisWeight, DoubleVector v, double incomingWeight) {
	//
	//		if (v.size() != vector.length) {
	//			System.out.println("error:  vectors not same size in averageIn(DoubleVector)");
	//			System.exit(0);
	//		}
	//
	//		double sumOfWeights = thisWeight + incomingWeight;
	//		for(int i = 0 ; i < v.size() ; ++i)
	//			vector[i] = ((vector[i] * thisWeight) + (v.get(i) * incomingWeight)) / sumOfWeights;
	//
	//	}


	//	public static DoubleVector addWeighted(DoubleVector v1, double strengthV1, DoubleVector v2, double strengthV2) {
	//
	//		if (v1.size() != v2.size()) {
	//			System.out.println("error:  vectors not same size in addWeighted(DoubleVector, DoubleVector");
	//			System.exit(0);
	//		}
	//
	//		double sumStrengths = strengthV1 + strengthV2;
	//		DoubleVector v = new DoubleVector(v1.size());
	//		for(int i = 0 ; i < v.size() ; ++i)
	//			v.set(i, ((v1.get(i) * strengthV1 + v2.get(i) * strengthV2)) / sumStrengths);
	//
	//		return v;
	//	}



}

