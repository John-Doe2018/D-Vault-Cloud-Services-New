package com.tranfode.logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tranfode.logger.util.LoggerConstant;

/**
 * The Class FILEITLoggerSLF4J is SLF4J implementation class.
 * 
 * @author BIKASH
 * @version 1.0
 */

public final class FILEITLoggerSLF4J implements FILEITLogger {

	/** The map. */
	private static Map<String, Logger> map = new HashMap<String, Logger>();

	/** The cl. */
	private Class<?> cl;

	/** The formatted cl name. */
	private String formattedClName;

	public FILEITLoggerSLF4J(String loggerName, Class<?> clazz) {
		Logger oLogger = map.get(loggerName);
		if (null == oLogger) {
			oLogger = LoggerFactory.getLogger(loggerName);
			map.put(loggerName, oLogger);
		}
		this.logger = oLogger;
		this.cl = clazz;
	}

	/** The logger. */
	private Logger logger;

	static {
		URL urlObj = FILEITLoggerSLF4J.class.getClassLoader().getResource(LoggerConstant.LOG4J_CONFIG_FILE);
		if (null != urlObj) {
			DOMConfigurator.configure(urlObj);
		}
	}

	/**
	 * Debug.
	 * 
	 * @param msg
	 *            the msg
	 */
	public void debug(final Object msg) {
		logger.debug("{}{}", getFormattedClassName(), msg);
	}

	/**
	 * Info.
	 * 
	 * @param msg
	 *            the msg
	 */
	public void info(final Object msg) {
		logger.info("{}{}", getFormattedClassName(), msg);
	}

	/**
	 * Warn.
	 * 
	 * @param msg
	 *            the msg
	 */
	public void warn(final Object msg) {
		logger.warn("{}{}", getFormattedClassName(), msg);
	}

	/**
	 * Error.
	 * 
	 * @param msg
	 *            the msg
	 */
	public void error(final Object msg) {

		if (msg instanceof Exception) {
			Exception e = (Exception) msg;

			logger.error("{}{}{}{}", getFormattedClassName(), msg, getFormattedMessage((Exception) msg),
					e.getStackTrace());

		} else {
			logger.error("{}{}", getFormattedClassName(), msg);
		}

	}

	/**
	 * Error.
	 * 
	 * @param strMsg
	 *            the str msg
	 * @param e
	 *            the e
	 */
	public void error(final String strMsg, final Exception e) {
		logger.error("{}{}{}", getFormattedClassName(), strMsg, getFormattedMessage(e));
	}

	/**
	 * Fatal.
	 * 
	 * @param msg
	 *            the msg
	 */
	public void fatal(final Object msg) {
		logger.error("{}{}", getFormattedClassName(), msg);
	}

	/**
	 * Fatal.
	 * 
	 * @param strMsg
	 *            the str msg
	 * @param e
	 *            the e
	 */
	public void fatal(final String strMsg, final Exception e) {
		logger.error("{}{}{}", getFormattedClassName(), strMsg, getFormattedMessage(e));
	}

	/**
	 * Trace_start.
	 * 
	 * @param methodName
	 *            the method name
	 * @param message
	 *            the message
	 */
	public void traceStart(String methodName, String message) {
		logger.debug("Trace Start : {}{}{}", getFormattedClassName(), methodName, message);
	}

	/**
	 * Trace_end.
	 * 
	 * @param methodName
	 *            the method name
	 * @param message
	 *            the message
	 */
	public void traceEnd(String methodName, String message) {
		logger.debug("Trace End : {}{}{}", getFormattedClassName(), methodName, message);

	}

	/**
	 * Event trace.
	 * 
	 * @param methodName
	 *            the method name
	 * @param message
	 *            the message
	 */
	public void eventTrace(String methodName, String message) {
		logger.debug("Event Trace : {}{}{}", getFormattedClassName(), methodName, message);

	}

	/**
	 * used to padding a string.
	 * 
	 * @param s
	 *            string that has to be padded
	 * @param n
	 *            no of digits to pad
	 * @return padded string
	 */
	private static String padRight(String p, String s) {
		return String.format(p, s);
	}

	/**
	 * used to format the class name with <code>Class</code> object.
	 * 
	 * @param cl
	 *            class to which the name has to be formatted
	 * @return formated class name
	 */
	public static String getFormattedClassName(Class<?> cl) {
		String className = cl.getCanonicalName();
		if (className.length() < LoggerConstant.MAX_LENGTH) {
			StringBuffer padding = new StringBuffer("%1$-").append(LoggerConstant.MAX_LENGTH).append('s');
			className = padRight(padding.toString(), className);
		}
		return className + LoggerConstant.COLON_SEPARATOR;
	}

