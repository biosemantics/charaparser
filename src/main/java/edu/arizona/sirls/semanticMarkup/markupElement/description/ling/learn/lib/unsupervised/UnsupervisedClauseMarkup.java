package edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.lib.unsupervised;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.sirls.semanticMarkup.know.IGlossary;
import edu.arizona.sirls.semanticMarkup.ling.Token;
import edu.arizona.sirls.semanticMarkup.ling.transform.ITokenizer;
import edu.arizona.sirls.semanticMarkup.markupElement.description.io.ParentTagProvider;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.AdjectiveReplacementForNoun;
import edu.arizona.sirls.semanticMarkup.markupElement.description.ling.learn.ITerminologyLearner;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;
import edu.arizona.sirls.semanticMarkup.markupElement.description.model.Description;


public class UnsupervisedClauseMarkup implements ITerminologyLearner {	
	// Date holder
	public DataHolder myDataHolder;
	
	// Configuration
	private Configuration myConfiguration;
		
	// Utility
	private Utility myUtility;

	// Learner
	private Learner myLearner;

	protected List<String> adjnouns;
	protected Map<String, String> adjnounsent;
	protected Set<String> bracketTags;
	protected Map<String, Set<String>> categoryTerms;
	protected Map<String, String> heuristicNouns;
	protected Set<String> modifiers;
	protected Map<String, Set<String>> roleToWords;
	protected Set<String> sentences;
	protected Map<Description, LinkedHashMap<String, String>> sentencesForOrganStateMarker;
	protected Map<Description, LinkedHashMap<String, String>> sentenceTags;
	protected Set<String> tags;
	protected Map<String, Set<String>> termCategories;
	protected Set<String> wordRoleTags;
	protected Map<String, Set<String>> wordsToRoles;
	protected Map<String, Set<String>> wordToSources;
	
	protected Map<String, Description> fileDescriptions = new HashMap<String, Description>();
	
	private Map<String, AdjectiveReplacementForNoun> adjectiveReplacementsForNouns;
	private String markupMode;
	private ParentTagProvider parentTagProvider;
	private Set<String> selectedSources;
	private ITokenizer tokenizer;


	/**
	 * Constructor of UnsupervisedClauseMarkup class. Create a new
	 * UnsupervisedClauseMarkup object.
	 * 
	 */
	@Inject
	public UnsupervisedClauseMarkup(@Named("MarkupMode") String markupMode,
			@Named("SelectedSources") Set<String> selectedSources,
			@Named("WordTokenizer") ITokenizer tokenizer,
			@Named("ParentTagProvider")ParentTagProvider parentTagProvider) {
		
		//this.chrDir = desDir.replaceAll("descriptions.*", "characters/");
		
		this.selectedSources = new HashSet<String>();
		this.selectedSources.addAll(selectedSources);
		
		this.markupMode = markupMode;
		this.parentTagProvider = parentTagProvider;
		this.tokenizer = tokenizer;
		
		this.myConfiguration = new Configuration();
		this.myUtility = new Utility(myConfiguration, tokenizer);
		this.myDataHolder = new DataHolder(myConfiguration, myUtility);
		myLearner = new Learner(this.myConfiguration, this.tokenizer, this.myUtility);
		
	}

	// learn
	public void learn(List<AbstractDescriptionsFile> descriptionFiles, String glossaryTable) {
		this.myDataHolder = this.myLearner.Learn(descriptionFiles);
		readResults(descriptionFiles);
	}
	
	@Override
	public void readResults(List<AbstractDescriptionsFile> descriptionsFiles) {
		// import data from data holder
		this.adjnouns = this.readAdjNouns();
		this.adjnounsent = this.readAdjNounSent();
		this.bracketTags = this.readBracketTags();
		this.heuristicNouns = this.readHeuristicNouns();
		this.categoryTerms = this.readCategoryTerms();
		this.modifiers = this.readModifiers();
		this.roleToWords = this.readRoleToWords();
		this.sentences = this.readSentences(descriptionsFiles);
		this.sentencesForOrganStateMarker = this.readSentencesForOrganStateMarker(descriptionsFiles);
		this.sentenceTags = this.readSentenceTags(descriptionsFiles);
		this.tags = this.readTags();
		this.termCategories = this.readTermCategories();
		this.wordRoleTags = this.readWordRoleTags();
		this.wordsToRoles = this.readWordsToRoles();
		this.wordToSources = this.readWordToSources();
		
		this.adjectiveReplacementsForNouns = readAdjectiveReplacementsForNouns();
	}
		
