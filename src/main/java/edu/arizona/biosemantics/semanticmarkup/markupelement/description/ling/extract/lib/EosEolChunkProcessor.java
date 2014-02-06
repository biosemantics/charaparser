package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;



import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.ILastChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * EosEolChunkProcessor processes chunks of ChunkType.END_OF_LINE or ChunkType.END_OF_SUBCLAUSE
 * @author rodenhausen
 */
public class EosEolChunkProcessor extends AbstractChunkProcessor implements ILastChunkProcessor {

	/**
	 * @param inflector
	 * @param glossary
	 * @param terminologyLearner
	 * @param characterKnowledgeBase
	 * @param posKnowledgeBase
	 * @param baseCountWords
	 * @param locationPrepositions
	 * @param clusters
	 * @param units
	 * @param equalCharacters
	 * @param numberPattern
	 * @param times
	 */
	@Inject
	public EosEolChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Element> result = new ArrayList<Element>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		processingContextState.setCommaAndOrEosEolAfterLastElements(true);
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		
		if(!unassignedModifiers.isEmpty()) {
			LinkedList<Element> lastElements = processingContextState.getLastElements();
			if(!lastElements.isEmpty() && lastElements.getLast().isStructure()) {
				for(Element element : lastElements) {
					if(element.isStructure()) {
						int structureId = Integer.valueOf(((Structure)element).getId().substring(1));
						
						Set<Relation> relations = processingContext.getRelationsTo(structureId);
						int greatestId = 0;
						Relation latestRelation = null;
						for(Relation relation : relations) {
							int id = Integer.valueOf(relation.getId().substring(1));
							if(id > greatestId) {
								greatestId = id;
								latestRelation = relation;
							}
						}
						
						if(latestRelation != null) {
							for(Chunk modifier : unassignedModifiers) 
								latestRelation.appendModifier(modifier.getTerminalsText());
							result.add(latestRelation);
						}
						//TODO: otherwise, categorize modifier and create a character for the structure e.g.{thin} {dorsal} {median} <septum> {centrally} only ;
					}
				}
				
			} else if(!lastElements.isEmpty() && lastElements.getLast().isCharacter()) {
				for(Element element : lastElements) {
					if(element.isCharacter()) {
						for(Chunk modifier : unassignedModifiers) 
							((Character)element).appendModifier(modifier.getTerminalsText());
						result.add(element);
					}
				}
			}
		}
		
		List<Character> unassignedCharacters = processingContextState.getUnassignedCharacters();
		if(!unassignedCharacters.isEmpty()) {
			Structure structureElement = new Structure();
			int structureIdString = processingContext.fetchAndIncrementStructureId(structureElement);
			structureElement.setId("o" + String.valueOf(structureIdString));	
			structureElement.setName("whole_organism"); 
			List<Structure> structureElements = new LinkedList<Structure>();
			structureElements.add(structureElement);
			result.addAll(establishSubject(structureElements, processingContextState));
			
			for(Character character : unassignedCharacters) {
				for(Structure parent : structureElements) {
					parent.addCharacter(character);
				}
			}
		}
		unassignedCharacters.clear();
		
		processingContextState.clearUnassignedModifiers();
		
		return result;
	}

	@Override
	public List<? extends Element> process(ProcessingContext processingContext) {
		return this.process(null, processingContext);
	}
}
