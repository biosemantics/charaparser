package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.know.ICharacterKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.know.IPOSKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.chunk.Chunk;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkType;
import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Character;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Relation;
import edu.arizona.sirls.semanticMarkup.model.Element;

/**
 * MyModifierChunkProcessor processes chunks of ChunkType.MODIFIER
 * @author rodenhausen
 */
public class MyModifierChunkProcessor extends AbstractChunkProcessor {

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
	public MyModifierChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		Chunk nextChunk = chunkListIterator.next();
		chunkListIterator.previous();
		
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<Element> lastElements = processingContextState.getLastElements();
		if(!lastElements.isEmpty()) {
			Element lastElement = lastElements.getLast();
			
			if(!processingContextState.isCommaAndOrEosEolAfterLastElements() 
					&& !processingContextState.isUnassignedChunkAfterLastElements()) {
				if(lastElement.isRelation()) 
					((Relation)lastElement).appendModifier(chunk.getTerminalsText());
				else if((!nextChunk.isOfChunkType(ChunkType.PP) && 
									lastElement.isCharacter())) {
					((Character)lastElement).appendModifier(chunk.getTerminalsText());
				}
				
			} else 
				processingContextState.getUnassignedModifiers().add(chunk);
		}
		return new LinkedList<Element>();
	}
}
