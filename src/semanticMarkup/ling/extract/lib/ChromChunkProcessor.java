package semanticMarkup.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.ICharacterKnowledgeBase;
import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.extract.AbstractChunkProcessor;
import semanticMarkup.ling.extract.ProcessingContext;
import semanticMarkup.ling.extract.ProcessingContextState;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.transform.IInflector;
import semanticMarkup.markupElement.description.model.Structure;
import semanticMarkup.model.Element;
import semanticMarkup.model.description.DescriptionTreatmentElement;
import semanticMarkup.model.description.DescriptionTreatmentElementType;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * ChromChunkProcessor processes chunks of ChunkType.CHROM
 * @author rodenhausen
 */
public class ChromChunkProcessor extends AbstractChunkProcessor {

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
	public ChromChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times);
	}

	@Override
	protected List<Structure> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<Structure> result = new LinkedList<Structure>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		String[] parts = chunk.getTerminalsText().split("=");
		if(parts.length==2) {
			String value = parts[1];
			//String content = chunk.getTerminalsText().replaceAll("[^\\d()\\[\\],+ -]", "").trim();
			//Element structure = new Element("chromosome");
			Structure structure = new Structure();
			structure.setName("chromosome");
			structure.setId("o" + String.valueOf(processingContextState.fetchAndIncrementStructureId(structure)));
			result.add(structure);
			
			List<Chunk> modifiers = new LinkedList<Chunk>();
			this.annotateNumericals(value, "count", modifiers, result, false, processingContextState);
			addClauseModifierConstraint(structure, processingContextState);
			processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		}
		return result;
	}

}
