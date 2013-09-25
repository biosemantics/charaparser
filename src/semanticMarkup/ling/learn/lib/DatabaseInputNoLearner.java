package semanticMarkup.ling.learn.lib;

import java.util.List;
import java.util.Set;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.input.lib.db.ParentTagProvider;
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
	public DatabaseInputNoLearner(@Named("Run_TemporaryPath") String temporaryPath,
			@Named("markupMode") String markupMode,
			@Named("databaseHost") String databaseHost,
			@Named("databasePort") String databasePort,
			@Named("databaseName") String databaseName,
			@Named("databasePrefix") String databasePrefix, 
			@Named("databaseUser") String databaseUser, 
			@Named("databasePassword") String databasePassword, 
			@Named("StopWords") Set<String> stopWords,
			@Named("selectedSources") Set<String> selectedSources,
			IGlossary glossary, 
			@Named("WordTokenizer") ITokenizer tokenizer, 
			@Named("parentTagProvider") ParentTagProvider parentTagProvider, 
			@Named("SrcDirectory") String srcDirectory) throws Exception {
		super(temporaryPath, markupMode, databaseHost, databasePort, databaseName,
				databasePrefix, databaseUser, databasePassword, stopWords, selectedSources, glossary, tokenizer, parentTagProvider, 
				srcDirectory);
	}
	
	@Override
	public void learn(List<Treatment> treatments, String glossaryTable) { 
		int i = 0;
		for(Treatment treatment : treatments) {
			String prefix = intToString(i++, Math.max(String.valueOf(treatments.size()).length(), 3));
			fileTreatments.put(prefix, treatment);
		}
	}
}
