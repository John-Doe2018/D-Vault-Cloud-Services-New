package com.tranfode.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.tranfode.Constants.ErrorCodeConstants;
import com.tranfode.Encryption.AesUtil;
import com.tranfode.domain.SignupRequest;
import com.tranfode.domain.User;
import com.tranfode.domain.Users;
import com.tranfode.logger.FILEITLogger;
import com.tranfode.logger.FILEITLoggerFactory;
import com.tranfode.util.CloudFilesOperationUtil;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.ErrorMessageReader;
import com.tranfode.util.FileItException;

public class ProcessUserDetails {

	private static final FILEITLogger fileItLogger = FILEITLoggerFactory.getLogger(BookTreeProcessor.class);

	public static boolean processUserDetailsToUserXml(SignupRequest signupRequest) throws FileItException {
		fileItLogger.info("Entering to ProcessUserDetails Processor");
		CloudFilesOperationUtil cloudOperationUtil = new CloudFilesOperationUtil();
		boolean created = false;
		List<String> userNames = new ArrayList<String>();
		String filePath = "Security/userDetailsRepo.xml";
		String encryptedPasswd = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Users.class);
			InputStream oInputStream = cloudOperationUtil.getFIleInputStream(filePath);
			// Read XML
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Users users = (Users) unmarshaller.unmarshal(new InputStreamReader(oInputStream));
			encryptedPasswd = encryptPassword(signupRequest.getPassword());
			User userObj = new User(signupRequest.getFirstName(), signupRequest.getLastName(),
					signupRequest.getUserName(), encryptedPasswd, signupRequest.getRole());

			List<User> usersList = users.getUsers();
			for (User user : usersList) {
				userNames.add(user.getUserName());
			}
			if (!userNames.contains(signupRequest.getUserName())) {
				usersList.add(userObj);
				created = true;
				users.setUsers(usersList);
			} else {
				throw new FileItException(ErrorCodeConstants.ERR_CODE_0008,
						ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0008));
			}
			File file = new File(ProcessUserDetails.class.getClassLoader().getResource("/").getPath() + "Test.xml");
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(users, file);
			InputStream targetStream = new FileInputStream(file);
			CloudStorageConfig.getInstance().uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					filePath, targetStream, "application/xml");
		} catch (JAXBException | FileNotFoundException e) {
			e.printStackTrace();
		}

		fileItLogger.info("Exiting from ProcessUserDetails Processor");
		return created;
	}

	public static String encryptPassword(String password) throws FileItException {
		String chipher_Text = AesUtil.Encrypt(password);
		return chipher_Text;
	}
}