	// interface methods
	@Override
	public List<String> getAdjNouns() {
		return this.adjnouns;
	}

	@Override
	public Map<String, String> getAdjNounSent() {
		return this.adjnounsent;
	}

	@Override
	public Set<String> getBracketTags() {
		return this.bracketTags;
	}
	
	@Override
	public Map<String, Set<String>> getCategoryTerms() {
		return this.categoryTerms;
	}

	@Override
	public Map<String, String> getHeuristicNouns() {
		return this.heuristicNouns;
	}
	
	@Override
	public Set<String> getModifiers() {
		return this.modifiers;
	}

	@Override
	public Map<String, Set<String>> getRoleToWords() {
		return this.roleToWords;

	}

	@Override
	public Set<String> getSentences() {
		return this.sentences;
	}

	@Override
	public Map<Description, LinkedHashMap<String, String>> getSentencesForOrganStateMarker() {
		return this.sentencesForOrganStateMarker;
	}	

	@Override
	public Map<Description, LinkedHashMap<String, String>> getSentenceTags() {
		return this.sentenceTags;
	}
	
	@Override
	public Set<String> getTags() {
		return this.tags;
	}

	@Override
	public Map<String, Set<String>> getTermCategories() {
		return this.termCategories;
	}

	@Override
	public Set<String> getWordRoleTags() {
		return this.wordRoleTags;
	}

	@Override
	public Map<String, Set<String>> getWordsToRoles() {
		return this.wordsToRoles;
	}
	
	@Override
	public Map<String, Set<String>> getWordToSources() {
		return this.wordToSources;
	}

	// methods dealing with ajectiveReplacementForNoun
	public Map<String, AdjectiveReplacementForNoun> readAdjectiveReplacementsForNouns() {
		Map<String, AdjectiveReplacementForNoun> result = new HashMap<String, AdjectiveReplacementForNoun>();

		Iterator<SentenceStructure> iter = this.getDataHolder().getSentenceHolder()
				.iterator();
		while (iter.hasNext()) {
			SentenceStructure sentenceObject = iter.next();
			String modifier = sentenceObject.getModifier();
			String tag = sentenceObject.getTag();

			if ((!StringUtils.equals(modifier, ""))
					&& (StringUtility.isEntireMatched("^\\[.*$", tag))) {
				String source = sentenceObject.getSource();
				modifier = modifier.replaceAll("\\[|\\]|>|<|(|)", "");
				tag = tag.replaceAll("\\[|\\]|>|<|(|)", "");
				result.put(source, new AdjectiveReplacementForNoun(modifier,tag, source));
			}
		}

		return result;
	}
	
	@Override
	public Map<String, AdjectiveReplacementForNoun> getAdjectiveReplacementsForNouns() {
		return this.adjectiveReplacementsForNouns;
	}
	
	// methods importing data from data holder to class variables
	public List<String> readAdjNouns() {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("unsupervisedClauseMarkup.getAdjectiveReplacementsForNouns");
		
		if (this.myDataHolder == null) {
			return null;
		}
		
		Set<String> myAdjNounSet = new HashSet<String>();

		Iterator<SentenceStructure> iter = this.myDataHolder.getSentenceHolder()
				.iterator();

		while (iter.hasNext()) {
			SentenceStructure sentenceObject = iter.next();
			String modifier = sentenceObject.getModifier();
			String tag = sentenceObject.getTag();
			myLogger.trace("tag: "+tag);
			if (tag != null) {
				if (tag.matches("^\\[.*$")) {
					modifier = modifier.replaceAll("\\[.*?\\]", "").trim();
					myAdjNounSet.add(modifier);
				}
			}
		}

		List<String> myAdjNouns = new ArrayList<String>();
		myAdjNouns.addAll(myAdjNounSet);

		return myAdjNouns;
	}

