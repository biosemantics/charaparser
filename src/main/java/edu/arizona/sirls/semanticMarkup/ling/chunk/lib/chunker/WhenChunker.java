package edu.arizona.sirls.semanticMarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.chunk.AbstractChunker;
import edu.arizona.sirls.semanticMarkup.ling.chunk.Chunk;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkCollector;
import edu.arizona.sirls.semanticMarkup.ling.chunk.ChunkType;
import edu.arizona.sirls.semanticMarkup.ling.parse.AbstractParseTree;
import edu.arizona.sirls.semanticMarkup.ling.parse.IParseTree;
import edu.arizona.sirls.semanticMarkup.ling.parse.IParseTreeFactory;
import edu.arizona.sirls.semanticMarkup.ling.transform.IInflector;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;

/**
 * WhenChunker chunks by handling 'when' terminals
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
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters, 
				glossary, terminologyLearner, inflector, organStateKnowledgeBase);
	}
	
	/**
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
	 * following text order, collect text for new element "when" until a , or . is reached
	 */
	private List<AbstractParseTree> collectTerminals(IParseTree startTerminal, ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = new ArrayList<AbstractParseTree>();
		boolean collect = false;
		for(AbstractParseTree terminal : chunkCollector.getTerminals()) {
			String text = terminal.getTerminalsText();
			//log(LogLevel.DEBUG, text + " " +  chunkCollector.getChunkType(terminal));
			if(startTerminal.equals(terminal)) 
				collect = true;
			if(collect)
				terminals.add(terminal);
				//result.append(text).append(" ");
			//if((result.length() > 0 && text.matches("\\w+\\[.*")) || 
			//		text.matches("[\\.:;,]")) {
			
			if(collect && ((terminals.size() > 0 && !chunkCollector.getChunkType(terminal).equals(ChunkType.UNASSIGNED)) || 
					text.matches("[\\.:;,]"))) {
				break;
			}
		}
		return terminals;
	}
}
