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
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * ValuePercentageOrDegreeChunkProcessor processes chunks of ChunkType.WHERE
 * not used anymore. Hong 5/14/2014
 * @author rodenhausen
 */
public class WhereChunkProcessor extends AbstractChunkProcessor {

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
	public WhereChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState contextState = processingContext.getCurrentState();
		ListIterator<Chunk> iterator = processingContext.getChunkListIterator();
		Chunk a = iterator.previous(); //chunk
		Chunk previousChunk = iterator.previous();
		Chunk b = iterator.next(); //back to chunk
		Chunk c = iterator.next(); //the chunk after chunk
		Chunk nextChunk = iterator.next(); //???
		Chunk d = iterator.previous();
		if(previousChunk.isOfChunkType(ChunkType.PP) && processingContext.getChunkCollector().isPartOfChunkType(nextChunk.getTerminals().get(0), ChunkType.CHARACTER_STATE)) {
			LinkedList<BiologicalEntity> subjects = contextState.getSubjects();
			LinkedList<Element> lastElements = contextState.getLastElements();
			
			if(!subjects.isEmpty() && lastElements.getLast().isStructure()) {
				contextState.setClauseModifierContraint(previousChunk.getTerminalsText());
				contextState.setClauseModifierContraintId(((BiologicalEntity)lastElements.getLast()).getId());
				lastElements.clear();
				lastElements.add(subjects.getLast());
			}
		}
		
		return new LinkedList<Element>();
	}

}
