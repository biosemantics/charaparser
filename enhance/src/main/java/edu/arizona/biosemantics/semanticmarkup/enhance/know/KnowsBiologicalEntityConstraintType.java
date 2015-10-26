package edu.arizona.biosemantics.semanticmarkup.enhance.know;

public interface KnowsBiologicalEntityConstraintType {

	public static enum ConstraintType {
		LOCATION_SELECTION, TYPE_IDENTIFICATION, OTHER
	}
	
	public ConstraintType getConstraintType(String constraint);
	
}
