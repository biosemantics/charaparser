package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;







import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * CountChunkProcessor processes chunks of ChunkType.COUNT
 * @author rodenhausen
 */
public class CountChunkProcessor extends AbstractChunkProcessor {

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
	public CountChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		LinkedList<Element> result = new LinkedList<Element>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		Chunk nextChunk = null;
		if(chunkListIterator.hasNext()){
			nextChunk = chunkListIterator.next();
			chunkListIterator.previous();
		}
		
		List<Chunk> modifiers = processingContextState.getUnassignedModifiers();

		LinkedList<Element> lastElements = processingContextState.getLastElements();
		LinkedList<BiologicalEntity> subjects = processingContextState.getSubjects();
		LinkedList<BiologicalEntity> parents = new LinkedList<BiologicalEntity>();
		if(lastElements.isEmpty() || !lastElements.getLast().isStructure()) {
			if(!subjects.isEmpty())
				parents.add(subjects.getLast());
		} else {
			parents.add((BiologicalEntity)lastElements.getLast()); 
		}
		
		List<Element> characterElement = 
				this.annotateNumericals(chunk.getTerminalsText(), "count", modifiers, parents, false, processingContextState);
		//1/4 PP: [PREPOSITION: [CHARACTER_STATE: characterName->character; [STATE: [lengths]], of], OBJECT: [ORGAN: [blades]]]
		if(nextChunk!=null && nextChunk.isOfChunkType(ChunkType.PP) && nextChunk.getChildChunk(ChunkType.PREPOSITION).getChildChunk(ChunkType.CHARACTER_STATE)!=null && 
				nextChunk.getChildChunk(ChunkType.PREPOSITION).getChildChunk(ChunkType.CHARACTER_STATE).getProperty("characterName").compareTo("character")==0){
			String character = nextChunk.getChildChunk(ChunkType.PREPOSITION).getChildChunk(ChunkType.CHARACTER_STATE).getChildChunk(ChunkType.STATE).getTerminalsText();
			for(Element ce: characterElement){
				Character c = (Character)ce;
				c.setName(inflector.getSingular(character)); //count => length
				//update values
				if(c.getValue()!=null){
					c.setValue(c.getValue()+" "+nextChunk.getTerminalsText());
				}else{
					if(c.getTo()!=null)
						c.setTo(c.getTo()+" "+nextChunk.getTerminalsText());
					if(c.getFrom()!=null)
						c.setFrom(c.getFrom()+" "+nextChunk.getTerminalsText());
				}
			}
			chunkListIterator.next(); //adjust the 'next' pointer
		}
		

		if(!parents.isEmpty() && parents.getLast().isStructure()) {
			if(characterElement!=null)
				result.addAll(characterElement);
			processingContextState.clearUnassignedModifiers();
		} else {
			for(Element ce: characterElement){
				if(ce.isCharacter()){
					processingContextState.getUnassignedCharacters().add((Character)ce);
				}
			}
		}
		
		processingContextState.setLastElements(result);
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		return result;
	}

}
