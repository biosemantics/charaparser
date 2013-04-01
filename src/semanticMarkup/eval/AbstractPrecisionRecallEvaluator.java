package semanticMarkup.eval;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import semanticMarkup.core.ContainerTreatmentElement;
import semanticMarkup.core.Treatment;
import semanticMarkup.core.TreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.core.description.DescriptionTreatmentElementType;
import semanticMarkup.log.LogLevel;

public abstract class AbstractPrecisionRecallEvaluator implements IEvaluator {

	IEvaluationResult result = null;

	@Override
	public IEvaluationResult getResult() {
		return result;
	}
	
	@Override
	public String getDescription() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void evaluate(List<Treatment> createdTreatments, List<Treatment> correctTreatments) {
		//log(LogLevel.DEBUG, "created treatments " + createdTreatments);
		//log(LogLevel.DEBUG, "correct treatments " + correctTreatments);
		Collections.sort(createdTreatments, new Comparator<Treatment>() {
			@Override
			public int compare(Treatment t1, Treatment t2) {
				return Integer.parseInt(t1.getName()) - Integer.parseInt(t2.getName());
			}
		});
		Collections.sort(correctTreatments, new Comparator<Treatment>() {
			@Override
			public int compare(Treatment t1, Treatment t2) {
				return Integer.parseInt(t1.getName()) - Integer.parseInt(t2.getName());
			}
		});
		
		if(createdTreatments.size()!=correctTreatments.size()) {
			result = new StringEvaluationResult("not equal size of treatments");
			return;
		}
		
		int overlapStructures = 0;
		int createdStructures = 0;
		int correctStructures = 0;
		
		int overlapRelations = 0;
		int createdRelations = 0;
		int correctRelations = 0;
		
		int overlapCharacters = 0;
		int createdCharacters = 0;
		int correctCharacters = 0;
		
		log(LogLevel.DEBUG, "number of treatments to compare: " + correctTreatments.size());
		for(int i=0; i<createdTreatments.size(); i++) {
			log(LogLevel.DEBUG, "evaluate treatment " + createdTreatments.get(i).getName());
			//log(LogLevel.DEBUG, "evaluate treatment " + createdTreatments.get(i));
			/*log(LogLevel.DEBUG, createdTreatments.get(i).getName());
			log(LogLevel.DEBUG, correctTreatments.get(i).getName());*/
			
			ContainerTreatmentElement createdDescription = createdTreatments.get(i).getContainerTreatmentElement("description");
			ContainerTreatmentElement correctDescription = correctTreatments.get(i).getContainerTreatmentElement("description");
			
			//statements are expected to have the same number if not, then some trivial error must have occured. They are hence not included in 
			//recall precision measures but instead an error is thrown if this is not the case.
			
			List<TreatmentElement> createdStatements = createdDescription.getTreatmentElements(DescriptionTreatmentElementType.STATEMENT.toString());
			List<TreatmentElement> correctStatements = correctDescription.getTreatmentElements(DescriptionTreatmentElementType.STATEMENT.toString());
			if(createdStatements.size()!=correctStatements.size()) {
				result = new StringEvaluationResult("not equals size of statements");
				return;
			}
			
			for(int j=0; j<createdStatements.size(); j++) {
				DescriptionTreatmentElement createdStatement = (DescriptionTreatmentElement)createdStatements.get(j);
				DescriptionTreatmentElement correctStatement = null;
				for(int k=0; k<correctStatements.size(); k++) {
					correctStatement = (DescriptionTreatmentElement)correctStatements.get(k);
					if(createdStatement.getAttribute("source").equals(correctStatement.getAttribute("source"))) 
						break;
				}
				if(correctStatement == null) {
					log(LogLevel.DEBUG, "couldnt find a correct statement that matches the source of the created statement: " 
							+ createdStatement.getAttribute("source"));
					break;
				}
				log(LogLevel.DEBUG, "source " + createdStatement.getAttribute("source"));
			
				//log(LogLevel.DEBUG, "statement " + j);
				//DescriptionTreatmentElement createdStatement = (DescriptionTreatmentElement)createdStatements.get(j);
				//DescriptionTreatmentElement correctStatement = (DescriptionTreatmentElement)correctStatements.get(j);
				
				HashMap<String, Set<DescriptionTreatmentElement>> createdStructuresSet = getStructures(createdStatement);
				HashMap<String, Set<DescriptionTreatmentElement>> correctStructuresSet = getStructures(correctStatement);
				HashMap<String, DescriptionTreatmentElement> createdStructuresByIdSet = getStructuresById(createdStatement);
				HashMap<String, DescriptionTreatmentElement> correctStructuresByIdSet = getStructuresById(correctStatement);
				log(LogLevel.DEBUG, "created structures " + createdStructuresSet.keySet());
				log(LogLevel.DEBUG, "correct structures " + correctStructuresSet.keySet());
				for(String structure : createdStructuresSet.keySet()) {
					String print = structure + " created " + createdStructuresSet.get(structure).size() + " : correct ";
					if(correctStructuresSet.containsKey(structure))
						print += correctStructuresSet.get(structure).size();
					log(LogLevel.DEBUG, print);
				}
				Set<String> remaining = new HashSet<String>(correctStructuresSet.keySet());
				remaining.removeAll(createdStructuresSet.keySet());
				for(String structure : remaining) {
					String print = structure + " correct " + correctStructuresSet.get(structure).size() + " : created ";
					if(createdStructuresSet.containsKey(structure))
						print += createdStructuresSet.get(structure).size();
					log(LogLevel.DEBUG, print);
				}
				
				HashMap<String, Set<DescriptionTreatmentElement>> createdRelationsSet = getRelations(createdStatement);
				HashMap<String, Set<DescriptionTreatmentElement>> correctRelationsSet = getRelations(correctStatement);
				log(LogLevel.DEBUG, "created relations " + createdRelationsSet.keySet());
				log(LogLevel.DEBUG, "correct relations " + correctRelationsSet.keySet());
				for(String relation : createdRelationsSet.keySet()) {
					String print = relation + " created " + createdRelationsSet.get(relation).size() + " : correct ";
					if(createdRelationsSet.containsKey(relation))
						print += createdRelationsSet.get(relation).size();
					log(LogLevel.DEBUG, print);
				}
				remaining = new HashSet<String>(correctRelationsSet.keySet());
				remaining.removeAll(createdRelationsSet.keySet());
				for(String relation : remaining) {
					String print = relation + " correct " + correctRelationsSet.get(relation).size() + " : created ";
					if(createdRelationsSet.containsKey(relation))
						print += createdRelationsSet.get(relation).size();
					log(LogLevel.DEBUG, print);
				}
				
				Set<String> overlapStructuresSet = getStructureOverlap(createdStructuresSet, correctStructuresSet);
				log(LogLevel.DEBUG, "-----------------structures overlap " + overlapStructuresSet);
				overlapStructures += overlapStructuresSet.size();
				createdStructures += createdStructuresSet.size();
				correctStructures += correctStructuresSet.size();
				for(String structure : overlapStructuresSet) {
					log(LogLevel.DEBUG, "do structure " + structure);
					
					String[] nameIdParts = structure.split(": ");
					structure = nameIdParts[0];
					String[] ids = nameIdParts[1].split(" <-> ");
					String createdStructureId = ids[0];
					String correctStructureId = ids[1];
					
					HashMap<String, Set<DescriptionTreatmentElement>> createdCharactersSet = getCharacters(createdStructuresSet.get(structure), createdStructureId);
					HashMap<String, Set<DescriptionTreatmentElement>> correctCharactersSet = getCharacters(correctStructuresSet.get(structure), correctStructureId);
					log(LogLevel.DEBUG, "created characters for " +structure + " " + createdCharactersSet.keySet());
					log(LogLevel.DEBUG, "correct characters for " +structure + " " + correctCharactersSet.keySet());
					for(String character : createdCharactersSet.keySet()) {
						String print = character + " created " + createdCharactersSet.get(character).size() + " : correct ";
						if(correctCharactersSet.containsKey(character))
							print += correctCharactersSet.get(character).size();
						log(LogLevel.DEBUG, print);
					}
					remaining = new HashSet<String>(correctCharactersSet.keySet());
					remaining.removeAll(createdCharactersSet.keySet());
					for(String character : remaining) {
						String print = character + " correct " + correctCharactersSet.get(character).size() + " : created ";
						if(createdCharactersSet.containsKey(character))
							print += createdCharactersSet.get(character).size();
						log(LogLevel.DEBUG, print);
					}
					
					Set<String> overlapCharactersSet = getCharachterOverlap(createdCharactersSet, correctCharactersSet);
					log(LogLevel.DEBUG, "characters overlap " + overlapCharactersSet);
					overlapCharacters += overlapCharactersSet.size();
					createdCharacters += createdCharactersSet.size();
					correctCharacters += correctCharactersSet.size();
				}
				//int overlap = overlapStructures.size(); not like that, calc from characters too
				
				Set<String> overlapRelationsSet = getRelationOverlap(createdRelationsSet, createdStructuresByIdSet,
						correctRelationsSet, correctStructuresByIdSet);
				log(LogLevel.DEBUG, "----------------------relations overlap " + overlapRelationsSet.size());
				overlapRelations += overlapRelationsSet.size();
				createdRelations += countAllElements(createdRelationsSet);
				correctRelations += countAllElements(correctRelationsSet);
			}
		}
		
		int overlap = overlapStructures + overlapRelations + overlapCharacters;
		int createdItems = createdStructures + createdRelations + createdCharacters;
		int correctItems = correctStructures + correctRelations + correctCharacters;
		
		log(LogLevel.DEBUG, "overlap " + overlap);
		log(LogLevel.DEBUG, "createdItems " + createdItems);
		log(LogLevel.DEBUG, "correctItems " + correctItems);
		double precision = ((double)overlap)/createdItems;
		double recall = ((double)overlap)/correctItems;
		
		log(LogLevel.DEBUG, "overlapStructures " + overlapStructures);
		log(LogLevel.DEBUG, "createdStructures " + createdStructures);
		log(LogLevel.DEBUG, "correctStructures " + correctStructures);
		double precisionStructures = ((double)overlapStructures) / createdStructures;
		double recallStructures = ((double)overlapStructures) / correctStructures;
		
		log(LogLevel.DEBUG, "overlapRelations " + overlapRelations);
		log(LogLevel.DEBUG, "createdRelations " + createdRelations);
		log(LogLevel.DEBUG, "correctRelations " + correctRelations);
		double precisionRelations = ((double)overlapRelations) / createdRelations;
		double recallRelations = ((double)overlapRelations) / correctRelations;
		
		log(LogLevel.DEBUG, "overlapCharacters " + overlapCharacters);
		log(LogLevel.DEBUG, "createdCharacters " + createdCharacters);
		log(LogLevel.DEBUG, "correctCharacters " + correctCharacters);
		double precisionCharacters = ((double)overlapCharacters) / createdCharacters;
		double recallCharacters = ((double)overlapCharacters) / correctCharacters;
				
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Overall \t | \t Precision: " + precision + " \t Recall: " + recall + "\n");
		stringBuilder.append("Structures \t | \t Precision: " + precisionStructures + " \t Recall: " + recallStructures + "\n");
		stringBuilder.append("Characters \t | \t Precision: " + precisionCharacters + " \t Recall: " + recallCharacters + "\n");
		stringBuilder.append("Relations \t | \t Precision: " + precisionRelations + " \t Recall: " + recallRelations + "\n");
		
		result = new StringEvaluationResult(stringBuilder.toString());
	}

