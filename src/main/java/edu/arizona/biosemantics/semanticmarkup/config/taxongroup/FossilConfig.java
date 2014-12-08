package edu.arizona.biosemantics.semanticmarkup.config.taxongroup;

import java.io.File;
import java.io.IOException;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.semanticmarkup.config.RunConfig;
import edu.arizona.biosemantics.semanticmarkup.io.InputStreamCreator;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.lib.TreatisehNormalizer;


/**
 * Guice config file for treatise dataset specific parameters
 * @author rodenhausen
 */
public class FossilConfig extends RunConfig {
		
	public FossilConfig() throws IOException {
		
		// ENVIRONMENTAL 
		//this.setDatabaseTablePrefix("treatise");
		this.setDatabaseGlossaryTable("treatisehglossaryfixed");
		this.setGlossaryFile("edu/arizona/biosemantics/semanticmarkup/know/glossaries/treatisehglossaryfixed.csv");
		
		// IO
		this.setInputDirectory("evaluationData" + File.separator + "TIP_AnsKey_CharaParser_Evaluation");
		
		this.setGlossaryFile("edu/arizona/biosemantics/semanticmarkup/know/glossaries/treatisehglossaryfixed.csv");
		
		// PROCESSING 
		this.setTaxonGroup(TaxonGroup.FOSSIL);
		this.setNormalizer(TreatisehNormalizer.class);
		
		// MISC
		
	}
}
