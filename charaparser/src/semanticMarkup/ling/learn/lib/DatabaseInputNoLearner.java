package semanticMarkup.ling.learn.lib;

import java.util.List;
import java.util.Set;

import semanticMarkup.core.Treatment;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.transform.ITokenizer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DatabaseInputNoLearner extends PerlTerminologyLearner {

	@Inject
	public DatabaseInputNoLearner(@Named("temporaryPath") String temporaryPath, 
			@Named("descriptionSeparator") String descriptionSeparator, 
			@Named("markupMode") String markupMode,
			@Named("databaseName") String databaseName,
			@Named("databasePrefix") String databasePrefix, 
			@Named("databaseUser") String databaseUser, 
			@Named("databasePassword") String databasePassword, 
			@Named("StopWords") Set<String> stopWords,
			@Named("selectedSources") Set<String> selectedSources,
			IGlossary glossary, 
			@Named("WordTokenizer") ITokenizer tokenizer) throws Exception {
		super(temporaryPath, descriptionSeparator, markupMode, databaseName, 
				databasePrefix, databaseUser, databasePassword, stopWords, selectedSources, glossary, tokenizer);
	}
	
	@Override
	public void learn(List<Treatment> treatments) {
		this.sentences = readSentences();
		this.sentencesForOrganStateMarker = readSentencesForOrganStateMarker(treatments);
		this.adjnouns = readAdjNouns();
		this.adjnounsent = readAdjNounSent();
		this.sentenceTags = readSentenceTags(treatments);
		this.bracketTags = readBracketTags(treatments);
		this.wordRoleTags = readWordRoleTags(); //these are in the previous version only given by the users in GUI not by the algorithm autonomously
		this.wordSources = readWordToSourcesMap();
		this.roleToWords = readRoleToWords();
		this.wordsToRoles = readWordsToRoles();
		this.heuristicNouns = readHeuristicNouns();
		this.termCategories = readTermCategories();
	}

}
