package semanticMarkup.eval.matcher.perfect;

import java.util.Objects;

import semanticMarkup.eval.matcher.IMatcher;
import semanticMarkup.eval.model.Character;


public class CharacterMatcher implements IMatcher<Character> {

	private StructureMatcher structureMatcher = new StructureMatcher();
		
	@Override
	public boolean isMatch(Character characterA, Character characterB) {
		return characterA.getStructure() != null && characterB.getStructure() != null && 
				structureMatcher.isMatch(characterA.getStructure(), characterB.getStructure()) && 
				Objects.equals(characterA.getCharType(), characterB.getCharType()) && 
				Objects.equals(characterA.getConstraint(), characterB.getConstraint()) && 
				Objects.equals(characterA.getName(), characterB.getName()) &&
				Objects.equals(characterA.getValue(), characterB.getValue()) && 
				Objects.equals(characterA.getModifier(), characterB.getModifier()) && 
				Objects.equals(characterA.getFrom(), characterB.getFrom()) && 
				Objects.equals(characterA.getTo(), characterB.getTo()) && 
				Objects.equals(characterA.getFromUnit(), characterB.getFromUnit()) && 
				Objects.equals(characterA.getToUnit(), characterB.getToUnit()) && 
				Objects.equals(characterA.getUnit(), characterB.getUnit()) &&
				Objects.equals(characterA.getUpperRestricted(), characterB.getUpperRestricted()) && 
				Objects.equals(characterA.getFromInclusive(), characterB.getFromInclusive()) && 
				Objects.equals(characterA.getToInclusive(), characterB.getToInclusive());
	}
	
}
