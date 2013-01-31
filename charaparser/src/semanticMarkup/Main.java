package semanticMarkup;

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
		//TreatisehConfig //FNAv19Config
		Injector injector = Guice.createInjector(new FNAv19Config());
		IRun run = injector.getInstance(IRun.class);
		System.out.println("running " + run.getDescription() + "...");
		run.run();	
	}
	
}
