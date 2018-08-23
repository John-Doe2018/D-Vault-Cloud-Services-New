package com.tranfode.domain;

public class DeleteFileRequest {

	String bookName;
	String fileName;
	boolean bookcreated;

	public boolean isBookcreated() {
		return bookcreated;
	}

	public void setBookcreated(boolean bookcreated) {
		this.bookcreated = bookcreated;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}