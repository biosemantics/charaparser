package semanticMarkup;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Guice config file for fna dataset specific parameters
 * @author rodenhausen
 */
public class FNAv19Config extends RunConfig {

	public FNAv19Config() {
		this.setEvaluationDataPath("evaluationData" + File.separator + "FNAV19_AnsKey_CharaParser_Evaluation");
		this.setDatabaseTablePrefix("type2");
		this.setDatabaseGlossaryTable("fnaglossaryfixed");
		this.setGlossaryFile("resources" + File.separator + "fnaglossaryfixed.csv");
		this.setNormalizer(FNAv19Normalizer.class);
	}
}