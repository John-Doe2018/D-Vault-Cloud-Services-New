package com.tranfode.loginService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.tranfode.auth.FileItAuthentication;
import com.tranfode.auth.GetOrValidateUser;
import com.tranfode.domain.LoginRequest;
import com.tranfode.domain.LoginResponse;
import com.tranfode.domain.SignupRequest;
import com.tranfode.domain.SignupResponse;
import com.tranfode.processor.ProcessUserDetails;
import com.tranfode.util.FileItException;

public class LoginAuthenticationService {

	/**
	 * @param loginRequest
	 * @return
	 * @throws FileItException
	 */
	@POST
	@Path("login")
	@Produces("application/json")
	public LoginResponse authenticate(LoginRequest loginRequest) throws FileItException {
		LoginResponse loginResponse = new LoginResponse();
		String userName = loginRequest.getUserName();
		String password = loginRequest.getPassword();
		FileItAuthentication.checkCredentials(userName, password);
		loginResponse.setSuccessMsg("Login Successful");
		return loginResponse;
	}

	/**
	 * @param signupRequest
	 * @return
	 * @throws FileItException
	 */
	@POST
	@Path("signup")
	@Produces("application/json")
	public SignupResponse createUser(SignupRequest signupRequest) throws FileItException {
		SignupResponse signupResponse = new SignupResponse();
		GetOrValidateUser.validateSignUPDetails(signupRequest);
		boolean createdStatus = false;
		createdStatus = ProcessUserDetails.processUserDetailsToUserXml(signupRequest);
		if (createdStatus) {
			signupResponse.setSignupSuccessMsg("Thank You.User successfully Registered.");
		}
		return signupResponse;
	}

}