	/**
	 * returns formatted class name from the logger object for which its logging.
	 * 
	 * @return formatted class name
	 */
	public String getFormattedClassName() {
		if (null != formattedClName) {
			return formattedClName;
		}
		if (null != cl) {
			String className = cl.getCanonicalName();
			if (className.length() < LoggerConstant.MAX_LENGTH) {
				StringBuffer padding = new StringBuffer("%1$-").append(LoggerConstant.MAX_LENGTH).append('s');
				className = padRight(padding.toString(), className);
			}
			formattedClName = className + LoggerConstant.COLON_SEPARATOR;
			return formattedClName;
		}
		return "";
	}

	/**
	 * Gets the formatted message.
	 * 
	 * @param excepObj
	 *            Formats the exception object
	 * @return the human readable exception message
	 */
	public static String getFormattedMessage(final Exception excepObj) {
		StackTraceElement[] st = null;
		st = excepObj.getStackTrace();
		int piLength = st.length;
		boolean pbFlag = false;
		StringBuffer sbExceptionMsg = new StringBuffer();
		for (int i = 0; i < piLength; i++) {
			pbFlag = check(st[i]);
			// pbFlag = true;
			if (pbFlag) {
				sbExceptionMsg.append("\r\n");
				sbExceptionMsg.append(
						"#####################################################################################################\r\n");
				sbExceptionMsg.append("POS Code Related Exceptions \r\n");
				sbExceptionMsg.append("Exception Occured \r\n");
				sbExceptionMsg.append("Exception Cause   --> " + excepObj.toString() + "\r\n");
				sbExceptionMsg.append("File   Name\t\t\t :	" + st[i].getFileName() + "\r\n");
				sbExceptionMsg.append("Class  Name\t\t\t :	" + st[i].getClassName() + "\r\n");
				sbExceptionMsg.append("Method Name\t\t\t :	" + st[i].getMethodName() + "\r\n");
				sbExceptionMsg.append("Line   No  \t\t\t :	" + st[i].getLineNumber() + "\r\n");

				sbExceptionMsg.append("\r\n");
				sbExceptionMsg.append(
						"#####################################################################################################\r\n");
				break;
			} else {
				// Suppose is it is a FileSecurity Exception this will help
				sbExceptionMsg.append("\r\n");
				sbExceptionMsg.append("Exception Cause  --> " + st[i].getClassName() + "(" + excepObj.toString() + ")");
			}
		}

		Throwable throwObj = excepObj.getCause();
		if (null != throwObj) {
			boolean pbApplCauseFlag = false;
			StringBuffer sbCauseBuffer = new StringBuffer(400);

			StackTraceElement[] stCauseElement = throwObj.getStackTrace();
			int piCauseLength = stCauseElement.length;

			for (int count = 0; count < piCauseLength; count++) {
				pbApplCauseFlag = check(stCauseElement[count]);
				if (pbApplCauseFlag) {
					sbCauseBuffer.append("\r\n");
					sbCauseBuffer.append(
							"#####################################################################################################\r\n");
					sbCauseBuffer.append("Caused Exception  \r\n");
					sbCauseBuffer.append("Exception Cause   --> " + throwObj.toString() + "\r\n");
					sbCauseBuffer.append("File   Name\t\t\t :  " + stCauseElement[count].getFileName() + "\r\n");
					sbCauseBuffer.append("Class  Name\t\t\t :  " + stCauseElement[count].getClassName() + "\r\n");
					sbCauseBuffer.append("Method Name\t\t\t :  " + stCauseElement[count].getMethodName() + "\r\n");
					sbCauseBuffer.append("Line   No  \t\t\t :  " + stCauseElement[count].getLineNumber() + "\r\n");
					sbCauseBuffer.append("\r\n");
					sbCauseBuffer.append(
							"#####################################################################################################\r\n");
					break;
				}
			}
			sbExceptionMsg.append(sbCauseBuffer.toString());
		}

		return sbExceptionMsg.toString();
	}

	private static final int DELIM = -1;

	/**
	 * Check.
	 * 
	 * @param stackTraceObj
	 *            stacktrace element
	 * @return boolean value
	 */
	private static boolean check(final StackTraceElement stackTraceObj) {
		return (stackTraceObj.getClassName().indexOf("com.tranfode") != DELIM
				|| stackTraceObj.getClassName().indexOf("_jasper._html_0002djsp") != DELIM);
	}

}
