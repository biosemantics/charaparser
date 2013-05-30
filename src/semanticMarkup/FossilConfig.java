package semanticMarkup;

import java.io.File;

import semanticMarkup.know.Glossary;
import semanticMarkup.ling.normalize.lib.TreatisehNormalizer;

/**
 * Guice config file for treatise dataset specific parameters
 * @author rodenhausen
 */
public class FossilConfig extends RunConfig {
	
	public FossilConfig() {
		this.setStandardVolumeReaderSourcefiles("evaluationData" + File.separator + "TIP_AnsKey_CharaParser_Evaluation");
		//this.setDatabaseTablePrefix("treatise");
		this.setDatabaseGlossaryTable("treatisehglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "treatisehglossaryfixed.csv");
		this.setNormalizer(TreatisehNormalizer.class);	
		this.setGlossaryType(Glossary.Fossil.toString());
	}
}