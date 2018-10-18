import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.io.PrintWriter;
import java.util.*;

import java.io.*;

public class GPTree {
	
	// names of flock parameter variables in the Particle class that could be needed in a GPTree
	public static final String[] VAR_NAMES = { "maxSpeed", "normalSpeed", "neighborRadius",
										"separationWeight", "alignmentWeight", "cohesionWeight", "pacekeepingWeight",
										"randomMotionProbability", "numNeighborsOwnFlock", "numNeighborsAllFlocks" };

	// root node of tree
	private GPNode root;
	
	// needed for breeding
	private double fitness;
	
	public int mutatingNode;
	
	// needed for fitness depth penalty
	public static int treeDepth;

	//keep track of specific trees
	public static int treeID;
	
	public int numNodes;
	
	//set seed for repeatable randomness
	public int seed;
	
	//for use in seeing the flocking
	public int generation;
	
	public GPTree() {
		generateTree();
		fitness = 0;
		generation = 0;
	}


	// make completely separate copy, i.e. no references to nodes in tree being copied
	public GPTree getDeepCopy() {
		GPTree copy = new GPTree();
		copy.setRoot(root.getDeepCopy(null));
		copy.setFitness(fitness);
		copy.generation = generation;
		
		
		return copy;
	}
	
	
	// called from the Particle class at the end of the moveFlockAndGetNeighbors method
	// running the tree requires the particle so that the tree has access to the particle's variables;
	public void run(Particle particle) {
		root.evaluate(particle);
	}



	// generate a tree: 
	// ****************************************************************************************************************
	// NOTE: currently, a tree always has an IF node at the root; 
	// SEQUENCE nodes would also make sense                                          THIS SHOULD BE IMPLEMENTED!!!!!!!!
	// ****************************************************************************************************************
	public void generateTree() {
		
		//randomize, because I was trying to get some repeatable results by setting the seed elsewhere
		seed = (int) System.nanoTime();
		
//		// The root (depth 0) must be an IF or SEQUENCE
//		//
		root = new GPNode(null, 0, GPNode.ReturnType.NONE);
		
//		root.setNodeType(GPNode.NodeType.IF);
		//Create a header node
		root.setNodeType(GPNode.NodeType.HEADER_SEQUENCE);
		
		//The following doesn't actually do anything, I was trying to 
		// just set the seed to a wide range
		root.setConstValue(Utilities.nextInt(0, 100000000));
//		
//		//after the header, the depth is 2
		int depth = 2;
//		
//		//use Swarm.generateRandomParameters
		double[] RandomParameters = Swarm.generateRandomParameters();
		
//		// child 1 of IF is BOOLEAN            
//		GPNode newChild = generateSubtree(root, depth, GPNode.ReturnType.BOOLEAN, false);
		
		//Create the header nodes
		for (int i = 0; i < Parameters.headerLength; i++) {	
			
			//assign node
			root.getChildren().add(new GPNode(root, 1, GPNode.ReturnType.NONE)); 
			GPNode currentNode = root.getChildren().get(i);
			currentNode.setNodeType(GPNode.NodeType.HEADER_ASSIGN);
			//set each HEADER_ASSIGN the root (a HEADER_SEQUENCE) as parent
			
			//children of assign node- they set every flock parameter that needs to be set
			
			//var node first
			GPNode VarChild = new GPNode(currentNode, 2, GPNode.ReturnType.STRING);
			currentNode.getChildren().add(VarChild);
			VarChild.setNodeType(GPNode.NodeType.HEADER_VAR);	
			VarChild.setVarName(VAR_NAMES[i]);
			
			//const node second
			GPNode ConstChild = new GPNode(currentNode, 2, GPNode.ReturnType.DOUBLE);
			currentNode.getChildren().add(ConstChild);
			ConstChild.setNodeType(GPNode.NodeType.HEADER_CONST);	
			ConstChild.setConstValue(RandomParameters[i]);
			
			 
			
		}
		
		//the last node in the HEADER_SEQUENCE is an if node
		GPNode ifNode = new GPNode(root, 1, GPNode.ReturnType.NONE);
		ifNode.setNodeType(GPNode.NodeType.IF);
		root.getChildren().add(ifNode);
		
		// child 1 of IF is BOOLEAN   
		GPNode newChild = generateSubtree(ifNode, depth, GPNode.ReturnType.BOOLEAN, false);
		ifNode.getChildren().add(newChild);
		//add another child here for the initial if node
		//continue the tree: GPNode newChild = generateSubtree(root, depth, GPNode.ReturnType.BOOLEAN, false);
		
//		// either 1 more child (if) or 2 more children (if-else);
//		// decide randomly
		int numChildren = determineNumIfChildren();
//
//		// child 2 and child 3 (if present) must evaluate to NONE
		for(int i = 1; i < numChildren; i++) {
			newChild = generateSubtree(ifNode, depth, GPNode.ReturnType.NONE, false);
			ifNode.getChildren().add(newChild);
		}

	}

