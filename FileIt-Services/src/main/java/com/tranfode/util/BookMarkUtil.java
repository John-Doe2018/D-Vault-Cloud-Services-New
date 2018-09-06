package com.tranfode.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.tranfode.domain.BookClassMap;
import com.tranfode.domain.BookMarkDetails;

public class BookMarkUtil {

	public static BookMarkUtil bookMark;

	/**
	 * @return
	 */
	public static BookMarkUtil getInstance() {
		if (bookMark == null) {
			bookMark = new BookMarkUtil();
		}
		return bookMark;
	}

	/**
	 * @param loggedInUser
	 * @param bookName
	 * @param classificationName
	 * @return
	 * @throws FileItException
	 */
	public BookMarkDetails saveUserBookMarkDetails(String loggedInUser, String bookName, String classificationName)
			throws FileItException {
		BookMarkDetails bookMarkDetails = new BookMarkDetails();
		if (loggedInUser != null && bookName != null && classificationName != null) {
			bookMarkDetails.setUserName(loggedInUser);
			bookMarkDetails.setBookName(bookName);
			bookMarkDetails.setClassification(classificationName);
		} else if (loggedInUser == null || bookName == null || classificationName == null) {
			throw new FileItException("Something went wrong.BookMark can not be added");
		}
		WriteBookMarkDetails(bookMarkDetails);
		return bookMarkDetails;
	}

	/**
	 * @param bookMarkDetails
	 * @throws FileItException
	 */
	@SuppressWarnings("unchecked")
	public void WriteBookMarkDetails(BookMarkDetails bookMarkDetails) throws FileItException {
		Map<String, List<BookClassMap>> bookMarkDetailMap = new HashMap<String, List<BookClassMap>>();
		// add classification wise books to the map
		List<BookClassMap> bookClassList = new ArrayList<BookClassMap>();
		BookClassMap bookClassMap = new BookClassMap();
		bookClassMap.setBookName(bookMarkDetails.getBookName());
		bookClassMap.setClassification(bookMarkDetails.getClassification());
		bookClassList.add(bookClassMap);
		bookMarkDetailMap.put(bookMarkDetails.getUserName(), bookClassList);
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		JSONParser parser = new JSONParser();
		InputStream inputStream;
		boolean isBookmark = true;
		JSONArray newUserBookClassListObj = new JSONArray();
		boolean isNewUserBookmark = false;
		try {
			inputStream = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"BookMarkDetails.JSON");
			if (inputStream != null) {
				Gson gson = new Gson();
				String json = gson.toJson(bookMarkDetailMap);
				JSONObject bookArray = (JSONObject) parser.parse(new InputStreamReader(inputStream));
				JSONArray newBookClassListObj = (JSONArray) bookArray.get(bookMarkDetails.getUserName());
				if (null != newBookClassListObj) {
					// Check for already added bookName in the same classification
					for (int i = 0; i < newUserBookClassListObj.size(); i++) {
						JSONObject json_data = (JSONObject) newUserBookClassListObj.get(i);
						String bookname = (String) json_data.get("bookName");
						String classificationName = (String) json_data.get("classification");
						if (classificationName.equalsIgnoreCase(bookMarkDetails.getClassification())) {
							if (bookname.equalsIgnoreCase(bookMarkDetails.getBookName())) {
								throw new FileItException("Bookmark already exists.");
							} else {
								isBookmark = false;
							}
						} else {
							isBookmark = false;
						}
					}
				} else {
					isNewUserBookmark = true;
				}
				if (!isBookmark) {
					newBookClassListObj.add(bookClassMap);
					bookArray.put(bookMarkDetails.getUserName(), newUserBookClassListObj);
					Gson gsonmap = new Gson();
					String jsonmap = gsonmap.toJson(bookArray);
					InputStream is = new ByteArrayInputStream(jsonmap.getBytes());
					oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
							"BookMarkDetails.JSON", is, "application/json");
				} else if (isNewUserBookmark) {
					newUserBookClassListObj.add(bookClassMap);
					bookArray.put(bookMarkDetails.getUserName(), newUserBookClassListObj);
					Gson gsonmap = new Gson();
					String jsonmap = gsonmap.toJson(bookArray);
					System.out.println(jsonmap);
					InputStream is = new ByteArrayInputStream(jsonmap.getBytes());
					oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
							"BookMarkDetails.JSON", is, "application/json");
				}
			}
		} catch (Exception ex) {
			JSONArray jsonArray1 = new JSONArray();
			JSONObject parentObj = new JSONObject();
			parentObj.put("BookMarkList", jsonArray1);
			Gson gson = new Gson();
			String json = gson.toJson(bookMarkDetailMap);
			InputStream is = new ByteArrayInputStream(json.getBytes());
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					"BookMarkDetails.JSON", is, "application/json");
		}

	}

	/**
	 * @param loggedInUser
	 * @return
	 * @throws FileItException
	 */
	public List<BookMarkDetails> getBookMarks(String loggedInUser) throws FileItException {
		BookMarkDetails bookMarkDetails = new BookMarkDetails();
		List<BookMarkDetails> bookMarkDetailsList = new ArrayList<BookMarkDetails>();
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		JSONParser parser = new JSONParser();
		InputStream inputStream;
		if (loggedInUser != null) {
			bookMarkDetails.setUserName(loggedInUser);
		} else if (loggedInUser == null) {
			throw new FileItException("Something went wrong.BookMark can not be added");
		}
		inputStream = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
				"BookMarkDetails.JSON");
		if (inputStream != null) {
			JSONObject bookArray = null;
			try {
				bookArray = (JSONObject) parser.parse(new InputStreamReader(inputStream));
				JSONArray newBookClassListObj = (JSONArray) bookArray.get(bookMarkDetails.getUserName());
				if (null != newBookClassListObj) {
					for (int i = 0; i < newBookClassListObj.size(); i++) {
						JSONObject json_data = (JSONObject) newBookClassListObj.get(i);
						String bookname = (String) json_data.get("bookName");
						String classificationName = (String) json_data.get("classification");
						BookMarkDetails bookMarkDetail = new BookMarkDetails();
						bookMarkDetail.setBookName(bookname);
						bookMarkDetail.setClassification(classificationName);
						bookMarkDetail.setUserName(loggedInUser);
						bookMarkDetailsList.add(bookMarkDetail);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new FileItException(e.getMessage());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				throw new FileItException(e.getMessage());
			}

		}
		return bookMarkDetailsList;
	}
}