	private int countAllElements(HashMap<String, Set<DescriptionTreatmentElement>> map) {
		int result = 0;
		for(Entry<String, Set<DescriptionTreatmentElement>> entry : map.entrySet())
			result += entry.getValue().size();
		return result;
	}

	private HashMap<String, Set<DescriptionTreatmentElement>> getCharacters(Set<DescriptionTreatmentElement> structures, String structureId) {
		for(DescriptionTreatmentElement structure : structures) {
			if(structure.getAttribute("id").equals(structureId)) {
				return getCharacters(structure);
			}
		}
		return new HashMap<String, Set<DescriptionTreatmentElement>>();
	}

	protected HashMap<String, Set<DescriptionTreatmentElement>> getCharacters(DescriptionTreatmentElement structure) {
		HashMap<String, Set<DescriptionTreatmentElement>> result = new HashMap<String, Set<DescriptionTreatmentElement>>();
		for(TreatmentElement characterElement : structure.getTreatmentElements(DescriptionTreatmentElementType.CHARACTER.toString())) {
			DescriptionTreatmentElement character = (DescriptionTreatmentElement)characterElement;
			String name = normalizePropertyName(character.getAttribute("name"));
			if(!result.containsKey(name))
				result.put(name, new HashSet<DescriptionTreatmentElement>());
			result.get(name).add(character);
		}
		return result;
	}

