package semanticMarkup.config.dataset;

import java.io.File;

import semanticMarkup.config.RunConfig;
import semanticMarkup.know.Glossary;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;

public class AlgaeConfig extends RunConfig {

	public AlgaeConfig() {
		//this.setStandardVolumeReaderSourcefiles("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		//this.setDatabaseTablePrefix("foc_v10_jing");
		//this.setDatabaseGlossaryTable("antglossaryfixed");
		//this.setGlossaryFile(getResourcesDirectory() + File.separator + "antglossaryfixed.csv");
		//this.setNormalizer(FNAv19Normalizer.class);
		this.setGlossaryType(Glossary.Algae.toString());
	}

}
