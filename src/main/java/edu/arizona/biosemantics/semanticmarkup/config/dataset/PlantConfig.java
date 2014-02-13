package edu.arizona.biosemantics.semanticmarkup.config.dataset;

import java.io.File;
import java.io.IOException;

import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.semanticmarkup.know.Glossary;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.FNAv19Normalizer;


/**
 * Guice config file for fna dataset specific parameters
 * @author rodenhausen
 */
public class PlantConfig extends RunConfig {

	public PlantConfig() throws IOException {
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setDatabaseGlossaryTable("fnaglossaryfixed");
		this.setGlossaryFile("edu/arizona/biosemantics/semanticmarkup/know/glossaries/fnaglossaryfixed.csv");
		
		// IO
		this.setInputDirectory("evaluationData" + File.separator + "FNAV19_AnsKey_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Plant.toString());
		this.setNormalizer(FNAv19Normalizer.class);

		// MISC

	}
}
