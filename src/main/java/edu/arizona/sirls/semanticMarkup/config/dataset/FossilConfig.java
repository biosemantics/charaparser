package edu.arizona.sirls.semanticMarkup.config.dataset;

import java.io.File;

import edu.arizona.sirls.semanticMarkup.config.RunConfig;
import edu.arizona.sirls.semanticMarkup.io.InputStreamCreator;
import edu.arizona.sirls.semanticMarkup.know.Glossary;
import edu.arizona.sirls.semanticMarkup.ling.normalize.lib.TreatisehNormalizer;


/**
 * Guice config file for treatise dataset specific parameters
 * @author rodenhausen
 */
public class FossilConfig extends RunConfig {
		
	public FossilConfig() {
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("treatise");
		this.setDatabaseGlossaryTable("treatisehglossaryfixed");
		this.setGlossaryFile("/edu/arizona/sirls/semanticMarkup/know/glossaries/treatisehglossaryfixed.csv");
		
		// IO
		this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "TIP_AnsKey_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Fossil.toString());
		this.setNormalizer(TreatisehNormalizer.class);
		
		// MISC
		
	}
}
