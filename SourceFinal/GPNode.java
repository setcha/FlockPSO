
import java.util.ArrayList;

/* 
 * The program that changes flock parameter values is in the form of a tree.
 * The nodes in the tree are GPNodes.
 * 
 * Should add function to create random CONST values?
 *
 */

public class GPNode {
	 
	// Node attributes
	private NodeType nodeType;
	private int depth;
	private GPNode parent;
	private ArrayList<GPNode> children;
	private String varName = "";
	private double constValue = 0; //for a Header Sequence, this becomes the seed
	// allows node to return double, boolean, or String
	private ReturnType returnType;

	// set in the toArrayList method in GPTree.java
	// and used when printing the tree in printTree method in GPTree.java
	private int index;
	
	// Types of nodes
	// SEQUENCE: 
	//     arbitrary (within limits)  number of children representing operations to be performed in sequence
	// IF: 
	//     Child 1: boolean expression, Child 2: node to execute if true, Child 3 (optional): node to execute if false
	// ASSIGN:
	//     assign the variable in Child 1 the value that Child 2 evaluates to
	// VAR:
	//     a variable name
	// CONST:
	//     a constant value (always a double)
	// ADD, SUB, MULT, DIV, EXP:
	//     perform this operation on the two children
	// INC, DEC:
	//     increment or decrement the value of the child
	// LT, GT, EQ, LEQ, GEQ
	//     checks whether the specified relation holds between the two children
	// AND, OR
	//     performs the specified boolean operator on the two children
	// NEG:
	//     logically negates the child
	public static enum NodeType {
		SEQUENCE, IF, ASSIGN, HEADER_SEQUENCE, HEADER_ASSIGN,
		VAR, CONST, HEADER_VAR, HEADER_CONST,
		ADD, SUB, MULT, DIV, EXP, INC, DEC,
		LT, GT, EQ, LEQ, GEQ, 
		AND, OR, NEG,
		NONE
	}

	
	// flags to signal what value is stored in a ReturnValue object
	public static final double DOUBLE_FLAG = 0.0;
	public static final boolean BOOLEAN_FLAG = false;
	public static final String STRING_FLAG = "";
	public static final char NONE_FLAG = 'n';

	
	// types that a node can evaluate to
	// (the following arrays categorize the node types)
	public static enum ReturnType { DOUBLE, BOOLEAN, STRING, NONE };
	
	// nodes that evaluate to a double value                                                     
	public static NodeType[] doubleReturn = 
		{ NodeType.VAR, NodeType.CONST, NodeType.ADD, NodeType.SUB, NodeType.MULT, NodeType.DIV, NodeType.EXP, NodeType.INC, NodeType.DEC };
	
	// nodes that evaluate to a boolean value                                                     
	public static NodeType[] booleanReturn = 
		{ NodeType.LT, NodeType.GT, NodeType.EQ, NodeType.LEQ, NodeType.GEQ, NodeType.AND, NodeType.OR, NodeType.NEG };
	
	// nodes that evaluate to a String value                                                     
	public static NodeType[] stringReturn = { NodeType.VAR };
	
	// nodes that evaluate to a NONE value                                                     
	public static NodeType[] noReturn = { NodeType.SEQUENCE, NodeType.IF, NodeType.ASSIGN };

	
	// nodes that could have no more children either immediately or in one more level down;
	// this is important when we need to limit the depth of a tree being constructed          
	public static NodeType[] terminalDoubleReturn = { NodeType.VAR, NodeType.CONST };
	
	public static NodeType[] terminalBoolReturn = { NodeType.LT, NodeType.GT, NodeType.EQ, NodeType.LEQ, NodeType.GEQ };
	
	public static NodeType[] terminalStringReturn = { NodeType.VAR };
	
	public static NodeType[] terminalNoReturn = { NodeType.ASSIGN };
	
	
	// used during mutation
	public static NodeType[] allArithmeticOps = { NodeType.ADD, NodeType.SUB, NodeType.MULT, NodeType.DIV, NodeType.EXP, NodeType.INC, NodeType.DEC };	
	public static NodeType[] allRelationalOps = { NodeType.LT, NodeType.GT, NodeType.EQ, NodeType.LEQ, NodeType.GEQ }; 



