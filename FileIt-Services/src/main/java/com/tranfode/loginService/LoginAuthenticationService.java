package com.tranfode.loginService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.tranfode.auth.FileItAuthentication;
import com.tranfode.domain.LoginRequest;
import com.tranfode.domain.LoginResponse;
import com.tranfode.util.FileItException;

public class LoginAuthenticationService {


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


}
