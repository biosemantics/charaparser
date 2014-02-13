package edu.arizona.biosemantics.semanticmarkup.config.dataset;

import java.io.File;
import java.io.IOException;

import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.semanticmarkup.io.InputStreamCreator;
import edu.arizona.biosemantics.semanticmarkup.know.Glossary;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.TreatisehNormalizer;


/**
 * Guice config file for treatise dataset specific parameters
 * @author rodenhausen
 */
public class FossilConfig extends RunConfig {
		
	public FossilConfig() throws IOException {
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("treatise");
		this.setDatabaseGlossaryTable("treatisehglossaryfixed");
		this.setGlossaryFile("edu/arizona/biosemantics/semanticmarkup/know/glossaries/treatisehglossaryfixed.csv");
		
		// IO
		this.setInputDirectory("evaluationData" + File.separator + "TIP_AnsKey_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Fossil.toString());
		this.setNormalizer(TreatisehNormalizer.class);
		
		// MISC
		
	}
}
