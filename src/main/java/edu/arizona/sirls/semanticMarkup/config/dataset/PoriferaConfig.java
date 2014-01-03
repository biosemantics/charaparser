package edu.arizona.sirls.semanticMarkup.config.dataset;

import java.io.File;

import edu.arizona.sirls.semanticMarkup.config.RunConfig;
import edu.arizona.sirls.semanticMarkup.know.Glossary;
import edu.arizona.sirls.semanticMarkup.ling.normalize.lib.FNAv19Normalizer;


public class PoriferaConfig extends RunConfig {

	public PoriferaConfig() {
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		//this.setDatabaseGlossaryTable("antglossaryfixed");
		//this.setGlossaryFile("resources" + File.separator + "glossaries" + File.separator + "antglossaryfixed.csv");
		
		// IO
		//this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Porifera.toString());
		//this.setNormalizer(FNAv19Normalizer.class);

		// MISC
		
	}

}
