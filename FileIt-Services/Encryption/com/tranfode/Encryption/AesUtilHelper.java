package com.tranfode.Encryption;

import java.io.UnsupportedEncodingException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.tranfode.logger.FILEITLogger;


public class AesUtilHelper {
	FILEITLogger LOGGER;
    private final int keySize;
    private final int iterationCount;
    private final Cipher cipher;
    
    public AesUtilHelper(int keySize, int iterationCnt) {
        this.keySize = keySize;
        this.iterationCount = iterationCnt;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }catch (Exception e) {
            throw fail(e);
        }
    }
    
    public String encrypt(String salt, String iv, String passPhrase, String plaintExt) {
        try {
            SecretKey key = generateKey(salt, passPhrase);
            byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, key, iv, plaintExt.getBytes("UTF-8"));
            return base64(encrypted);
        }
        catch (UnsupportedEncodingException e) {
        	LOGGER.error(e);
            throw fail(e);
        }
    }
    
    public String decrypt(String salt, String iv, String passPhrase, String cipherText) {
        try {
            SecretKey key = generateKey(salt, passPhrase);
            byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, base64(cipherText));
            return new String(decrypted, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
        	LOGGER.error(e);
            throw fail(e);
        }
    }
    
    private byte[] doFinal(int encryptMode, SecretKey secKey, String iv, byte[] bytes) {
        try {
            cipher.init(encryptMode, secKey, new IvParameterSpec(hex(iv)));
            return cipher.doFinal(bytes);
        }
        catch (Exception e) {
        	LOGGER.error(e);
            throw fail(e);
        }
    }
    
    private SecretKey generateKey(String salt, String passPhrase) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), hex(salt), iterationCount, keySize);
            SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return key;
        }
        catch (Exception e) {
        	LOGGER.error(e);
        	return null;
        }
    }
    
   /* public static String random(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return hex(salt);
    }*/
    
    public static String base64(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
    }
    
    public static byte[] base64(String str) {
        return DatatypeConverter.parseBase64Binary(str);
    }
    
    public static String hex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }
    
    public static byte[] hex(String str) {
        return DatatypeConverter.parseHexBinary(str);
    }
    
    private IllegalStateException fail(Exception e) {
        return new IllegalStateException(e);
    }
}
