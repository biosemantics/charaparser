package edu.arizona.biosemantics.semanticmarkup.ling.pos.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.Token;
import edu.arizona.biosemantics.common.ling.know.CharacterMatch;
//import edu.arizona.biosemantics.semanticmarkup.know.IOrganStateKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.ICorpus;
import edu.arizona.biosemantics.common.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.common.ling.pos.POS;
import edu.arizona.biosemantics.common.ling.pos.POSedToken;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;

/**
 * OrganCharacterPOSTagger taggs tokens using IOrganStateKnowledgeBase, ITerminologyLearner, and an ICorpus
 * @author rodenhausen
 */
public class OrganCharacterPOSTagger implements IPOSTagger {

	private String roman="i|ii|iii|iv|v|vi|vii|viii|ix|x|xi|xii|xiii|xiv|xv|xvi|xvii|xviii|xix|xx|I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX";
	private ICorpus corpus;
	private String prepositions;
	private Set<String> stopWords;
	private String units;
	private ITerminologyLearner terminologyLearner;
	private Set<String> vbWords;
	private Pattern compreppattern = Pattern.compile("\\{?(according-to|ahead-of|along-with|apart-from|as-for|aside-from|as-per|as-to-as-well-as|away-from|because-of|but-for|by-means-of|close-to|contrary-to|depending-on|due-to|except-for|forward-of|further-to|in-addition-to|in-between|in-case-of|in-face-of|in-favour-of|in-front-of|in-lieu-of|in-spite-of|instead-of|in-view-of|near-to|next-to|on-account-of|on-behalf-of|on-board|on-to|on-top-of|opposite-to|other-than|out-of|outside-of|owing-to|preparatory-to|prior-to|regardless-of|save-for|thanks-to|together-with|up-against|up-to|up-until|vis-a-vis|with-reference-to|with-regard-to)\\}?");
	//private Pattern compreppattern;
	//private IOrganStateKnowledgeBase organStateKnowledgeBase;
	private ICharacterKnowledgeBase learnedCharacterKnowledgeBase;

	/**
	 * @param corpus
	 * @param prepositions
	 * @param stopWords
	 * @param units
	 * @param terminologyLearner
	 * @param vbWords
	 * @param learnedCharacterKnowledgeBase
	 * @param compoundPrepWords
	 */
	@Inject
	public OrganCharacterPOSTagger(ICorpus corpus, @Named("PrepositionWords") String prepositions, @Named("StopWords") Set<String> stopWords,
			@Named("Units") String units, ITerminologyLearner terminologyLearner, @Named("VBWords")Set<String> vbWords,
			/*IOrganStateKnowledgeBase organStateKnowledgeBase,*/ICharacterKnowledgeBase learnedCharacterKnowledgeBase, @Named("CompoundPrepWords")String compoundPrepWords) {
		this.corpus = corpus;
		this.prepositions = prepositions;
		this.stopWords = stopWords;
		this.units = units;
		this.terminologyLearner = terminologyLearner;
		this.vbWords = vbWords;
		//this.organStateKnowledgeBase = organStateKnowledgeBase;
		this.learnedCharacterKnowledgeBase = learnedCharacterKnowledgeBase;
		this.compreppattern = Pattern.compile("("+compoundPrepWords.replaceAll("\\s+", "-")+")");
	}


