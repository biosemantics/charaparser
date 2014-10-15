package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.unsupervised;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.arizona.biosemantics.semanticmarkup.know.lib.WordNetPOSKnowledgeBase;
import edu.arizona.biosemantics.semanticmarkup.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class Utility {

	private WordNetPOSKnowledgeBase myWN = null;
	private SentenceDetectorME mySentenceDetector = null;
	
	private ITokenizer myTokenizer;

	private PopulateSentenceUtility myPopulateSentenceUtility = null;
	private WordFormUtility myWordFormUtility = null;
	private LearnerUtility myLearnerUtility = null;
	
	public Utility(Configuration configuration, ITokenizer tokenizer) {
		// get those tools
		// Get WordNetAPI instance
		try {
			this.myWN = new WordNetPOSKnowledgeBase(configuration.getWordNetDictDir(), false);
		} catch (IOException e) {
			log(LogLevel.ERROR, "Couldn't instantate wordnet pos knowledgebase", e);
		}
		
		this.myTokenizer = tokenizer;
		
		// Get OpenNLP sentence detector
		InputStream sentModelIn;
		try {
			sentModelIn = new FileInputStream(configuration.getOpenNLPSentenceDetectorDir());
			SentenceModel model = new SentenceModel(sentModelIn);
			this.mySentenceDetector = new SentenceDetectorME(model);
		} catch (Exception e) {
			log(LogLevel.ERROR, "Couldn't read open nlp sentence detector files", e);
		}	
		
		this.myPopulateSentenceUtility = new PopulateSentenceUtility(this.mySentenceDetector);
		this.myWordFormUtility = new WordFormUtility(this.myWN);
		this.myLearnerUtility = new LearnerUtility(configuration, myTokenizer);
	}
	
	public  PopulateSentenceUtility getPopulateSentenceUtility(){
		return this.myPopulateSentenceUtility;
	}
	
	public WordFormUtility getWordFormUtility(){
		return this.myWordFormUtility;
	}
	
	public LearnerUtility getLearnerUtility() {
		return this.myLearnerUtility;
	}

	public WordNetPOSKnowledgeBase getWordNet() {
		return this.myWN;
	}
	
	public SentenceDetectorME getSentenceDetector(){
		return this.mySentenceDetector;
	}

}
