package pl.edu.pjwstk.mteam.pubsub.logging;


public class Logger {
	private org.apache.log4j.Logger logger;
	
	private Logger(String loggerName){ 
		logger = org.apache.log4j.Logger.getLogger(loggerName);
	}

	public void trace(Object msg){ 
		logger.trace(msg);
	}
	
	public void debug(Object msg){ 
		logger.debug(msg);
	}
	
	public void info(Object msg){ 
		logger.info(msg);
	}
	
	public void warn(Object msg){ 
		logger.warn(msg);
	}
	
	public void error(Object msg){ 
		logger.error(msg);
	}
	
	public void fatal(Object msg){
		logger.fatal(msg);
	}
        public void fatal(Object msg, Exception e){
            logger.fatal(msg, e);
        }

	public static Logger getLogger(String loggerName){ 
		return new Logger(loggerName);
	}

}
