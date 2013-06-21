package semanticMarkup.config.dataset;

import java.io.File;

import semanticMarkup.config.RunConfig;
import semanticMarkup.know.Glossary;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;

/**
 * Guice config file for fna dataset specific parameters
 * @author rodenhausen
 */
public class PlantConfig extends RunConfig {

	public PlantConfig() {
		this.setStandardVolumeReaderSourcefiles("evaluationData" + File.separator + "FNAV19_AnsKey_CharaParser_Evaluation");
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setDatabaseGlossaryTable("fnaglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "fnaglossaryfixed.csv");
		this.setNormalizer(FNAv19Normalizer.class);
		this.setGlossaryType(Glossary.Plant.toString());
	}
}