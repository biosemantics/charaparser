package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.lib;

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
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.AbstractChunkProcessor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContext;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.ProcessingContextState;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Relation;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Structure;
import edu.arizona.biosemantics.semanticmarkup.model.Element;

/**
 * DummyChunkProcessor does not do any processing. It may be used as a placeholder IChunkProcessor in the configuration
 * @author rodenhausen
 */
public class DummyChunkProcessor extends AbstractChunkProcessor {

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
	public DummyChunkProcessor(IInflector inflector, IGlossary glossary, ITerminologyLearner terminologyLearner, 
			ICharacterKnowledgeBase characterKnowledgeBase, @Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase,
			@Named("BaseCountWords")Set<String> baseCountWords, @Named("LocationPrepositionWords")Set<String> locationPrepositions, 
			@Named("Clusters")Set<String> clusters, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			@Named("NumberPattern")String numberPattern, @Named("TimesWords")String times, @Named("CompoundPrepWords") String compoundPreps, @Named("StopWords")Set<String> stopWords) {
		super(inflector, glossary, terminologyLearner, characterKnowledgeBase, posKnowledgeBase, baseCountWords, locationPrepositions, clusters, units, equalCharacters, 
				numberPattern, times, compoundPreps, stopWords);
	}

	@Override
	protected List<Element> processChunk(Chunk chunk,
			ProcessingContext processingContext) {
		ProcessingContextState processingContextState = processingContext.getCurrentState();
		LinkedList<Element> result = new LinkedList<Element>();
		if(chunk.getTerminalsText().compareTo("latter") ==0){
			int index =	processingContext.getChunkCollector().getChunks().indexOf(chunk);
			if(index>0){
				Chunk prevChunk = processingContext.getChunkCollector().getChunks().get(index-1);
				if(prevChunk.getTerminalsText().compareTo("the")==0){ //the latter
					Element lastStructure = processingContext.getLastResult(Structure.class); 
					Set<Relation> relations = processingContext.getRelationsTo(Integer.parseInt(((Structure)(lastStructure)).getId().replaceAll("[^\\d]", "")));
					int minRId = -1;
					for(Relation r: relations){
						if(r.getName().compareTo("part_of")==0){
							//find the relation with the greatest id
							int rId = Integer.parseInt(r.getId().replaceAll("[^\\d]", ""));
							if(minRId == -1){
								minRId = rId;
								lastStructure = r.getFromStructure();
							}
							else if(minRId < rId){
								lastStructure = r.getFromStructure();
								minRId = rId;
							}
						}
					}
					result.add(lastStructure);
					processingContextState.setLastElements(result);
					processingContextState.setCommaAndOrEosEolAfterLastElements(false);
					return result;
				}
			}	
		}
		return result;
	}

}
