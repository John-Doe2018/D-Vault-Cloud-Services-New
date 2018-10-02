package com.tranfode.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.tranfode.Constants.ErrorCodeConstants;
import com.tranfode.domain.BinderList;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.ErrorMessageReader;
import com.tranfode.util.FileItException;
import com.tranfode.util.FileUtil;
import com.tranfode.util.ReadJsonUtil;

public class UpdateMasterJson {
	/**
	 * @param bookObject
	 * @return
	 * @throws FileItException
	 * @throws ParseException
	 */
	/*
	 * @SuppressWarnings("unchecked") public String prepareMasterJson_Old(BinderList
	 * bookObject) throws FileItException { JSONParser parser = new JSONParser();
	 * JSONObject obj = new JSONObject(); JSONObject superObj = new JSONObject();
	 * JSONObject parentObj = new JSONObject();
	 * 
	 * boolean isSameName = false; String xmlFilePath =
	 * FileUtil.createDynamicFilePath(bookObject.getName()); // Check any book with
	 * same name already present or not CloudStorageConfig oCloudStorageConfig = new
	 * CloudStorageConfig(); InputStream oInputStream = null; JSONObject array =
	 * null; try { oInputStream =
	 * oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString(
	 * "bucket.name"), "ClassificationMap.JSON"); array = (JSONObject)
	 * parser.parse(new InputStreamReader(oInputStream)); } catch (Exception e1) {
	 * // TODO Auto-generated catch block throw new
	 * FileItException(e1.getMessage()); } JSONArray jsonArray = (JSONArray)
	 * array.get(bookObject.getClassification()); if (oInputStream != null) { if
	 * (jsonArray != null) isSameName =
	 * ReadJsonUtil.CheckBinderWithSameName(jsonArray, bookObject.getName()); if
	 * (isSameName) { throw new FileItException(ErrorCodeConstants.ERR_CODE_0002,
	 * ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0002))
	 * ; } else { try { jsonArray = new JSONArray(); // Add the new object to
	 * existing obj.put("Name", bookObject.getName()); obj.put("Classification",
	 * bookObject.getClassification()); obj.put("Path", xmlFilePath);
	 * superObj.put(bookObject.getName(), obj); jsonArray.add(superObj);
	 * parentObj.put("BookList", jsonArray); InputStream is = new
	 * ByteArrayInputStream(parentObj.toJSONString().getBytes());
	 * oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString(
	 * "bucket.name"), "test.JSON", is, "application/json"); is.close(); } catch
	 * (IOException e) { throw new FileItException(e.getMessage()); } catch
	 * (Exception e) { // TODO Auto-generated catch block throw new
	 * FileItException(e.getMessage()); } } } else if (!isSameName) { JSONArray
	 * jsonArray1 = new JSONArray(); obj.put("Name", bookObject.getName());
	 * obj.put("Classification", bookObject.getClassification()); obj.put("Path",
	 * xmlFilePath); superObj.put(bookObject.getName(), obj);
	 * jsonArray1.add(superObj); parentObj.put("BookList", jsonArray1); try {
	 * InputStream is = new
	 * ByteArrayInputStream(parentObj.toJSONString().getBytes());
	 * oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString(
	 * "bucket.name"), "test.JSON", is, "application/json"); is.close(); } catch
	 * (IOException e) { throw new FileItException(e.getMessage()); } catch
	 * (Exception e) { // TODO Auto-generated catch block throw new
	 * FileItException(e.getMessage()); } } else { throw new
	 * FileItException(ErrorCodeConstants.ERR_CODE_0002,
	 * ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0002))
	 * ; } return bookObject.getName(); }
	 * 
	 */

	// Re-factored the code

	@SuppressWarnings("unchecked")
	public String prepareMasterJson(BinderList bookObject) throws FileItException, ParseException {

		String xmlFilePath = FileUtil.createDynamicFilePath(bookObject.getName());

		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream oInputStream = oCloudStorageConfig
				.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "ClassificationMap.JSON");

		JSONParser parser = new JSONParser();
		try {
			if (oInputStream != null) {
				JSONObject array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
				JSONArray arrayBook = (JSONArray) array.get(bookObject.getClassification());
				boolean isSameName = ReadJsonUtil.CheckBinderWithSameName(arrayBook, bookObject.getName());
				if (isSameName) {
					throw new FileItException(ErrorCodeConstants.ERR_CODE_0002,
							ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0002));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		// Create new obj
		JSONObject obj = new JSONObject();
		obj.put("Name", bookObject.getName());
		obj.put("Classification", bookObject.getClassification());
		obj.put("Path", xmlFilePath);

		// Create new superObj
		JSONObject superObj = new JSONObject();
		superObj.put(bookObject.getName(), obj);

		// Create new array of book
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(superObj);

		// create new parentObj
		JSONObject parentObj = new JSONObject();
		parentObj.put("BookList", jsonArray);

		// Update test.json file
		InputStream is = new ByteArrayInputStream(parentObj.toJSONString().getBytes());
		oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "test.JSON", is,
				"application/json");
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bookObject.getName();
	}

}