	public Map<String, String> readAdjNounSent() {
		PropertyConfigurator.configure( "conf/log4j.properties" );
		Logger myLogger = Logger.getLogger("unsupervisedClauseMarkup.readAdjNounSent");
		
		if (this.myDataHolder == null) {
			return null;
		}
		
		Map<String, String> myAdjNounSent = new HashMap<String, String>();

		// collect sentences that need adj-nn disambiguation
		Iterator<SentenceStructure> iter = this.myDataHolder.getSentenceHolder()
				.iterator();

		while (iter.hasNext()) {
			SentenceStructure sentenceObject = iter.next();
			String modifier = sentenceObject.getModifier();
			String tag = sentenceObject.getTag();
			myLogger.trace("tag: "+tag);
			if ((modifier != null)&&(tag != null)) {
				if ((!(modifier.equals(""))) && (tag.matches("^\\[.*$"))) {
					modifier = modifier.replaceAll("\\[.*?\\]", "").trim();
					myAdjNounSent.put(tag, modifier);
				}
			}
		}

		return myAdjNounSent;
	}

	public Set<String> readBracketTags() {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Set<String> tags = new HashSet<String>();
		
		Iterator<SentenceStructure> iter = this.getDataHolder().getSentenceHolder().iterator();
		
		while (iter.hasNext()) {
			SentenceStructure sentence = iter.next();
			String thisTag = sentence.getTag();
			if (thisTag != null) {
				if (StringUtility.createMatcher("^\\[.*\\]$", thisTag).find()) {
					String thisModifier = sentence.getModifier();
					String modifier = thisModifier
							.replaceAll("\\[^\\[*\\]", "");
					if (!modifier.equals("")) {
						String tag;
						if (modifier.lastIndexOf(" ") < 0) {
							tag = modifier;
						} else {
							// last word from modifier
							tag = modifier
									.substring(modifier.lastIndexOf(" ") + 1); 
						}

						if (tag.indexOf("[") >= 0
								|| tag.matches(".*?(\\d|" + Constant.STOP
										+ ").*"))
							continue;
						tags.add(tag);
					}
				}
			}
		}
		
		return tags;
		
	}
	
	protected Map<String, Set<String>> readCategoryTerms() {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Map<String, Set<String>> categoryNames = new HashMap<String, Set<String>>();
		Iterator<StringPair> iter = this.getDataHolder().getTermCategoryHolder().iterator();
		while (iter.hasNext()) {
			StringPair termCategoryObject = iter.next();
			String term = termCategoryObject.getHead();
			String category = termCategoryObject.getTail();
			if (!categoryNames.containsKey(category))
				categoryNames.put(category, new HashSet<String>());
			categoryNames.get(category).add(term);
		}

		return categoryNames;
	}

	public Map<String, String> readHeuristicNouns() {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Map<String, String> myHeuristicNouns = new HashMap<String, String>();
		myHeuristicNouns.putAll(this.getDataHolder().getHeuristicNounHolder());
		
		return myHeuristicNouns;
		
	}
	
	public Set<String> readModifiers() {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Set<String> modifiers = new HashSet<String>();
		Iterator<SentenceStructure> iter = this.getDataHolder().getSentenceHolder()
				.iterator();
		while (iter.hasNext()) {
			String modifier = iter.next().getTag();
			modifiers.add(modifier);
		}
		return modifiers;
	}

	public Map<String, Set<String>> readRoleToWords() {
		if (this.myDataHolder == null) {
			return null;
		}

		Map<String, Set<String>> roleToWords = new HashMap<String, Set<String>>();

		 Iterator<Entry<StringPair, String>> iter = this.getDataHolder()
				.getWordRoleHolder().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<StringPair, String> wordRoleObject = iter.next();
			String word = wordRoleObject.getKey().getHead();
			// perl treated hyphens as underscores
			word = word.replaceAll("_", "-");
			String semanticRole = wordRoleObject.getKey().getTail();
			if (!roleToWords.containsKey(semanticRole))
				roleToWords.put(semanticRole, new HashSet<String>());
			roleToWords.get(semanticRole).add(word);
		}

