package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;


/**
 * ProcessingContextState provides contextual information for the processing of a single chunk
 * @author rodenhausen
 */
public class ProcessingContextState implements Cloneable {

	private String unassignedCharacter;
	private List<Chunk> unassignedModifiers = new ArrayList<Chunk>(); //character/relation modifiers
	private List<Chunk> unassignedConstraints = new ArrayList<Chunk>();//structure constraint
	private List<Character> unassignedCharacters = new ArrayList<Character>();

	//private Structure mainSubjectStructure = null;
	private Character previousCharacter = null;
	
	private LinkedList<BiologicalEntity> subjects = new LinkedList<BiologicalEntity>();
	private LinkedList<Element> lastElements = new LinkedList<Element>();
	
	private boolean commaAndOrEosEolAfterLastElements = false; 
	private boolean unassignedChunkAfterLastElements = false;
	
	private String notInModifier;

	private String clauseModifierContraintId = null;
	private String clauseModifierContraint = null;

	private ArrayList<Entry<String, String>> scopeProperties;

	private int inBracketsLevel = 0;
	
	/**
	 * Reset to the initial state
	 */
	public void reset() {
		unassignedCharacter = null;
		unassignedModifiers = new ArrayList<Chunk>();
		unassignedConstraints = new ArrayList<Chunk>();
		unassignedCharacters = new ArrayList<Character>();
		previousCharacter = null;
		//mainSubjectStructure = null;
		subjects = new LinkedList<BiologicalEntity>();
		lastElements = new LinkedList<Element>();
		commaAndOrEosEolAfterLastElements = false; 
		unassignedChunkAfterLastElements = false;
		notInModifier = null;
		clauseModifierContraintId = null;
		clauseModifierContraint = null;
		scopeProperties = null;
		inBracketsLevel = 0;
	}

	@Override
	public Object clone() {
		try {
			ProcessingContextState clone = (ProcessingContextState)super.clone();
			clone.unassignedCharacter = this.unassignedCharacter==null ? null : new String(this.unassignedCharacter);
			clone.unassignedModifiers = new ArrayList<Chunk>();
			clone.unassignedModifiers.addAll(this.unassignedModifiers);
			clone.unassignedConstraints = new ArrayList<Chunk>();
			clone.unassignedConstraints.addAll(this.unassignedConstraints);
			clone.unassignedCharacters = new ArrayList<Character>();
			clone.unassignedCharacters.addAll(this.unassignedCharacters);
			//clone.mainSubjectStructure = this.mainSubjectStructure;
			clone.previousCharacter = this.previousCharacter==null? null: this.previousCharacter;
			clone.subjects = new LinkedList<BiologicalEntity>();
			clone.subjects.addAll(this.subjects);
			clone.lastElements = new LinkedList<Element>(this.lastElements);
			clone.commaAndOrEosEolAfterLastElements = this.commaAndOrEosEolAfterLastElements; 
			clone.unassignedChunkAfterLastElements = this.unassignedChunkAfterLastElements;
			clone.notInModifier = this.notInModifier==null ? null : new String(this.notInModifier);
			//clone.chunkListIterator = chunkCollector.getChunks().listIterator(this.chunkListIterator.nextIndex());
			//clone.chunkCollector = this.chunkCollector;
			clone.clauseModifierContraintId = this.clauseModifierContraintId==null ? null : new String(this.clauseModifierContraintId);
			clone.clauseModifierContraint = this.clauseModifierContraint==null ? null : new String(this.clauseModifierContraint);
			clone.scopeProperties = new ArrayList<Entry<String, String>>();
			if(scopeProperties!=null)
				clone.scopeProperties.addAll(scopeProperties);
			clone.inBracketsLevel = new Integer(this.inBracketsLevel);
			//clone.chunkProcessorProvider = this.chunkProcessorProvider;
			//clone.result = this.result;
			return clone;
		} catch (CloneNotSupportedException e) {
			log(LogLevel.ERROR, "Problem cloning ProcessingContext", e);
		}
		return null;
	}
	/**
	 * @return if there was an unassigned chunk after the last elements
	 */
	public boolean isUnassignedChunkAfterLastElements() {
		return unassignedChunkAfterLastElements;
	}

	/**
	 * @param unassignedChunkAfterLastElements
	 * sets the unassignedChunkAfterLastElements flag
	 */
	public void setUnassignedChunkAfterLastElements(
			boolean unassignedChunkAfterLastElements) {
		this.unassignedChunkAfterLastElements = unassignedChunkAfterLastElements;
	}

	/**
	 * @return the scope properties
	 */
	public ArrayList<Entry<String, String>> getScopeProperties() {
		return scopeProperties;
	}

	/**
	 * @param scopeProperties to set
	 */
	public void setScopeProperties(
			ArrayList<Entry<String, String>> scopeProperties) {
		this.scopeProperties = scopeProperties;
	}

