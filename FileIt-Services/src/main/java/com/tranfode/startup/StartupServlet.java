package com.tranfode.startup;

import java.util.Timer;
import java.util.TimerTask;

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
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Timer timer = new Timer();
		TimerTask myTask = new TimerTask() {
			@Override
			public void run() {
				// whatever you need to do every 2 seconds
				try {
					PrepareClassificationMap.createClassifiedMap(
							FileInfoPropertyReader.getInstance().getString("masterjson.file.path"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		timer.schedule(myTask, 20000, 20000);
	}

}
