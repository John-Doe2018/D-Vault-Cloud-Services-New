package com.tranfode.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.tranfode.Constants.BinderConstants;
import com.tranfode.Constants.CloudFileConstants;
import com.tranfode.domain.FileItContext;
import com.tranfode.util.CloudFilesOperationUtil;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.FileItException;

public class DeleteBookProcessor {
	static CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
	@SuppressWarnings("unchecked")
	public JSONObject deleteBookProcessor(String deleteBookRequest, String classificationName) throws Exception {
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		JSONObject parentObj = new JSONObject();
		JSONObject deleteMsg = new JSONObject();
		InputStream oInputStream = cloudFilesOperationUtil.getFIleInputStream(CloudFileConstants.CLASSIFICATIONMAPJSON);
		JSONParser parser = new JSONParser();
		JSONObject array;
		try {
			array = (JSONObject) parser.parse(new InputStreamReader(oInputStream));
			if (array.containsKey(classificationName)) {
				List<String> bookList = (List<String>) array.get(classificationName);
				if (bookList.contains(deleteBookRequest)) {
					bookList.remove(deleteBookRequest);
				}
				array.put(classificationName, bookList);
			}
			InputStream is = new ByteArrayInputStream(array.toJSONString().getBytes());
			cloudFilesOperationUtil.fIleUploaded(CloudFileConstants.CLASSIFICATIONMAPJSON, is, CloudFileConstants.JSONFILETYPE);
			FileItContext.remove(BinderConstants.CLASSIFIED_BOOK_NAMES);
			FileItContext.add(BinderConstants.CLASSIFIED_BOOK_NAMES, array);
			oInputStream.close();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new FileItException(e.getMessage());
		}
		List<String> oList = oCloudStorageConfig
				.listBucket(CloudPropertiesReader.getInstance().getString("bucket.name"));
		String wordToSearchFor = classificationName+"/"+deleteBookRequest + "/Images";
		String contentName = classificationName+"/"+deleteBookRequest + "/Contents";
		String xmlName = "files/" +classificationName+"/"+ deleteBookRequest + ".xml";
		for (String word : oList) {
			if (word.contains(wordToSearchFor) || word.contains(contentName) || word.contains(xmlName)) {
				oCloudStorageConfig.deleteFile(CloudPropertiesReader.getInstance().getString("bucket.name"), word);
			}

		}
		return deleteMsg;
	}

}