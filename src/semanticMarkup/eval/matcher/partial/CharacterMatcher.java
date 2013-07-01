package semanticMarkup.eval.matcher.partial;

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
				((characterA.getConstraint() == null && characterB.getConstraint() == null) ||
						((characterA.getConstraint() != null && characterB.getConstraint() != null) && 
								(characterA.getConstraint().contains(characterB.getConstraint()) ||
										characterB.getConstraint().contains(characterA.getConstraint())))) && 
				((characterA.getName() == null && characterB.getName() == null) || 
						((characterA.getName() != null && characterB.getName() != null) && 
								(characterA.getName().contains(characterB.getName()) ||
										characterB.getName().contains(characterA.getName())))) && 
				((characterA.getValue() == null && characterB.getValue() == null) || 
						((characterA.getValue() != null && characterB.getValue() != null) && 
								(characterA.getValue().contains(characterB.getValue()) ||
										characterB.getValue().contains(characterA.getValue())))) && 
				((characterA.getModifier() == null && characterB.getModifier() == null) || 
						((characterA.getModifier() != null && characterB.getModifier() != null) && 
								(characterA.getModifier().contains(characterB.getModifier()) ||
									characterB.getModifier().contains(characterA.getModifier())))) && 
				((characterA.getFrom() == null && characterB.getFrom() == null) || 
						((characterA.getFrom() != null && characterB.getFrom() != null) && 
								(characterA.getFrom().contains(characterB.getFrom()) ||
									characterB.getFrom().contains(characterA.getFrom())))) && 
				((characterA.getTo() == null && characterB.getTo() == null) || 
						((characterA.getTo() != null && characterB.getTo() != null) && 
								(characterA.getTo().contains(characterB.getTo()) ||
									characterB.getTo().contains(characterA.getTo())))) &&  
				Objects.equals(characterA.getFromUnit(), characterB.getFromUnit()) && 
				Objects.equals(characterA.getToUnit(), characterB.getToUnit()) && 
				Objects.equals(characterA.getUnit(), characterB.getUnit()) &&
				Objects.equals(characterA.getUpperRestricted(), characterB.getUpperRestricted()) && 
				Objects.equals(characterA.getFromInclusive(), characterB.getFromInclusive()) && 
				Objects.equals(characterA.getToInclusive(), characterB.getToInclusive());
	}
	
}
