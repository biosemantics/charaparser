package semanticMarkup.eval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;

public class AdvancedPrecisionRecallEvaluator extends AbstractPrecisionRecallEvaluator {

	@Override
	protected Set<String> getStructureOverlap(HashMap<String, Set<DescriptionTreatmentElement>> created, HashMap<String, Set<DescriptionTreatmentElement>> correct) {
		Set<String> result = new HashSet<String>();
		for(String keyName : created.keySet()) {
			if(correct.containsKey(keyName)) {
				Set<DescriptionTreatmentElement> createdElements = created.get(keyName);
				Set<DescriptionTreatmentElement> correctElements = correct.get(keyName);
				String bestMatch = getBestStructureMatch(keyName, createdElements, correctElements);
				if(bestMatch!=null)
					result.add(bestMatch);
			}
		}
		return result;
	}

	private String getBestStructureMatch(String name, Set<DescriptionTreatmentElement> createdElements, Set<DescriptionTreatmentElement> correctElements) {
		String candidate = null;
		int candidateScore = -1;
		for(DescriptionTreatmentElement createdElement : createdElements) {
			for(DescriptionTreatmentElement correctElement : correctElements) {
				/*System.out.println("structure comparison: ");
				System.out.println(createdElement.getProperty("name"));
				System.out.println(correctElement.getProperty("name"));
				System.out.println(createdElement.getProperty("constraint"));
				System.out.println(correctElement.getProperty("constraint"));*/
				if(isEqualProperty(correctElement.getProperty("name"), createdElement.getProperty("name")) &&
						isEqualProperty(correctElement.getProperty("constraint"), createdElement.getProperty("constraint"))) {
					int score = getCharachterOverlap(getCharacters(createdElement), getCharacters(correctElement)).size();
					if(score > candidateScore) {
						candidateScore = score;
						candidate = name + ": " + createdElement.getProperty("id") + " <-> " + correctElement.getProperty("id");
					}
				}
			}
		}
		return candidate;
	}

	@Override
	protected Set<String> getCharachterOverlap(HashMap<String, Set<DescriptionTreatmentElement>> created, HashMap<String, Set<DescriptionTreatmentElement>> correct) {
		Set<String> result = new HashSet<String>();
		for(String keyName : created.keySet()) {
			if(correct.containsKey(keyName)) {
				Set<DescriptionTreatmentElement> createdElements = created.get(keyName);
				Set<DescriptionTreatmentElement> correctElements = correct.get(keyName);
				/*System.out.println("character comparison: ");
				System.out.println(createdElement.getProperty("name"));
				System.out.println(correctElement.getProperty("name"));
				System.out.println(createdElement.getProperty("value"));
				System.out.println(correctElement.getProperty("value"));
				System.out.println(createdElement.getProperty("from"));
				System.out.println(correctElement.getProperty("from"));
				System.out.println(createdElement.getProperty("to"));
				System.out.println(correctElement.getProperty("to"));*/
				String bestMatch = getBestCharacterMatch(keyName, createdElements, correctElements);
				if(bestMatch!=null)
					result.add(bestMatch);
			}
		}
		return result;
	}

	private String getBestCharacterMatch(String name, Set<DescriptionTreatmentElement> createdElements, Set<DescriptionTreatmentElement> correctElements) {
		for(DescriptionTreatmentElement createdElement : createdElements) {
			for(DescriptionTreatmentElement correctElement : correctElements) {
				//System.out.println("created element " + createdElement);
				///System.out.println("correct element " + correctElements);
				if(isEqualProperty(correctElement.getProperty("name"), createdElement.getProperty("name")) && 
						isEqualProperty(correctElement.getProperty("value"), createdElement.getProperty("value")) &&
						isEqualProperty(correctElement.getProperty("from"), createdElement.getProperty("from")) && 
						isEqualProperty(correctElement.getProperty("to"), createdElement.getProperty("to")) ) {
					return name;// + ": " + createdElement.getProperty("name") + " <-> " + correctElement.getProperty("name");
				}
			}
		}
		return null;
	}

