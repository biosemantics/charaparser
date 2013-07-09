package semanticMarkup.markupElement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.ling.extract.AbstractChunkProcessor;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContext;
import semanticMarkup.markupElement.description.ling.extract.ProcessingContextState;
import semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.model.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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
	 * @param times
	 */
	@Inject
	public WhereChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk, ProcessingContext processingContext) {
		ProcessingContextState contextState = processingContext.getCurrentState();
		ListIterator<Chunk> iterator = processingContext.getChunkListIterator();
		Chunk a = iterator.previous();
		Chunk previousChunk = iterator.previous();
		Chunk b = iterator.next();
		Chunk c = iterator.next();
		Chunk nextChunk = iterator.next();
		Chunk d = iterator.previous();
		if(previousChunk.isOfChunkType(ChunkType.PP) && processingContext.getChunkCollector().isPartOfChunkType(nextChunk.getTerminals().get(0), ChunkType.CHARACTER_STATE)) {
			LinkedList<Structure> subjects = contextState.getSubjects();
			LinkedList<Element> lastElements = contextState.getLastElements();
			
			if(!subjects.isEmpty() && lastElements.getLast().isStructure()) {
				contextState.setClauseModifierContraint(previousChunk.getTerminalsText());
				contextState.setClauseModifierContraintId(((Structure)lastElements.getLast()).getId());
				lastElements.clear();
				lastElements.add(subjects.getLast());
			}
		}
		
		return new LinkedList<Element>();
	}

}
