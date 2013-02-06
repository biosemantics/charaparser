package semanticMarkup.log;

import java.util.HashMap;

public class Logger {
	
	private static HashMap<Class<?>, Logger> loggers = new HashMap<Class<?>, Logger>();
	
	public static Logger getLogger(Class<?> clazz) {
		if(!loggers.containsKey(clazz)) 
			loggers.put(clazz, new Logger(clazz));
		return loggers.get(clazz);
	}
	
	private Class<?> clazz;
	private org.slf4j.Logger logger;
	
	public Logger(Class<?> clazz) {
		this.clazz = clazz;
		this.logger = org.slf4j.LoggerFactory.getLogger(clazz);
	}
	
	public void trace(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.trace(message.toString());
	}
	
	public void debug(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.debug(message.toString());
	}
	
	public void info(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.info(message.toString());
	}
	
	public void warn(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.warn(message.toString());
	}
	
	public void error(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.error(message.toString());
	}
	
	public void fatal(Object message) {
		//System.out.println(clazz.getSimpleName() + ": " + message.toString());
		logger.error(message.toString());
	}
}
