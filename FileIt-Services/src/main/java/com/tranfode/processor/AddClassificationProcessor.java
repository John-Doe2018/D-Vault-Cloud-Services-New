package com.tranfode.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tranfode.Constants.CloudFileConstants;
import com.tranfode.Constants.ErrorCodeConstants;
import com.tranfode.domain.AddClassificationResponse;
import com.tranfode.domain.BusinessErrorData;
import com.tranfode.util.CloudFilesOperationUtil;
import com.tranfode.util.ErrorMessageReader;
import com.tranfode.util.FileItException;

public class AddClassificationProcessor {
	static CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
	private static AddClassificationProcessor addClassProcessor;

	/**
	 * Create a static method to get instance.
	 */
	public static AddClassificationProcessor getInstance() {
		if (addClassProcessor == null) {
			addClassProcessor = new AddClassificationProcessor();
		}
		return addClassProcessor;
	}

	/**
	 * @param classificationName
	 * @param addClassificationResponse
	 * @return
	 * @throws FileItException
	 */
	@SuppressWarnings("unchecked")
	public AddClassificationResponse addClassification(String classificationName,
			AddClassificationResponse addClassificationResponse) throws FileItException {
		List<String> classifcations = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		InputStream inputStream;
		AddClassificationResponse oAddClassificationResponse = new AddClassificationResponse();
		try {
			classifcations.add(classificationName);
			inputStream = cloudFilesOperationUtil.getFIleInputStream(CloudFileConstants.CLASSIFICATIONLISTJSON);

			JSONObject array = (JSONObject) parser.parse(new InputStreamReader(inputStream));
			JSONArray jsonArray = (JSONArray) array.get("classificationList");
			if (!jsonArray.contains(classificationName)) {
				jsonArray.add(classificationName);
				array.put("classificationList", jsonArray);
				InputStream is = new ByteArrayInputStream(array.toJSONString().getBytes());
				cloudFilesOperationUtil.fIleUploaded(CloudFileConstants.CLASSIFICATIONLISTJSON, is,
						CloudFileConstants.JSONFILETYPE);
				oAddClassificationResponse.setSuccessMsg("Classification Created Successfully");
			} else {
				BusinessErrorData oBusinessErrorData = new BusinessErrorData();
				oBusinessErrorData.setDescription("Classification Already exists");
				oAddClassificationResponse.setBusinessErrorData(oBusinessErrorData);
			}
		} catch (Exception e) {
			throw new FileItException(e.getMessage());

		}

		return oAddClassificationResponse;

	}

	/**
	 * @return
	 * @throws FileItException
	 */
	@SuppressWarnings("unchecked")
	public List<String> getClassifications() throws FileItException {
		List<String> classifcations = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		InputStream inputStream;
		try {
			inputStream = cloudFilesOperationUtil.getFIleInputStream(CloudFileConstants.CLASSIFICATIONLISTJSON);

			JSONObject array = (JSONObject) parser.parse(new InputStreamReader(inputStream));
			classifcations = (List<String>) array.get("classificationList");
			if (null == classifcations) {
				throw new FileItException(ErrorCodeConstants.ERR_CODE_0010,
						ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0010));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classifcations;

	}

}