	//errors from generate subtree probably here
	public GPNode() {
		this.nodeType = NodeType.NONE;
		this.depth = 0;
		this.parent = null;
		this.children = null;
		this.varName = "";
		this.constValue = 0.0;
		this.returnType = ReturnType.NONE;
		this.index = 0;
	}


	// Constructor if have only parent and return type.
	//	This constructor is the one used to create nodes throughout the code. 
	//	If a node exists, it must have a parent and it must have a return type. 
	//	The rest can be created and updated afterwards.
	public GPNode(GPNode parent, int depth,  ReturnType returnType) {

		// ???????????????????????????????????????????????????????????????????????
//		// assigning any type for now                     
//		if (returnType == ReturnType.DOUBLE) {
//			this.nodeType = NodeType.ADD;               // "ADD" IS ARBITRARY; WILL BE RESET LATER
//		} 
//		else {
//			this.nodeType = NodeType.GT;                // "GT" IS ARBITRARY; WILL BE RESET LATER
//		}
		// ???????????????????????????????????????????????????????????????????????
	
		this.nodeType = NodeType.NONE;
		this.depth = depth;
		this.parent = parent;
		this.children = new ArrayList<GPNode>();
		this.varName = "";             
		this.constValue = 0.0;
		this.returnType = returnType;
		this.index = 0;
	}

	//  CHECK THIS!!!!!!!!  *********************************************************
	public GPNode getDeepCopy(GPNode parent) {         
		
		GPNode copy = new GPNode();
		copy.setNodeType(nodeType);
		copy.setDepth(depth);
		copy.setParent(parent);
		ArrayList<GPNode> childrenCopy = new ArrayList<GPNode>();
		for (int i = 0; i < children.size(); ++i) {
			childrenCopy.add(children.get(i).getDeepCopy(copy));
		}
		copy.setChildren(childrenCopy);
		copy.setVarName(varName);
		copy.setConstValue(constValue);
		copy.setReturnType(returnType);
		
		return copy;
	}
	
	
	// the ReturnValue object is needed for the recursive calls in runAux
	// but must be created only once, so must be created outside the runAux method;
	// we need the PSO Particle, so we can operate on a specific particle's
	// parameter values
	public void evaluate(Particle particle) {
		ReturnValue retVal = new ReturnValue();
		evaluateAux(retVal, particle);
	}
	
	
	// evaluates node and puts resulting value in the ReturnValue object retVal
	public void evaluateAux(ReturnValue retVal, Particle particle) {

		// possibly need three ReturnValue objects, so create them here
		ReturnValue retVal0 = new ReturnValue();
		ReturnValue retVal1 = new ReturnValue();
		ReturnValue retVal2 = new ReturnValue();
		
		
        //     NEED TO REVIEW THIS  *********************************************************
		switch(nodeType) {

		// sequence
		case HEADER_SEQUENCE:
			//Parameters.globalSeed = (int) constValue;
			//Utilities.setConsistentSeed((int) constValue); // header sequence also set the seed of the tree, then also do sequence stuff
		case SEQUENCE:
			// just evaluate the children
			for(int i = 0; i < children.size(); i++) { //should be exactly 9 in a HEADER_SEQUENCE
				children.get(i).evaluateAux(retVal0, particle);
			}
			return;
			
		// if
		case IF:
			// evaluate the first child to get the value of the boolean expression
			children.get(0).evaluateAux(retVal0, particle);

			// if true, evaluate the second child
			if (retVal0.getValue(BOOLEAN_FLAG)) {
				children.get(1).evaluateAux(retVal1, particle);
				return;
			}

			// if there's no third node, we're done
			if (children.size() < 3) {
				return;
			}

			// if there's a third node and the boolean was false,
			// evaluate the third child
			children.get(2).evaluateAux(retVal2, particle);
			return;

		// variable 
		case VAR:
		case HEADER_VAR:
			// WHICH ONE??????????????????????                               CHECK THIS!
			// there are contexts where we want the variable name,
			// and contexts where we want the variable value
			retVal.setValue(varName);      
			retVal.setValue(particle.getVarValue(varName));
			return;
			
			
		// constant
		case CONST:
		case HEADER_CONST:
			// just return the constant value
			retVal.setValue(constValue);
			return;
		
	
		// assigning new value to Particle variable
		case ASSIGN:
		case HEADER_ASSIGN:
			// child 1 gives us the variable name
			children.get(0).evaluateAux(retVal0, particle);
			String ASSIGNvarName = retVal0.getValue(STRING_FLAG);
			
			// child 2 gives us the value being assigned
			children.get(1).evaluateAux(retVal1, particle);
			double value = retVal1.getValue(DOUBLE_FLAG);
			
			// make the assigment
			particle.assignVariable(ASSIGNvarName, value);
			return;			
			

		// addition
		case ADD:
			// evaluate the two children and add the values
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(DOUBLE_FLAG) + retVal1.getValue(DOUBLE_FLAG));
			return;
			
		// subtraction
		case SUB:
			// evaluate the two children and subtract the child 2 value from the child 1 value
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(DOUBLE_FLAG) - retVal1.getValue(DOUBLE_FLAG));
			return;
			
