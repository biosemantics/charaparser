package semanticMarkup.ling.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.log.LogLevel;

public class ProcessingContextState implements Cloneable {

	private String unassignedCharacter;
	private List<Chunk> unassignedModifiers = new ArrayList<Chunk>();
	private List<Chunk> unassignedConstraints = new ArrayList<Chunk>();
	private List<DescriptionTreatmentElement> unassignedCharacters = new ArrayList<DescriptionTreatmentElement>();

	private DescriptionTreatmentElement mainSubjectStructure = null;
	private DescriptionTreatmentElement previousCharacter = null;
	
	private LinkedList<DescriptionTreatmentElement> subjects = new LinkedList<DescriptionTreatmentElement>();
	private LinkedList<DescriptionTreatmentElement> lastElements = new LinkedList<DescriptionTreatmentElement>();
	
	private boolean commaEosEolAfterLastElements = false; 

	private int structureId;
	private HashMap<Integer, DescriptionTreatmentElement> structures = new HashMap<Integer, DescriptionTreatmentElement>();
	private int relationId;
	private HashMap<Integer, DescriptionTreatmentElement> relations = new HashMap<Integer, DescriptionTreatmentElement>();
	private HashMap<Integer, Set<DescriptionTreatmentElement>> relationsFromStructure = new HashMap<Integer, Set<DescriptionTreatmentElement>>();
	private HashMap<Integer, Set<DescriptionTreatmentElement>> relationsToStructure = new HashMap<Integer, Set<DescriptionTreatmentElement>>();
	
	private String notInModifier;

	private int clauseModifierContraintId = -1;
	private String clauseModifierContraint = null;

	private ArrayList<Entry<String, String>> scopeProperties;

	private int inBracketsLevel = 0;
	
	public void reset() {
		unassignedCharacter = null;
		mainSubjectStructure = null;
	}

	public DescriptionTreatmentElement getStructure(int structureId) {
		return structures.get(structureId);
	}
	
	public DescriptionTreatmentElement getRelation(int relationId) {
		return relations.get(relationId);
	}
	
	public Set<DescriptionTreatmentElement> getRelationsTo(int toStructureId) {
		Set<DescriptionTreatmentElement> result = new HashSet<DescriptionTreatmentElement>();
		if(relationsToStructure.containsKey(toStructureId))
			return relationsToStructure.get(toStructureId);
		return result;
	}
	
	public Set<DescriptionTreatmentElement> getRelationsFrom(int fromStructureId) {
		Set<DescriptionTreatmentElement> result = new HashSet<DescriptionTreatmentElement>();
		if(relationsFromStructure.containsKey(fromStructureId))
			return relationsFromStructure.get(fromStructureId);
		return result;
	}
	
	public int getRelationId() {
		return relationId;
	}
	
	public int fetchAndIncrementRelationId(DescriptionTreatmentElement relation) {
		relations.put(relationId, relation);
		int fromId = Integer.parseInt(relation.getProperty("from").substring(1));
		int toId = Integer.parseInt(relation.getProperty("to").substring(1));
		
		if(!relationsFromStructure.containsKey(fromId))
			relationsFromStructure.put(fromId, new HashSet<DescriptionTreatmentElement>());
		if(!relationsToStructure.containsKey(toId))
			relationsToStructure.put(toId, new HashSet<DescriptionTreatmentElement>());
		relationsFromStructure.get(fromId).add(relation);
		relationsToStructure.get(toId).add(relation);
		
		return relationId++;
	}

	public void setRelationId(int relationId) {
		this.relationId = relationId;
	}

	public ArrayList<Entry<String, String>> getScopeProperties() {
		return scopeProperties;
	}

	public void setScopeProperties(
			ArrayList<Entry<String, String>> scopeProperties) {
		this.scopeProperties = scopeProperties;
	}

	public int getClauseModifierContraintId() {
		return clauseModifierContraintId;
	}

	public void setClauseModifierContraintId(int clauseModifierContraintId) {
		this.clauseModifierContraintId = clauseModifierContraintId;
	}

	public String getClauseModifierContraint() {
		return clauseModifierContraint;
	}

	public void setClauseModifierContraint(String clauseModifierContraint) {
		this.clauseModifierContraint = clauseModifierContraint;
	}


	public String getUnassignedCharacter() {
		return unassignedCharacter;
	}

	public void setUnassignedCharacter(String unassignedCharacter) {
		this.unassignedCharacter = unassignedCharacter;
	}

	public DescriptionTreatmentElement getMainSubjectStructure() {
		return mainSubjectStructure;
	}

	public DescriptionTreatmentElement getPreviousCharacter() {
		return previousCharacter;
	}

	public LinkedList<DescriptionTreatmentElement> getSubjects() {
		return subjects;
	}
	
	public void setSubjects(LinkedList<DescriptionTreatmentElement> subjects) {
		this.subjects = subjects;
	}

	public LinkedList<DescriptionTreatmentElement> getLastElements() {
		return lastElements;
	}

	public void setLastElements(LinkedList<DescriptionTreatmentElement> lastElements) {
		if(lastElements==null)
			this.lastElements.clear();
		else
			this.lastElements = lastElements;
	}
	
	//same info stored in the results list of descriptionextractor?
	/*
	public boolean hasLastElements() {
		return !lastElements.isEmpty();
	}
	
	public int numberOfLastElements() {
		return lastElements.size();
	}
	
	public DescriptionTreatmentElement getLastElement() {
		return lastElements.getLast();
	}
	
	public DescriptionTreatmentElement getIthLastElement(int i) {
		return lastElements.get(lastElements.size()-(i));
	}
	
	public void addLastElement(DescriptionTreatmentElement lastElement) {
		lastElements.add(lastElement);
	}
	
	public void addLastElements(List<DescriptionTreatmentElement> lastElements) {
		lastElements.addAll(lastElements);
	}
	
	public void clearLastElements() {
		lastElements.clear();
	}*/
	
