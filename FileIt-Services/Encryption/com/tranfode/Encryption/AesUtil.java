package com.tranfode.Encryption;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class AesUtil {
    private static final String IV =   "00000000000000000000000000000000";
    private static final String SALT = "00000000000000000000000000000000";   
    private static final int KEY_SIZE = 128;
    private static final int ITERATION_COUNT = 10000;
    private static final String PASSPHRASE = "FileItSecretKey";
   
    
   
    public static String Encrypt(String PLAIN_TEXT) {
        AesUtilHelper util = new AesUtilHelper(KEY_SIZE, ITERATION_COUNT);
        String encrypt = util.encrypt(SALT, IV, PASSPHRASE, PLAIN_TEXT);    
        return encrypt;
    }
    
   
    public static String  Decrypt(String CIPHER_TEXT) {
        AesUtilHelper util = new AesUtilHelper(KEY_SIZE, ITERATION_COUNT);
        String decrypt = util.decrypt(SALT, IV, PASSPHRASE, CIPHER_TEXT);
        return decrypt;
    }
    
   /* public static String getAPIToken(String requestURL) {    	
		String [] requestTokenizer = requestURL.split("rest/");
		String requestAPI = requestTokenizer[1].substring(0, requestTokenizer[1].indexOf("?")==-1?requestTokenizer[1].length():requestTokenizer[1].indexOf("?"));
		String apiToken = "Bearer_Server ";
		apiToken = apiToken + Encrypt(requestAPI);				
		return apiToken;
    }*/
    
    /*public static void main(String args[]) throws IOException
    {
    	String line = "", str = "";
    	BufferedReader br =  null;
		br = new BufferedReader(new FileReader("D:\\files\\xmlFile.xml"));
	
		while ((line = br.readLine()) != null) {
			str += line;
		}
	br.close();
    	
    	String PLAIN_TEXT = str;
    	//String token = getAPIToken(PLAIN_TEXT);
    	String CIPHER_TEXT = Encrypt("Bikash@123");
    	System.out.println("CIPHER_TEXT ="+CIPHER_TEXT);
    	//System.out.println("token ="+token);
    	//CIPHER_TEXT = "3cxgj2t+Qpm6bpEGD6XyOw==";
    	String Decrypt_TEXT = Decrypt("=a0IRP+JLhza7nbmCeqpIyw==");
    	System.out.println("Decrypt_TEXT ="+Decrypt_TEXT);
    }*/
}
