package edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib;

import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.semanticmarkup.db.ConnectionPool;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.ParentTagProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;

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
			@Named("ParentTagProvider") ParentTagProvider parentTagProvider,
			@Named("PerlDirectory") String perlDirectory,
			IInflector inflector, 
			ConnectionPool connectionPool) throws Exception {
		super(temporaryPath, markupMode, databaseHost, databasePort, databaseName,
				databasePrefix, databaseUser, databasePassword, stopWords, selectedSources, glossary, tokenizer, 
				parentTagProvider, perlDirectory, inflector, connectionPool);
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
