package semanticMarkup.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import semanticMarkup.core.description.DescriptionTreatmentElement;
import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;

/**
 * ValuePercentageOrDegreeChunkProcessor processes chunks of ChunkType.WHERE
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
	 * @param attachToLast
	 * @param times
	 */
	@Inject
	public WhereChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("AttachToLast")boolean attachToLast, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, attachToLast, times);
	}

	@Override
	protected List<DescriptionTreatmentElement> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState contextState = processingContext.getCurrentState();
		ListIterator<Chunk> iterator = processingContext.getChunkListIterator();
		Chunk a = iterator.previous();
		Chunk previousChunk = iterator.previous();
		Chunk b = iterator.next();
		Chunk c = iterator.next();
		Chunk nextChunk = iterator.next();
		Chunk d = iterator.previous();
		if(previousChunk.isOfChunkType(ChunkType.PP) && processingContext.getChunkCollector().isPartOfChunkType(nextChunk.getTerminals().get(0), ChunkType.CHARACTER_STATE)) {
			LinkedList<DescriptionTreatmentElement> subjects = contextState.getSubjects();
			if(!subjects.isEmpty()) {
				LinkedList<DescriptionTreatmentElement> lastElements = contextState.getLastElements();
				
				contextState.setClauseModifierContraint(previousChunk.getTerminalsText());
				contextState.setClauseModifierContraintId(lastElements.getLast().getAttribute("id"));
				
				lastElements.clear();
				lastElements.add(subjects.getLast());
			}
		}
		
		//System.out.println(iterator.next());
		
		return new LinkedList<DescriptionTreatmentElement>();
	}

}
