package edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import edu.arizona.biosemantics.semanticmarkup.ling.Token;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.AbstractParseTree;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParser;
import edu.arizona.biosemantics.semanticmarkup.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.log.Timer;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;


/**
 * A SentenceChunkerRun chunks the sentence of a given treatment and collects the chunks in a ChunkCollector
 * @author rodenhausen
 */
public class SentenceChunkerRun implements Callable<ChunkCollector> {

	private String source;
	private String sentenceString;
	private ChunkerChain chunkerChain;
	private IParser parser;
	private IPOSTagger posTagger;
	private ITokenizer wordTokenizer;
	private INormalizer normalizer;
	private ChunkCollector result;
	private Description description;
	private AbstractDescriptionsFile descriptionsFile;
	private CountDownLatch sentencesLatch;

	/**
	 * @param source
	 * @param sentenceString
	 * @param treatment
	 * @param normalizer
	 * @param wordTokenizer
	 * @param posTagger
	 * @param parser
	 * @param chunkerChain
	 * @param sentencesLatch 
	 */
	public SentenceChunkerRun(String source, String sentenceString, Description description, 
			AbstractDescriptionsFile descriptionsFile,
			INormalizer normalizer, 
			ITokenizer wordTokenizer, IPOSTagger posTagger, IParser parser, ChunkerChain chunkerChain, CountDownLatch sentencesLatch) {
		this.source = source;
		this.sentenceString = sentenceString;
		this.description = description;
		this.descriptionsFile = descriptionsFile;
		this.normalizer = normalizer;
		this.wordTokenizer = wordTokenizer;
		this.posTagger = posTagger;
		this.parser = parser;
		this.chunkerChain = chunkerChain;
		this.sentencesLatch = sentencesLatch;
	}

	@Override
	public ChunkCollector call() throws Exception {
		try { 
			log(LogLevel.DEBUG, "Process sentence: " + sentenceString);
			
			String[] sentenceArray = sentenceString.split("##");
			sentenceString = sentenceArray[2];
			String subjectTag = sentenceArray[1]; //TODO: Hong stop using subjectTag and modifier
			String modifier = sentenceArray[0];
			modifier = modifier.replaceAll("\\[|\\]|>|<|(|)", "");
			subjectTag = subjectTag.replaceAll("\\[|\\]|>|<|(|)", "");
			
			// normalize sentence
			String normalizedSentence="";
			normalizedSentence = normalizer.normalize(sentenceString, subjectTag, modifier, source);
			log(LogLevel.DEBUG, "Normalized sentence: " + normalizedSentence);//TODO: (4) is not '3'
			
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
			this.result = chunkerChain.chunk(parseTree, subjectTag, description, descriptionsFile, source, sentenceString);
			log(LogLevel.DEBUG, "Sentence processing finished.\n");
		} catch (Exception e) {
			log(LogLevel.ERROR, "Problem chunking sentencence: " + sentenceString + "\nSentence is contained in file: " + source, e);
		} 
		this.sentencesLatch.countDown();
		return result;
	}
	
}
