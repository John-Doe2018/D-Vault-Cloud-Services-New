package com.tranfode.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tranfode.Constants.BinderConstants;
import com.tranfode.Constants.CloudFileConstants;

public class FileUtil {
	static CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
	public static String createDynamicFilePath(String name) {
		String extension = BinderConstants.EXTENSION;
		String filePath = FileInfoPropertyReader.getInstance().getString("xml.file.path");
		extension = name.concat(extension);
		filePath = filePath + extension;
		return filePath;
	}

	// Path manipulation
	public static String correctFilePath(String filePath) {
		String modifiedfilePath = java.util.regex.Pattern.compile("\\\\").matcher(filePath).replaceAll("\\\\\\\\");
		return modifiedfilePath;
	}

	public static void checkTestJson() throws Exception {
//		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		//CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
		try {
			cloudFilesOperationUtil.getFIleInputStream(CloudFileConstants.TESTJSON);
		} catch (FileItException e) {
			JSONArray jsonArray = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("BookList", jsonArray);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			cloudFilesOperationUtil.fIleUploaded(CloudFileConstants.TESTJSON, is, CloudFileConstants.JSONFILETYPE);
		}

	}

	public static boolean checkBookClassificationJson() throws Exception {
		//CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
		try {
			cloudFilesOperationUtil.getFIleInputStream(CloudFileConstants.CLASSIFICATIONMAPJSON);
//			oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
				//	"ClassificationMap.JSON");
			return true;
		} catch (Exception e) {
			JSONArray jsonArray = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("BlankArray", jsonArray);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			cloudFilesOperationUtil.fIleUploaded(CloudFileConstants.CLASSIFICATIONMAPJSON, is, CloudFileConstants.JSONFILETYPE);
			return false;
		}

	}

	public static JSONArray checkBookList() throws FileItException {
		//CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		//CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
		InputStream oInputStream;
		JSONArray jsonArray = new JSONArray();
		try {
			oInputStream = cloudFilesOperationUtil.getFIleInputStream(CloudFileConstants.BOOKLISTJSON);
			JSONObject array = null;
			JSONParser parser = new JSONParser();
			array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
			jsonArray = (JSONArray) array.get("Books");
		} catch (Exception e) {
			JSONObject parentObj = new JSONObject();
			parentObj.put("Books", jsonArray);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			cloudFilesOperationUtil.fIleUploaded(CloudFileConstants.BOOKLISTJSON, is, CloudFileConstants.JSONFILETYPE);
		}
		return jsonArray;

	}

	public static boolean checkClassificationListJson() throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		try {
			cloudFilesOperationUtil.getFIleInputStream(CloudFileConstants.CLASSIFICATIONLISTJSON);
			return true;
		} catch (Exception e) {
			JSONArray jsonArray = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("classificationList", jsonArray);
			InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
			cloudFilesOperationUtil.fIleUploaded(CloudFileConstants.CLASSIFICATIONLISTJSON, is, CloudFileConstants.JSONFILETYPE);
			return false;
		}

	}
}