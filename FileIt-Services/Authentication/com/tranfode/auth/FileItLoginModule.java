package com.tranfode.auth;
import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.tranfode.Constants.ErrorCodeConstants;
import com.tranfode.Encryption.AesUtil;
import com.tranfode.util.ErrorMessageReader;
import com.tranfode.util.FileItException;

public class FileItLoginModule implements LoginModule{
	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map sharedState;
	private Map options;

	private boolean succeeded = false;

	public FileItLoginModule() {
		System.out.println("Login Module - constructor called");
	}

	public boolean abort() throws LoginException {
		System.out.println("Login Module - abort called");
		return false;
	}

	public boolean commit() throws LoginException {
		System.out.println("Login Module - commit called");
		return succeeded;
	}

	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options) {

		System.out.println("Login Module - initialize called");
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;

		System.out.println("testOption value: " + (String) options.get("testOption"));

		succeeded = false;
	}

	public boolean login() throws LoginException {
		String encryptedPassword = null;
		String originalPswd = null;
		System.out.println("Login Module - login called");
		if (callbackHandler == null) {
			throw new LoginException("Oops, callbackHandler is null");
		}

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("name:");
		callbacks[1] = new PasswordCallback("password:", false);

		try {
			callbackHandler.handle(callbacks);
		} catch (IOException e) {
			throw new LoginException("Oops, IOException calling handle on callbackHandler");
		} catch (UnsupportedCallbackException e) {
			throw new LoginException("Oops, UnsupportedCallbackException calling handle on callbackHandler");
		}

		NameCallback nameCallback = (NameCallback) callbacks[0];
		PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];

		String name = nameCallback.getName();
		String password = new String(passwordCallback.getPassword());

		//Get the username password from the Repo & Roles also.
		try {
			encryptedPassword = GetOrValidateUser.validateUser(name);
			//Decrypt the password & then send it for Matching
			
			originalPswd = AesUtil.Decrypt(encryptedPassword);
		
		if (originalPswd.equals(password)) {
			System.out.println("Success! You get to log in!");
			succeeded = true;
			return succeeded;
		} else {
			System.out.println("Failure! You don't get to log in");
			succeeded = false;
			throw new FileItException(ErrorCodeConstants.ERR_CODE_0006,ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0006));
		}
		} catch (FileItException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return succeeded;
	}

	public boolean logout() throws LoginException {
		System.out.println("Login Module - logout called");
		return false;
	}

}