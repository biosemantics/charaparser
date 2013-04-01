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
				/*log(LogLevel.DEBUG, "structure comparison: ");
				log(LogLevel.DEBUG, createdElement.getProperty("name"));
				log(LogLevel.DEBUG, correctElement.getProperty("name"));
				log(LogLevel.DEBUG, createdElement.getProperty("constraint"));
				log(LogLevel.DEBUG, correctElement.getProperty("constraint"));*/
				if(isEqualProperty(correctElement.getAttribute("name"), createdElement.getAttribute("name")) &&
						isEqualProperty(correctElement.getAttribute("constraint"), createdElement.getAttribute("constraint"))) {
					int score = getCharachterOverlap(getCharacters(createdElement), getCharacters(correctElement)).size();
					if(score > candidateScore) {
						candidateScore = score;
						candidate = name + ": " + createdElement.getAttribute("id") + " <-> " + correctElement.getAttribute("id");
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
				/*log(LogLevel.DEBUG, "character comparison: ");
				log(LogLevel.DEBUG, createdElement.getProperty("name"));
				log(LogLevel.DEBUG, correctElement.getProperty("name"));
				log(LogLevel.DEBUG, createdElement.getProperty("value"));
				log(LogLevel.DEBUG, correctElement.getProperty("value"));
				log(LogLevel.DEBUG, createdElement.getProperty("from"));
				log(LogLevel.DEBUG, correctElement.getProperty("from"));
				log(LogLevel.DEBUG, createdElement.getProperty("to"));
				log(LogLevel.DEBUG, correctElement.getProperty("to"));*/
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
				//log(LogLevelDEBUG, "created element " + createdElement);
				///log(LogLevel.DEBUG, "correct element " + correctElements);
				if(isEqualProperty(correctElement.getAttribute("name"), createdElement.getAttribute("name")) && 
						isEqualProperty(correctElement.getAttribute("value"), createdElement.getAttribute("value")) &&
						isEqualProperty(correctElement.getAttribute("from"), createdElement.getAttribute("from")) && 
						isEqualProperty(correctElement.getAttribute("to"), createdElement.getAttribute("to")) ) {
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
				/*log(LogLevel.DEBUG, "relation comparison: ");
				log(LogLevel.DEBUG, createdElement.getProperty("name"));
				log(LogLevel.DEBUG, correctElement.getProperty("name"));
				log(LogLevel.DEBUG, createdElement.getProperty("negation"));
				log(LogLevel.DEBUG, correctElement.getProperty("negation"));
				log(LogLevel.DEBUG, createdElement.getProperty("from"));
				log(LogLevel.DEBUG, correctElement.getProperty("from"));
				log(LogLevel.DEBUG, createdElement.getProperty("to"));
				log(LogLevel.DEBUG, correctElement.getProperty("to")); */
				/*log(LogLevel.DEBUG, "1" + createdRelationsSet);
				log(LogLevel.DEBUG, "2" + createdStructuresByIdSet);
				log(LogLevel.DEBUG, "3" + correctRelationsSet);
				log(LogLevel.DEBUG, "4" + correctStructuresByIdSet);
				log(LogLevel.DEBUG, "5" + createdElement.getProperty("to"));
				log(LogLevel.DEBUG, "6" + createdElement.getProperty("from"));
				log(LogLevel.DEBUG, "7" + correctElement.getProperty("to"));
				log(LogLevel.DEBUG, "8" + correctElement.getProperty("from"));
				log(LogLevel.DEBUG, "9" + createdStructuresByIdSet.get(createdElement.getProperty("to")));
				log(LogLevel.DEBUG, "10" + createdStructuresByIdSet.get(createdElement.getProperty("from")));
				log(LogLevel.DEBUG, "11" + correctStructuresByIdSet.get(correctElement.getProperty("to")));
				log(LogLevel.DEBUG, "12" + correctStructuresByIdSet.get(correctElement.getProperty("from")));*/
				
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
			if(createdStructuresByIdSet.containsKey(createdElement.getAttribute("to")) && createdStructuresByIdSet.containsKey(createdElement.getAttribute("from"))) {
				String createdToStructureName = createdStructuresByIdSet.get(createdElement.getAttribute("to")).getName();
				String createdFromStructureName = createdStructuresByIdSet.get(createdElement.getAttribute("from")).getName();
				
				for(DescriptionTreatmentElement correctElement : correctElements) {
					String correctToStructureName = correctStructuresByIdSet.get(correctElement.getAttribute("to")).getName();
					String correctFromStructureName = correctStructuresByIdSet.get(correctElement.getAttribute("from")).getName();
					
					if(isEqualProperty(correctElement.getAttribute("name"), createdElement.getAttribute("name")) &&
							isEqualProperty(correctElement.getAttribute("negation"), createdElement.getAttribute("negation")) &&
							isEqualProperty(createdToStructureName, correctToStructureName) && 
							isEqualProperty(createdFromStructureName, correctFromStructureName) ) {
						return name + ": " + createdElement.getAttribute("id") + " <-> " + correctElement.getAttribute("id");
					}
				}
			}
		}
		return null;
	}
}
