package com.tranfode.util;

public class FileItException extends Exception {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	// Error id
	private String errorId;

	// errorMessage parameter to store the error messages.
	private String errorMessage = "";

	// Constructor
	public FileItException(String errorMessage) {
		super(errorMessage);
		this.errorMessage = errorMessage;
	}

	// Constructor
	public FileItException(String errorId, String errorMessage) {
		super(errorMessage);
		this.errorId = errorId;
		this.errorMessage = errorMessage;
	}

	// Method called to retrieve the error messages.
	public String getErrorMessage() {
		return errorMessage;
	}

	public String getErrorId() {
		return errorId;
	}
}