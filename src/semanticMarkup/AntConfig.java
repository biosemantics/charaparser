package semanticMarkup;

import java.io.File;

import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;

public class AntConfig extends RunConfig {

	public AntConfig() {
		this.setStandardVolumeReaderSourcefiles("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setPermanentGlossaryPrefixAtWebService("ant");
		this.setDatabaseGlossaryTable("antglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "antglossaryfixed.csv");
		this.setNormalizer(FNAv19Normalizer.class);
	}
	
}
