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
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setDatabaseGlossaryTable("fnaglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "glossaries" + File.separator + "fnaglossaryfixed.csv");
		
		// IO
		this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "FNAV19_AnsKey_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Plant.toString());
		this.setNormalizer(FNAv19Normalizer.class);

		// MISC

	}
}