	/**
	 * @return the clauseModifierConstraintId
	 */
	public String getClauseModifierContraintId() {
		return clauseModifierContraintId;
	}

	/**
	 * @param clauseModifierContraintId to set
	 */
	public void setClauseModifierContraintId(String clauseModifierContraintId) {
		this.clauseModifierContraintId = clauseModifierContraintId;
	}

	/**
	 * @return the clauseModifierConstraint
	 */
	public String getClauseModifierContraint() {
		return clauseModifierContraint;
	}

	/**
	 * @param clauseModifierContraint to set
	 */
	public void setClauseModifierContraint(String clauseModifierContraint) {
		this.clauseModifierContraint = clauseModifierContraint;
	}

	/**
	 * @return unassignedCharacter
	 */
	public String getUnassignedCharacter() {
		return unassignedCharacter;
	}

	/**
	 * @param unassignedCharacter to set
	 */
	public void setUnassignedCharacter(String unassignedCharacter) {
		this.unassignedCharacter = unassignedCharacter;
	}

	/**
	 * @return the mainSubjectStructure
	 */
	/*public Structure getMainSubjectStructure() {
		return mainSubjectStructure;
	}*/

	/**
	 * @return the previousCharacter
	 */
	public Character getPreviousCharacter() {
		return previousCharacter;
	}

	/**
	 * @return the subjects
	 */
	public LinkedList<BiologicalEntity> getSubjects() {
		return subjects;
	}
	
	/**
	 * set the subjects
	 * @param subjects
	 */
	public void setSubjects(LinkedList<BiologicalEntity> subjects) {
		this.subjects = subjects;
	}

	/**
	 * @return the last elements
	 */
	public LinkedList<Element> getLastElements() {
		return lastElements;
	}

	/**
	 * @param lastElements to set
	 */
	public void setLastElements(List<? extends Element> lastElements) {
		if(lastElements==null)
			this.lastElements.clear();
		else {
			this.lastElements.clear();
			this.lastElements.addAll(lastElements);
			System.out.print("");
		}
	}

	/**
	 * @return notInModifier
	 */
	public String getNotInModifier() {
		return notInModifier;
	}

	/**
	 * @return unassigned character Modifiers
	 */
	public List<Chunk> getUnassignedModifiers() {
		return unassignedModifiers;
	}
	
	/**
	 * @return unassigned structure constraints
	 */
	public List<Chunk> getUnassignedConstraints() {
		return unassignedConstraints;
	}

	/**
	 * @param previousCharacter to set
	 */
	public void setPreviousCharacter(Character previousCharacter) {
		this.previousCharacter = previousCharacter;
	}

	/**
	 * clears the unassigned constraints
	 */
	public void clearUnassignedConstraints() {
		this.unassignedConstraints.clear();
	}
	
	/**
	 * @param unassignedConstraints to set
	 */
	public void setUnassignedConstraints(List<Chunk> unassignedConstraints) {
		this.unassignedConstraints = unassignedConstraints;
	}

	/**
	 * clears unassigned modifiers
	 */
	public void clearUnassignedModifiers() {
		this.unassignedModifiers.clear();
	}
	
	/**
	 * @param unassignedModifiers to set
	 */
	public void setUnassignedModifiers(List<Chunk> unassignedModifiers) {
		this.unassignedModifiers = unassignedModifiers;
	}

	/**
	 * increases the inbrackets count
	 */
	public void increaseInBrackets() {
		this.inBracketsLevel++;
	}
	
	/**
	 * decreases the inbrackets count
	 */
	public void decreaseInBrackets() {
		this.inBracketsLevel--;
	}
	
	/**
	 * @return if current processed chunks are within brackets
	 */
	public boolean isInBrackets() {
		return this.inBracketsLevel != 0;
	}
	
	/**
	 * @return inbrackets count
	 */
	public int getInBracketsLevel() {
		return this.inBracketsLevel;
	}
	
	/**
	 * @return unassigned characters
	 */
	public List<Character> getUnassignedCharacters() {
		return unassignedCharacters;
	}

	/**
	 * @param unassignedCharacters to set
	 */
	public void setUnassignedCharacters(List<Character> unassignedCharacters) {
		this.unassignedCharacters = unassignedCharacters;
	}
	
	/**
	 * @return if comma, and, or, eos, or eol appeared after the last elements
	 */
	public boolean isCommaAndOrEosEolAfterLastElements() {
		return commaAndOrEosEolAfterLastElements;
	}

	/**
	 * @param commaAndOrEosEolAfterLastElements to set
	 */
	public void setCommaAndOrEosEolAfterLastElements(boolean commaAndOrEosEolAfterLastElements) {
		this.commaAndOrEosEolAfterLastElements = commaAndOrEosEolAfterLastElements;
	}




}