package semanticMarkup.core.transformation.lib;

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
		log(LogLevel.DEBUG, "Process sentence: " + sentenceString);
		
		String[] sentenceArray = sentenceString.split("##");
		sentenceString = sentenceArray[2];
		String subjectTag = sentenceArray[1];
		String modifier = sentenceArray[0];
		modifier = modifier.replaceAll("\\[|\\]|>|<|(|)", "");
		subjectTag = subjectTag.replaceAll("\\[|\\]|>|<|(|)", "");
		
		String normalizedSentence="";
		normalizedSentence = normalizer.normalize(sentenceString, subjectTag, modifier, source);
		
		log(LogLevel.DEBUG, "Normalized sentence: " + normalizedSentence);
		List<Token> sentence = wordTokenizer.tokenize(normalizedSentence);
		
		List<Token> posedSentence = posTagger.tag(sentence);
		log(LogLevel.DEBUG, "POSed sentence " + posedSentence);
		
		AbstractParseTree parseTree = parser.parse(posedSentence);
		log(LogLevel.DEBUG, "Parse tree: ");
		log(LogLevel.DEBUG, parseTree.prettyPrint());
		//parseTree.prettyPrint();
		
		this.result = chunkerChain.chunk(parseTree, subjectTag, treatment, source, sentenceString);
		log(LogLevel.DEBUG, "Sentence processing finished.\n");
		this.notifyListeners();
	}
	
	public ChunkCollector getResult() {
		return result;
	}
	
	private Set<ISentenceChunkerRunListener> listeners = new HashSet<ISentenceChunkerRunListener>();

	public void addListener(ISentenceChunkerRunListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ISentenceChunkerRunListener listener) {
		listeners.remove(listener);
	}

	public void notifyListeners() {
		for(ISentenceChunkerRunListener listener : listeners) {
			listener.done(this);
		}
	}

	public Treatment getTreatment() {
		return this.treatment;
	}
	
}
