package edu.arizona.biosemantics.semanticmarkup;


import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.arizona.biosemantics.semanticmarkup.config.dataset.PlantConfig;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.run.IRun;

/**
 * Entry point into the processing of the charaparser framework
 * @author thomas rodenhausen
 */
public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.run();
	}

	private void run() throws Exception {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory()); 
		//TreatiseConfig //FNAv19Config
		Injector injector = Guice.createInjector(new PlantConfig());
		IRun run = injector.getInstance(IRun.class);
		
		log(LogLevel.INFO, "running " + run.getDescription() + "...");
		run.run();
	}	
}