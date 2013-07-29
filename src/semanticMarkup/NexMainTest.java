package semanticMarkup;

import java.io.File;

import semanticMarkup.config.dataset.PlantConfig;
import semanticMarkup.log.LogLevel;
import semanticMarkup.markupElement.description.io.lib.MOXyBinderDescriptionReader;
import semanticMarkup.run.IRun;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class NexMainTest {

	public static void main(String[] args) throws Exception {
		NexMainTest main = new NexMainTest();
		main.run();
	}

	private void run() throws Exception {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory()); 
		//TreatiseConfig //FNAv19Config
		PlantConfig config = new PlantConfig();
		config.setDescriptionReaderBindings("resources" + File.separator + "io" + File.separator + "bindings" + File.separator + "nexBindings.xml");
		config.setDescriptionReader(MOXyBinderDescriptionReader.class);
		config.setDescriptionReaderInputDirectory("input");
		Injector injector = Guice.createInjector(config);
		IRun run = injector.getInstance(IRun.class);
		
		log(LogLevel.INFO, "running " + run.getDescription() + "...");
		run.run();
	}	
	
}
