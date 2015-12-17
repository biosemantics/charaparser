package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;





import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.IPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;

/**
 * ChromChunkProcessor processes chunks of ChunkType.CHROM
 * 
 *  * x= means basal number of chromosomes
   n= number of chromosomes in haploid state
   2n= two sets of chromosomes
   n=2n= number of chromosomes in apogamous condition
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
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<BiologicalEntity> processChunk(Chunk chunk, ProcessingContext processingContext) {
		List<BiologicalEntity> result = new LinkedList<BiologicalEntity>();
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		int split = chunk.getTerminalsText().lastIndexOf("=");
		String constraint = "";
		String value = "";
		if(split > 0){
			constraint = chunk.getTerminalsText().substring(0, split);
			value = chunk.getTerminalsText().substring(split+1);
			value = value.replaceFirst("[.;, ]+\\s*$", "").replaceAll("\\bor\\b", ",").replaceAll("(\\s*,\\s*)+", ",");
		}		
		if(!value.isEmpty() && !constraint.isEmpty()){
			//String content = chunk.getTerminalsText().replaceAll("[^\\d()\\[\\],+ -]", "").trim();
			//Element structure = new Element("chromosome");
			BiologicalEntity structure = new BiologicalEntity();
			structure.setName("chromosome");
			structure.setNameOriginal("");
			structure.setType("structure");
			structure.setConstraint(constraint);
			structure.setId("o" + String.valueOf(processingContext.fetchAndIncrementStructureId(structure)));
			result.add(structure);
			
			List<Chunk> modifiers = new LinkedList<Chunk>();
			this.annotateNumericals(value, "count", modifiers, result, false, processingContextState);
			addClauseModifierConstraint(structure, processingContextState);
			processingContextState.setCommaAndOrEosEolAfterLastElements(false);
		}
		return result;
	}

}
