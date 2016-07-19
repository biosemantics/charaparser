package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsModifierType;

public class KeyWordBasedKnowsModifierType implements KnowsModifierType {

	private String adjectivesUsedAsNouns = "inner|outer|middle|mid|innermost|outermost|cauline|basal|"
			+ "adaxial|abaxial|left|right|medial|upper|lower|superior|"
			+ "inferior|anterior|posterior|apical|basal|central|proximal|distal|dorsal|ventral|peripheral|"
			+ "leftmost|rightmost|centralmost|uppermost|lowermost|superiormost|inferiormost|anteriormost|posteriormost|"
			+ "proximalmost|distalmost|centralmost|dorsalmost|ventralmost|medialmost";
	private String degreeOccurence = "not|usually|mostly|generally|entirely|sometimes|typically|predominantly|variably|always|frequently|" 
			+ "primarily|at-least|often|never|occasionally|only|rarely|mainly";
	private String degreeAttribute = "slightly|relatively|very|entirely|rather|much|somewhat|heavily|uniformly|sharply|largely|highly|nearly|"
			+ "moderately|mainly|distinctly|fairly|strongly|finely|easily|deeply|extensively|more or less|just|boldly|completely|"
			+ "closely|almost|faintly|conspicuously|fully|noticeably|extremely|quite|greatly|readily|partly";
	private String limitation = "otherwise";
	private IPOSKnowledgeBase posKnowledgeBase;
	
	public KeyWordBasedKnowsModifierType(IPOSKnowledgeBase posKnowledgeBase) {
		this.posKnowledgeBase = posKnowledgeBase;
	}

	@Override
	public ModifierType getModifierType(String modifier) {
		boolean lastTermOf = false;
		for(String term : modifier.split(" ")) {
			if(term.matches(adjectivesUsedAsNouns)) 
				return ModifierType.LOCATION;
			if(term.endsWith("ly")) {
				String removedLy = term.substring(0, term.length() - 2);
				if(removedLy.matches(adjectivesUsedAsNouns)) 
					return ModifierType.LOCATION;
			}
			if(term.matches(degreeAttribute))
				return ModifierType.DEGREE_ATTRIBUTE;
			if(term.matches(degreeOccurence))
				return ModifierType.DEGREE_OCCURENCE;
			if(term.endsWith("ing")) 
				return ModifierType.TIMING;
			if((lastTermOf && posKnowledgeBase.isNoun(term)) || term.matches(limitation))
				return ModifierType.LIMITATION;
			if(term.equals("of")) 
				lastTermOf = true;
		}
		return ModifierType.OTHER;
	}
}
