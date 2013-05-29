package semanticMarkup;

import java.io.File;

import semanticMarkup.know.Glossary;
import semanticMarkup.ling.normalize.lib.FNAv19Normalizer;

public class PoriferaConfig extends RunConfig {

	public PoriferaConfig() {
		//this.setStandardVolumeReaderSourcefiles("evaluationData" + File.separator + "Ant_CharaParser_Evaluation");
		//this.setDatabaseTablePrefix("foc_v10_jing");
		//this.setDatabaseGlossaryTable("antglossaryfixed");
		//this.setGlossaryFile("resources" + File.separator + "antglossaryfixed.csv");
		//this.setNormalizer(FNAv19Normalizer.class);
		this.setGlossaryType(Glossary.Porifera.toString());
	}

}