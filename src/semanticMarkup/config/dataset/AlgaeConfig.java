package semanticMarkup.config.dataset;

import java.io.File;

import semanticMarkup.config.RunConfig;
import semanticMarkup.know.Glossary;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;

public class AlgaeConfig extends RunConfig {

	public AlgaeConfig() {
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Algae.toString());
		//this.setNormalizer(FNAv19Normalizer.class);
		
		// IO
		//this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		//this.setDatabaseGlossaryTable("antglossaryfixed");
		//this.setGlossaryFile("resources" + File.separator + "antglossaryfixed.csv");

		// MISC		
		
	}

}
