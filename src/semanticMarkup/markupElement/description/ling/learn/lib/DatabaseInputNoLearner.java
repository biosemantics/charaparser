package semanticMarkup.markupElement.description.ling.learn.lib;

import java.util.List;
import java.util.Set;

import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.markupElement.description.io.ParentTagProvider;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.AbstractDescriptionsFile;

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
	public DatabaseInputNoLearner(@Named("Run_TemporaryDirectory") String temporaryPath,
			@Named("MarkupMode") String markupMode,
			@Named("DatabaseHost") String databaseHost,
			@Named("DatabasePort") String databasePort,
			@Named("DatabaseName") String databaseName,
			@Named("DatabasePrefix") String databasePrefix, 
			@Named("DatabaseUser") String databaseUser, 
			@Named("DatabasePassword") String databasePassword, 
			@Named("StopWords") Set<String> stopWords,
			@Named("SelectedSources") Set<String> selectedSources,
			IGlossary glossary, 
			@Named("WordTokenizer") ITokenizer tokenizer, 
			@Named("ParentTagProvider") ParentTagProvider parentTagProvider, 
			@Named("SrcDirectory") String srcDirectory) throws Exception {
		super(temporaryPath, markupMode, databaseHost, databasePort, databaseName,
				databasePrefix, databaseUser, databasePassword, stopWords, selectedSources, glossary, tokenizer, parentTagProvider, srcDirectory);
	}
	
	@Override
	public void learn(List<AbstractDescriptionsFile> descriptionsFiles, String glossaryTable) { 
		int i = 0;
		int descriptionCount = 0;
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) 
			descriptionCount += descriptionsFile.getDescriptions().size();
		
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				String prefix = intToString(i++, Math.max(String.valueOf(descriptionCount).length(), 3));
				fileTreatments.put(prefix, description);
			}
		}
	}
}
