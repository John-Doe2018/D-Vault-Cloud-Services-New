package com.tranfode.util;

import java.util.List;

import org.json.simple.JSONArray;

public class ReadJsonUtil {

	@SuppressWarnings("unchecked")
	public static boolean CheckBinderWithSameName(JSONArray oJsonArray, String bookName) {

		boolean isSameBookName = false;
		try {
			List<String> bookarray = oJsonArray;
			if (bookarray.contains(bookName)) {
				isSameBookName = true;
			}
		} catch (Exception e) {
		}
		return isSameBookName;
	}
}
