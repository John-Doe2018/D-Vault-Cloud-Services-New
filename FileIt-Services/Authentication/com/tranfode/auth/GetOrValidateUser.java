package com.tranfode.auth;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.tranfode.Constants.ErrorCodeConstants;
import com.tranfode.domain.User;
import com.tranfode.domain.Users;
import com.tranfode.util.CloudFilesOperationUtil;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.ErrorMessageReader;
import com.tranfode.util.FileItException;

public class GetOrValidateUser {

	public static String validateUser(String userName) throws FileItException{
//		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		CloudFilesOperationUtil cloudOperationUtil = new CloudFilesOperationUtil();
		boolean isUser = false;
		String passWord = null;
		//String xmlPath =FileInfoPropertyReader.getInstance().getString("doc.user.profile.path");
		String filePath = "Security/userDetailsRepo.xml";
		Unmarshaller un;
		Users userList = null;
		try {
			InputStream oInputStream = cloudOperationUtil.getFIleInputStream(filePath);
			JAXBContext context = JAXBContext.newInstance(Users.class);
			un = context.createUnmarshaller();
		//	userList = (Users) un.unmarshal(new File(xmlPath));
			userList = (Users) un.unmarshal(new InputStreamReader(oInputStream));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(User user: userList.getUsers()) {
			if(userName.equals(user.getUserName())) {
				passWord = user.getPassword();
				isUser = true;
			}
		}
		if(!isUser) {
			throw new FileItException(ErrorCodeConstants.ERR_CODE_0004, ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0004));
		}
		return passWord;
	}

}
