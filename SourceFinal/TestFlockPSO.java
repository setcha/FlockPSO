
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class TestFlockPSO {

	// this will hold testing data for every individual in every generation;
	// it is public so it can be accessed from the Population class
	public static TestResults testResults;
    
	
	
    //order of arguments: numGens, numTrees, numPSORuns,  maxCrossoverTries, , maxTreeDepth, numIterations, probCrossover, probMutation
	public static void main(String[] args) {
        
		try {
			
			// create a PrintWriter that sends things to the screen
			PrintWriter outputWindow = new PrintWriter(System.out);

			// create date string for screen output and output file names
			SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd--hh.mm.ss.SSS-a");
			Calendar date = Calendar.getInstance();
			String dateString = dateformatter.format(date.getTime());
			
			outputWindow.print("STARTING RUN ON " + dateString + "\n\n");
            
			//randomized key that differentiates runs started at the exact same time
			int runKey = Utilities.rand.nextInt();
			
			// create a PrintWriter that sends things to a file
			// NOTE: there needs to be "results" folder on the same level as the "src" folder
			// before the program is run; Java will not create it
			PrintWriter dataFile = new PrintWriter(new FileWriter("results/" + dateString + runKey + ".txt"));
			dataFile.print("# STARTING RUN ON " + dateString + "\n# \n");		

            
            // create a Parameters object; currently all the values are set in the
            // Parameters constructor
            Parameters parameters = new Parameters();
            //the runKey is used in Parameters when FlockTracker data needs to be generated
            parameters.runKey = runKey;
            
            //parse the integer arguments
            ArrayList<Integer> ParametersArray = new ArrayList<Integer>();
            //ParametersArray.add(0)
            
            for (int i = 0; i < 8; i++){
                //Parsing Integers
                try {
                    // Parse the string argument into an integer value.
                    ParametersArray.add(Integer.parseInt(args[i]));
                }
                catch (NumberFormatException nfe) {
                    // The first argument isn't a valid integer.  Print
                    // an error message, then exit with an error code.
                    System.out.println("Argument number "+ i +" must be an integer.");
                    System.exit(1);
                }
            }
            //******add elitistNum to this list
            parameters.numGens = ParametersArray.get(0);
            parameters.numTrees = ParametersArray.get(1);
            parameters.numPSORuns = ParametersArray.get(2);
            parameters.functionNum = ParametersArray.get(3);
            parameters.maxTreeDepth = ParametersArray.get(4);
            parameters.numIterations = ParametersArray.get(5);
            parameters.dummyParticles = ParametersArray.get(6);
            parameters.FLOCK_SPACE_DIM_LENGTH = ParametersArray.get(7);
            
            //parse the Double arguments
            ArrayList<Double> DoubleParametersArray = new ArrayList<Double>();
            //ParametersArray.add(0)
            
            for (int j = 8; j < 10; j++){
                //Parsing Doubles
                try {
                    // Parse the string argument into an integer value.
                    DoubleParametersArray.add(Double.parseDouble(args[j]));
                }
                catch (NumberFormatException nfe) {
                    // The first argument isn't a valid integer.  Print
                    // an error message, then exit with an error code.
                    System.out.println("Argument number "+ j +" must be a Double.");
                    System.exit(1);
                }
            }
            parameters.probCrossover = DoubleParametersArray.get(0);
            parameters.probMutation = DoubleParametersArray.get(1);
            
            // create TestResults object for given number of generations and individuals
            testResults = new TestResults(Parameters.numGens, Parameters.numTrees);
            
			parameters.outputParameters(outputWindow, false);
			parameters.outputParameters(dataFile, true);

			// create a population of random trees and run the GP
			Population population = new Population();
			
			//Utilities.setConsistentSeed();
			population.run();

			// calculate the stats, e.g. mean , median, minimum, and maximum fitness
			// for each generation; see TestResults.java for more information
			testResults.calcStats();
			
            dataFile.println("# ");
            dataFile.println("# Best Fitness: " + Parameters.bestFitness);
            dataFile.println("# Best function value: " + Parameters.bestValue);
            testResults.outputData(dataFile);
            
            //we also want to see what the best tree looks like
            Parameters.bestTree.printTree(dataFile);
            
            
			date = Calendar.getInstance();
			dateString = dateformatter.format(date.getTime());
			outputWindow.print("\nFINISHED RUN ON " + dateString + "\n");	
			outputWindow.flush();
			
			dataFile.println("# \n# FINISHED RUN ON " + dateString + "\n");	
			dataFile.flush();
			dataFile.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}


}
