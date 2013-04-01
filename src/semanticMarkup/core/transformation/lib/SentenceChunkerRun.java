package semanticMarkup.core.transformation.lib;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import semanticMarkup.core.Treatment;
import semanticMarkup.ling.Token;
import semanticMarkup.ling.chunk.ChunkCollector;
import semanticMarkup.ling.chunk.ChunkerChain;
import semanticMarkup.ling.learn.ITerminologyLearner;
import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.parse.AbstractParseTree;
import semanticMarkup.ling.parse.IParser;
import semanticMarkup.ling.pos.IPOSTagger;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.log.LogLevel;
import semanticMarkup.log.Timer;

/**
 * A SentenceChunkerRun chunks the sentence of a given treatment and collects the chunks in a ChunkCollector
 * @author rodenhausen
 */
public class SentenceChunkerRun implements Runnable {

	private String source;
	private String sentenceString;
	private ChunkerChain chunkerChain;
	private IParser parser;
	private IPOSTagger posTagger;
	private ITokenizer wordTokenizer;
	private INormalizer normalizer;
	private ITerminologyLearner terminologyLearner;
	private ChunkCollector result;
	private Treatment treatment;

	/**
	 * @param source
	 * @param sentenceString
	 * @param treatment
	 * @param terminologyLearner
	 * @param normalizer
	 * @param wordTokenizer
	 * @param posTagger
	 * @param parser
	 * @param chunkerChain
	 */
	public SentenceChunkerRun(String source, String sentenceString, Treatment treatment, ITerminologyLearner terminologyLearner, INormalizer normalizer, 
			ITokenizer wordTokenizer, IPOSTagger posTagger, IParser parser, ChunkerChain chunkerChain) {
		this.source = source;
		this.sentenceString = sentenceString;
		this.treatment = treatment;
		this.terminologyLearner = terminologyLearner;
		this.normalizer = normalizer;
		this.wordTokenizer = wordTokenizer;
		this.posTagger = posTagger;
		this.parser = parser;
		this.chunkerChain = chunkerChain;
	}

	@Override
	public void run() {
		try { 
			log(LogLevel.DEBUG, "Process sentence: " + sentenceString);
			
			String[] sentenceArray = sentenceString.split("##");
			sentenceString = sentenceArray[2];
			String subjectTag = sentenceArray[1];
			String modifier = sentenceArray[0];
			modifier = modifier.replaceAll("\\[|\\]|>|<|(|)", "");
			subjectTag = subjectTag.replaceAll("\\[|\\]|>|<|(|)", "");
			
			// normalize sentence
			String normalizedSentence="";
			normalizedSentence = normalizer.normalize(sentenceString, subjectTag, modifier, source);
			log(LogLevel.DEBUG, "Normalized sentence: " + normalizedSentence);
			
			// tokenize sentence
			List<Token> sentence = wordTokenizer.tokenize(normalizedSentence);
			
			// POS tag sentence
			List<Token> posedSentence = posTagger.tag(sentence);
			log(LogLevel.DEBUG, "POSed sentence " + posedSentence);
			
			// parse sentence
			long startTime = Calendar.getInstance().getTimeInMillis();
			AbstractParseTree parseTree = parser.parse(posedSentence);
			long endTime = Calendar.getInstance().getTimeInMillis();
			Timer.addParseTime(endTime - startTime);
			
			log(LogLevel.DEBUG, "Parse tree: ");
			log(LogLevel.DEBUG, parseTree.prettyPrint());
			//parseTree.prettyPrint();
			
			// chunk sentence using chunkerChain
			this.result = chunkerChain.chunk(parseTree, subjectTag, treatment, source, sentenceString);
			log(LogLevel.DEBUG, "Sentence processing finished.\n");
			
			// notify listeners to be done
			this.notifyListeners();
		} catch (Exception e) {
			e.printStackTrace();
			log(LogLevel.ERROR, e);
		}
	}
	
	/**
	 * @return the resulting ChunkCollector
	 */
	public ChunkCollector getResult() {
		return result;
	}
	
	private Set<ISentenceChunkerRunListener> listeners = new HashSet<ISentenceChunkerRunListener>();

	/**
	 * @param listener
	 */
	public void addListener(ISentenceChunkerRunListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * @param listener
	 */
	public void removeListener(ISentenceChunkerRunListener listener) {
		listeners.remove(listener);
	}

	/**
	 * notify the ISentenceChunkerRunListeners
	 */
	private void notifyListeners() {
		for(ISentenceChunkerRunListener listener : listeners) {
			listener.done(this);
		}
	}

	/**
	 * @return the treatment this SentenceChunkerRun is for
	 */
	public Treatment getTreatment() {
		return this.treatment;
	}
	
}
