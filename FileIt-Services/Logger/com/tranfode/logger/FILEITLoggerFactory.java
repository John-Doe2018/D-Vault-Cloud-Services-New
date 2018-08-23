
package com.tranfode.logger;

import com.tranfode.logger.util.LoggerConstant;


/**
 * Singleton class serving as a factory for different logger implementations
 * 
 * @author BIKASH
 * @version 1.0
 */
public final class FILEITLoggerFactory {

	
	private static FILEITLoggerFactory instance;

	
	private FILEITLoggerFactory() {

	}

	/**
	 * Returns singleton instance of FILEITLoggerFactory.
	 * 
	 * @return FILEITLoggerFactory instance
	 */
	public static synchronized FILEITLoggerFactory getInstance() {
		if (null == instance) {
			instance = new FILEITLoggerFactory();
		}
		return instance;
	}

	/**
	 * Returns the specified logger instance.
	 * 
	 * @param loggerName
	 *            The logger name
	 * @param clazz
	 *            The logging class instance
	 * @return Logger instance
	 */
	public static FILEITLogger getLogger(String loggerName, Class<?> clazz) {
		return new FILEITLoggerSLF4J(loggerName, clazz);
	}

	/**
	 * Returns the specified logger instance.
	 * 
	 * @param clazz
	 *            The logging class instance
	 * @return Logger instance
	 */
	public static FILEITLogger getLogger(Class<?> clazz) {
		return new FILEITLoggerSLF4J(LoggerConstant.FILEIT_LOG, clazz);
	}
}
