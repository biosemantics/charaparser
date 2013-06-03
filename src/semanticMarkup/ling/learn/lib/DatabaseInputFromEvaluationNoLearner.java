package semanticMarkup.ling.learn.lib;

import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import semanticMarkup.core.Treatment;
import semanticMarkup.io.input.lib.db.ParentTagProvider;
import semanticMarkup.know.IGlossary;
import semanticMarkup.ling.transform.ITokenizer;

public class DatabaseInputFromEvaluationNoLearner extends PerlTerminologyLearner {

	@Inject
	public DatabaseInputFromEvaluationNoLearner(@Named("Run_TemporaryPath") String temporaryPath,
			@Named("markupMode") String markupMode,
			@Named("databaseHost") String databaseHost,
			@Named("databasePort") String databasePort,
			@Named("databaseName") String databaseName,
			@Named("GlossaryTable") String glossaryTable,
			@Named("databasePrefix") String databasePrefix, 
			@Named("databaseUser") String databaseUser, 
			@Named("databasePassword") String databasePassword, 
			@Named("StopWords") Set<String> stopWords,
			@Named("selectedSources") Set<String> selectedSources,
			IGlossary glossary, 
			@Named("WordTokenizer") ITokenizer tokenizer, 
			@Named("parentTagProvider") ParentTagProvider parentTagProvider) throws Exception {
		super(temporaryPath, markupMode, databaseHost, databasePort, databaseName,
				glossaryTable, databasePrefix, databaseUser, databasePassword,
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
	public void learn(List<Treatment> treatments) { 
		int i = 0;
		for(Treatment treatment : treatments) {
			//String prefix = intToString(i++, Math.max(String.valueOf(treatments.size()).length(), 3));
			fileTreatments.put(treatment.getName(), treatment);
		}
	}
}
