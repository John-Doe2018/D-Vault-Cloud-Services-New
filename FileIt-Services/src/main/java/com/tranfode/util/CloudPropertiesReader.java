package com.tranfode.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class CloudPropertiesReader {

	private static Map<String, String> propertiesMap = new Hashtable<String, String>();
	private static CloudPropertiesReader fpr;

	/**
	 * Create a static method to get instance.
	 */
	public static CloudPropertiesReader getInstance() {
		if (fpr == null) {
			fpr = new CloudPropertiesReader();
		}
		return fpr;
	}

	private CloudPropertiesReader() {
		Properties properties = new Properties();
		InputStream inputStream = null;

		try {
			inputStream = getClass().getClassLoader().getResourceAsStream("cloud.properties");
			if (null != inputStream) {
				properties.load(inputStream);
				for (String keyName : properties.stringPropertyNames()) {
					String keyValue = properties.getProperty(keyName);
					propertiesMap.put(keyName, keyValue);
				}
			}
		} catch (IOException ioException) {
			System.out.println(ioException.getMessage());
		} finally {
			try {
				if (null != inputStream) {
					inputStream.close();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

	}

	/**
	 * @param keyName
	 * @return
	 */
	public String getString(String keyName) {
		// return String.valueOf(properties.get(keyName));
		return propertiesMap.get(keyName);
	}

}
