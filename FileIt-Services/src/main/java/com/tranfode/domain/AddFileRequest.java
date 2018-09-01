package com.tranfode.domain;

import java.util.List;

public class AddFileRequest {
	String bookName;
	
	String classifcationName;
	
	public String getClassifcationName() {
		return classifcationName;
	}

	public void setClassifcationName(String classifcationName) {
		this.classifcationName = classifcationName;
	}

	List<BookRequests> oBookRequests;

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public List<BookRequests> getoBookRequests() {
		return oBookRequests;
	}

	public void setoBookRequests(List<BookRequests> oBookRequests) {
		this.oBookRequests = oBookRequests;
	}

}
