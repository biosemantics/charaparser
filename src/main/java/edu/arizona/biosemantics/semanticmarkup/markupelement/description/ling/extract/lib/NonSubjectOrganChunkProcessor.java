package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;









import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * NonSubjectOrganChunkProcessor processes chunks of ChunkType.NON_SUBJECT_ORGAN
 * @author rodenhausen
 */
public class NonSubjectOrganChunkProcessor extends AbstractChunkProcessor {

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
	public NonSubjectOrganChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Structure> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Structure> result = new ArrayList<Structure>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		
		ListIterator<Chunk> chunkListIterator = processingContext.getChunkListIterator();
		Chunk nextChunk = null;
		if(chunkListIterator.hasNext()){
			nextChunk = chunkListIterator.next();
			chunkListIterator.previous();
		}
		if(nextChunk.isOfChunkType(ChunkType.COMMA) || nextChunk.isOfChunkType(ChunkType.END_OF_LINE)||nextChunk.isOfChunkType(ChunkType.END_OF_SUBCLAUSE)){
			//LinkedList<Element> lastElements = processingContextState.getLastElements();
			ArrayList<Chunk> chunks = new ArrayList<Chunk>();
			chunks.add(chunk);
			List<Structure> structures = this.createStructureElements(chunks, processingContext, processingContextState);
			result.addAll(structures);
			//Element lastElement = lastElements.isEmpty() ? null : lastElements.getLast();
			//if(lastElement != null && lastElement.isOfDescriptionType(DescriptionTreatmentElementType.STRUCTURE))
			//annotateType(chunk, lastElement);
			//else 
			processingContextState.setLastElements(structures);
			processingContextState.setCommaAndOrEosEolAfterLastElements(true);
		}else{
			result = this.establishSubject(chunk, processingContext, processingContextState);
			processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		}
		return result;
	}

}
