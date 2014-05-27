package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;



import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.semanticmarkup.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.know.IGlossary;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * ChromosomeChunker chunks by handling chromosome describing terminals
 * @author rodenhausen
 */
public class ChromosomeChunker extends AbstractChunker {

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 * @param organStateKnowledgeBase
	 */
	@Inject
	public ChromosomeChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,  ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner, 
				inflector, learnedCharacterKnowledgeBase);
	}
	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		for(int i=0; i<terminals.size()-1; i++) {
			AbstractParseTree terminal = terminals.get(i);
			
			if(terminal.getTerminalsText().matches("\\d{0,1}[xn]=.*")) {
				//chromosome count 2n=, FNA specific
				Chunk chromosomeChunk = new Chunk(ChunkType.CHROM);
				LinkedHashSet<Chunk> chromosomeChunkChildren = new LinkedHashSet<Chunk>();
				for(; i<terminals.size()-1; i++){
					terminal = terminals.get(i);
					if(!chunkCollector.getChunk(terminal).containsChunkType(ChunkType.ORGAN)){ //collect all subsequent terminals until an organ is encountered.
						chromosomeChunkChildren.add(terminal);
						chromosomeChunk.setChunks(chromosomeChunkChildren);
						chunkCollector.addChunk(chromosomeChunk);
					}
				}
			}
		}
	}
	//TODO
			/*String l = "";
			String t = this.chunkedtokens.get(pointer++);
			while(t.indexOf("SG")<0){
				l +=t+" ";
				t= this.chunkedtokens.get(pointer++);				
			}
			l = l.replaceFirst("\\d[xn]=", "").trim();
			chunk = new ChunkChrom(l);
			return chunk;
		}
	}*/

}
