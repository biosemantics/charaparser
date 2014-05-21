package edu.arizona.biosemantics.semanticmarkup.config.dataset;

import java.io.File;
import java.io.IOException;

import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.semanticmarkup.know.Glossary;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.FNAv19Normalizer;


public class PoriferaConfig extends RunConfig {

	public PoriferaConfig() throws IOException {
		
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