		// multiplication
		case MULT:
			// evaluate the two children and multiply the values
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(DOUBLE_FLAG) * retVal1.getValue(DOUBLE_FLAG));
			return;
			
		// division
		case DIV:
			// evaluate the two children and divide the child 1 value by the child 2 value
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			if (retVal1.getValue(DOUBLE_FLAG) != 0) {
				retVal.setValue(retVal0.getValue(DOUBLE_FLAG) / retVal1.getValue(DOUBLE_FLAG));
			}
			return;
			
		// exponent
		case EXP:
			// evaluate the two children and raise the child 1 value to the child 2 value power
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue((double) Math.pow(retVal0.getValue(DOUBLE_FLAG), retVal1.getValue(DOUBLE_FLAG)));
			return;

		// ++
		case INC:
			// add 1 to the child variable
			children.get(0).evaluateAux(retVal0, particle);
			String INCvarName = retVal0.getValue(STRING_FLAG);
			particle.increment(INCvarName);
			return;

		// --
		case DEC:
			// subtract 1 from the child variable
			children.get(0).evaluateAux(retVal0, particle);
			String DECvarName = retVal0.getValue(STRING_FLAG);
			particle.decrement(DECvarName);
			return;
			
		// less than
		case LT:
			// check whether child 1 value is less than child 2 value
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(DOUBLE_FLAG) < retVal1.getValue(DOUBLE_FLAG));
			return;

		// greater than
		case GT:
			// check whether child 1 value is greater than child 2 value
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(DOUBLE_FLAG) > retVal1.getValue(DOUBLE_FLAG));
			return;

		// less than or equal to
		case LEQ:
			// check whether child 1 value is less than or equal to child 2 value
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(DOUBLE_FLAG) <= retVal1.getValue(DOUBLE_FLAG));
			return;

		// greater than or equal to
		case GEQ:
			// check whether child 1 value is greater than or equal to child 2 value
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(DOUBLE_FLAG) >= retVal1.getValue(DOUBLE_FLAG));
			return;

		// equal
		case EQ:
			// check whether child 1 value is equal to the child 2 value
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(DOUBLE_FLAG) == retVal1.getValue(DOUBLE_FLAG));
			return;

		// and
		case AND:
			// logically AND the values of the two children
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(BOOLEAN_FLAG) && retVal1.getValue(BOOLEAN_FLAG));
			return;

		// or
		case OR:
			// logically Or the values of the two children
			children.get(0).evaluateAux(retVal0, particle);
			children.get(1).evaluateAux(retVal1, particle);
			retVal.setValue(retVal0.getValue(BOOLEAN_FLAG) || retVal1.getValue(BOOLEAN_FLAG));
			return;

		// negate
		case NEG:
			// logically negate the single child
			children.get(0).evaluateAux(retVal0, particle);
			retVal.setValue(!retVal0.getValue(BOOLEAN_FLAG));
			return;


		default:
			System.out.println("error: undefined NodeType");
			System.exit(0);
		}
	}



	//
	// Methods needed to create random trees
	//

	
	// for getting random nonterminal nodes
	
	public static NodeType getRandomNode(double doubleFlag) {
		int randVal = Utilities.nextInt(0, doubleReturn.length);
		return doubleReturn[randVal];
	}
	
	public static NodeType getRandomNode(boolean booleanFlag) {
		int randVal = Utilities.nextInt(0, booleanReturn.length);
		return booleanReturn[randVal];
	}

	public static NodeType getRandomNode(String stringFlag) {
		int randVal = Utilities.nextInt(0, stringReturn.length);
		return stringReturn[randVal];
	}
	
	public static NodeType getRandomNode(char noneFlag) {
		int randVal = Utilities.nextInt(0, noReturn.length);
		return noReturn[randVal];
	}
	

	// for getting random terminal nodes

	public static NodeType getRandomTerminalNode(double doubleFlag) {
		int randVal = Utilities.nextInt(0, terminalDoubleReturn.length);
		return terminalDoubleReturn[randVal];
	}
	
	public static NodeType getRandomTerminalNode(boolean booleanFlag) {
		int randVal = Utilities.nextInt(0, terminalBoolReturn.length);
		return terminalBoolReturn[randVal];
	}
	
	public static NodeType getRandomTerminalNode(String stringFlag) {
		int randVal = Utilities.nextInt(0, terminalStringReturn.length);
		return terminalStringReturn[randVal];
	}
	
	public static NodeType getRandomTerminalNode(char noneFlag) {
		int randVal = Utilities.nextInt(0, terminalNoReturn.length);
		return terminalNoReturn[randVal];
	}



	// getters and setters
	

	public NodeType getNodeType() {
		return nodeType;
	}



	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}


	public int getDepth() {
		return depth;
	}
	
	
	public void setDepth(int depth) {
		this.depth = depth;
	}


	public GPNode getParent() {
		return parent;
	}



	public void setParent(GPNode parent) {
		this.parent = parent;
	}



	public ArrayList<GPNode> getChildren() {
		return children;
	}



	public void setChildren(ArrayList<GPNode> children) {
		this.children = children;
	}



	public String getVarName() {
		return varName;
	}



	public void setVarName(String varName) {
		this.varName = varName;
	}



	public double getConstValue() {
		return constValue;
	}



	public void setConstValue(double constValue) {
		this.constValue = constValue;
	}



	public ReturnType getReturnType() {
		return returnType;
	}



	public void setReturnType(ReturnType returnType) {
		this.returnType = returnType;
	}



	public int getIndex() {
		return index;
	}



	public void setIndex(int index) {
		this.index = index;
	}
	
	public void printNode() {
		int currNode = this.getIndex();
		System.out.println("Node: " + currNode);
		System.out.println("Type: " + this.getNodeType());
		System.out.println("Depth: " + this.getDepth());
		
		if(this.getNodeType() == GPNode.NodeType.VAR || this.getNodeType() == GPNode.NodeType.HEADER_VAR) {
			System.out.println("Variable: " + this.getVarName());
		}
		if(this.getNodeType() == GPNode.NodeType.CONST || this.getNodeType() == GPNode.NodeType.HEADER_CONST) {
			System.out.println("Constant Value: " + this.getConstValue());
		}
		
		System.out.println("Return Type: " + this.getReturnType());
		
		// parent
		if (this.getParent() == null) {
			System.out.println("Parent: none");
		}
		else {
			System.out.println("Parent: " + this.getParent().getIndex());
			
		}

		// children
		ArrayList<GPNode> children = this.getChildren();
		for(int j = 0; j < children.size(); j++) {
			System.out.println("Child Indices: " + children.get(j).getIndex());
		}

		System.out.println();
	}



}

