package com.tranfode.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

public class CloudStorageConfig {

	private static CloudStorageConfig cstCnf;

	/**
	 * Create a static method to get instance.
	 */
	public static CloudStorageConfig getInstance() {
		if (cstCnf == null) {
			cstCnf = new CloudStorageConfig();
		}
		return cstCnf;
	}
	
	private Properties properties;
	private Storage storage;
	private static final String PROJECT_ID_PROPERTY = "project.id";
	private static final String APPLICATION_NAME_PROPERTY = "application.name";
	private static final String ACCOUNT_ID_PROPERTY = "account.id";
	private static final String PRIVATE_KEY = "private.key";
	private static final String API_URL = "api.url";
	private static final String BUCKET_NAME = "bucket.name";
	private String expiryTime;

	// Get private key object from unencrypted PKCS#8 file content
	private PrivateKey getPrivateKey() throws FileItException {
		// Remove extra characters in private key.
		String realPK = getProperties().getProperty(PRIVATE_KEY).replaceAll("-----END PRIVATE KEY-----", "")
				.replaceAll("-----BEGIN PRIVATE KEY-----", "").replaceAll("\n", "");
		byte[] b1 = Base64.getDecoder().decode(realPK);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(spec);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		}
	}

	private Storage getStorage() throws FileItException {

		if (storage == null) {
			PrivateKey oPrivateKey = getPrivateKey();
			HttpTransport httpTransport = new NetHttpTransport();
			JsonFactory jsonFactory = new JacksonFactory();

			List<String> scopes = new ArrayList<String>();
			scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

			Credential credential = new GoogleCredential.Builder().setTransport(httpTransport)
					.setJsonFactory(jsonFactory).setServiceAccountId(getProperties().getProperty(ACCOUNT_ID_PROPERTY))
					.setServiceAccountPrivateKey(oPrivateKey).setServiceAccountScopes(scopes).build();

			storage = new Storage.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName(getProperties().getProperty(APPLICATION_NAME_PROPERTY)).build();
		}

		return storage;
	}

	private Properties getProperties() throws FileItException {

		if (properties == null) {
			properties = new Properties();
			InputStream stream = CloudStorageConfig.class.getResourceAsStream("/cloud.properties");
			try {
				properties.load(stream);
			} catch (IOException e) {
				throw new RuntimeException("cloudstorage.properties must be present in classpath", e);
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					throw new FileItException(e.getMessage());
				}
			}
		}
		return properties;
	}

	/**
	 * Uploads a file to a bucket. Filename and content type will be based on the
	 * original file.
	 * 
	 * @param bucketName
	 *            Bucket where file will be uploaded
	 * @param filePath
	 *            Absolute path of the file to upload
	 * @throws Exception
	 */
	public void uploadFile(String bucketName, String filePath, InputStream oInputStream, String contentType)
			throws FileItException {

		Storage storage = getStorage();
		try {
			StorageObject object = new StorageObject();
			object.setContentType(contentType);
			InputStreamContent content = new InputStreamContent(contentType, oInputStream);
			Storage.Objects.Insert insert = storage.objects().insert(bucketName, object, content);
			insert.setName(filePath);
			insert.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		} finally {
			try {
				oInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new FileItException(e.getMessage());
			}
		}
	}

	public void downloadFile(String bucketName, String fileName, String destinationDirectory) throws FileItException {

		File directory = new File(destinationDirectory);
		if (!directory.isDirectory()) {
			throw new FileItException("Provided destinationDirectory path is not a directory");
		}
		File file = new File(directory.getAbsolutePath() + "/" + fileName);
		Storage storage;
		try {
			storage = getStorage();
			Storage.Objects.Get get = storage.objects().get(bucketName, fileName);
			FileOutputStream stream = new FileOutputStream(file);
			get.executeAndDownloadTo(stream);
			stream.close();
		} catch (Exception e) {
			throw new FileItException("Exception Occured !!!");
		}
	}

	public InputStream getFile(String bucketName, String filePath) throws FileItException {
		setExpiryTimeInEpoch();
		InputStream getOutput = null;
		String stringToSign = getSignInput(filePath);
		PrivateKey pk = getPrivateKey();
		String signedString = getSignedString(stringToSign, pk);
		try {
			signedString = URLEncoder.encode(signedString, "UTF-8");
			String signedUrl = getSignedUrl(signedString, filePath);
			getOutput = sendGet(signedUrl);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		}
		return getOutput;
	}

	public String getSignedString(String bucketName, String filePath) throws Exception {
		setExpiryTimeForImage();
		String stringToSign = getSignInput(filePath);
		PrivateKey pk = getPrivateKey();
		String signedString = getSignedString(stringToSign, pk);
		signedString = URLEncoder.encode(signedString, "UTF-8");
		String signedUrl = getSignedUrl(signedString, filePath);
		return signedUrl;
	}

	/**
	 * Deletes a file within a bucket
	 * 
	 * @param bucketName
	 *            Name of bucket that contains the file
	 * @param fileName
	 *            The file to delete
	 * @throws Exception
	 */
	public void deleteFile(String bucketName, String fileName) throws FileItException {
		Storage storage;
		try {
			storage = getStorage();
			storage.objects().delete(bucketName, fileName).execute();
		} catch (Exception e) {
			throw new FileItException("Exception Occured !!!");
		}

	}

	/**
	 * Creates a bucket
	 * 
	 * @param bucketName
	 *            Name of bucket to create
	 * @throws Exception
	 */
	public void createBucket(String bucketName) throws FileItException {
		Storage storage;
		try {
			storage = getStorage();
			Bucket bucket = new Bucket();
			bucket.setName(bucketName);
			storage.buckets().insert(getProperties().getProperty(PROJECT_ID_PROPERTY), bucket).execute();
		} catch (Exception e) {
			throw new FileItException("Exception Occured !!!");
		}

	}

	/**
	 * Deletes a bucket
	 * 
	 * @param bucketName
	 *            Name of bucket to delete
	 * @throws Exception
	 */
	public void deleteBucket(String bucketName) throws FileItException {

		Storage storage;
		try {
			storage = getStorage();
			storage.buckets().delete(bucketName).execute();
		} catch (Exception e) {
			throw new FileItException("Exception Occured !!!");
		}

	}

	/**
	 * Lists the objects in a bucket
	 * 
	 * @param bucketName
	 *            bucket name to list
	 * @return Array of object names
	 * @throws Exception
	 */
	public List<String> listBucket(String bucketName) throws FileItException {
		Storage storage;
		List<String> list = new ArrayList<String>();
		try {
			storage = getStorage();
			List<StorageObject> objects = storage.objects().list(bucketName).execute().getItems();
			if (objects != null) {
				for (StorageObject o : objects) {
					list.add(o.getName());
				}
			}
		} catch (Exception e) {
			throw new FileItException("Exception Occured !!!");
		}
		return list;
	}

	/**
	 * List the buckets with the project (Project is configured in properties)
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> listBuckets() throws FileItException {
		Storage storage;
		List<String> list = new ArrayList<String>();
		try {
			storage = getStorage();
			List<Bucket> buckets = storage.buckets().list(getProperties().getProperty(PROJECT_ID_PROPERTY)).execute()
					.getItems();
			if (buckets != null) {
				for (Bucket b : buckets) {
					list.add(b.getName());
				}
			}
		} catch (Exception e) {
			throw new FileItException("Exception Occured !!!");
		}
		return list;
	}

	private InputStream sendGet(String url) throws IOException {
		URL obj;
		HttpURLConnection con;
		obj = new URL(url);
		con = (HttpURLConnection) obj.openConnection();
		System.out.println(url);
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		return con.getInputStream();

		/*
		 * BufferedReader in = new BufferedReader(new
		 * InputStreamReader(con.getInputStream())); String inputLine; StringBuffer
		 * response = new StringBuffer();
		 * 
		 * while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
		 * in.close(); return response.toString();
		 */

	}

	private void setExpiryTimeInEpoch() {
		long now = System.currentTimeMillis();
		long expiredTimeInSeconds = (now + 120 * 1000L) / 1000;
		expiryTime = expiredTimeInSeconds + "";
	}

	private void setExpiryTimeForImage() {
		long now = System.currentTimeMillis();
		long expiredTimeInSeconds = (now + 20000 * 1000L) / 1000;
		expiryTime = expiredTimeInSeconds + "";
	}

	private String getSignedUrl(String signedString, String objectPath) throws FileItException {
		String signedUrl = getProperties().getProperty(API_URL) + '/' + getProperties().getProperty(BUCKET_NAME) + '/'
				+ objectPath + "?GoogleAccessId=" + getProperties().getProperty(ACCOUNT_ID_PROPERTY) + "&Expires="
				+ expiryTime + "&Signature=" + signedString;
		return signedUrl;
	}

	private String getSignInput(String objectPath) throws FileItException {
		return "GET" + "\n" + "" + "\n" + "" + "\n" + expiryTime + "\n" + '/' + getProperties().getProperty(BUCKET_NAME)
				+ '/' + objectPath;
	}

	private String getSignedString(String input, PrivateKey pk) throws FileItException {
		Signature privateSignature;
		byte[] s = null;
		try {
			privateSignature = Signature.getInstance("SHA256withRSA");
			privateSignature.initSign(pk);
			privateSignature.update(input.getBytes("UTF-8"));
			s = privateSignature.sign();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		}
		return Base64.getEncoder().encodeToString(s);
	}
}
