package edu.arizona.biosemantics.semanticmarkup;

import java.util.LinkedList;
import java.util.List;


import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.arizona.biosemantics.semanticmarkup.config.dataset.PlantConfig;
import edu.arizona.biosemantics.semanticmarkup.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.lib.MOXyBinderDescriptionReader;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.unsupervised.UnsupervisedClauseMarkup;
import edu.arizona.biosemantics.semanticmarkup.run.IRun;

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
		bindingFiles.add("resources//io//bindings//semanticmarkup.markupelement.description.model//baseBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticmarkup.markupelement.description.model//neXMLBindings.xml");
		bindingFiles.add("resources//io//bindings//semanticmarkup.markupelement.description.model.nexml//neXMLBindings.xml");
		config.setIODescriptionBindingsList(bindingFiles);
		config.setDescriptionReader(MOXyBinderDescriptionReader.class);
		config.setInputDirectory("input");
		config.setTerminologyLearner(UnsupervisedClauseMarkup.class);
		Injector injector = Guice.createInjector(config);
		IRun run = injector.getInstance(IRun.class);
		
		log(LogLevel.INFO, "running " + run.getDescription() + "...");
		run.run();
	}	
	
}
