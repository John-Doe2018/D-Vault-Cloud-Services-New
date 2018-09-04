package com.tranfode.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tranfode.Constants.BinderConstants;
import com.tranfode.Constants.CloudFileConstants;
import com.tranfode.domain.BookClassification;
import com.tranfode.domain.FileItContext;
import com.tranfode.util.CloudFilesOperationUtil;

public class WriteClassificationMap {
	static CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();

	/**
	 * @param classifiedBook
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public static void writeClassificationMap(Map<String, List<String>> classifiedBook) throws Exception {
		String classification = null;
		List<String> bookList = new ArrayList<String>();
		BookClassification bookClassification = new BookClassification();
		for (Map.Entry<String, List<String>> entry : classifiedBook.entrySet()) {
			classification = entry.getKey();
			bookList = classifiedBook.get(classification);
			bookClassification = checkifClassPresent(classification, bookList);
			if (bookClassification.isClassification()) {
				InputStream is = new ByteArrayInputStream(bookClassification.getJsonArray().toJSONString().getBytes());
				cloudFilesOperationUtil.fIleUploaded(CloudFileConstants.CLASSIFICATIONMAPJSON, is,
						CloudFileConstants.JSONFILETYPE);
				FileItContext forBookClassifcation = new FileItContext();
				forBookClassifcation.remove(BinderConstants.CLASSIFIED_BOOK_NAMES);
				forBookClassifcation.add(BinderConstants.CLASSIFIED_BOOK_NAMES, bookClassification.getJsonArray());
				is.close();
			} else {
			}
		}
	}

	/**
	 * @param classificationName
	 * @param bookList
	 * @return
	 * @throws Exception
	 */
	public static BookClassification checkifClassPresent(String classificationName, List<String> bookList)
			throws Exception {
		InputStream oInputStream;
		BookClassification bookClassification = new BookClassification();
		try {
			JSONParser parser = new JSONParser();
			oInputStream = cloudFilesOperationUtil.getFIleInputStream(CloudFileConstants.CLASSIFICATIONMAPJSON);
			if (null != oInputStream) {
				bookClassification.setClassification(true);
				JSONObject array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
				JSONArray jsonArray = (JSONArray) array.get(classificationName);
				if (null != jsonArray) {
					for (String bookName : bookList) {
						jsonArray.add(bookName);
					}
					array.put(classificationName, jsonArray);
				} else {
					array.put(classificationName, bookList);
				}
				bookClassification.setJsonArray(array);
			} else {
				bookClassification.setClassification(false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bookClassification;
	}
}
