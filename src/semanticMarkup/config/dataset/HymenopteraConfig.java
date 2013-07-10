package semanticMarkup.config.dataset;

import java.io.File;

import semanticMarkup.config.RunConfig;
import semanticMarkup.know.Glossary;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;
import semanticMarkup.ling.normalize.lib.TreatisehNormalizer;

public class HymenopteraConfig extends RunConfig {

	public HymenopteraConfig() {

		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setDatabaseGlossaryTable("antglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "glossaries" + File.separator + "antglossaryfixed.csv");
		
		// IO
		this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setGlossaryType(Glossary.Hymenoptera.toString());
		this.setNormalizer(FNAv19Normalizer.class);

		// MISC

	}
	
}
