package com.tranfode.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.tranfode.Constants.ErrorCodeConstants;
import com.tranfode.domain.BusinessErrorData;

@SuppressWarnings("rawtypes")
public class ServiceExceptionMapper implements ExceptionMapper {

	@Override
	public Response toResponse(Throwable exception) {
		// TODO Auto-generated method stub
		BusinessErrorData businessErrorData = new BusinessErrorData();
		// System.out.println("Exception from Service Layer: "+exception.getMessage());
		if ((exception instanceof FileItException) && null != ((FileItException) exception).getErrorId()) {
			businessErrorData.setErrorId(((FileItException) exception).getErrorId());
			businessErrorData.setDescription(((FileItException) exception).getErrorMessage());

		} else if(((FileItException) exception).getMessage().contains("Login Failure")){
			businessErrorData.setErrorId(ErrorCodeConstants.ERR_CODE_0006);
			businessErrorData.setDescription(ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0006));
		}else {
			businessErrorData.setErrorId(ErrorCodeConstants.ERR_CODE_0001);
			businessErrorData
					.setDescription(ErrorMessageReader.getInstance().getString(ErrorCodeConstants.ERR_CODE_0001));
		}
		Response response = Response.status(Response.Status.OK).entity(businessErrorData).build();
		return response;
	}

}