package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import java.util.ArrayList;
import java.util.Set;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsBiologicalEntityConstraintType;

public class KeyWordBasedKnowsBiologicalEntityConstraintType implements KnowsBiologicalEntityConstraintType {

	/*String locationTerms =  "aboard|about|above|across|after|against|along|alongside|amid|amidst|among|amongst|anti|"
			+ "around|as|astride|at|atop|bar|barring|before|behind|below|beneath|beside|besides|between|beyond|but|by|"
			+ "circa|concerning|considering|counting|cum|despite|down|during|except|excepting|excluding|following|for|"
			+ "from|given|gone|in|including|inside|into|less|like|minus|near|notwithstanding|of|off|on|onto|opposite|"
			+ "outside|over|past|pending|per|plus|pro|re|regarding|respecting|round|save|saving|since|than|through|"
			+ "throughout|till|to|touching|toward|towards|under|underneath|unlike|until|up|upon|versus|via|with|within|"
			+ "without|worth";*/
	
	String adjectivesUsedAsNouns = "inner|outer|middle|mid|innermost|outermost|cauline|basal|"
			+ "adaxial|abaxial|left|right|medial|upper|lower|superior|"
			+ "inferior|anterior|posterior|apical|basal|central|proximal|distal|dorsal|ventral|peripheral|"
			+ "leftmost|rightmost|centralmost|uppermost|lowermost|superiormost|inferiormost|anteriormost|posteriormost|"
			+ "proximalmost|distalmost|centralmost|dorsalmost|ventralmost|medialmost";
	
	private IGlossary glossary;
	
	public KeyWordBasedKnowsBiologicalEntityConstraintType(IGlossary glossary) {
		this.glossary = glossary;
	}
	
	@Override
	public ConstraintType getConstraintType(String constraint) {
		for(String term : constraint.split(" ")) {
			if(term.matches(adjectivesUsedAsNouns)) {
				return ConstraintType.LOCATION_SELECTION;
			}
			Set<String> categories = glossary.getCategories(term);
			if(categories.contains("structure")) {
				return ConstraintType.TYPE_IDENTIFICATION;
			}
		}
		return ConstraintType.OTHER;
	}

}
