package edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParseTreeFactory;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * WhenChunker chunks by handling 'when' terminals
 * 
 * collect when-phrase (not a complete when-clause) which will be included as character modifiers in the output.
 * 
 * @author rodenhausen
 */
public class WhenChunker extends AbstractChunker {

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
	public WhenChunker(IParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			 ICharacterKnowledgeBase learnedCharacterKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, 
				glossary, terminologyLearner, inflector,  learnedCharacterKnowledgeBase);
	}
	
	/**
	 * Stanford parse tends to include more in a when clause
	 * so use heuristic rules here to collect when-phrase (not a complete when clause) which will be included as character modifiers in the output.
	 * a when-clause ends at a ","
	 * collect from when to nearest ,
	 * collapse the element of when (WRB, IN, or any wired tag when appears with)
	 * e.g., the last example above will be collapsed as: note WRB is changed to WHENCLS 
	    (SBAR
        (WHADVP (WHENCLS when hydrated)
          (ADJP (, ,) (JJ obscured)))
        (FRAG
          (ADJP
            (WHADVP (WHENCLS when desiccated))
            ))))
        (. .))) 
        
        
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		IParseTree parseTree = chunkCollector.getParseTree();
		List<IParseTree> whenTerminals = parseTree.getTerminalsOfText("when");
		for(IParseTree whenTerminal : whenTerminals) {	
			
			if(!chunkCollector.getChunk((AbstractParseTree)whenTerminal).getChunkType().equals(ChunkType.UNASSIGNED)){
				continue; //e..g when is part of a THAT chunk.
			}
			List<AbstractParseTree> terminals = collectTerminals(whenTerminal, chunkCollector);
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			for(AbstractParseTree terminal : terminals) {
				Chunk childChunk = chunkCollector.getChunk(terminal);
				childChunks.add(childChunk);
			}
			Chunk whenChunk = new Chunk(ChunkType.WHEN, childChunks);
			chunkCollector.addChunk(whenChunk);
			
		
			/*IParseTree whenTerminalParent = whenTerminal.getParent(parseTree);
			//collect words/leaf nodes after "when" until a [,.] is found
			//growing the text in WHEN while removing included leaf nodes
			String text = collectText(whenTerminal, parseTree);
			IParseTree collapsedTerminal = parseTreeFactory.create();
			collapsedTerminal.setTerminalsText(text);
			//whenTerminalParent.removeChild(whenTerminal);
			IParseTree collapsedWhen = parseTreeFactory.create();
			collapsedWhen.setPOS(POS.COLLAPSED_WHEN);
			collapsedWhen.addChild(collapsedTerminal);
			whenTerminalParent.addChild(collapsedWhen);*/
		}
	}	
	
	/**
	 * case 1: , when structure character ,? ... 
	 * e.g:  , soon after when the o[pileus margin] [separates from the stipe] the o[annulus] is intermediate
	 * 
	 * case 2: , when condition ,? ... 
	 * e.g.:   , when cut first white , 
	 *         When young leaves purple.
	 * 
	 * case 3: character when structure is character ,? ... 
	 * 
	 * case 4: character when condition ,?...
	 * e.g.:   red when bruised or broken .
	 * 
	 * 
	 *
	 * 1. if 'when' follows a character chunk: cases 3 and 4
	 * 			  collect the chunk right after 'when', and continue to
	 *            collect until a (OrganChunk|[\\.:;,]|but) is reached
	 * 2. else if 'when' not follows a character chunk [follows a ",|but|prep"]: cases 1 and 2
	 *  	 	if 'when' is followed immediately by a structure|"it"|"the latter", : case 1 
	 *  			collect the chunk right after 'when', and continue to
	 *            	collect until a (OrganChunk|[\\.:;,]|but) is reached
	 *  		else if 'when' is not followed immediately by a structure: case 2
	 *              collect only the chunk right after 'when'.
	 * 
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
		boolean followedByOrgan = false;
		boolean case2 = false;

		Chunk previous = null;
		//for(AbstractParseTree terminal : chunkCollector.getTerminals()) {
		for(Chunk chunk: chunkCollector.getChunks()){
			//Chunk chunk = chunkCollector.getChunk(terminal);
			//String text = terminal.getTerminalsText();
			String text = chunk.getTerminalsText();
			if(startTerminal.equals(chunk.getTerminals().get(0))){ 
				collect = true;
				if(previous!=null && previous.isOfChunkType(ChunkType.UNASSIGNED) && previous.getTerminalsText().matches(prepositionWords)){
					terminals.addAll(previous.getTerminals());
				}
				terminals.addAll(chunk.getTerminals()); //collected 'when'
				if(start && previous!=null && characters.contains(previous.getChunkType())){
					followCharacter = true; //cases 3, 4
				}
				previous = chunk;
				continue;
			}
			if(collect && start){
				terminals.addAll(chunk.getTerminals()); //save the first terminal after 'when'
				if(previous.getTerminalsText().compareTo("when")==0 && (organs.contains(chunk.getChunkType())||text.matches("it|the|they"))){
					followedByOrgan = true; //case 1
				}
				start = false;	
				previous = chunk;
				continue;
			}
			if(collect && !followedByOrgan && !followCharacter ){ //case 2
				case2 = true;
			}
			if(collect){// all cases 1, 2, 3, and 4, continue collect until the stop condition is meet. case 2 will be checked latter
				if(((terminals.size() > 0 && organs.contains(chunk.getChunkType()) || text.matches("[\\.:;,]|but")))) {
					break;
				}else{
					terminals.addAll(chunk.getTerminals());
				}
			}
			
			previous = chunk;
		}
		
		if(case2){
			//differentiate these two kinds: "when cut first white"  and  "when young"
			 //if terminals is of this kind: "when, cut, first, white",  reduce terminals to the first chunk after 'when'
			//last chunk is a character and there are some chunks between 'when' and the last chunk.
			if(characters.contains(chunkCollector.getChunk(terminals.get(terminals.size()-1)).getChunkType()) && terminals.size()>2){
				for(int i =2; i<terminals.size(); i++){
					terminals.remove(i);
				}
			}			
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
