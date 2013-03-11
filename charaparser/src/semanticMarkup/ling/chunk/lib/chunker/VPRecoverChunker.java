package semanticMarkup.ling.chunk.lib.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IPOSKnowledgeBase;
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

public class VPRecoverChunker extends AbstractChunker {

	private IPOSKnowledgeBase posKnowledgeBase;

	@Inject
	public VPRecoverChunker(ParseTreeFactory parseTreeFactory, @Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector, 
			@Named("LearnedPOSKnowledgeBase") IPOSKnowledgeBase posKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,	glossary, terminologyLearner, inflector);
		this.posKnowledgeBase = posKnowledgeBase;
	}

	/**
	 * scan through a chunkedtokens to find Verbs not parsed as such by the parser
	 * find verbs by
	 * 1. look into this.verbs
	 * 2. find pattern o ting/ted by o, then t must be a verb and save this verb in verbs
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		/*List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		String text = "";
		for(int i = 0; i < terminals.size(); i++){
			text += terminals.get(i) + " ";
		}
		if(text.contains("usually whitish to stramineous"))
			log(LogLevel.DEBUG, "here");*/
		
		List<AbstractParseTree> terminals  = chunkCollector.getTerminals();
		for(int i = 0; i < terminals.size(); i++){
			AbstractParseTree terminal = terminals.get(i);
			if(terminal.getTerminalsText().contains("-"))
				continue;
			//!chunkCollector.isPartOfAChunk(terminal)
			if(!chunkCollector.isPartOfChunkType(terminal, ChunkType.VP) && 
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN) && 
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.PP) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) &&
					posKnowledgeBase.isVerb(terminal.getTerminalsText())) { 
				recoverVPChunk(terminals, i, chunkCollector);
			//!chunkCollector.isPartOfAChunk(terminal)
			} else if(!chunkCollector.isPartOfChunkType(terminal, ChunkType.VP) && 
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHARACTER_STATE) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.PP) &&
					!chunkCollector.isPartOfChunkType(terminal, ChunkType.CONSTRAINT) &&
					terminal.getTerminalsText().endsWith("ing")) {
				if(connectsTwoOrgans(terminals, i, chunkCollector)){
					 posKnowledgeBase.addVerb(terminal.getTerminalsText().replaceAll("\\W", ""));
					 recoverVPChunk(terminals, i, chunkCollector);
				 }
			}
		}
	}
	
	/**
	 * 
	 * @param i :index of the verb
	 * @return
	 */
	private boolean connectsTwoOrgans(List<AbstractParseTree> terminals, int i, ChunkCollector chunkCollector) {
		boolean organ1 = false;
		boolean organ2 = false;
		if(i >= 1 && terminals.size() > i+1){
			AbstractParseTree terminal = terminals.get(i-1);
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.OBJECT) || 
					chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
				organ1 = true;
			}
			do {
				i++;
				terminal = terminals.get(i);
			} while(terminal.getTerminalsText().length() == 0);
			
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.OBJECT) || 
					chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
				organ2 = true;
			}
		}
		return organ1 && organ2;
	}
	
	
	/**
	 * 
	 * @param i: the index of a possible verb
	 */
	private void recoverVPChunk(List<AbstractParseTree> terminals, int i, ChunkCollector chunkCollector) {
		boolean foundOrgan = false;
		List<AbstractParseTree> collectedTerminals = new ArrayList<AbstractParseTree>();
		for(int j=i+1; j < terminals.size(); j++){
			//scan for the end of the chunk TODO: may refactor with normalizeOtherINs on this search
			AbstractParseTree terminal = terminals.get(j);
			String terminalsText = terminal.getTerminalsText();
			if(j==i+1 && terminalsText.equals(","))
				//verb not have object
				return;
			if(terminalsText.matches("(;|\\.)")) 
				break;
			if(foundOrgan && (chunkCollector.isPartOfChunkType(terminal, ChunkType.STATE)
					|| terminalsText.contains("~list~") || terminalsText.matches("(\\w+|,|;|\\.)")
					|| chunkCollector.isPartOfANonTerminalChunk(terminal))) {
				break;
			}
			if(chunkCollector.isPartOfChunkType(terminal, ChunkType.VP)) {
				Chunk chunk = chunkCollector.getChunk(terminal);
				collectedTerminals.addAll(chunk.getTerminals());
				foundOrgan = true;
			} else if(chunkCollector.isPartOfChunkType(terminal, ChunkType.ORGAN)) {
				collectedTerminals.add(terminal);
				foundOrgan = true;
			} else if(chunkCollector.isPartOfChunkType(terminal, ChunkType.OBJECT) || 
					chunkCollector.isPartOfChunkType(terminal, ChunkType.NP_LIST)) {//found noun)
				collectedTerminals.add(terminal);
				foundOrgan = true;
				j++;
				break;
			} else
				collectedTerminals.add(terminal);
		}
		if(!foundOrgan) 
			return;
		//format the chunk
		if(chunkCollector.isPartOfChunkType(collectedTerminals.get(collectedTerminals.size()-1), ChunkType.ORGAN)) {
			LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>(); 
			Chunk functionChunk = chunkCollector.getChunk(terminals.get(i));
			function.add(chunkCollector.getChunk(terminals.get(i)));
			if(functionChunk.isOfChunkType(ChunkType.CHARACTER_STATE))
				functionChunk = new Chunk(ChunkType.SPECIFIER, function);
			else
				functionChunk = new Chunk(ChunkType.VERB, function);
			LinkedHashSet<Chunk> object = new LinkedHashSet<Chunk>();
			for(AbstractParseTree terminal : collectedTerminals)
				object.add(chunkCollector.getChunk(terminal));
			Chunk objectChunk = new Chunk(ChunkType.OBJECT, object);
			
			LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
			childChunks.add(functionChunk);
			childChunks.add(objectChunk);
			Chunk chunk;
			if(functionChunk.isOfChunkType(ChunkType.SPECIFIER))
				chunk = new Chunk(ChunkType.SPECIFIC_PP, childChunks);
			else
				chunk = new Chunk(ChunkType.VP, childChunks);
			chunkCollector.addChunk(chunk);

		} else if(chunkCollector.containsPartOfChunkType(collectedTerminals, ChunkType.OBJECT)) {
			if(!chunkCollector.isPartOfChunkType(collectedTerminals.get(0), ChunkType.PP) && chunkCollector.containsPartOfChunkType(collectedTerminals, ChunkType.VP)) {
					Chunk vpChunk = chunkCollector.getChunkOfChunkType(collectedTerminals, ChunkType.VP);
					LinkedHashSet<Chunk> childChunks = (LinkedHashSet<Chunk>)vpChunk.getChunks().clone();
					for(AbstractParseTree terminal : collectedTerminals) {
						List<Chunk> childChunksList = new ArrayList<Chunk>(childChunks);
						childChunks.clear();
						Chunk terminalChunk = chunkCollector.getChunk(terminal);
						childChunks.add(terminalChunk);
						childChunks.addAll(childChunksList);
					}
					//System.out.println(childChunks.toString());
					vpChunk = new Chunk(ChunkType.VP, childChunks);
					chunkCollector.addChunk(vpChunk);
			} else if(chunkCollector.isPartOfChunkType(collectedTerminals.get(0), ChunkType.PP)) {
				//t[c[{extending}] r[p[to] o[(midvalve)]]]
				//chunk = chunk.replaceFirst("^r[p[", "b[v["+this.chunkedtokens.get(i)+ " "); 
				//need to make the v is taken as a relation in processChunkVP
				Chunk specifierChunk = new Chunk(ChunkType.SPECIFIER, chunkCollector.getChunk(terminals.get(i)));
				Chunk ppChunk = chunkCollector.getChunk(collectedTerminals.get(0));
				LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
				childChunks.add(specifierChunk);
				childChunks.add(ppChunk);
				Chunk chunk = new Chunk(ChunkType.SPECIFIC_PP, childChunks);
				chunkCollector.addChunk(chunk);
			} else if(chunkCollector.isPartOfChunkType(collectedTerminals.get(0), ChunkType.NP_LIST)) {
				//Chunk npListChunk = chunkCollector.getChunk(collectedTerminals.get(0));
				//npListChunk.setChunkType(ChunkType.ChunkOrgan);
				LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>();
				function.add(chunkCollector.getChunk(terminals.get(i)));
				Chunk functionChunk = new Chunk(ChunkType.VERB, function);
				
				LinkedHashSet<Chunk> object = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : collectedTerminals)
					object.add(chunkCollector.getChunk(terminal));
				Chunk objectChunk = new Chunk(ChunkType.OBJECT, object);
				
				LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
				childChunks.add(functionChunk);
				childChunks.add(objectChunk);
				Chunk chunk = new Chunk(ChunkType.VP, childChunks);
				chunkCollector.addChunk(chunk);
				
			} else if(chunkCollector.isPartOfChunkType(collectedTerminals.get(0), ChunkType.NON_SUBJECT_ORGAN)) {
				LinkedHashSet<Chunk> function = new LinkedHashSet<Chunk>();
				function.add(chunkCollector.getChunk(terminals.get(i)));
				Chunk functionChunk = new Chunk(ChunkType.VERB, function);
				
				LinkedHashSet<Chunk> object = new LinkedHashSet<Chunk>();
				for(AbstractParseTree terminal : collectedTerminals)
					object.add(chunkCollector.getChunk(terminal));
				Chunk objectChunk = new Chunk(ChunkType.OBJECT, object);
				
				LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
				childChunks.add(functionChunk);
				childChunks.add(objectChunk);
				Chunk chunk = new Chunk(ChunkType.VP, childChunks);
				chunkCollector.addChunk(chunk);
			}
		}	
	}
}
