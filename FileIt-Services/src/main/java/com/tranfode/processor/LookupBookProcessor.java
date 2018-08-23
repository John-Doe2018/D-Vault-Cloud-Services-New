package com.tranfode.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.tranfode.Constants.CloudFileConstants;
import com.tranfode.Constants.ErrorCodeConstants;
import com.tranfode.util.CloudFilesOperationUtil;
import com.tranfode.util.ErrorMessageReader;
import com.tranfode.util.FileItException;

public class LookupBookProcessor {
	private static LookupBookProcessor INSTANCE;
	public static synchronized LookupBookProcessor getInstance() {
			if (null == INSTANCE) {
				INSTANCE = new LookupBookProcessor();
			}
			return INSTANCE;
		}
	static CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
	public static JSONObject lookupBookbyName(String bookName) throws Exception {
		InputStream oInputStream = cloudFilesOperationUtil
				.getFIleInputStream(CloudFileConstants.BOOKLISTJSON);
		JSONParser parser = new JSONParser();
		String book = null;
		boolean bookNameFound = false;
		JSONObject array = null;
		try {
			array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileItException(e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileItException(e.getMessage());
		}
		JSONArray jsonArray = (JSONArray) array.get("Books");
		for (Object obj : jsonArray) {
			book = String.valueOf(obj);
			if (jsonArray.contains(obj)) {
				bookNameFound = true;
				break;
			}
		}
		if (!bookNameFound) {
			throw new FileItException(ErrorCodeConstants.ERR_CODE_0003,
					ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0003));
		}
		JSONObject object = new JSONObject();
		object.put("BookName", book);
		return object;
	}

}