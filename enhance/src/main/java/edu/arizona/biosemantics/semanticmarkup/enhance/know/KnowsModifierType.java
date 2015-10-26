package edu.arizona.biosemantics.semanticmarkup.enhance.know;

public interface KnowsModifierType {

	public static enum ModifierType {
		SHAPE, LOCATION, ORIENTATION, DEGREE_OCCURENCE, DEGREE_ATTRIBUTE, TIMING, LIMITATION, OTHER
	}
	
	public ModifierType getModifierType(String modifier);
	
}
