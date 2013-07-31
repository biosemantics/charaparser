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

public class DatabaseInputFromEvaluationNoLearner extends PerlTerminologyLearner {

	@Inject
	public DatabaseInputFromEvaluationNoLearner(@Named("Run_TemporaryDirectory") String temporaryPath,
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
			@Named("ParentTagProvider")ParentTagProvider parentTagProvider) throws Exception {
		super(temporaryPath, markupMode, databaseHost, databasePort, databaseName,
				databasePrefix, databaseUser, databasePassword,
				stopWords, selectedSources, glossary, tokenizer, parentTagProvider);
	}
	
	@Override
	protected String getTreatmentId(String sourceString) {
		//String[] sourceParts = sourceString.split("\\.");
		//return sourceParts[0];
		return sourceString;
	}

	@Override
	protected String getSource(String sourceString) {
		//String[] sourceParts = sourceString.split("\\.");
		//return sourceParts[0] + "." + sourceParts[1];
		return sourceString;
	}
	
	@Override
	public void learn(List<AbstractDescriptionsFile> descriptionsFiles, String glossaryTable) { 
		int i = 0;
		for(AbstractDescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				//String prefix = intToString(i++, Math.max(String.valueOf(treatments.size()).length(), 3));
				fileTreatments.put(descriptionsFile.getName(), description);
			}
		}
	}
}