	@Override
	protected Set<String> getRelationOverlap(HashMap<String, Set<DescriptionTreatmentElement>> createdRelationsSet,
			HashMap<String, DescriptionTreatmentElement> createdStructuresByIdSet, 
			HashMap<String, Set<DescriptionTreatmentElement>> correctRelationsSet, 
			HashMap<String, DescriptionTreatmentElement> correctStructuresByIdSet) {
		Set<String> result = new HashSet<String>();
		for(String keyName : createdRelationsSet.keySet()) {
			if(correctRelationsSet.containsKey(keyName)) {
				Set<DescriptionTreatmentElement> createdElements = createdRelationsSet.get(keyName);
				Set<DescriptionTreatmentElement> correctElements = correctRelationsSet.get(keyName);
				/*System.out.println("relation comparison: ");
				System.out.println(createdElement.getProperty("name"));
				System.out.println(correctElement.getProperty("name"));
				System.out.println(createdElement.getProperty("negation"));
				System.out.println(correctElement.getProperty("negation"));
				System.out.println(createdElement.getProperty("from"));
				System.out.println(correctElement.getProperty("from"));
				System.out.println(createdElement.getProperty("to"));
				System.out.println(correctElement.getProperty("to")); */
				/*System.out.println("1" + createdRelationsSet);
				System.out.println("2" + createdStructuresByIdSet);
				System.out.println("3" + correctRelationsSet);
				System.out.println("4" + correctStructuresByIdSet);
				System.out.println("5" + createdElement.getProperty("to"));
				System.out.println("6" + createdElement.getProperty("from"));
				System.out.println("7" + correctElement.getProperty("to"));
				System.out.println("8" + correctElement.getProperty("from"));
				System.out.println("9" + createdStructuresByIdSet.get(createdElement.getProperty("to")));
				System.out.println("10" + createdStructuresByIdSet.get(createdElement.getProperty("from")));
				System.out.println("11" + correctStructuresByIdSet.get(correctElement.getProperty("to")));
				System.out.println("12" + correctStructuresByIdSet.get(correctElement.getProperty("from")));*/
				
				String bestMatch = getBestRelationMatch(keyName, createdElements, correctElements, createdStructuresByIdSet, correctStructuresByIdSet);
				if(bestMatch!=null)
					result.add(bestMatch);
			}
		}
		return result;
	}

	private String getBestRelationMatch(String name, Set<DescriptionTreatmentElement> createdElements, 
			Set<DescriptionTreatmentElement> correctElements, 
			HashMap<String, DescriptionTreatmentElement> createdStructuresByIdSet, 
			HashMap<String, DescriptionTreatmentElement> correctStructuresByIdSet) {
		for(DescriptionTreatmentElement createdElement : createdElements) {
			if(createdStructuresByIdSet.containsKey(createdElement.getProperty("to")) && createdStructuresByIdSet.containsKey(createdElement.getProperty("from"))) {
				String createdToStructureName = createdStructuresByIdSet.get(createdElement.getProperty("to")).getName();
				String createdFromStructureName = createdStructuresByIdSet.get(createdElement.getProperty("from")).getName();
				
				for(DescriptionTreatmentElement correctElement : correctElements) {
					String correctToStructureName = correctStructuresByIdSet.get(correctElement.getProperty("to")).getName();
					String correctFromStructureName = correctStructuresByIdSet.get(correctElement.getProperty("from")).getName();
					
					if(isEqualProperty(correctElement.getProperty("name"), createdElement.getProperty("name")) &&
							isEqualProperty(correctElement.getProperty("negation"), createdElement.getProperty("negation")) &&
							isEqualProperty(createdToStructureName, correctToStructureName) && 
							isEqualProperty(createdFromStructureName, correctFromStructureName) ) {
						return name + ": " + createdElement.getProperty("id") + " <-> " + correctElement.getProperty("id");
					}
				}
			}
		}
		return null;
	}
}
