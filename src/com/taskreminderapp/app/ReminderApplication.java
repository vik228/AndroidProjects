package com.taskreminderapp.app;

import java.io.File;

import com.taskreminderapp.util.AppUtil;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

public class ReminderApplication extends Application {
	/* Checks if external storage is available for read and write */
	private static final String FILE_NAME = "Record";

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public File getRecordStorageDirectory(){

		File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);
		if (!file.mkdirs()) {
			Log.e(AppUtil.TAG, "Directory not created");
		}
		return file;
	}

}
