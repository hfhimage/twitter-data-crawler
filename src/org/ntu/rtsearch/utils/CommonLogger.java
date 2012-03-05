package org.ntu.rtsearch.utils;

import org.apache.log4j.Logger;

public class CommonLogger {
	
	public static Logger logger = Logger.getLogger(CommonLogger.class.getName());
	
	private CommonLogger() {}
	
	public static void info(Object msg) {
		logger.info(msg);
	}
	
	public static void debug(Object msg) {
		logger.info(msg);
	}

}
