package semanticMarkup;

import semanticMarkup.log.LogLevel;
import semanticMarkup.run.IRun;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
		//TreatiseConfig //FNAv19Config
		Injector injector = Guice.createInjector(new FNAv19Config());
		IRun run = injector.getInstance(IRun.class);
		
		log(LogLevel.INFO, "running " + run.getDescription() + "...");
		run.run();
	}
	
}
