package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.ParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;

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
	 */
	public ChromosomeChunker(ParseTreeFactory parseTreeFactory,
			String prepositionWords, Set<String> stopWords, String units,
			HashMap<String, String> equalCharacters, IGlossary glossary,
			ITerminologyLearner terminologyLearner, IInflector inflector) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,
				glossary, terminologyLearner, inflector);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		for(AbstractParseTree terminal : chunkCollector.getTerminals())  
			if(terminal.getTerminalsText().contains("=")) //chromosome count 2n=, FNA specific
				chunkCollector.addChunk(new Chunk(ChunkType.CHROM, terminal));
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
