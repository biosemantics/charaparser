package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * WhenChunker chunks by handling 'where' terminals
 * @author rodenhausen
 */
public class WhereChunker extends AbstractChunker {

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
	public WhereChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
		 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, 
				glossary, terminologyLearner, inflector, learnedCharacterKnowledgeBase);
	}

	/**
	 *     (SBAR
        (WHADVP (WRB where))
        (S
          (NP (NN spine) (NNS bases))
          (ADJP (JJ tend)
            (S
              (VP (TO to)
                (VP (VB be)
                  (ADJP (JJ elongate))))))))       
                  

            WHEREChunker and WHENChunker are quite similar. When making changes in either class, consider including the changes in the other. 

	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		List<IParseTree> whenTerminals = parseTree.getTerminalsOfText("where");
		for(IParseTree whenTerminal : whenTerminals) {	
			List<AbstractParseTree> ts = whenTerminal.getChildren();
			if(!chunkCollector.getChunk((AbstractParseTree)whenTerminal).getChunkType().equals(ChunkType.UNASSIGNED) || //e..g when is part of a THAT chunk.
					(ts.size()>0 && ts.get(ts.size()-1).getTerminalsText().compareTo("where")!=0)	){ //last terminal is not 'when'.
					continue; 
				}
			List<AbstractParseTree> terminals = collectTerminals(whenTerminal, chunkCollector);
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			for(AbstractParseTree terminal : terminals) {
				Chunk childChunk = chunkCollector.getChunk(terminal);
				childChunks.add(childChunk);
			}
			Chunk whenChunk = new Chunk(ChunkType.WHERE, childChunks);
			chunkCollector.addChunk(whenChunk);
		}
	}
	
	/**
	 * case 1: ... organ,? where it characters: 
	 *      e.g. ... the base, where it remains white
	 * case 2: ... organ,? where organ characters
	 *      e.g. ... the spot where the blackish layer narrows
	 * case 3: ... character where ....
	 *      e.g. ... grey where injured 
	 *      
	 *      
	 * if 'where' follows an organ
	 *       collect 'where' alone (which can either be a subject [case 1] or a character constraint [case 2])     
	 * else if 'where' follows a character
	 *       //collect the entire where-clause (to be used as a modifier for the character [case 3]) 
	 *       collect the chunk right after 'where', and continue to
	 *       collect until a (OrganChunk|[\\.:;,]|but) is reached
	 *  
	 * @param startTerminal
	 * @param chunkCollector
	 * @return
	 */
	private List<AbstractParseTree> collectTerminals(IParseTree startTerminal, ChunkCollector chunkCollector) {
		Set<ChunkType> characters = new HashSet<ChunkType> ();
		characters.add(ChunkType.AREA);
		characters.add(ChunkType.AVERAGE);
		characters.add(ChunkType.BASED_COUNT);
		characters.add(ChunkType.CHARACTER_STATE);
		characters.add(ChunkType.CHROM);
		characters.add(ChunkType.COMPARATIVE_VALUE);
		characters.add(ChunkType.COUNT);
		characters.add(ChunkType.NUMERICALS);
		characters.add(ChunkType.RATIO);
		characters.add(ChunkType.STATE);
		characters.add(ChunkType.THAN);
		characters.add(ChunkType.THAN_CHARACTER_PHRASE);
		characters.add(ChunkType.THAN_PHRASE);
		characters.add(ChunkType.TO);
		characters.add(ChunkType.TO_PHRASE);
		characters.add(ChunkType.TYPE);
		characters.add(ChunkType.VALUE);
		characters.add(ChunkType.VALUE_DEGREE);
		characters.add(ChunkType.VALUE_PERCENTAGE);
		characters.add(ChunkType.VP);
		Set<ChunkType> organs = new HashSet<ChunkType> ();
		organs.add(ChunkType.NON_SUBJECT_ORGAN);
		organs.add(ChunkType.NP_LIST);
		organs.add(ChunkType.ORGAN);
		organs.add(ChunkType.MAIN_SUBJECT_ORGAN);

		
		
		List<AbstractParseTree> terminals = new ArrayList<AbstractParseTree>();
		boolean collect = false;
		boolean start = true;
		boolean followCharacter = false;
		boolean followOrgan = false;
		Chunk organ = null;

		Chunk previous = null;
		//for(AbstractParseTree terminal : chunkCollector.getTerminals()) {
		//	Chunk chunk = chunkCollector.getChunk(terminal);
		for(Chunk chunk: chunkCollector.getChunks()){
			String text = chunk.getTerminalsText();
			if(startTerminal.equals(chunk.getTerminals().get(0))){ 
				collect = true;
				if(previous.isOfChunkType(ChunkType.UNASSIGNED) && previous.getTerminalsText().matches(prepositionWords)){
					terminals.addAll(previous.getTerminals());
				}
				terminals.addAll(chunk.getTerminals()); //collected 'where'
				if(start && previous!=null && characters.contains(previous.getChunkType())){
					followCharacter = true; //case 3
				}else if(start && previous!=null && organs.contains(previous.getChunkType())){
					followOrgan = true;//cases 1, 2
					organ = previous;
				}
				if(!text.equals(",")) previous = chunk;
				continue;
			}
			if(collect && start){
				terminals.addAll(chunk.getTerminals()); //save the first terminal after 'where'
				start = false;	
				if(!text.equals(",")) previous = chunk;
				continue;
			}
			if(collect && followCharacter){//case 3
				if(((terminals.size() > 0 && organs.contains(chunk.getChunkType()) || text.matches("[\\.:;,]|but")))) {
					break;
				}else{
					terminals.addAll(chunk.getTerminals());
				}
			}else if(collect && followOrgan){//cases 1 and 2
				/*for(int i = 1; i < terminals.size(); i++) //keep only 'where'
					terminals.remove(i);*/
				boolean remove = false;
				for(int i =0; i<terminals.size(); i++){
					if(terminals.get(i).getTerminalsText().compareTo("where")==0) {
						remove = true;
						continue;
					}
					
					if(remove) terminals.remove(i);
				}
				break;
			}
			if(!text.equals(",")) previous = chunk;
		}
		

		return terminals;
		
		
		/* following text order, collect text for new element "when" until a [\\.:;,] is reached
		 * List<AbstractParseTree> terminals = new ArrayList<AbstractParseTree>();
		boolean collect = false;
		for(AbstractParseTree terminal : chunkCollector.getTerminals()) {
			String text = terminal.getTerminalsText();
			if(startTerminal.equals(terminal)) 
				collect = true;
			if(collect)
				terminals.add(terminal);			
			if(collect && ((terminals.size() > 0 && !chunkCollector.getChunkType(terminal).equals(ChunkType.UNASSIGNED)) || 
					text.matches("[\\.:;,]"))) {
				break;
			}
		}
		return terminals;*/
	}
}
