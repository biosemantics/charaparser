package semanticMarkup.ling.learn.lib;

import java.util.List;
import java.util.Set;

import semanticMarkup.io.input.lib.db.ParentTagProvider;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.transform.ITokenizer;
import semanticMarkup.markupElement.description.model.Description;
import semanticMarkup.markupElement.description.model.DescriptionsFile;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DatabaseInputFromEvaluationNoLearner extends PerlTerminologyLearner {

	@Inject
	public DatabaseInputFromEvaluationNoLearner(@Named("Run_TemporaryPath") String temporaryPath,
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
			@Named("parentTagProvider") ParentTagProvider parentTagProvider) throws Exception {
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
	public void learn(List<DescriptionsFile> descriptionsFiles, String glossaryTable) { 
		int i = 0;
		for(DescriptionsFile descriptionsFile : descriptionsFiles) {
			for(Description description : descriptionsFile.getDescriptions()) {
				//String prefix = intToString(i++, Math.max(String.valueOf(treatments.size()).length(), 3));
				fileTreatments.put(descriptionsFile.getName(), description);
			}
		}
	}
}
