package semanticMarkup.config.dataset;

import java.io.File;

import semanticMarkup.config.RunConfig;
import semanticMarkup.know.Glossary;
import semanticMarkup.ling.normalize.lib.TreatisehNormalizer;

/**
 * Guice config file for treatise dataset specific parameters
 * @author rodenhausen
 */
public class FossilConfig extends RunConfig {
	
	public FossilConfig() {
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Fossil.toString());
		this.setNormalizer(TreatisehNormalizer.class);
		
		// IO
		this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "TIP_AnsKey_CharaParser_Evaluation");
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("treatise");
		this.setDatabaseGlossaryTable("treatisehglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "treatisehglossaryfixed.csv");
		
		// MISC
		
	}
}