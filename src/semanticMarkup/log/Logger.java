package semanticMarkup.log;

import java.util.HashMap;

/**
 * A Logger loggs messages of the various debug levels using slf4j
 * @author rodenhausen
 */
public class Logger {
	
	private static HashMap<Class<?>, Logger> loggers = new HashMap<Class<?>, Logger>();
	
	/**
	 * Returns the 'singleton' Logger per class
	 * @param clazz
	 * @return the 'singleton' Logger
	 */
	public static Logger getLogger(Class<?> clazz) {
		if(!loggers.containsKey(clazz)) 
			loggers.put(clazz, new Logger(clazz));
		return loggers.get(clazz);
	}
	
	private Class<?> clazz;
	private org.slf4j.Logger logger;
	
	/**
	 * @param clazz
	 */
	private Logger(Class<?> clazz) {
		this.clazz = clazz;
		this.logger = org.slf4j.LoggerFactory.getLogger(clazz);
	}
	
	/**
	 * @param message to display
	 */
	public void trace(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.trace(message.toString());
	}
	
	/**
	 * @param message to display
	 */
	public void debug(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.debug(message.toString());
	}
	
	/**
	 * @param message to display
	 */
	public void info(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.info(message.toString());
	}
	
	/**
	 * @param message to display
	 */
	public void warn(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.warn(message.toString());
	}
	
	/**
	 * @param message to display
	 */
	public void error(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.error(message.toString());
	}
	
	/**
	 * @param message to display
	 */
	public void fatal(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.error(message.toString());
	}
}
