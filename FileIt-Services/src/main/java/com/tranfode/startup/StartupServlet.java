package com.tranfode.startup;

import javax.servlet.http.HttpServlet;

import com.tranfode.processor.PrepareClassificationMap;
import com.tranfode.util.FileInfoPropertyReader;
import com.tranfode.util.FileUtil;

public class StartupServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StartupServlet() {
		try {
			FileUtil.checkBookClassificationJson();
			FileUtil.checkClassificationListJson();
			PrepareClassificationMap
					.createClassifiedMap(FileInfoPropertyReader.getInstance().getString("masterjson.file.path"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