	private HashMap<String, DescriptionTreatmentElement> getStructuresById(DescriptionTreatmentElement statement) {
		HashMap<String, DescriptionTreatmentElement> result = new HashMap<String, DescriptionTreatmentElement>();
		for(TreatmentElement structureElement : statement.getTreatmentElements(DescriptionTreatmentElementType.STRUCTURE.toString())) {
			DescriptionTreatmentElement structure = (DescriptionTreatmentElement)structureElement;
			result.put(structure.getAttribute("id"), structure);
		}
		return result;
	}

	protected abstract Set<String> getStructureOverlap(HashMap<String, Set<DescriptionTreatmentElement>> createdStructuresSet,
			HashMap<String, Set<DescriptionTreatmentElement>> correctStructuresSet);
	
	protected abstract Set<String> getCharachterOverlap(HashMap<String, Set<DescriptionTreatmentElement>> createdCharactersSet,
			HashMap<String, Set<DescriptionTreatmentElement>> correctCharactersSet);
	
	protected abstract Set<String> getRelationOverlap(HashMap<String, Set<DescriptionTreatmentElement>> createdRelationsSet,
			HashMap<String, DescriptionTreatmentElement> createdStructuresByIdSet, 
			HashMap<String, Set<DescriptionTreatmentElement>> correctRelationsSet, 
			HashMap<String, DescriptionTreatmentElement> correctStructuresByIdSet);

