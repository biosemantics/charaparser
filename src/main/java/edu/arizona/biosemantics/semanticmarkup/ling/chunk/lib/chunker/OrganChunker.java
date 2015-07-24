package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.AbstractChunker;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.Chunk;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkType;
import edu.arizona.biosemantics.semanticmarkup.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * OrganChunker chunks by handling 'organ' terminals
 * @author rodenhausen
 */
public class OrganChunker extends AbstractChunker {

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
	public OrganChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,
			 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, glossary, terminologyLearner, 
				inflector,  learnedCharacterKnowledgeBase);
	}
	
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		
		/*IParseTree parseTree = chunkCollector.getParseTree();
		log(LogLevel.DEBUG, "parseTree in organ chunker " + parseTree.getClass().getName() + "@" + Integer.toHexString(parseTree.hashCode()));
		for(IParseTree terminal : parseTree.getTerminals()) {
			log(LogLevel.DEBUG, "terminals1 in organ chunker " + terminal.getClass().getName() + "@" + Integer.toHexString(terminal.hashCode()));
		}*/
		
		Chunk previousOrgan = null;
		for(AbstractParseTree terminal : chunkCollector.getTerminals())  { 
			//collapse here? no decided to create chunk on the fly 
			//terminalParent = terminal.getParent(parseTree);
			Chunk chunk = chunkCollector.getChunk(terminal);
			//if(chunk.isOfChunkType(ChunkType.UNASSIGNED) && organStateKnowledgeBase.isOrgan(terminal.getTerminalsText())) {
			if(chunk.isOfChunkType(ChunkType.UNASSIGNED) && learnedCharacterKnowledgeBase.isEntity(terminal.getTerminalsText())) {
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
			} else if(chunk.isOfChunkType(ChunkType.UNASSIGNED) && learnedCharacterKnowledgeBase.isEntityStructuralContraint(terminal.getTerminalsText())){
				String character = null;
				if(learnedCharacterKnowledgeBase.containsCharacterState(terminal.getTerminalsText())) {
					character = learnedCharacterKnowledgeBase.getCharacterName(terminal.getTerminalsText()).getCategories();
				}
				Chunk stateChunk = new Chunk(ChunkType.STATE, terminal);
				chunkCollector.addChunk(stateChunk); //forking => STATE: [forking]
				List<Chunk> characterStateChildChunks = new ArrayList<Chunk>();
				characterStateChildChunks.add(stateChunk);
				Chunk characterStateChunk = new Chunk(ChunkType.CHARACTER_STATE, characterStateChildChunks);
				
				if(character != null) {//forking => CHARACTER_STATE: characterName->arrangement; [MODIFIER: [mostly], STATE: [forking]]
					characterStateChunk.setProperty("characterName", character);
					chunkCollector.addChunk(characterStateChunk);
				} 
				previousOrgan = characterStateChunk; //a potential constraint chunk
			} else {	
				previousOrgan = null;
			}
		}
	}
}
