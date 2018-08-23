package com.tranfode.logger;

/**
 * @author BIKASH
 * @version 1.0
 */

public interface FILEITLogger {

	
	public void debug(final Object msg);

	

	public void info(final Object msg);

	
	public void warn(final Object msg);

	
	public void error(final Object msg);

	
	public void error(final String strMsg, final Exception e);

	
	public void fatal(final Object msg);

	
	public void fatal(final String strMsg, final Exception e);

	
	public void traceStart(String methodName, String message);


	public void traceEnd(String methodName, String message);

	
	public void eventTrace(String methodName, String message);

}
