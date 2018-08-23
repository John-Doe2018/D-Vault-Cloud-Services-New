package com.tranfode.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.tranfode.util.CloudFilesOperationUtil;
import com.tranfode.util.FileItException;

public class BookTreeProcessor {
	private static BookTreeProcessor INSTANCE;
	public static synchronized BookTreeProcessor getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new BookTreeProcessor();
		}
		return INSTANCE;
	}

	
	static CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();
	public JSONObject processBookXmltoDoc(String bookName) throws Exception {

		String line = "", str = "";
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		JSONObject json;
		try {
			documentBuilder = documentFactory.newDocumentBuilder();
			String requiredXmlPath = "files/" + bookName + ".xml";
			JSONParser parser = new JSONParser();
			BufferedReader br = null;
			InputStream xmlInputStream = cloudFilesOperationUtil
					.getFIleInputStream(requiredXmlPath);
			br = new BufferedReader(new InputStreamReader(xmlInputStream));

			while ((line = br.readLine()) != null) {
				str += line;
			}
			br.close();
			org.json.JSONObject jsondata = XML.toJSONObject(str);

			json = (JSONObject) parser.parse(jsondata.toString());
		} catch (ParserConfigurationException | ParseException | IOException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		}
		return json;
	}

}