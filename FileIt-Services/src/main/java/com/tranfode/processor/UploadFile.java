package com.tranfode.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import com.tranfode.util.CloudPropertiesReader;

public class UploadFile {
	static Storage storage = null;
	static String bucketName = CloudPropertiesReader.getInstance().getString("bucket.name");
	static String name = "Souvik";
	static String contentType = "application/xml";

	public static void pushFilesToCloudStorage(String xmlFile) throws Exception {
		// TODO Auto-generated method stub

		/*
		 * Properties properties; final String PROJECT_ID_PROPERTY = "project.id"; final
		 * String APPLICATION_NAME_PROPERTY = "application.name"; final String
		 * ACCOUNT_ID_PROPERTY = "account.id"; final String PRIVATE_KEY_PATH_PROPERTY =
		 * "private.key.path";
		 */

		uploadFile(name, xmlFile, bucketName);
	}

	private static void uploadFile(String name, String file, String bucketName) throws IOException {
		System.out.println("Entry to this upload file");
		try {
			storage = getStorage();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		StorageObject object = new StorageObject();
		object.setBucket(bucketName);

		File xmlFilePath = new File(file);

		InputStream stream = null;
		try {
			stream = new FileInputStream(xmlFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			/*
			 * String contentType = URLConnection .guessContentTypeFromStream(stream);
			 */
			System.out.println("contentType" + contentType);
			InputStreamContent content = new InputStreamContent(contentType, stream);
			Storage.Objects.Insert insert = storage.objects().insert(bucketName, null, content);
			insert.setName(xmlFilePath.getName());
			System.out.println("contentType" + contentType);
			insert.execute();

		} finally {
			stream.close();
		}
	}

	private static Storage getStorage() throws Exception {

		if (storage == null) {

			HttpTransport httpTransport = new NetHttpTransport();
			JsonFactory jsonFactory = new JacksonFactory();

			List<String> scopes = new ArrayList<String>();
			scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

			Credential credential = new GoogleCredential.Builder().setTransport(httpTransport)
					.setJsonFactory(jsonFactory)
					.setServiceAccountId("testdevelopment@winter-alliance-204618.iam.gserviceaccount.com")
					.setServiceAccountPrivateKeyFromP12File((new File("D:\\Poc git\\The Vault-7d055ecdfb3a.p12")))
					.setServiceAccountScopes(scopes).build();

			storage = new Storage.Builder(httpTransport, jsonFactory, credential).setApplicationName("testdevelopment")
					.build();
		}
		System.out.println("exit from storage");

		return storage;
	}

}
