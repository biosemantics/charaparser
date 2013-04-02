package semanticMarkup.run;

/**
 * An IRun can be run
 * @author rodenhausen
 */
public interface IRun {
	
	/**
	 * Run
	 * @throws Exception
	 */
	public void run() throws Exception;
	
	/**
	 * @return a descriptive String of the IRun
	 */
	public String getDescription();
	
}
