package edu.arizona.biosemantics.semanticmarkup.config.taxongroup;

import java.io.IOException;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;

public class ColeopteraConfig extends RunConfig {

	public ColeopteraConfig() throws IOException {
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("foc_v10_jing");
		//this.setDatabaseGlossaryTable("antglossaryfixed");
		//this.setGlossaryFile("resources" + File.separator + "antglossaryfixed.csv");
		
		// IO
		//this.setDescriptionReaderInputDirectory("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		
		// PROCESSING 
		this.setTaxonGroup(TaxonGroup.COLEOPTERA);
		//this.setNormalizer(FNAv19Normalizer.class);

		// MISC		
		
	}
	
}
