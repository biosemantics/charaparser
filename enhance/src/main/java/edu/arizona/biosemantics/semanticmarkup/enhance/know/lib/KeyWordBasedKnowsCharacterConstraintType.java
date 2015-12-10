package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.util.Arrays;
import java.util.Set;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsCharacterConstraintType;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsBiologicalEntityConstraintType.ConstraintType;

public class KeyWordBasedKnowsCharacterConstraintType implements KnowsCharacterConstraintType {

	String adjectivesUsedAsNouns = "inner|outer|middle|mid|innermost|outermost|cauline|basal|"
			+ "adaxial|abaxial|left|right|medial|upper|lower|superior|"
			+ "inferior|anterior|posterior|apical|basal|central|proximal|distal|dorsal|ventral|peripheral|"
			+ "leftmost|rightmost|centralmost|uppermost|lowermost|superiormost|inferiormost|anteriormost|posteriormost|"
			+ "proximalmost|distalmost|centralmost|dorsalmost|ventralmost|medialmost";
	String degreeOccurence = "not|usually|mostly|generally|entirely|sometimes|typically|predominantly|variably|always|frequently|" 
			+ "primarily|at-least|often|never|occasionally|only|rarely|mainly";
	String degreeAttribute = "slightly|relatively|very|entirely|rather|much|somewhat|heavily|uniformly|sharply|largely|highly|nearly|"
			+ "moderately|mainly|distinctly|fairly|strongly|finely|easily|deeply|extensively|more or less|just|boldly|completely|"
			+ "closely|almost|faintly|conspicuously|fully|noticeably|extremely|quite|greatly|readily|partly";
	String locationPrefixes = "at|in|on|below|above|from|towards|between|near";
	
	String units= "(pm|cm|mm|dm|ft|m|meters|meter|micro_m|micro-m|microns|micron|unes|µm|μm|um|centimeters|centimeter|millimeters|millimeter|transdiameters|transdiameter)[23]?"; //squared or cubed
	String limitation = "for.*\\d+.*[" + units + "].*";
	String relationArgument = "with|into|of";
			
	private IPOSKnowledgeBase posKnowledgeBase;
	
	public KeyWordBasedKnowsCharacterConstraintType(IPOSKnowledgeBase posKnowledgeBase) {
		this.posKnowledgeBase = posKnowledgeBase;
	}
	
	@Override
	public ConstraintType getConstraintType(String constraint) {
		if(constraint.matches(limitation))
			return ConstraintType.LIMITATION;
		
		String[] terms = constraint.split(" ");
		if(terms[0].matches(locationPrefixes))
			return ConstraintType.LOCATION;
		if(terms[0].matches(relationArgument))
			if(terms.length > 1 && containsNoun(Arrays.copyOfRange(terms, 1, terms.length)))
				return ConstraintType.ARGUMENT;
		for(String term : constraint.split(" ")) 
			if(term.matches(adjectivesUsedAsNouns)) 
				return ConstraintType.LOCATION;
		return ConstraintType.OTHER;
	}

	private boolean containsNoun(String[] terms) {
		for(String term : terms) {
			if(posKnowledgeBase.isNoun(term))
				return true;
		}
		return false;
	}

}