	@Override
	public List<Token> tag(List<Token> sentence) {
		List<Token> posedSentence = new ArrayList<Token>();

		for (int i = 0; i < sentence.size(); i++) {

			Token token = sentence.get(i);
			String word = token.getContent();

			String p = "";

			boolean isState = false;
			boolean isOrgan = false;
			boolean isVerb = false;
			//if(word.contains("~list~") || word.contains("_c_") || organStateKnowledgeBase.isState(word))
			//don't send numbers, values to learnedCharacterKnowledgeBase
			if(word.contains("~list~") || word.contains("_c_")  ||
					((word.matches("[^a-z]+") || word.contains("=")|| word.matches(".*?(^|[^a-z])("+units+")([^a-z]|$).*"))&& word.matches(".*?\\d.*"))){ //units could be mixed in the numbers
				isState = true;
			}else if(learnedCharacterKnowledgeBase.isCategoricalState(word)){
				isState = true;
				CharacterMatch m = learnedCharacterKnowledgeBase.getCharacterName(word);
				if(m!=null &&  m.getCategories()!=null && m.getCategories().matches("(^|.*?_)position_relational(_.*|$)")){
					isVerb = true;
				}
			}else {
				//isOrgan = organStateKnowledgeBase.isOrgan(word);
				isOrgan = learnedCharacterKnowledgeBase.isEntity(word);
			}


			Map<String, Set<String>> wordsToRoles = terminologyLearner
					.getWordsToRoles();
			if (word.length() > 0 && !word.matches("\\W")
					&& !word.matches("(" + prepositions + ")")
					&& !stopWords.contains(word)) {
				if (wordsToRoles.containsKey(word))
					p = wordsToRoles.get(word).iterator().next();
			}

			//Hong TODO modifiertokens
			Matcher mc = compreppattern.matcher(word);
			if(word.equals("lengths") || word.equals("length")){
				posedSentence.add(new POSedToken(word, POS.NNS)); //stanford parser would sometimes take "lengths" as IN
			}else if(word.equals("moreorless")) {
				posedSentence.add(new POSedToken(word, POS.RB));
			} else if (mc.matches()) {
				posedSentence.add(new POSedToken(word, POS.IN));
			} else if (word.matches("in-.*?(-view|profile)")) {
				posedSentence.add(new POSedToken(word, POS.RB));
			} else if (word.endsWith("ly") && word.indexOf("~") < 0) {
				posedSentence.add(new POSedToken(word, POS.RB));
			} else if (word.compareTo("becoming") == 0
					|| word.compareTo("about") == 0) {
				posedSentence.add(new POSedToken(word, POS.RB));
			} else if (word.compareTo("throughout") == 0
					&&(i+1 == sentence.size() || sentence.get(i + 1).getContent().matches("(,|or)"))) {
				posedSentence.add(new POSedToken(word, POS.RB));
			} else if (word.contains("#")) {
				posedSentence.add(new POSedToken(word.replace('#','-'), POS.RB));
			} else if (word.compareTo("plus") == 0
					|| word.compareTo("and-or") == 0) {
				posedSentence.add(new POSedToken(word, POS.CC));
			} else if (word.matches("\\d+[cmd]?m\\d+[cmd]?m")) {
				posedSentence.add(new POSedToken(word, POS.CD));
			} else if (word.matches("(" + units + ")")) {
				posedSentence.add(new POSedToken(word, POS.NNS));
			} else if (word.matches("as-\\S+")) {
				posedSentence.add(new POSedToken(word, POS.IN));
			} else if (p.contains("op")) { // <inner> larger.
				posedSentence.add(new POSedToken(word, POS.NNS));
			} else if (p.contains("os")
					|| (p.length() == 0 && isOrgan)) {
				posedSentence.add(new POSedToken(word, POS.NN));
			} else if (word.matches("(\\{?\\b" + roman + "\\b\\}?)")) {// mohan
				// code
				// to
				// mark
				// roman
				// numbers
				// {ii}
				// or ii
				// as
				// ii/NNS
				word = word.replaceAll("\\{|\\}", "");
				posedSentence.add(new POSedToken(word, POS.NNS));
			} else if(word.matches("\\d*.{0,1}\\d+")) {
				posedSentence.add(new POSedToken(word, POS.CD));
			} else if(isVerb){
				posedSentence.add(new POSedToken(word, POS.VB));
			} else if (p.contains("c") || isState) {
				int wordFrequency = corpus.getFrequency(word);
				if (wordFrequency > 79) {
					posedSentence.add(new Token(word));
				} else {
					posedSentence.add(new POSedToken(token.getContent(), POS.JJ));
				}
			} else {
				posedSentence.add(new Token(token.getContent()));
			}
		}

		return posedSentence;
	}
}
