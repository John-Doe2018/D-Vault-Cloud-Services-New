package com.tranfode.domain;

import org.json.simple.JSONObject;

public class BookClassification {

	boolean isClassification;
	
	JSONObject jsonArray;

	public JSONObject getJsonArray() {
		return jsonArray;
	}

	public void setJsonArray(JSONObject jsonArray) {
		this.jsonArray = jsonArray;
	}

	public boolean isClassification() {
		return isClassification;
	}

	public void setClassification(boolean isClassification) {
		this.isClassification = isClassification;
	}

	
	
}
