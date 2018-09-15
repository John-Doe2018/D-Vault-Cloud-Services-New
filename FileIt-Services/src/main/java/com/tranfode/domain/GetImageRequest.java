package com.tranfode.domain;

import java.util.List;

public class GetImageRequest {
	private String bookName;
	private List<Integer> rangeList;
	private String classification;

	public List<Integer> getRangeList() {
		return rangeList;
	}

	public void setRangeList(List<Integer> rangeList) {
		this.rangeList = rangeList;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

}
