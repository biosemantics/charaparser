package semanticMarkup;

import java.io.File;

import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;

/**
 * Guice config file for fna dataset specific parameters
 * @author rodenhausen
 */
public class FNAv19Config extends RunConfig {

	public FNAv19Config() {
		this.setStandardVolumeReaderSourcefiles("evaluationData" + File.separator + "FNAV19_AnsKey_CharaParser_Evaluation");
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setPermanentGlossaryPrefixAtWebService("fna");
		this.setDatabaseGlossaryTable("fnaglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "fnaglossaryfixed_foc.csv");
		this.setNormalizer(FNAv19Normalizer.class);
	}
}