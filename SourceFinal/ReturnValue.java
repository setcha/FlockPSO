/*
 * nodes in the GP trees can "return" both double, boolean,
 * and string values, e.g.
 * 	- double from an arithmetic operation
 * 	- boolean from  a comparison operation
 * 	- String from the left-hand side of an assignment operation
 *  
 * this class allows the thing that a node returns to be the same,
 * regardless of the actual type of the return value
 * 
 */

public class ReturnValue {

	private double valueDouble = 0;
	private boolean valueBoolean = false;
	private String valueString = null;

	
	// set all to default values
	public ReturnValue(){
		valueDouble = 0;
		valueBoolean = false;
		this.valueString = null;
	}

	// set double value
	public ReturnValue(double valueDouble){
		this.valueDouble = valueDouble;
		valueBoolean = false;
		this.valueString = null;
	}

	// set boolean value
	public ReturnValue(boolean valueBoolean){
		valueDouble = 0;
		this.valueBoolean = valueBoolean;
		this.valueString = null;
	}

	// set string value
	public ReturnValue(String valueString){
		valueDouble = 0;
		valueBoolean = false;
		this.valueString = valueString;
	}

	// getters and setters
	// NOTE: the type of the parameter of the setters indicates which value is to be set;
	//       normally, there would be no parameter in a setter, but we need to know which 
	//		 value to return, so we send a "flag" that has the desired value; the flags are
	//   	 defined in GPNode.java

	public void setValue(double valueDouble) {
		this.valueDouble = valueDouble;		
	}

	public void setValue(boolean valueBoolean) {
		this.valueBoolean = valueBoolean;		
	}

	public void setValue(String valueString) {
		this.valueString = valueString;		
	}

	public double getValue(double doubleFlag) {
		return valueDouble;
	}

	public boolean getValue(boolean booleanFlag) {
		return valueBoolean;
	}

	public String getValue(String stringFlag) {
		return valueString;
	}

}