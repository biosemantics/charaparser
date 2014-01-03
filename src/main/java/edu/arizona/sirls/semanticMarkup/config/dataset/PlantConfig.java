package edu.arizona.sirls.semanticMarkup.config.dataset;

import java.io.File;

import edu.arizona.sirls.semanticMarkup.config.RunConfig;
import edu.arizona.sirls.semanticMarkup.know.Glossary;
import edu.arizona.sirls.semanticMarkup.ling.normalize.lib.FNAv19Normalizer;


/**
 * Guice config file for fna dataset specific parameters
 * @author rodenhausen
 */
public class PlantConfig extends RunConfig {

	public PlantConfig() {
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setDatabaseGlossaryTable("fnaglossaryfixed");
		this.setGlossaryFile("/edu/arizona/sirls/semanticMarkup/know/glossaries/fnaglossaryfixed.csv");
		
		// IO
		this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "FNAV19_AnsKey_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Plant.toString());
		this.setNormalizer(FNAv19Normalizer.class);

		// MISC

	}
}
