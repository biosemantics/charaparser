package semanticMarkup.ling.learn.lib;

import java.util.List;
import java.util.Set;

import semanticMarkup.core.Treatment;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.transform.ITokenizer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * DatabaseInputNoLearner does not learn actively itself but reads the results of a learn process of the e.g. the previous charaparser perl part, 
 * or PerlTerminologyLearner from the database.
 * @author rodenhausen
 */
public class DatabaseInputNoLearner extends PerlTerminologyLearner {

	/**
	 * @param temporaryPath
	 * @param descriptionSeparator
	 * @param markupMode
	 * @param databaseName
	 * @param glossaryTable
	 * @param databasePrefix
	 * @param databaseUser
	 * @param databasePassword
	 * @param stopWords
	 * @param selectedSources
	 * @param glossary
	 * @param tokenizer
	 * @throws Exception
	 */
	@Inject
	public DatabaseInputNoLearner(@Named("temporaryPath") String temporaryPath, 
			@Named("descriptionSeparator") String descriptionSeparator, 
			@Named("markupMode") String markupMode,
			@Named("databaseName") String databaseName,
			@Named("GlossaryTable") String glossaryTable,
			@Named("databasePrefix") String databasePrefix, 
			@Named("databaseUser") String databaseUser, 
			@Named("databasePassword") String databasePassword, 
			@Named("StopWords") Set<String> stopWords,
			@Named("selectedSources") Set<String> selectedSources,
			IGlossary glossary, 
			@Named("WordTokenizer") ITokenizer tokenizer) throws Exception {
		super(temporaryPath, descriptionSeparator, markupMode, databaseName, glossaryTable,
				databasePrefix, databaseUser, databasePassword, stopWords, selectedSources, glossary, tokenizer);
	}
	
	@Override
	public void readResults(List<Treatment> treatments) {
		this.sentences = readSentences();
		this.sentencesForOrganStateMarker = readSentencesForOrganStateMarker(treatments);
		this.adjnouns = readAdjNouns();
		this.adjnounsent = readAdjNounSent();
		this.sentenceTags = readSentenceTags(treatments);
		this.bracketTags = readBracketTags(treatments);
		this.wordRoleTags = readWordRoleTags(); 
		this.wordSources = readWordToSourcesMap();
		this.roleToWords = readRoleToWords();
		this.wordsToRoles = readWordsToRoles();
		this.heuristicNouns = readHeuristicNouns();
		this.termCategories = readTermCategories();
		this.tags = readTags();
		this.modifiers = readModifiers();
		this.categoryTerms = readCategoryTerms();
	}

}
