package edu.arizona.biosemantics.semanticmarkup.enhance.know;

public interface KnowsCharacterConstraintType {

	public static enum ConstraintType {
		LOCATION, SHAPE, LIMITATION, ARGUMENT, OTHER
	}
	
	public ConstraintType getConstraintType(String constraint);
	
}
