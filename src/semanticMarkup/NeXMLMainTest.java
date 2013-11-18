package semanticMarkup;

import java.util.LinkedList;
import java.util.List;

import semanticMarkup.config.dataset.PlantConfig;
import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.io.lib.MOXyBinderDescriptionReader;
import semanticMarkup.markupElement.description.ling.learn.lib.unsupervised.UnsupervisedClauseMarkup;
import semanticMarkup.run.IRun;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class NeXMLMainTest {

	public static void main(String[] args) throws Exception {
		NeXMLMainTest main = new NeXMLMainTest();
		main.run();
	}

	private void run() throws Exception {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory()); 
		//TreatiseConfig //FNAv19Config
		PlantConfig config = new PlantConfig();
		List<String> bindingFiles = new LinkedList<String>();
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model//baseBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model//neXMLBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticMarkup.markupElement.description.model.nexml//neXMLBindings.xml");
		config.setDescriptionReaderBindingsList(bindingFiles);
		config.setDescriptionReader(MOXyBinderDescriptionReader.class);
		config.setDescriptionReaderInputDirectory("input");
		config.setTerminologyLearner(UnsupervisedClauseMarkup.class);
		Injector injector = Guice.createInjector(config);
		IRun run = injector.getInstance(IRun.class);
		
		log(LogLevel.INFO, "running " + run.getDescription() + "...");
		run.run();
	}	
	
}