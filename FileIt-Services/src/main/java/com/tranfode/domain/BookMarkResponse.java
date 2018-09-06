package com.tranfode.domain;

import java.util.List;

public class BookMarkResponse extends Response {

	public String errorMessage;

	public BookMarkDetails bookmarkDetails;

	public List<BookMarkDetails> bookmarkDetailsList;

	public List<BookMarkDetails> getBookmarkDetailsList() {
		return bookmarkDetailsList;
	}

	public void setBookmarkDetailsList(List<BookMarkDetails> bookmarkDetailsList) {
		this.bookmarkDetailsList = bookmarkDetailsList;
	}

	public BookMarkDetails getBookmarkDetails() {
		return bookmarkDetails;
	}

	public void setBookmarkDetails(BookMarkDetails bookmarkDetails) {
		this.bookmarkDetails = bookmarkDetails;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