	private HashMap<String, Set<DescriptionTreatmentElement>> getRelations(DescriptionTreatmentElement statement) {
		HashMap<String, Set<DescriptionTreatmentElement>> result = new HashMap<String, Set<DescriptionTreatmentElement>>();
		for(TreatmentElement relationElement : statement.getTreatmentElements(DescriptionTreatmentElementType.RELATION.toString())) {
			DescriptionTreatmentElement relation = (DescriptionTreatmentElement)relationElement;
			String name = normalizePropertyName(relation.getAttribute("name"));
			if(!result.containsKey(name))
				result.put(name, new HashSet<DescriptionTreatmentElement>());
			result.get(name).add(relation);
		}
		return result;
	}

	private HashMap<String, Set<DescriptionTreatmentElement>> getStructures(DescriptionTreatmentElement statement) {
		HashMap<String, Set<DescriptionTreatmentElement>> result = new HashMap<String, Set<DescriptionTreatmentElement>>();
		for(TreatmentElement structureElement : statement.getTreatmentElements(DescriptionTreatmentElementType.STRUCTURE.toString())) {
			DescriptionTreatmentElement structure = (DescriptionTreatmentElement)structureElement;
			String name = normalizePropertyName(structure.getAttribute("name"));
			if(!result.containsKey(name))
				result.put(name, new HashSet<DescriptionTreatmentElement>());
			result.get(name).add(structure);
		}
		return result;
	}
	
	protected boolean isEqualProperty(String propertyA, String propertyB) {
		propertyA = normalizePropertyName(propertyA);
		propertyB = normalizePropertyName(propertyB);
		boolean result = Objects.equals(propertyA, propertyB);	
		//if(!result)
		//	log(LogLevel.DEBUG, "properties deemed not equal: " + propertyA + " <-> " + propertyB);
		return result;	
	}
	
	protected String normalizePropertyName(String name) {
		if(name==null) 
			return "";
		return name.replaceAll("_", "").replaceAll("\\s", "").toLowerCase();
	}
}
