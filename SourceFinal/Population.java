
import java.util.ArrayList;
import java.util.Calendar;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Population{
	
	// population of trees
	private ArrayList<GPTree> population; 
	private double selectValue;
	private double selectPrintProb = 0.0;
	private double flockPrintProb = 0.0002;
	
	public Population() {
		
		//generate the specified number of random trees
		this.population = new ArrayList<GPTree>();		
		for (int i = 0; i < Parameters.numTrees; i++) {
			GPTree newTree = new GPTree();
			newTree.treeID = i;
			population.add(newTree);
		}
		
		// calculate fitness of trees
		for (int i = 0; i < Parameters.numTrees; i++) {
			GPTree nextTree = population.get(i);
			nextTree.setFitness(calcFitness(nextTree));
		}
		
		Collections.sort(this.population, new GPTreeComparator());
		
	}
	
	
	// run the specified number of generations;
	// save the fitness of each particle in each generation to testResults;
	// also, print out the fitnesses just to see how things are going
	public void run() {	
		
		for(int i = 0; i < Parameters.numGens; i++) {

			System.out.println("Generation " + i + " Fitnesses:");
			for (int j = 0; j < Parameters.numTrees; j++) {
				double fitness = population.get(j).getFitness();
				System.out.printf("    %3d: %7.3f\n", j, fitness);
				TestFlockPSO.testResults.setFitness(i, j, fitness);
				population.get(j).generation = j;
			}
			System.out.println();

			// advance one generation
			runSingleGeneration();
		}
	}



	// Runs one generation, applying crossover and mutation
	// NOTE: assumes that population size is even number                                      NOT A PROBLEM **********************
	public void runSingleGeneration() {
		
		ArrayList<GPTree> nextPopulation = new ArrayList<GPTree>();
		int populationSize = population.size();
		
		selectValue = Utilities.rand.nextDouble();
		
		SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd--hh.mm.ss.SSS-a");
		Calendar date = Calendar.getInstance();
		String dateString = dateformatter.format(date.getTime());
		System.out.println(dateString);
		System.out.println("");
		
		//to turn elitism off, just set eliteNumber to 0
		for (int j = 0; j < Parameters.eliteNumber; j++) {
			population.get(j).setFitness(calcFitness(population.get(j)));
			nextPopulation.add(population.get(j).getDeepCopy());
			System.out.println("child " + nextPopulation.size() + " fitness: " + population.get(j).getFitness());
			System.out.println("");
			
			//maybe add an "age" to the elite ones
		}
		
		
		while (nextPopulation.size() < populationSize) {
			
			//for every family in every population, determine if they are printed or not
			selectValue = Utilities.rand.nextDouble();
			
			// get parents and make deep copies of them (not just references);
			// those copies will be the children after we do crossover and mutation on them
			GPTree parent1 = tournamentSelection();
			GPTree child1 = parent1.getDeepCopy();   
			
			GPTree parent2 = tournamentSelection();
			GPTree child2 = parent2.getDeepCopy();  
			
			// crossover
			if(Utilities.rand.nextDouble() <= Parameters.probCrossover) { 
//				
//				if (selectValue <= selectPrintProb) {
//					System.out.println("Before Crossover");
//					System.out.println("");
//					System.out.println("parent1");
//					parent1.printTree();				
//					System.out.println("parent2");
//					parent2.printTree();
//				}
//				
				singlePointCrossover(child1, child2); 
//				
//				if (selectValue <= selectPrintProb) {
//					System.out.println("After Crossover");
//					System.out.println("");
//					System.out.println("child1");
//					child1.printTree();
//					System.out.println("child2");
//					child2.printTree();
//				}
			}
			
//			if (selectValue <= selectPrintProb) {
//				System.out.println("Before Mutation:");
//				System.out.println("");
//				System.out.println("parent1");
//				parent1.printTree();				
//				System.out.println("parent2");
//				parent2.printTree();
//			}
			// mutate
			mutateTree(child1);
			mutateTree(child2);
			
			ArrayList<GPNode> parent1Nodes = parent1.toArrayList();
			ArrayList<GPNode> parent2Nodes = parent2.toArrayList();
			ArrayList<GPNode> child1Nodes = child1.toArrayList();
			ArrayList<GPNode> child2Nodes = child2.toArrayList();
			
			
			if (selectValue <= selectPrintProb && child1Nodes.size() > parent1Nodes.size()) {
				System.out.println("Before Mutation:");
				System.out.println("");
				System.out.println("parent1");
				parent1.printTree();				
				
				System.out.println("After Mutation");
				System.out.println("");
				System.out.println("child1");
				child1.printTree();
			}
			
			if (selectValue <= selectPrintProb && child2Nodes.size() > parent2Nodes.size()) {
				System.out.println("Before Mutation:");
				System.out.println("");			
				System.out.println("parent2");
				parent2.printTree();
				
				System.out.println("After Mutation");
				System.out.println("");
				System.out.println("child2");
				child2.printTree();
			}
			

			// how fit?
			child1.setFitness(calcFitness(child1));
			child2.setFitness(calcFitness(child2));
			
			
			
			// add to next generation
			nextPopulation.add(child1);
			System.out.println("child " + nextPopulation.size() + " fitness: " + child1.getFitness());
			System.out.println("");
			
			nextPopulation.add(child2);
			System.out.println("child " + nextPopulation.size() + " fitness: " + child2.getFitness());
			System.out.println("");
		}
		
		Collections.sort(nextPopulation, new GPTreeComparator());
		population = nextPopulation;
		
	}



	// calculate the fitness of a tree
	public double calcFitness(GPTree tree) {
		
		// a PSO object takes care of running PSO some number of times
		// in order to evaluate the tree
		PSO pso = new PSO(tree);
		FitnessResult fitResult = pso.evalGPTree();
		
		// scales mwScore, which will be 0.0 to 1.0, to be 
		// 0.0 to "fitnessInterval"
		// an MWScore of 0.0, if S-PSO did better, means S-PSO crushed FLOCK-PSO,
		//                    so fitness of 0.0
		// at the other end:
		// an MWScore of 0.0, if FLOCK-PSO did better, means FLOCK-PSO crushed S-PSO,
		//                    so fitness of "fitnessInterval"
		double fitScore = fitResult.getFunctionValue().getFitScore();
		double distScore = fitResult.getDistGlobalOpt().getFitScore();
		int treeDepth = tree.treeDepth;
		
		
		
        if (fitScore * Parameters.fitnessInterval > Parameters.bestFitness){
            Parameters.bestFitness = fitScore * Parameters.fitnessInterval;
            Parameters.bestValue = fitResult.getFunctionValue().getU();
            Parameters.bestTree = tree.getDeepCopy();
        }
        
		//don't really even need this if statement
		if (fitResult.getFunctionValue().flockDidBetter()) {
			//return (2.0 - fitScore) * Parameters.fitnessInterval/2;  
					
			//tree.printTree();
			//parent1.printTree();
			//child1.printTree();
			//parent2.printTree();
			//child2.printTree();
					
			//include weights in Parameters! don't have them hard-coded
			return Parameters.fitnessInterval * (fitScore * 1 );// - treeDepth; // + distScore * .05;
		} 
		
		//return fitScore * Parameters.fitnessInterval/2; 
		//tree.printTree();
        
		selectValue = Utilities.nextDouble(0,1);
		if(selectValue <= flockPrintProb) {//|| Parameters.fitnessInterval * (fitScore) < -1000
			
			System.out.println("###PRINTING FLOCK DATA###");
			SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd--hh.mm.ss.SSS-a");
			Calendar date = Calendar.getInstance();
			String dateString = dateformatter.format(date.getTime());
			System.out.println(dateString + "K" + Parameters.runKey + ".csv");
			Parameters.currFlockData.outputDataCSV();
			System.out.println("Fitness: " + (Parameters.fitnessInterval * (fitScore * 1 )));
			System.out.println("");
			tree.printTree();
			
			
		}
		
		//include weights in Parameters! don't have them hard-coded
		return Parameters.fitnessInterval * (fitScore * 1 );// - treeDepth;// + distScore * .05;
	}


	
	// pick two individuals randomly; return the one with higher fitness
	// ******************************************************************
	// ******************************************************************
	// NOTE: THIS NEEDS TO BE CHANGED TO PREVENT THE SAME
	//       INDIVIDUAL FROM BEING CHOSEN TWICE
	// ******************************************************************
	// ******************************************************************
	// NOTE: ALSO NEED TO IMPLEMENT OTHER SELECTION TECHNIQUES
	// ******************************************************************
	// ******************************************************************
	public GPTree tournamentSelection() {
		
		int treeIndex1 = Utilities.rand.nextInt(population.size());
		int treeIndex2 = Utilities.rand.nextInt(population.size());		
		
		//non-duplicate code
		while (treeIndex2 == treeIndex1) {
			treeIndex2 = Utilities.rand.nextInt(population.size());	
		}
		
		GPTree tree1 = population.get(treeIndex1);
		GPTree tree2 = population.get(treeIndex2);	
		
		double tree1Fitness = tree1.getFitness();
		double tree2Fitness = tree2.getFitness();
		
		if (tree1Fitness >= tree2Fitness) {
			return tree1;
		}//else?
		return tree2;
		
		/*if (tree1Fitness >= tree2Fitness) {
			//remove tree1 from population here, and return it
			return population.remove(treeIndex1);
		}
		//remove tree 2 from population here, and return it
		return population.remove(treeIndex2);*/

	}

	
	// Pick a point on each tree and swap the subtrees rooted at those points
	public void singlePointCrossover(GPTree t1, GPTree t2) {

		// easier to pick a crossover point if nodes are in a list instead of a tree
		// because we can just pick an index; nodes are ordered most->least depth
		ArrayList<GPNode> t1Nodes = t1.toArrayList();
		ArrayList<GPNode> t2Nodes = t2.toArrayList();

		int numNodesT1 = t1Nodes.size();
		int numNodesT2 = t2Nodes.size();

		// we will pick a random node in tree 1 and see if there are nodes in
		// tree 2 that are compatible; if so, we will swap those nodes (and,
		// as a result, the subtrees rooted at those nodes
		GPNode nodeFromT1Nodes = null;
		GPNode nodeFromT2Nodes = null;
		
		int numCompatibleNodes = 0;
		ArrayList<Integer> compatibleT2NodeIndices = new ArrayList<Integer>();
		
		// we only allow a certain number of tries here
		int tries = 0;
		int index = 0;
		int selectedCompatibleT2NodesIndex = 0;
		
		boolean headerCrossover = false;
		
		while (numCompatibleNodes == 0 && tries < Parameters.maxCrossoverTries && !headerCrossover) {
			
			compatibleT2NodeIndices.clear();
 
			// randomly select node from t1Nodes (but not the root)
			// random number range starts at 1 to avoid the root
			index = Utilities.nextInt(1, numNodesT1);
			nodeFromT1Nodes = t1Nodes.get(index);
			
			if (index < 26 && index != 9) { // 1 (root) + 9 (depth 1) + 16 (depth 2 for assigning VARs). index 9 is IF node
				if (index < 9) {
					nodeFromT2Nodes = t2Nodes.get(index);
				}
				else { //index > 9
					nodeFromT1Nodes = t1Nodes.get(index).getParent(); //want to swap over a full set of sequence children
					nodeFromT2Nodes = t2Nodes.get(index).getParent();  
				}
				headerCrossover = true;
			}
			
			GPNode.NodeType t1NodeType = nodeFromT1Nodes.getNodeType();		
			GPNode.ReturnType t1NodeRetType = nodeFromT1Nodes.getReturnType();

			// collect the indices of compatible nodes from t2Nodes (but not the root)
			if (t1NodeType != GPNode.NodeType.VAR && t1NodeType != GPNode.NodeType.CONST) {                   // NECESSARY????
				// i starts at 1 to avoid root
				for (int i = 1; i < numNodesT2; i++) {
					GPNode t2Node = t2Nodes.get(i);
					if (t2Node.getReturnType() == t1NodeRetType && 
							t2Node.getNodeType() != GPNode.NodeType.CONST &&
							    t2Node.getNodeType() != GPNode.NodeType.VAR) {
						compatibleT2NodeIndices.add(0, i);
					}
				}
			}

			numCompatibleNodes = compatibleT2NodeIndices.size();
			//System.out.println("numCompatibleNodes = " + numCompatibleNodes);
			tries++;
		} 
		
		if(headerCrossover) {
			//just swap all of the roots children. make sure not to get the roots mixed up
			//node from t1 and t2 is a HEADER_ASSIGN node, index 1-8
			GPNode root1 = nodeFromT1Nodes.getParent();
			GPNode root2 = nodeFromT2Nodes.getParent();
			ArrayList<GPNode> siblings1 = root1.getChildren();
			ArrayList<GPNode> siblings2 = root2.getChildren();
			
			//swap the root children arrays at index by repeatedly taking away the children at the index
			for(int i = 8; i > index - 2; i--) {
				siblings2.get(index - 1).setParent(root1);
				siblings1.add(siblings2.get(index - 1));
			    //remove the ones from 2 that we don't need
				siblings2.remove(index - 1);
			}	
			for(int j = 8; j > index - 2; j--) {
				siblings1.get(index - 1).setParent(root2);
				siblings2.add(siblings1.get(index - 1));
				//remove the ones from 1 that we don't need
				siblings1.remove(index - 1);
			}	
			
		}
		else {
			if (numCompatibleNodes == 0) {
				//System.out.println("maximum number of tries in Population.singlePointCrossover exceeded");
				return;
			}

			// make a list of weights of the compatible nodes from tree 2;
			// weight for each node candidate is its depth, so we will tend to do
			// crossover lower down in the tree; this makes crossover changes less dramatic                          IS THIS GOOD?????????
			ArrayList<Integer> weights = new ArrayList<Integer>();
			for(int i = 0; i < numCompatibleNodes; i++) {
				weights.add(t2Nodes.get(compatibleT2NodeIndices.get(i)).getDepth());
			}

			// select an index proportional to the weights
			int secIndex = selectIndexWeighted(weights);
			selectedCompatibleT2NodesIndex = compatibleT2NodeIndices.get(secIndex);
			nodeFromT2Nodes = t2Nodes.get(selectedCompatibleT2NodesIndex);


			// swap parent references
			GPNode nodeFromT1NodesParent = nodeFromT1Nodes.getParent();
			GPNode nodeFromT2NodesParent = nodeFromT2Nodes.getParent();
			//nodeFromT2Nodes.getParent().setChildren(nodeFromT1NodesParent.getChildren()), but just with 1 child

			//get children from a parent (ArrayList)
			//find the one we need to switch with: ArrayList node index is (nodeFromT1Nodes index - nodeFromT1Nodes.getParent().getIndex (gives parentIndex) - 1)
			//ChildrenArrayList.set(index, nodeFromT2Nodes)
			//same for other

			ArrayList<GPNode> siblingsT1 = nodeFromT1NodesParent.getChildren();
			ArrayList<GPNode> siblingsT2 = nodeFromT2NodesParent.getChildren();

			//System.out.println(selectedCompatibleT2NodesIndex + " - " + siblingsT2.get(0).getIndex() + " = " + (selectedCompatibleT2NodesIndex - siblingsT2.get(0).getIndex()));	
			//System.out.println(index + " - " + siblingsT1.get(0).getIndex() + " = " + (index - siblingsT1.get(0).getIndex()));

			//using the fact that children are listed consecutively
			int indexT2 = selectedCompatibleT2NodesIndex - siblingsT2.get(0).getIndex();
			int indexT1 = index - siblingsT1.get(0).getIndex();

			//at the correct spot in t1 children, replace with T2 node
			siblingsT1.set(indexT1, nodeFromT2Nodes);//nodeFromT2Nodes.getParent().getIndex() - 1, nodeFromT2Nodes); 

			//error always on the second one... are we altering the tree in the first one? should we make copies?
			//at the correct spot in t2 children, replace with T1 node
			siblingsT2.set(indexT2, nodeFromT1Nodes);//nodeFromT1Nodes.getParent().getIndex() - 1, nodeFromT1Nodes); 

			nodeFromT1NodesParent.setChildren(siblingsT1);
			nodeFromT2NodesParent.setChildren(siblingsT2);

			nodeFromT1Nodes.setParent(nodeFromT2NodesParent);
			nodeFromT2Nodes.setParent(nodeFromT1NodesParent);
			//		
			//		if (selectValue <= selectPrintProb) {
			//			System.out.println("Crossover Points ");
			//			System.out.println("Node from tree 1 chosen: " + index);
			//			System.out.println("Node from tree 2 chosen: " + selectedCompatibleT2NodesIndex);
			//		}		
			//set the children of the parent nodes too

			//node.setChildren(children) //children is ArrayList of nodes

			// recalculate depths in subtree nodes
			// NOTE: will swap depths in nodeFromT1Nodes and nodeFromT2Nodes
			int nodeFromT1NodesDepth = nodeFromT1Nodes.getDepth();
			int nodeFromT2NodesDepth = nodeFromT2Nodes.getDepth();
			recalculateDepths(nodeFromT1Nodes, nodeFromT2NodesDepth);
			recalculateDepths(nodeFromT2Nodes, nodeFromT1NodesDepth);
		}
	}

	
	
	//	 Given an array of raw weights, randomly select an index proportional
	//	 to the weights and return it.
	//	 Useful for crossover, single mutation, subtree mutation
	//   NOTE:
	//   might be able to do this more easily with an EnumeratedIntegerDistribution
	//   in the Apache Commons Math library
	public int selectIndexWeighted(ArrayList<Integer> weights) {          
				
		// calculate the cumulative weights
		ArrayList<Integer> cumulativeWeights = new ArrayList<Integer>();
		cumulativeWeights.add(weights.get(0));
		for (int i = 1; i < weights.size(); i++) { 
			cumulativeWeights.add(cumulativeWeights.get(i-1) + weights.get(i));
		}
		
		// calculate the sum of the weights
		int sumWeights = 0;
		for (int i = 0; i < weights.size(); i++) { 
			sumWeights += weights.get(i); 
		}
		
		// generate random number and find what interval it falls in
		int threshold = Utilities.nextInt(1, sumWeights+1);
		for (int i = 0; i < cumulativeWeights.size(); i++) { 
			if (threshold <= cumulativeWeights.get(i)) {
				return i;
			}
		}

		// should never get here
		return 0;
	}


	// after crossover, depth information in nodes of swapped subtrees is
	// probably wrong, so recalculate
	public void recalculateDepths(GPNode root, int startingDepth) {
		root.setDepth(startingDepth);
		for (int i = 0; i < root.getChildren().size(); ++i) {
			recalculateDepths(root.getChildren().get(i), startingDepth+1);
		}
	}
	
	
	// mutate each node with probability probMutation
	public void mutateTree(GPTree tree) {
		
		// easier to pick a crossover point if nodes are in a list instead of a tree
		// because we can just pick an index; nodes are ordered most->least depth
		ArrayList<GPNode> treeNodes = tree.toArrayList();
		int mutationCount = 0;
		
		// mutate each node with probability Parameters.probMutation
		//starting at 1 now to avoid picking the root node
		for(int i = 1; i < treeNodes.size(); i++) {
			if(Utilities.rand.nextDouble() <= Parameters.probMutation) {	
				
				mutationCount += 1;
				
				//are we actually changing the tree here? do we need to use
				//something like tree.fromArraytoTree?
				mutateNode(treeNodes.get(i));
			}
		}
		//possibly want to do a toArrayList here to give everything correct indices
		//System.out.println(mutationCount + " mutations in this tree.");
	}

	
	// mutation = replace subtree rooted at "node" with a new subtree that has the
	// same ReturnType
	// NOTE 1: mutation probability is applied in method mutateTree
	// NOTE 2: generateSubtree makes the parent reference of the new subtree
	//         root refer to the parent of "node"
	// NOTE 3: the "false" value of last argument means node is non-terminal                              // IS THIS OKAY????????????????/
	public void mutateNode(GPNode node) {  
		//just do the child setting here
		
		//node.printNode();
		int index = node.getIndex();
		
		//if we are in the header- maybe set a constant called sizeofHeader
		if (index < 26 && index != 9 && index != 0) {
			if (index < 9) {
				node = node.getChildren().get(1); //the const node
				//maybe assert const here
			}
			else { //index > 9
				node = node.getParent().getChildren().get(1); //the const node
				//maybe assert const here
			}
			double change = Utilities.nextDouble(0.9, 1.1); //random const change between -10% and +10%
			node.setConstValue(node.getConstValue() * change); 
		}
		else if(index == 0){
			//changing the starting seed
			node.setConstValue(node.getConstValue() + Utilities.nextInt(-10, 10));
		}
		else {

			//this "new" stuff probably isn't necessary, but I'm scared if I change it I'll break it
			GPNode parent = new GPNode();
			parent = node.getParent();
			ArrayList<GPNode> siblings = new ArrayList<GPNode>();
			siblings = parent.getChildren();

			GPNode child = GPTree.generateSubtree(node.getParent(), node.getDepth(), node.getReturnType(), false);//this already has its parent as the 

			//print parent index because we haven't really set stuff for the new node
			//System.out.println("Parent index: " + parent.getIndex());	

			//IDEA: append it to the end of the siblings, then do all of the switching back up in mutate node

			//using the fact that children are listed consecutively
			//		System.out.println(node.getIndex() + " - " + siblings.get(0).getIndex() + " = " + (node.getIndex() - siblings.get(0).getIndex()));	
			//		System.out.println("");
			//index out of bounds here
			int siblingIndex = node.getIndex() - siblings.get(0).getIndex();
			child.setIndex(node.getIndex());

			//index out of bounds here
			//could it be that the mutations are changing the tree before the rest of it?
			//yes, we need to go through the arrayList without modifying it, then 
			siblings.set(siblingIndex, child); //nodeFromT2Nodes.getParent().getIndex() - 1, nodeFromT2Nodes);
			//		System.out.println("Sibling Array:");
			//		System.out.println("");
			//		for (int i = 0; i < siblings.size(); i++) {
			//			siblings.get(i).printNode();
			//		}

			parent.setChildren(siblings);
		}

	}
	

}
