package semanticMarkup;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import semanticMarkup.ling.normalize.INormalizer;
import semanticMarkup.ling.normalize.lib.TreatisehNormalizer;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Guice config file for treatise dataset specific parameters
 * @author rodenhausen
 */
public class TreatiseConfig extends RunConfig {

	@Override
	public void configure() {
		super.configure();
		
		bind(String.class).annotatedWith(Names.named("GuiceModuleFile")).toInstance("src//semanticMarkup//TreatiseConfig.java");

		String evaluationDataPath = "resources//evaluationData//TIP_AnsKey_CharaParser_Evaluation";
		bind(String.class).annotatedWith(Names.named("StandardVolumeReader_Sourcefiles")).toInstance(evaluationDataPath);

		bind(new TypeLiteral<Set<String>>() {}).annotatedWith(Names.named("selectedSources")).toInstance(getSelectedSources(evaluationDataPath));
		
		bind(String.class).annotatedWith(Names.named("databasePrefix")).toInstance("treatiseh"); 

		bind(String.class).annotatedWith(Names.named("CSVGlossary_filePath")).toInstance("resources//treatisehglossaryfixed.csv"); 
		
		bind(INormalizer.class).to(TreatisehNormalizer.class); //FNAv19Normalizer, TreatisehNormalizer
	}
	
	protected HashSet<String> getSelectedSources(String evaluationDataPath) {
		HashSet<String> result = new HashSet<String>();

		String file;
		File folder = new File(evaluationDataPath);
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					file = listOfFiles[i].getName();
					file = file.replace(".xml", "");
					// if(file.startsWith("175") || file.startsWith("174"))
					// if(file.equals("346.txt-15"))
					// if(file.equals("349.txt-1"))
					// if(file.equals("369.txt-11"))
					// if(file.equals("177.txt-2"))
					// if(file.equals("108.txt-9"))
					// if(file.equals("788.txt-3"))
					// if(file.equals("203.txt-2"))
					// if(file.equals("131.txt-5"))
					// if(file.equals("212.txt-4"))
					// if(file.equals("118.txt-1"))
					// if(file.equals("359.txt-10"))
					// if(file.equals("346.txt-15"))
					// if(file.equals("120.txt-1"))
					// if(file.equals("163.txt-7"))
					// if(file.equals("544.txt-6"))
					// if(file.equals("51.txt-13"))
					// if(file.equals("160.txt-0"))
					// if(file.equals("148.txt-12"))
					result.add(file);
					// break; //TODO remove. only for test
				}
			}
		}

		return result;
	}
}