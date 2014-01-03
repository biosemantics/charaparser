package edu.arizona.sirls.semanticMarkup.config.dataset;

import java.io.File;

import edu.arizona.sirls.semanticMarkup.config.RunConfig;
import edu.arizona.sirls.semanticMarkup.know.Glossary;
import edu.arizona.sirls.semanticMarkup.ling.normalize.lib.FNAv19Normalizer;
import edu.arizona.sirls.semanticMarkup.ling.normalize.lib.TreatisehNormalizer;


public class HymenopteraConfig extends RunConfig {

	public HymenopteraConfig() {

		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setDatabaseGlossaryTable("antglossaryfixed");
		this.setGlossaryFile("/edu/arizona/sirls/semanticMarkup/know/glossaries/antglossaryfixed.csv");
		
		// IO
		this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Hymenoptera.toString());
		this.setNormalizer(FNAv19Normalizer.class);

		// MISC

	}
	
}
