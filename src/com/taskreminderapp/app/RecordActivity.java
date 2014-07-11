package com.taskreminderapp.app;

import java.io.IOException;

import com.taskreminderapp.util.AppUtil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;

public class RecordActivity extends Activity implements MediaRecorder.OnInfoListener
{

	private static final String LOG_TAG = "RecordActivity";
	private MediaRecorder mRecorder;
	private String mFileName;
	private ReminderApplication application;
	private Chronometer mTimerChronometer;
	private String taskfilename;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		mTimerChronometer = (Chronometer) findViewById(R.id.chrn_time_es);
		setupActionBar();
		application = (ReminderApplication) getApplication();
		if (application.isExternalStorageWritable()) 
		{
			taskfilename = System.currentTimeMillis()+ ".amr";
			mFileName = application.getRecordStorageDirectory().getAbsolutePath() + "/" + taskfilename;
			mTimerChronometer.start();
			mTimerChronometer.setBase(SystemClock.elapsedRealtime());
			startRecording();
		} else 
		{
			Log.d(LOG_TAG, "External Storage Not avalable");
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() 
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void startRecording() 
	{
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setMaxDuration(10000);
		mRecorder.setOnInfoListener(this);
		
		try 
		{
			mRecorder.prepare();
		} 
		catch (IOException e) 
		{
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	/**
	 * {@link R.layout.activity_record}
	 * 
	 * @param view
	 */
	public void onStop(View view) 
	{
		stopRecording();
	}

	private void stopRecording() 
	{
		mTimerChronometer.stop();
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
		Toast.makeText(this,taskfilename, Toast.LENGTH_SHORT).show();
		Intent intent=new Intent(this, ReminderSavingActivity.class);
		intent.putExtra(AppUtil.FILE,taskfilename);
		//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
			case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() 
	{
		super.onPause();
		if (mRecorder != null) 
		{
			mRecorder.release();
			mRecorder = null;
		}
	}

	@Override
	public void onInfo(MediaRecorder arg0, int arg1, int arg2) 
	{
		if (arg1 == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
		{
			Toast.makeText(this, "Maximum duration reached..autosaving.. ", Toast.LENGTH_LONG).show();
			stopRecording();
		}
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBackPressed ()
	{
		
	}
	
	public void onConfigurationChanged (Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}
