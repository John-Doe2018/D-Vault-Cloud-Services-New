package com.tranfode.webservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;

public class helloWorldService {

	@SuppressWarnings("unchecked")
	@GET
	@Path("getMasterJson")
	@Produces("application/json")
	public String getMasterJson() throws FileNotFoundException, IOException, ParseException {
		try {
			CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
			InputStream oInputStream = oCloudStorageConfig
					.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"), "test.JSON");
			JSONParser parser = new JSONParser();
			Object object = parser.parse(new InputStreamReader(oInputStream));
			oInputStream.close();
			JSONObject jsonObject = (JSONObject) object;
			return jsonObject.toJSONString();
		} catch (Exception e) {
			JSONObject oJsonObject = new JSONObject();
			oJsonObject.put("Error", "No Book Present");
			return oJsonObject.toJSONString();
		}

	}

}
