package semanticMarkup.log;

public aspect LogInjectionAspect {
	
	public void ILoggable.log(LogLevel logLevel, Object message) {
		Logger logger = Logger.getLogger(this.getClass());
		switch(logLevel) {
		case TRACE:
			logger.trace(message);
			break;
		case DEBUG:
			logger.debug(message);
			break;
		case INFO:
			logger.info(message);
			break;
		case ERROR:
			logger.error(message);
			break;
		case FATAL:
			logger.fatal(message);
			break;
		case WARN:
			logger.warn(message);
			break;
		}
	}
	
	declare parents : semanticMarkup..* implements ILoggable;
}