package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import edu.arizona.sirls.semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
import edu.arizona.sirls.semanticMarkup.ling.transform.ITokenizer;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.myTokenizer = tokenizer;
		
		// Get OpenNLP sentence detector
		InputStream sentModelIn;
		try {
			sentModelIn = new FileInputStream(configuration.getOpenNLPSentenceDetectorDir());
			SentenceModel model = new SentenceModel(sentModelIn);
			this.mySentenceDetector = new SentenceDetectorME(model);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
