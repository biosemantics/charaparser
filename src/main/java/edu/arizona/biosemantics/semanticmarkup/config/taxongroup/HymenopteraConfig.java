package edu.arizona.biosemantics.semanticmarkup.config.taxongroup;

import java.io.File;
import java.io.IOException;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.FNAv19Normalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.TreatisehNormalizer;


public class HymenopteraConfig extends RunConfig {

	public HymenopteraConfig() throws IOException {

		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		this.setDatabaseGlossaryTable("antglossaryfixed");
		this.setGlossaryFile("edu/arizona/biosemantics/semanticmarkup/know/glossaries/antglossaryfixed.csv");
		
		// IO
		this.setInputDirectory("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setTaxonGroup(TaxonGroup.HYMENOPTERA);
		this.setNormalizer(FNAv19Normalizer.class);

		// MISC

	}
	
}