		return roleToWords;

	}

	public Set<String> readSentences(List<AbstractDescriptionsFile> treatments) {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Set<String> result = new HashSet<String>();
		
		Iterator<SentenceStructure> iter = this.getDataHolder().getSentenceHolder().iterator();
		while (iter.hasNext()) {
			SentenceStructure sentenceObject = iter.next();
			String sentence = sentenceObject.getSentence();
			result.add(sentence);
		}

		return sentences;
	}

	public Map<Description, LinkedHashMap<String, String>> readSentencesForOrganStateMarker(List<AbstractDescriptionsFile> treatments) {
		if (this.myDataHolder == null) {
			return null;
		}
		
		HashMap<Description, LinkedHashMap<String, String>> sentences = new  HashMap<Description, LinkedHashMap<String, String>>();
		
		List<SentenceStructure> sentenceHolder = this.getDataHolder().getSentenceHolder();
		String previousAbstractDescriptionsFileId = "-1";
		for (int i = sentenceHolder.size()-1;i>=0;i--) {
			SentenceStructure sentenceObject = sentenceHolder.get(i);
			String source = this.getSource(sentenceObject.getSource());
			String modifier = sentenceObject.getModifier();
			String tag = sentenceObject.getTag();
			String sentence = sentenceObject.getSentence().trim();
			
			if(sentence.length()!=0){
				String treatmentId = getAbstractDescriptionsFileId(source);
				
				if(selectedSources.isEmpty() || selectedSources.contains(source)) {
					if(!treatmentId.equals(previousAbstractDescriptionsFileId)) {
						previousAbstractDescriptionsFileId = treatmentId;
					}
					
					String text = sentence;
					text = text.replaceAll("[ _-]+\\s*shaped", "-shaped").replaceAll("(?<=\\s)µ\\s+m\\b", "um");
					text = text.replaceAll("&#176;", "°");
					text = text.replaceAll("\\bca\\s*\\.", "ca");
					text = modifier+"##"+tag+"##"+text;
					
					Description description = this.fileDescriptions.get(treatmentId);
					if(!sentences.containsKey(description))
						sentences.put(description, new LinkedHashMap<String, String>());
					sentences.get(description).put(source, text);
				}
			}
			
		}

		return sentences;
		
	}

	public Map<Description, LinkedHashMap<String, String>> readSentenceTags(List<AbstractDescriptionsFile> treatments) {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Map<Description, LinkedHashMap<String, String>> tags = new HashMap<Description, LinkedHashMap<String, String>>();		
		String previousTag = null;
		String previousAbstractDescriptionsFileId = "-1";
		
		Iterator<SentenceStructure> iter = this.getDataHolder().getSentenceHolder().iterator();
		while (iter.hasNext()) {
			SentenceStructure sentenceObject = iter.next();
			
			String source = this.getSource(sentenceObject.getSource());
			String treatmentId = getAbstractDescriptionsFileId(source);
			if(selectedSources.isEmpty() || selectedSources.contains(source)) {
				
				if(!treatmentId.equals(previousAbstractDescriptionsFileId)) {
					previousAbstractDescriptionsFileId = treatmentId;
					//listId++;
				}
				
				String tag = sentenceObject.getTag();
				if(tag == null)
					tag = "";
				tag = tag.replaceAll("\\W", "");
				
				Description description = this.fileDescriptions.get(treatmentId);
				if(!tags.containsKey(description)) 
					tags.put(description, new LinkedHashMap<String, String>());
				if(!tag.equals("ditto")) {
					tags.get(description).put(source, tag);
					previousTag = tag;
				} else {
					tags.get(description).put(source, previousTag);
				}			
			}
		}

		return tags;
	}
	
	public Set<String> readTags() {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Set<String> tags = new HashSet<String>();
		Iterator<SentenceStructure> iter = this.getDataHolder().getSentenceHolder()
				.iterator();
		while (iter.hasNext()) {
			String tag = iter.next().getTag();

			tags.add(tag);
		}

		return tags;
	}

	public Map<String, Set<String>> readTermCategories() {
		if (this.myDataHolder == null) {
			return null;
		}

		Map<String, Set<String>> termCategories = new HashMap<String, Set<String>>();

		Iterator<StringPair> iter = this.getDataHolder()
				.getTermCategoryHolder().iterator();
		StringPair myTermCategoryPair;
		while (iter.hasNext()) {
			myTermCategoryPair = iter.next();
			String term = myTermCategoryPair.getHead();
			String category = myTermCategoryPair.getTail();
			if (!termCategories.containsKey(term))
				termCategories.put(term, new HashSet<String>());
			termCategories.get(term).add(category);
		}

		return termCategories;

	}

	public Set<String> readWordRoleTags() {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Set<String> tags = new HashSet<String>();

		Iterator<Entry<StringPair, String>> iter = this.getDataHolder()
				.getWordRoleHolder().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<StringPair, String> wordRoleObject = iter.next();
			String role = wordRoleObject.getKey().getTail();
			if (StringUtils.equals(role, "op")
					|| StringUtils.equals(role, "os")) {
				String word = wordRoleObject.getKey().getHead();
				String tag = word.replaceAll("_", "-").trim();
				if (!tag.isEmpty())
					tags.add(tag);
			}

		}

		return tags;

	}

	public Map<String, Set<String>> readWordsToRoles() {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Map<String, Set<String>> wordsToRoles = new HashMap<String, Set<String>>();

		Iterator<Entry<StringPair, String>> iter = this.getDataHolder()
				.getWordRoleHolder().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<StringPair, String> wordRoleObject = iter.next();
			String word = wordRoleObject.getKey().getHead();
			// learner treated hyphens as underscores
			word = word.replaceAll("_", "-");
			String semanticRole = wordRoleObject.getKey().getTail();
			if (!wordsToRoles.containsKey(word))
				wordsToRoles.put(word, new HashSet<String>());
			wordsToRoles.get(word).add(semanticRole);
		}

		return wordsToRoles;
		
	}

	public Map<String, Set<String>> readWordToSources() {
		if (this.myDataHolder == null) {
			return null;
		}
		
		Map<String, Set<String>> myWordToSources = new HashMap<String, Set<String>>();

		Iterator<SentenceStructure> iter = this.myDataHolder.getSentenceHolder()
				.iterator();
		
		while (iter.hasNext()) {
			SentenceStructure sentenceObject = iter.next();
			String source = this.getSource(sentenceObject.getSource());
			String sentence = sentenceObject.getSentence();			
			List<Token> tokens = this.tokenizer.tokenize(sentence);
			for(Token token : tokens) {
				String word = token.getContent();
				if(!myWordToSources.containsKey(word))
					myWordToSources.put(word, new HashSet<String>());
				myWordToSources.get(word).add(source);
			}
		}

		return myWordToSources;
	}
	
	// Miscellaneous
	public void initParentTagProvider(ParentTagProvider parentTagProvider2) {
		HashMap<String, String> parentTags = new HashMap<String, String>();
		HashMap<String, String> grandParentTags = new HashMap<String, String>();
			
		Iterator<SentenceStructure> iter = this.getDataHolder().getSentenceHolder()
				.iterator();
		while (iter.hasNext()) {
			SentenceStructure sentenceObject = iter.next();

			String parentTag = "";
			String grandParentTag = "";

			String source = getSource(sentenceObject.getSource());
			String tag = sentenceObject.getTag();
			parentTags.put(source, parentTag);
			grandParentTags.put(source, grandParentTag);

			grandParentTag = parentTag;
			if (tag != null && !tag.equals("ditto"))
				parentTag = tag;
			else if (tag == null)
				parentTag = "";
		}

		this.parentTagProvider.init(parentTags, grandParentTags);
	}
	
	
	//Utilities
	public DataHolder getDataHolder() {
		return this.myDataHolder;
	}
	
	protected String getAbstractDescriptionsFileId(String sourceString) {
		String[] sourceParts = sourceString.split("\\.");
		return sourceParts[0];
	}
	
	protected String getSource(String sourceString) {
		return sourceString;
		//String[] sourceParts = sourceString.split("\\.");
		//return sourceParts[0] + "." + sourceParts[2];
	}
}