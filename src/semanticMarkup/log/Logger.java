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
	public void trace(String message) {
		logger.trace(message);
	}
	
	/**
	 * @param message
	 * @param throwable
	 */
	public void trace(String message, Throwable throwable) {
		logger.trace(message, throwable);
	}
	
	/**
	 * @param message to display
	 */
	public void debug(String message) {
		logger.debug(message);
	}
	
	/**
	 * @param message
	 * @param throwable
	 */
	public void debug(String message, Throwable throwable) {
		logger.debug(message, throwable);
	}
	
	/**
	 * @param message to display
	 */
	public void info(String message) {
		logger.info(message);
	}
	
	/**
	 * @param message
	 * @param throwable
	 */
	public void info(String message, Throwable throwable) {
		logger.info(message, throwable);
	}
	
	/**
	 * @param message to display
	 */
	public void warn(String message) {
		logger.warn(message);
	}
	
	/**
	 * @param message
	 * @param throwable
	 */
	public void warn(String message, Throwable throwable) {
		logger.warn(message);
	}
	
	/**
	 * @param message to display
	 */
	public void error(String message) {
		logger.error(message);
	}
	
	/**
	 * @param message
	 * @param throwable
	 */
	public void error(String message, Throwable throwable) {
		logger.error(message);
	}
	
	/**
	 * @param message to display
	 */
	public void fatal(String message) {
		logger.error(message);
	}
	
	/**
	 * @param message
	 * @param throwable
	 */
	public void fatal(String message, Throwable throwable) {
		logger.error(message, throwable);
	}
}
