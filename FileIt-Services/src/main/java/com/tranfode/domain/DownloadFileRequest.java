package com.tranfode.domain;

import java.util.List;

public class DownloadFileRequest {

	String bookName;
	List<String> fileName;

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public List<String> getFileName() {
		return fileName;
	}

	public void setFileName(List<String> fileName) {
		this.fileName = fileName;
	}

}
