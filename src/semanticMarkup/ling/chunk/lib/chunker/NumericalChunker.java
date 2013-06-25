package semanticMarkup.ling.chunk.lib.chunker;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.know.IOrganStateKnowledgeBase;
import semanticMarkup.ling.chunk.AbstractChunker;
import semanticMarkup.ling.chunk.Chunk;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkType;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParseTreeFactory;
import semanticMarkup.ling.transform.IInflector;

import com.google.inject.Inject;
import com.google.inject.name.Named;

//not used class because functionality already implemented in CleanupChunker
//however, CleanupChunker is a big class and could be separated according to handling numerics or other things
/**
 * NumericalChunker chunks by handling numerical terminals 
 * @author rodenhausen
 */
public class NumericalChunker extends AbstractChunker {

	private String percentageWords;
	private String degreeWords;
	private String countWords;
	private String timesWords;
	private String numberPattern;

	/**
	 * @param parseTreeFactory
	 * @param prepositionWords
	 * @param stopWords
	 * @param units
	 * @param equalCharacters
	 * @param glossary
	 * @param terminologyLearner
	 * @param inflector
	 * @param percentageWords
	 * @param degreeWords
	 * @param countWords
	 * @param timesWords
	 * @param numberPattern
	 */
	@Inject
	public NumericalChunker(IParseTreeFactory parseTreeFactory,
			@Named("PrepositionWords")String prepositionWords,
			@Named("StopWords")Set<String> stopWords, @Named("Units")String units, @Named("EqualCharacters")HashMap<String, String> equalCharacters, 
			IGlossary glossary, ITerminologyLearner terminologyLearner, IInflector inflector,
			@Named("PercentageWords") String percentageWords,
			@Named("DegreeWords") String degreeWords, 
			@Named("CountWords") String countWords, 
			@Named("TimesWords") String timesWords, 
			@Named("NumberPattern") String numberPattern, 
			IOrganStateKnowledgeBase organStateKnowledgeBase) {
		super(parseTreeFactory, prepositionWords, stopWords, units, equalCharacters,
				glossary, terminologyLearner, inflector, organStateKnowledgeBase);
		this.percentageWords = percentageWords;
		this.degreeWords = degreeWords;
		this.countWords = countWords;
		this.timesWords = timesWords;
		this.numberPattern = numberPattern;
	}


