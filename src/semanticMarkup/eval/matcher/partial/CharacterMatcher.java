package semanticMarkup.eval.matcher.partial;

import semanticMarkup.eval.matcher.AbstractMatcher;
import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.model.Character;


public class CharacterMatcher extends AbstractMatcher implements IMatcher<Character> {
	
	private StructureMatcher structureMatcher = new StructureMatcher();
	
	@Override
	public boolean isMatch(Character characterA, Character characterB) {
		boolean result = this.areNotNull("character structure", characterA.getStructure(), characterB.getStructure()) && 
				structureMatcher.isMatch(characterA.getStructure(), characterB.getStructure()) && 
				this.equalsOrNull("character char_type", characterA.getCharType(), characterB.getCharType()) && 
				this.valuesNullOrContainedEitherWay("character constraint", characterA.getConstraint(), characterB.getConstraint()) &&
				this.valuesNullOrContainedEitherWay("character name", characterA.getName(), characterB.getName()) &&
				this.valuesNullOrContainedEitherWay("character value", characterA.getValue(), characterB.getValue()) &&
				this.valuesNullOrContainedEitherWay("character modifier", characterA.getModifier(), characterB.getModifier()) &&
				this.valuesNullOrContainedEitherWay("character from", characterA.getFrom(), characterB.getFrom()) &&
				this.valuesNullOrContainedEitherWay("character to", characterA.getTo(), characterB.getTo()) &&
				this.equalsOrNull("character fromUnit", characterA.getFromUnit(), characterB.getFromUnit()) && 
				this.equalsOrNull("character toUnit", characterA.getToUnit(), characterB.getToUnit()) && 
				this.equalsOrNull("character unit", characterA.getUnit(), characterB.getUnit()) &&
				this.equalsOrNull("character upperRestricted", characterA.getUpperRestricted(), characterB.getUpperRestricted()) && 
				this.equalsOrNull("character fromInclusive", characterA.getFromInclusive(), characterB.getFromInclusive()) && 
				this.equalsOrNull("character toInclusive", characterA.getToInclusive(), characterB.getToInclusive());
		return result;
	}
	
}
