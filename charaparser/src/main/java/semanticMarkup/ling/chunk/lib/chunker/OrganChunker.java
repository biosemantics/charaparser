package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.ParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OrganChunker extends AbstractChunker {

	private IOrganStateKnowledgeBase organStateKnowledgeBase;

	@Inject
	public OrganChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
		this.organStateKnowledgeBase = organStateKnowledgeBase;
	}
	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		
		/*IParseTree parseTree = chunkCollector.getParseTree();
		System.out.println("parseTree in organ chunker " + parseTree.getClass().getName() + "@" + Integer.toHexString(parseTree.hashCode()));
		for(IParseTree terminal : parseTree.getTerminals()) {
			System.out.println("terminals1 in organ chunker " + terminal.getClass().getName() + "@" + Integer.toHexString(terminal.hashCode()));
		}*/
		
		Chunk previousOrgan = null;
		for(AbstractParseTree terminal : chunkCollector.getTerminals())  { 
			//collapse here? no decided to create chunk on the fly 
			//terminalParent = terminal.getParent(parseTree);
			Chunk chunk = chunkCollector.getChunk(terminal);
			if(chunk.isOfChunkType(ChunkType.UNASSIGNED) && organStateKnowledgeBase.isOrgan(terminal.getTerminalsText())) {
				Chunk organ = new Chunk(ChunkType.ORGAN, terminal);
				if(previousOrgan!=null) {
					previousOrgan.setChunkType(ChunkType.CONSTRAINT);
					//Chunk constraintChunk = new Chunk(ChunkType.CONSTRAINT, previousOrgan);
					chunkCollector.addChunk(previousOrgan);
					/*LinkedHashSet<Chunk> chunkChildren = previousOrgan.getChunks();
					chunkChildren.add(terminal);
					organ = new Chunk(ChunkType.ORGAN, chunkChildren);*/
				}
				chunkCollector.addChunk(organ);
				previousOrgan = organ;
			} else {
				previousOrgan = null;
			}
		}
	}
}
