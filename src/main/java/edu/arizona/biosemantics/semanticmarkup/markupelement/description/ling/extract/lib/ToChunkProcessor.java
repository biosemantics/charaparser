package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;






import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.extract.IChunkProcessor;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character;


/**
 * ToChunkProcessor processes chunks of ChunkType.TO
 * @author rodenhausen
 */
public class ToChunkProcessor extends AbstractChunkProcessor {

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
	public ToChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase,
				posKnowledgeBase, baseCountWords, locationPrepositions, clusters,
				units, equalCharacters, numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		List<Element> results = new LinkedList<Element>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		ArrayList<String> alternativeIds = new ArrayList<String>();
		List<BiologicalEntity> parents = parentStructures(processingContext, processingContextState, alternativeIds);
		List<Chunk> modifiers = new LinkedList<Chunk>();
		List<Chunk> unassignedModifiers = processingContextState.getUnassignedModifiers();
		modifiers.addAll(unassignedModifiers);
		
		String characterName = null;
		if(processingContextState.getUnassignedCharacter()!=null) {
			characterName = processingContextState.getUnassignedCharacter();
			processingContextState.setUnassignedCharacter(null);
		}
				
		//process characters recursively with the appropriate processor
		List<Chunk> characterStateChunks = chunk.getChunks(ChunkType.CHARACTER_STATE);
		if(!characterStateChunks.isEmpty()) {
			for(Chunk characterStateChunk : characterStateChunks) {
				processingContextState.setUnassignedCharacter(characterName);
				processingContextState.setLastElements(parents);
				IChunkProcessor processor = processingContext.getChunkProcessor(ChunkType.CHARACTER_STATE);
				if(processor instanceof MyCharacterStateChunkProcessor)
					((MyCharacterStateChunkProcessor) processor).setEqcharaExempt();//ToChunk is exempted from eqcharacter checking				
				results.addAll(processor.process(characterStateChunk, processingContext));
				if(processor instanceof MyCharacterStateChunkProcessor)
					((MyCharacterStateChunkProcessor) processor).resetEqcharaExempt();//ToChunk is exempted from eqcharacter checking				
				processingContextState = processingContext.getCurrentState();
			}
			
			//and create a range charater too
			if(characterName == null)
				characterName = characterStateChunks.get(0).getProperty("characterName");
			
			String character = chunk.getTerminalsText();
			List<Element> rangeCharacter = createRangeCharacterElement(parents, modifiers, character, characterName, processingContextState);
			
			addAlternativeIds(rangeCharacter, alternativeIds);
			if(!rangeCharacter.isEmpty()){ //to [not or]
				for(Element element: results){ //add notes: "[duplicate value]" for the characters as they repeats what is in the rangeCharacter
					if(element.isCharacter()){
						((Character)element).setNotes("[duplicate value]");
					}
				}
			}
			results.addAll(rangeCharacter);
			
		}
		processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		processingContextState.setUnassignedCharacter(null);
		processingContextState.setLastElements(results);
		
		//return new LinkedList<Element>(); //why?
		return results;
	}
}
