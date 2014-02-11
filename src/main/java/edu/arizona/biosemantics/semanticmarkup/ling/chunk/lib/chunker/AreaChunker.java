package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.Set;



import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * AreaChunker chunks by handling an area describing terminals
 * @author rodenhausen
 */
public class AreaChunker extends AbstractChunker {
	
	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 */
	@Inject

	public AreaChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			ICharacterKnowledgeBase characterKnowledgeBase, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, 
				terminologyLearner, inflector, organStateKnowledgeBase);
	}

	@Override
	public void chunk(ChunkCollector chunkCollector) {
		for(AbstractParseTree terminal : chunkCollector.getTerminals()) {
			//token: 4-9cm×usually15-25mm		
			String terminalsText = terminal.getTerminalsText();
			if(terminalsText.contains("×") && !terminalsText.contains(" ")) {
				String dim[] = terminalsText.split("×");
				boolean hasValidDimensions = true;
				int dimensions = 0;
				for(int i = 0; i < dim.length; i++){
					hasValidDimensions = dim[i].matches(".*?\\d.*");
					if(!hasValidDimensions)
						return;
					dimensions++;
				}
				if(dimensions >= 2) {//TODO Hong
					terminalsText = terminalsText.replaceAll("�[^0-9]*", " � ").replaceAll("(?<=[^a-z])(?=[a-z])", " ").replaceAll("(?<=[a-z])(?=[^a-z])", " ").replaceAll("\\s+", " ").trim();
					terminal.setTerminalsText(terminalsText);
					chunkCollector.addChunk(new Chunk(ChunkType.AREA, terminal));
				}
			}
		}
	}
}
