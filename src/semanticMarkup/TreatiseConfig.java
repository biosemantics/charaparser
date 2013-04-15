package semanticMarkup;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;
import semanticMarkup.ling.normalize.lib.TreatisehNormalizer;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Guice config file for treatise dataset specific parameters
 * @author rodenhausen
 */
public class TreatiseConfig extends RunConfig {
	
	public TreatiseConfig() {
		this.setEvaluationDataPath("evaluationData" + File.separator + "TIP_AnsKey_CharaParser_Evaluation");
		this.setDatabaseTablePrefix("treatiseh");
		this.setDatabaseGlossaryTable("treatisehglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "treatisehglossaryfixed.csv");
		this.setNormalizer(TreatisehNormalizer.class);
	}
}