	/**
	 * TODO: deal with LRB-/-LRB
	 * e.g. 3 cm, what about "3 cm to 10 dm"?
	 * also 3 times (... longer than, as wide as ...)
	 */
	@Override
	public void chunk(ChunkCollector chunkCollector) {
		List<AbstractParseTree> terminals = chunkCollector.getTerminals();
		for(int i=0; i < terminals.size(); i++) {
			AbstractParseTree terminal = terminals.get(i);
			if(!chunkCollector.isPartOfChunkType(terminal, ChunkType.CHROM)) {
				String terminalsText = terminal.getTerminalsText();
				
				if(terminalsText.matches(".*?" + numberPattern + "$") || terminalsText.matches("\\(\\d+\\)") || terminalsText.matches("\\d+\\+?") || terminalsText.matches("^to~\\d.*")) { 
					terminalsText = originalNumForm(terminalsText).replaceAll("\\?", "");		
					if(terminalsText.matches("^to~\\d.*")){
						terminal.setTerminalsText(terminalsText.replaceAll("~",  " "));
						Chunk value = new Chunk(ChunkType.VALUE, chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(value);
						continue;
					}
					if(terminalsText.matches(".*?(" + percentageWords + ")")){
						Chunk valuePercentage = new Chunk(ChunkType.VALUE_PERCENTAGE,  chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(valuePercentage);
						continue;
					}
					if(terminalsText.matches(".*?(" + degreeWords + ")")){
						Chunk valueDegree = new Chunk(ChunkType.VALUE_DEGREE, chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(valueDegree);
						continue;
					}
					if(terminalsText.matches(".*?[()\\[\\]\\-\\–\\d\\.×\\+°²½/¼\\*/%]*?[½/¼\\d][()\\[\\]\\-\\–\\d\\.×\\+°²½/¼\\*/%]*(-\\s*(" + countWords + ")\\b|$)")) {
						
						//ends with a number
						if(i==terminals.size()-1) {
							Chunk count = new Chunk(ChunkType.COUNT,  chunkCollector.getChunk(terminal));
							chunkCollector.addChunk(count);
							continue;
						}
						
						i++;
						AbstractParseTree lookForwardTerminal = terminals.get(i);
						String lookForwardText = lookForwardTerminal.getTerminalsText();
						
						if(lookForwardText.matches("^[{<(]*(" + units + ")\\b.*?")){
							String combinedText = terminalsText + " " + lookForwardText;
							//adjustPointer4Dot(pointer, terminals);
							//in bhl, 10 cm . long, should skip the ". long" after the unit
							//numerics = numerics.replaceAll("[{(<>)}]", "").trim();
							LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
							childChunks.add(chunkCollector.getChunk(terminal));
							childChunks.add(chunkCollector.getChunk(lookForwardTerminal));
							if(combinedText.contains("×")) {
								Chunk area = new Chunk(ChunkType.AREA, childChunks);
								chunkCollector.addChunk(area);
							} else {
								Chunk value = new Chunk(ChunkType.VALUE,  childChunks);
								chunkCollector.addChunk(value);
							}
							continue;
						}
						if(lookForwardText.matches("^[{<(]*(" + timesWords + ")\\b.*?")){
							i++;
							AbstractParseTree lookDoubleForwardTerminal = terminals.get(i);
							Chunk lookDoubleForwardChunk = chunkCollector.getChunk(lookDoubleForwardTerminal);
							
							LinkedHashSet<Chunk> childChunks = new LinkedHashSet<Chunk>();
							childChunks.add(chunkCollector.getChunk(terminal));
							childChunks.add(chunkCollector.getChunk(lookForwardTerminal));
							childChunks.add(lookDoubleForwardChunk);
							
							if(lookDoubleForwardChunk.isOfChunkType(ChunkType.THAN_CHARACTER_PHRASE)) {
								Chunk value = new Chunk(ChunkType.VALUE, childChunks);
								chunkCollector.addChunk(value);
							} else {
								i++;
								AbstractParseTree lookAheadTerminal = terminals.get(i);
								while(chunkCollector.isOfChunkType(lookAheadTerminal, ChunkType.UNASSIGNED)) {
									childChunks.add(chunkCollector.getChunk(lookAheadTerminal));
									i++;
									lookAheadTerminal = terminals.get(i);
								}
								Chunk comparativeValue = new Chunk(ChunkType.COMPARATIVE_VALUE, childChunks);
								chunkCollector.addChunk(comparativeValue);
							}
							continue;
						} 
						
						Chunk count = new Chunk(ChunkType.COUNT,  chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(count);
					}
					
					if(terminalsText.matches("l\\s*\\W\\s*w")){
						while(!terminalsText.matches(".*?\\d.*")) {
							i++;
							terminal = terminals.get(i);
							terminalsText = terminal.getTerminalsText();
						}
						terminal.setTerminalsText(originalNumForm(terminalsText).trim());
						Chunk ratio = new Chunk(ChunkType.RATIO,  chunkCollector.getChunk(terminal));
						chunkCollector.addChunk(ratio);
					}
				}
			}
		}
	}
	
	
	public String originalNumForm(String token){
		if(token.matches(".*[a-z].*?")){
			return token.replaceAll("-\\s*LRB-/-LRB\\s*-?", "(").replaceAll("-\\s*RRB-/-RRB\\s*-?", ")");
		}else{
			return token.replaceAll("-\\s*LRB-/-LRB\\s*-?", "[").replaceAll("-\\s*RRB-/-RRB\\s*-?", "]");
		}
	}
}