	// probably not needed at this point
//	public static GPNode generateSubtree(GPNode parent, int depth, GPNode.ReturnType returnType) {
//		return generateSubtreeAux(parent, depth, returnType, false);
//	}

	
	// parameters:
	//   parent is the parent of the root of the subtree being created
	//   depth is the depth of the root of subtree being created
	//   returnType is the return/evaluation type of the root of the subtree
	//   terminal indicates if we want to end this branch
	public static GPNode generateSubtree(GPNode parent, int depth, GPNode.ReturnType returnType, boolean terminal, GPNode node) {

		// Create subtree root
		GPNode subtreeRoot = new GPNode(parent, depth, returnType);
		subtreeRoot.setParent(parent);
		GPNode.NodeType nodeType;
		
		//here, set the children of subtreeRoot's parent
		
		//nullpointer exceptions here
		ArrayList<GPNode> siblings = parent.getChildren();

		//print parent index because we havent really set stuff for the new node
		//System.out.println("Parent index: " + parent.getIndex());	

		
		//using the fact that children are listed consecutively
		System.out.println(node.getIndex() + " - " + siblings.get(0).getIndex() + " = " + (node.getIndex() - siblings.get(0).getIndex()));	
		
		//index out of bounds here
		int siblingIndex = node.getIndex() - siblings.get(0).getIndex();
		
		siblings.set(siblingIndex, subtreeRoot); //nodeFromT2Nodes.getParent().getIndex() - 1, nodeFromT2Nodes); 
		parent.setChildren(siblings);

				
		//
		// SET THE NODE TYPE OF THE SUBTREE ROOT
		//
		
		// TERMINAL NODE?
		// Probability that will be terminal; the greater the depth, higher the probability
		double probabilityTerminal = depth * 1.0 / Parameters.maxTreeDepth;
//		boolean terminal = false; // <-- to be used later when making recursive call                          ?????????
		
		if (terminal || Utilities.rand.nextDouble() <= probabilityTerminal) {
			terminal = true;
			
			//needed for fitness depth penalty
			if (depth > treeDepth) {
				treeDepth = depth;
			}
				
			// get random terminal node of the specified type
			if (returnType == GPNode.ReturnType.DOUBLE) {
				nodeType = GPNode.getRandomTerminalNode(GPNode.DOUBLE_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.BOOLEAN) {
				nodeType = GPNode.getRandomTerminalNode(GPNode.BOOLEAN_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.STRING) {
				nodeType = GPNode.getRandomTerminalNode(GPNode.STRING_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.NONE) {
				nodeType = GPNode.getRandomTerminalNode(GPNode.NONE_FLAG);
			} 
			else {
				System.out.println("error: unknown return type in GPTree.generateSubtree");
				return null;
			}
			subtreeRoot.setNodeType(nodeType);
		}
		
		// TERMINAL NODE?
		// get random NON-terminal node of the specified type
		else {
			terminal = false;
			
			if (returnType == GPNode.ReturnType.DOUBLE) {
				nodeType = GPNode.getRandomNode(GPNode.DOUBLE_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.BOOLEAN) {
				nodeType = GPNode.getRandomNode(GPNode.BOOLEAN_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.STRING) {
				nodeType = GPNode.getRandomNode(GPNode.STRING_FLAG);
			} 			
			else if (returnType == GPNode.ReturnType.NONE) {
				nodeType = GPNode.getRandomNode(GPNode.NONE_FLAG);
			} 
			else {
				System.out.println("error: unknown return type in GPTree.generateSubtree");
				return null;
			}
		}

		subtreeRoot.setNodeType(nodeType);
		

		//
		// ADD CHILDREN TO THE SUBTREE ROOT
		//
		
		// variables and constants don't have children (sad...)
		
		// if it's a constant, set its value
		if (nodeType == GPNode.NodeType.CONST) {
			
			//maybe replace .5 with pSmallConstantValue to avoid hard coding?
			if(Utilities.rand.nextDouble() < .5) {
				subtreeRoot.setConstValue(Utilities.nextDouble(0, 1));
			}
			else {
				subtreeRoot.setConstValue(Utilities.nextDouble(Parameters.minConstant, Parameters.maxConstant));
			}
		} 
		
		// if it's a variable, set its name
		else if (nodeType == GPNode.NodeType.VAR) {
			int randIndex = Utilities.nextInt(0, VAR_NAMES.length);
			subtreeRoot.setVarName(VAR_NAMES[randIndex]);
		}
		
		// add children if not a variable or constant
		else {
			ArrayList<GPNode> children = generateChildren(subtreeRoot, depth+1, nodeType, terminal);
			subtreeRoot.setChildren(children);
		}
		
		return subtreeRoot;
	}
	
	//original, unmodified generateSubtree
	// parameters:
		//   parent is the parent of the root of the subtree being created
		//   depth is the depth of the root of subtree being created
		//   returnType is the return/evaluation type of the root of the subtree
		//   terminal indicates if we want to end this branch
	public static GPNode generateSubtree(GPNode parent, int depth, GPNode.ReturnType returnType, boolean terminal) {

		// Create subtree root
		GPNode subtreeRoot = new GPNode(parent, depth, returnType);
		subtreeRoot.setParent(parent);
		GPNode.NodeType nodeType; 
		
		//
		// SET THE NODE TYPE OF THE SUBTREE ROOT
		//
		
		// TERMINAL NODE?
		// Probability that will be terminal; the greater the depth, higher the probability
		double probabilityTerminal = depth * 1.0 / (Parameters.maxTreeDepth + 1); //************** I changed this to + 1 to try to counteract the fact
																								// that terminal leafs can reach below the bottom of the 
																								// max depth of the tree
//		boolean terminal = false; // <-- to be used later when making recursive call                          ?????????
		
		if (terminal || Utilities.rand.nextDouble() <= probabilityTerminal) {
			terminal = true;
			
			//needed for fitness depth penalty
			if (depth > treeDepth) {
				treeDepth = depth;
			}
				
			// get random terminal node of the specified type
			if (returnType == GPNode.ReturnType.DOUBLE) {
				nodeType = GPNode.getRandomTerminalNode(GPNode.DOUBLE_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.BOOLEAN) {
				nodeType = GPNode.getRandomTerminalNode(GPNode.BOOLEAN_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.STRING) {
				nodeType = GPNode.getRandomTerminalNode(GPNode.STRING_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.NONE) {
				nodeType = GPNode.getRandomTerminalNode(GPNode.NONE_FLAG);
			} 
			else {
				System.out.println("error: unknown return type in GPTree.generateSubtree");
				return null;
			}
			subtreeRoot.setNodeType(nodeType);
		}
		
		// TERMINAL NODE?
		// get random NON-terminal node of the specified type
		else {
			terminal = false;
			
			if (returnType == GPNode.ReturnType.DOUBLE) {
				nodeType = GPNode.getRandomNode(GPNode.DOUBLE_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.BOOLEAN) {
				nodeType = GPNode.getRandomNode(GPNode.BOOLEAN_FLAG);
			} 
			else if (returnType == GPNode.ReturnType.STRING) {
				nodeType = GPNode.getRandomNode(GPNode.STRING_FLAG);
			} 			
			else if (returnType == GPNode.ReturnType.NONE) {
				nodeType = GPNode.getRandomNode(GPNode.NONE_FLAG);
			} 
			else {
				System.out.println("error: unknown return type in GPTree.generateSubtree");
				return null;
			}
		}

		subtreeRoot.setNodeType(nodeType);
		

		//
		// ADD CHILDREN TO THE SUBTREE ROOT
		//
		
		// variables and constants don't have children (sad...)
		
		// if it's a constant, set its value
		if (nodeType == GPNode.NodeType.CONST) {
			
			//maybe replace .5 with pSmallConstantValue to avoid hard coding?
			if(Utilities.rand.nextDouble() < .5) {
				subtreeRoot.setConstValue(Utilities.nextDouble(0, 1));
			}
			else {
				subtreeRoot.setConstValue(Utilities.nextDouble(Parameters.minConstant, Parameters.maxConstant));
			}
		} 
		
		// if it's a variable, set its name
		else if (nodeType == GPNode.NodeType.VAR) {
			int randIndex = Utilities.nextInt(0, VAR_NAMES.length);
			subtreeRoot.setVarName(VAR_NAMES[randIndex]);
		}
		
		// add children if not a variable or constant
		else {
			ArrayList<GPNode> children = generateChildren(subtreeRoot, depth+1, nodeType, terminal);
			subtreeRoot.setChildren(children);
		}
		
		return subtreeRoot;
	}
	

	
	// *************************************************************************************************************************
	// *************************************************************************************************************************
	// NEED TO CHECK TO SEE IF THE depth VARIABLE IS BEING INCREMENTED CORRECTLY
	// *************************************************************************************************************************
	// *************************************************************************************************************************
	
	
	
	// parameters:
	//   parent is the parent of the children being generated
	//   depth is the depth of the children
	//   nodeType is the return/evaluation type of the parent we are generating the children for
	//   terminal indicates if we want to end this branch
	public static ArrayList<GPNode> generateChildren(GPNode parent, int depth, GPNode.NodeType nodeType, boolean terminal) {

		// what will be returned (array of the children, where each is the root of a subtree)
		ArrayList<GPNode> children = new ArrayList<GPNode>();

		// variable used in many places in the switch statement
		GPNode child;

		// making recursive call for every kind but VAR and CONST (those have no children)
		switch(nodeType) {
			case VAR:
			case CONST:
				System.out.println("error: VAR or CONST node type in GPTree.addChildren");
				System.exit(0);
				break;
	
			case SEQUENCE:
				// children of a SEQUENCE are "commands" and don't return a value
				int numChildren = determineNumSequenceChildren();
				for(int i = 0; i < numChildren; i++) {
					child = generateSubtree(parent, depth, GPNode.ReturnType.NONE, terminal);
					children.add(child);
				}
				break;
	
			case IF:
				numChildren = determineNumIfChildren();

				// first child must be a boolean value
				child = generateSubtree(parent, depth, GPNode.ReturnType.BOOLEAN, terminal);
				children.add(child);

				// other child (children) should not return a value
				for(int i = 1; i < numChildren; i++) {
					child = generateSubtree(parent, depth, GPNode.ReturnType.NONE, terminal);
					children.add(child);
				}
				break;
	
			case ASSIGN:
			case INC:
			case DEC:
				// first child (only child for INC and DEC) must be a variable, not a subtree
				child = new GPNode(parent, depth, GPNode.ReturnType.STRING);
				String varName = VAR_NAMES[Utilities.nextInt(0, VAR_NAMES.length)];
				child.setVarName(varName);
				child.setNodeType(GPNode.NodeType.VAR);
				children.add(child);
				
				// second child (only for ASSIGN) must return DOUBLE
				if (nodeType == GPNode.NodeType.ASSIGN) {
					child = generateSubtree(parent, depth, GPNode.ReturnType.DOUBLE, terminal);
					children.add(child);
				}
				break;
				
			case ADD:
			case SUB:
			case MULT:
			case DIV:
			case EXP:
				// both children must be DOUBLEs
				child = generateSubtree(parent, depth, GPNode.ReturnType.DOUBLE, terminal);
				children.add(child);
				child = generateSubtree(parent, depth, GPNode.ReturnType.DOUBLE, terminal);
				children.add(child);
				break;

			case EQ:
			case LT:
			case GT:
			case LEQ:
			case GEQ:
				// to keep it simple, make this work like ASSIGN, i.e. there has to be a
				// variable on the LHS whose value is being compared to some arbitrary
				// DOUBLE value on the right side
				child = new GPNode(parent, depth, GPNode.ReturnType.STRING);
				varName = VAR_NAMES[Utilities.nextInt(0, VAR_NAMES.length)];
				child.setVarName(varName);
				child.setNodeType(GPNode.NodeType.VAR);
				children.add(child);

				child = generateSubtree(parent, depth, GPNode.ReturnType.DOUBLE, terminal);
				children.add(child);
				break;

			case OR:
			case AND:
				// both children must be BOOLEAN
				child = generateSubtree(parent, depth, GPNode.ReturnType.BOOLEAN, terminal);
				children.add(child);
				child = generateSubtree(parent, depth, GPNode.ReturnType.BOOLEAN, terminal);
				children.add(child);
				break;

			case NEG:
				// single child must be BOOLEAN
				child = generateSubtree(parent, depth, GPNode.ReturnType.BOOLEAN, terminal);
				children.add(child);
				break;

			default:
				System.out.println("error: undefined NodeType in child construction " + nodeType);
				System.exit(0);
		}
		
		return children;
	}

	
	// number of SEQUENCE children is defined by minSequenceLength and maxSequenceLength
	public static int determineNumSequenceChildren() {	
		return Utilities.nextInt(Parameters.minSequenceLength, Parameters.maxSequenceLength+1);
	}

	// will have either:
	//     2 children (boolean expression and what to do if true) OR
	//     3 children (boolean expression, what to do if true, and what to do if false)
	public static int determineNumIfChildren() {
		return Utilities.nextInt(2, 4); 
	}
	

	// Takes current tree and puts into ArrayList is Breath-First Search order     ************* IS IT OKAY THAT IT'S PUTTING *REFERENCES* 
	//                                                                                           (NOT COPIES OF NODES) INTO THE ARRAYLIST?
	public ArrayList<GPNode> toArrayList() {

		Queue<GPNode> queue = new LinkedList<GPNode>();
		ArrayList<GPNode> treeNodeOrderedList = new ArrayList<GPNode>();

		// start the queue
		queue.add(root);

		GPNode currNode;
		ArrayList<GPNode> children;
		int orderedListIndex = 0;

		while(!queue.isEmpty()) {
			// get next node, which will be the highest, leftmost 
			// node of the nodes currently in the queue, and add
			// it the ordered list of nodes
			currNode = queue.remove();
			currNode.setIndex(orderedListIndex++);
			treeNodeOrderedList.add(currNode);
						
			// add the children of currNode to the queue
			children = currNode.getChildren();      
			for(int i = 0; i < children.size(); i++) {
				queue.add(children.get(i));
			}
		}
		numNodes = treeNodeOrderedList.size();
		return treeNodeOrderedList;
	}


	// prints out nodes in the tree so that we can draw a picture....
	public void printTree() {

		ArrayList<GPNode> treeNodeList = toArrayList();

		System.out.println("# number of nodes: " + treeNodeList.size());
		

		// walk through all the nodes so we can draw the tree by hand
		for(int i = 0; i < treeNodeList.size(); i++) {
			GPNode currNode = treeNodeList.get(i);
			System.out.println("# Node: " + i);
			System.out.println("# Type: " + currNode.getNodeType());
			System.out.println("# Depth: " + currNode.getDepth());
			
			if(currNode.getNodeType() == GPNode.NodeType.VAR || currNode.getNodeType() == GPNode.NodeType.HEADER_VAR) {
				System.out.println("# Variable: " + currNode.getVarName());
			}
			if(currNode.getNodeType() == GPNode.NodeType.CONST || currNode.getNodeType() == GPNode.NodeType.HEADER_CONST) {
				System.out.println("# Constant Value: " + currNode.getConstValue());
			}
			
			System.out.println("# Return Type: " + currNode.getReturnType());
			
			// parent
			if (currNode.getParent() == null) {
				System.out.println("# Parent: none");
			}
			else {
				System.out.println("# Parent: " + currNode.getParent().getIndex());
				
			}

			// children
			ArrayList<GPNode> children = currNode.getChildren();
			for(int j = 0; j < children.size(); j++) {
				System.out.println("# Child Indices: " + children.get(j).getIndex());
			}

			System.out.println();
		}
	}
    
    // prints out nodes in the tree so that we can draw a picture.... with PrintWriter
    public void printTree(PrintWriter pw) {
        
        ArrayList<GPNode> treeNodeList = toArrayList();
        
        pw.println("# number of nodes: " + treeNodeList.size());
        
        
        // walk through all the nodes so we can draw the tree by hand
        for(int i = 0; i < treeNodeList.size(); i++) {
            GPNode currNode = treeNodeList.get(i);
            pw.println("# Node: " + i);
            pw.println("# Type: " + currNode.getNodeType());
            pw.println("# Depth: " + currNode.getDepth());
            
            if(currNode.getNodeType() == GPNode.NodeType.VAR || currNode.getNodeType() == GPNode.NodeType.HEADER_VAR) {
                pw.println("# Variable: " + currNode.getVarName());
            }
            if(currNode.getNodeType() == GPNode.NodeType.CONST || currNode.getNodeType() == GPNode.NodeType.HEADER_CONST) {
                pw.println("# Constant Value: " + currNode.getConstValue());
            }
            
            pw.println("# Return Type: " + currNode.getReturnType());
            
            // parent
            if (currNode.getParent() == null) {
                pw.println("# Parent: none");
            }
            else {
                pw.println("# Parent: " + currNode.getParent().getIndex());
                
            }
            
            // children
            ArrayList<GPNode> children = currNode.getChildren();
            for(int j = 0; j < children.size(); j++) {
                pw.println("# Child Indices: " + children.get(j).getIndex());
            }
            
            pw.println();
        }
    }

	// getters and setters
	
	public GPNode getRoot() {
		return root;
	}


	public void setRoot(GPNode root) {
		this.root = root;
	}


	public double getFitness() {
		return fitness;
	}


	public void setFitness(double fitness) {
		this.fitness = fitness;
	}


}


