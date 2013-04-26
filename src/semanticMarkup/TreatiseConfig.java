package semanticMarkup;

import java.io.File;

import semanticMarkup.ling.normalize.lib.TreatisehNormalizer;

/**
 * Guice config file for treatise dataset specific parameters
 * @author rodenhausen
 */
public class TreatiseConfig extends RunConfig {
	
	public TreatiseConfig() {
		this.setStandardVolumeReaderSourcefiles("evaluationData" + File.separator + "TIP_AnsKey_CharaParser_Evaluation");
		//this.setDatabaseTablePrefix("treatise");
		this.setPermanentGlossaryPrefix("treatise");
		this.setDatabaseGlossaryTable("treatisehglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "treatisehglossaryfixed.csv");
		this.setNormalizer(TreatisehNormalizer.class);	
	}
}