	/*public boolean hasSubjectElements() {
		return !subjects.isEmpty();
	}
	
	public int numberOfSubjectElements() {
		return subjects.size();
	}
	
	public DescriptionTreatmentElement getLastSubject() {
		return subjects.getLast();
	}
	
	public DescriptionTreatmentElement getIthLastSubject(int i) {
		return subjects.get(subjects.size()-(i));
	}
	
	public void addSubject(DescriptionTreatmentElement subject) {
		subjects.add(subject);
	}
	
	public void addSubjects(List<DescriptionTreatmentElement> subjects) {
		this.subjects.addAll(subjects);
	}
	
	public void clearLastElements() {
		subjects.clear();
	}*/
	
	public int getStructureId() {
		return structureId;
	}
	
	public int fetchAndIncrementStructureId(DescriptionTreatmentElement structure) {
		structures.put(structureId, structure);
		return structureId++;
	}

	public String getNotInModifier() {
		return notInModifier;
	}

	public List<Chunk> getUnassignedModifiers() {
		return unassignedModifiers;
	}
	
	public List<Chunk> getUnassignedConstraints() {
		return unassignedConstraints;
	}

	public void setPreviousCharacter(
			DescriptionTreatmentElement previousCharacter) {
		this.previousCharacter = previousCharacter;
	}

	public void setStructureId(int structureId) {
		this.structureId = structureId;
	}

	public void clearUnassignedConstraints() {
		this.unassignedConstraints.clear();
	}
	
	public void setUnassignedConstraints(List<Chunk> unassignedConstraints) {
		this.unassignedConstraints = unassignedConstraints;
	}

	public void clearUnassignedModifiers() {
		this.unassignedModifiers.clear();
	}

	public void clearUnassignedCharacters() {
		this.unassignedCharacter = null;
	}
	
	public void setUnassignedModifiers(List<Chunk> unassignedModifiers) {
		this.unassignedModifiers = unassignedModifiers;
	}

	public void increaseInBrackets() {
		this.inBracketsLevel++;
	}
	
	public void decreaseInBrackets() {
		this.inBracketsLevel--;
	}
	
	public boolean isInBrackets() {
		return this.inBracketsLevel != 0;
	}
	
	public int getInBracketsLevel() {
		return this.inBracketsLevel;
	}
	
	public List<DescriptionTreatmentElement> getUnassignedCharacters() {
		return unassignedCharacters;
	}

	public void setUnassignedCharacters(List<DescriptionTreatmentElement> unassignedCharacters) {
		this.unassignedCharacters = unassignedCharacters;
	}
	

	public boolean isCommaEosEolAfterLastElements() {
		return commaEosEolAfterLastElements;
	}

	public void setCommaEosEolAfterLastElements(boolean commaEosEolAfterLastElements) {
		this.commaEosEolAfterLastElements = commaEosEolAfterLastElements;
	}

	@Override
	public Object clone() {
		try {
			ProcessingContextState clone = (ProcessingContextState)super.clone();
			clone.unassignedCharacter = this.unassignedCharacter==null ? null : new String(this.unassignedCharacter);
			clone.unassignedModifiers = new ArrayList<Chunk>();
			clone.unassignedModifiers.addAll(this.unassignedModifiers);
			clone.mainSubjectStructure = this.mainSubjectStructure;
			clone.previousCharacter = this.previousCharacter;
			clone.subjects = new LinkedList<DescriptionTreatmentElement>();
			clone.subjects.addAll(this.subjects);
			clone.lastElements = new LinkedList<DescriptionTreatmentElement>();
			clone.lastElements.addAll(this.lastElements);
			clone.structureId = new Integer(this.structureId);
			clone.structures = new HashMap<Integer, DescriptionTreatmentElement>();
			clone.structures.putAll(this.structures);
			clone.relationId = new Integer(this.relationId);
			clone.relations = new HashMap<Integer, DescriptionTreatmentElement>();
			clone.relations.putAll(this.relations);
			clone.relationsFromStructure = new HashMap<Integer, Set<DescriptionTreatmentElement>>();
			clone.relationsFromStructure.putAll(this.relationsFromStructure);
			clone.relationsToStructure = new HashMap<Integer, Set<DescriptionTreatmentElement>>();
			clone.relationsToStructure.putAll(this.relationsToStructure);
			clone.notInModifier = this.notInModifier==null ? null : new String(this.notInModifier);
			//clone.chunkListIterator = chunkCollector.getChunks().listIterator(this.chunkListIterator.nextIndex());
			//clone.chunkCollector = this.chunkCollector;
			clone.clauseModifierContraintId = new Integer(this.clauseModifierContraintId);
			clone.clauseModifierContraint = this.clauseModifierContraint==null ? null : new String(this.clauseModifierContraint);
			clone.scopeProperties = new ArrayList<Entry<String, String>>();
			if(scopeProperties!=null)
				clone.scopeProperties.addAll(scopeProperties);
			clone.inBracketsLevel = new Integer(this.inBracketsLevel);
			//clone.chunkProcessorProvider = this.chunkProcessorProvider;
			//clone.result = this.result;
			return clone;
		} catch (CloneNotSupportedException e) {
			log(LogLevel.ERROR, e);
		}
		return null;
	}

}
