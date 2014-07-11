
/*
 * This file manages the alaram scheduled, snooze the alarma, listining the audio reminder etc...
 */

package com.taskreminderapp.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.taskreminderapp.component.OnAlarmReceiver;
import com.taskreminderapp.db.MainTable;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class ReminderActivity extends FragmentActivity {
	
	
	MediaPlayer mMediaPlayer = null;
	PowerManager.WakeLock mWakeLock;
	SharedPreferences getPrefs;
	Context mContext;
	String rowid;
	String rDesc;
	ArrayList <String> row;
	boolean playing = false;
	boolean vibrate = true;
	Vibrator v = null;
	Button done;
	MainTable mainTable;
	AlarmManager mAlarmManager;
	long snoozeTime;
	String currentReminder, mTime, mDate;
	long currentReminderKey;
	Timer timer = null;
	
	
	public void initializeWakeLock()
	{
		PowerManager pm = (PowerManager)getSystemService (Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Application Lock");
		mWakeLock.acquire();
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    Window window = this.getWindow();
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
	}
	
	public long getSnoozedTime()
	{
		Date d = Calendar.getInstance().getTime();
    	Calendar c = Calendar.getInstance();
    	c.setTime(d);
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MILLISECOND, 0);
    	long time = c.getTimeInMillis();
    	
    	Log.d("SnoozeTime", String.valueOf(snoozeTime));
		time += snoozeTime;
		
		/*
		 * find the next empty slot to schedule the next alarm...
		 */
		mainTable.fillListAndMap();
		Collections.sort(ScheduledReminders.rDurations);
		while (ScheduledReminders.rDurations.contains(String.valueOf(time)))
			time += 60*1000;
		Date d1 = new Date (time);
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		mTime = String.format(Locale.getDefault(), "%02d:%02d:00",hour, minute);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		mDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year,(month + 1), day);
		
		return time;
	}
	private String findKey (String rDuration)
	{
		Iterator<String> iterator = ScheduledReminders.reminderQueue.keySet().iterator();
		while (iterator.hasNext())
		{
			String key = (String) iterator.next();
			if (ScheduledReminders.reminderQueue.get(key).equals(rDuration))
			{
				return key;
			}
		}
		return null;
	}
	@Override
	protected void onCreate(Bundle arg0) 
	{
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		initializeWakeLock();
		setContentView(R.layout.alaram_screen_activity);
        mContext = this;
        /*
         * Getting user Preferences.....
         */
        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String sTime = getPrefs.getString("snooze_value", null);
        if (sTime == null)
        	sTime = "2";
        long s_time = Long.parseLong(sTime)*60*1000;
        snoozeTime = s_time;
        
        boolean b = getPrefs.getBoolean("vibrate_in_alarm", false);
        if (b)
        {
        	 long pattern[] = { 0, 100, 200, 500, 300, 600 };
        	  v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        	  v.vibrate(pattern, 0);
        }
        /*
         * User preferences code ends...
         */
        
		currentReminderKey = getIntent().getExtras().getLong("reminderKey");
        mainTable = new MainTable (this);
        row = mainTable.getDataAt (String.valueOf(currentReminderKey));
        TextView rDesc = (TextView)findViewById (R.id.reminder_description);
        try
        {
        	rDesc.setText(row.get(1));
        }
        catch(IndexOutOfBoundsException ex)
        {
        	Log.d("size", String.valueOf(row.size()));
        }
        
        done = (Button)findViewById (R.id.change_alarm_status);
        done.setClickable(false);
        done.setFocusable(false);
        done.setVisibility(View.INVISIBLE);
        playSound (this, getAlarmUri());
        
        
	}
	
	/*
	 * This function schedules next alarm...It selects the next closest time from the current 
	 * in the database and sets the alarm on that time...
	 */
	@SuppressLint("InlinedApi")
	private void scheduleNextAlarm ()
	{
		
		
		long time = getSnoozedTime();
		String key = String.valueOf(currentReminderKey);
		String nextTime = String.valueOf(time);
		mainTable.updateReminderWithKey(key, nextTime, mTime, mDate);
		Intent i = new Intent(mContext, OnAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, (int)currentReminderKey, i, PendingIntent.FLAG_CANCEL_CURRENT);
		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		mAlarmManager.cancel(pi);
		ContentValues cv = new ContentValues ();
		cv.put(MainTable.COLUMN_IS_ALARM_SET, "no");
		mainTable.updateAlarmStatus(cv, currentReminderKey);
		ScheduledReminders.rDurations.clear();
		ScheduledReminders.reminderQueue.clear();
		mainTable.fillListAndMap();
		Collections.sort(ScheduledReminders.rDurations);
		Intent intent = new Intent (mContext, MainActivity.class);
		intent.putExtra("referer", "alarmScreen");
		intent.putExtra("key", findKey(ScheduledReminders.rDurations.get(0)));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	//Play the ringtone....
	
	private void playSound (Context context, Uri alert)
	{
		final Handler h = new Handler () {
			@Override
			public void handleMessage (Message msg)
			{
				Toast.makeText(mContext, "Snoozing alarm", Toast.LENGTH_LONG).show();
				mMediaPlayer.stop();
				if (v != null)
					v.cancel();
				scheduleNextAlarm();
			}
		};
		
		class SleepTask extends TimerTask
		{

			@Override
			public void run() {
				h.sendEmptyMessage(0);
				// TODO Auto-generated method stub
				
			}
		}
		if (alert == null)
			Log.d("ringTone", "null");
		mMediaPlayer = new MediaPlayer();
		try
		{
			mMediaPlayer.setDataSource(context, alert);
			AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamMute(AudioManager.STREAM_ALARM,true);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)
			{
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.prepare();
				mMediaPlayer.setLooping(true);
				timer = new Timer("timer",true);
				timer.schedule(new SleepTask(),49000);
				mMediaPlayer.start();
			}
		}
		catch (IOException ex)
		{
			Log.i("Audio", "No Audio Device Found " + alert);
		}
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				Toast.makeText(mContext, "Alarm snoozed", Toast.LENGTH_LONG).show();
				if (timer != null)
						timer.cancel();
				if (v != null)
				{
					v.cancel();
				}
				scheduleNextAlarm ();
				
				
			}
		});
	}
	
	//get the URI of the Ringtone...
	private Uri getAlarmUri ()
	{
		Uri ringtonePath = null;
		Log.d("In getAlarmUri", "hiiii");
		String toneUri = mainTable.getAlarmUri(String.valueOf(currentReminderKey));
		if (!toneUri.equals("notSelected"))
		{
			ringtonePath = Uri.parse(toneUri);
			return ringtonePath;
		}
		getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		RingtoneManager ringtoneManager = new RingtoneManager (this);
		Cursor cursor = ringtoneManager.getCursor();
		cursor.moveToFirst();
		
		CharSequence defaultRingtone = getPrefs.getString("ringtone", null);
		if (defaultRingtone == null)
		{
			defaultRingtone = cursor.getString(1);
		}
		String ringtoneTitle = (String)defaultRingtone;
		Uri parcialUri = Uri.parse("content://media/internal/audio/media"); 
		

		while(!cursor.isAfterLast()) 
		{
		    if(ringtoneTitle.compareToIgnoreCase(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE))) == 0) 
		    {
		    	int ringtoneID = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
		        ringtonePath = Uri.withAppendedPath(parcialUri, "" + ringtoneID );
		        break;
		    }
		    cursor.moveToNext();
		}
		return ringtonePath;
		
	}
	
	//Onclick handler to listen reminder...
	public void listenReminder(View view)
	{
		final MediaPlayer mp = new MediaPlayer ();
		final Button bReminder = (Button)findViewById (R.id.listen_reminder);
		final Button snoozeButton = (Button)findViewById (R.id.snooze);
		snoozeButton.setClickable(false);
		snoozeButton.setFocusable(false);
		TextView rTitle = (TextView)findViewById (R.id.reminder_title);
		rTitle.setText("Playing reminder");
		playing = true;
		if (v != null)
		{
			v.cancel();
			v = null;
		}
		if (mMediaPlayer != null)
		{
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if (timer != null)
				timer.cancel();
	    //rDesc = row.get(2);
	    File file = new File(Environment.getExternalStorageDirectory(),"Record");
	    String path = file.toString();
	    String filePath = "file://"+path+"/"+row.get(0);
	    Uri f = Uri.parse(filePath);
	    Log.d("filePath", f.toString());
	    try
	    {
	    	mp.setDataSource(mContext, f);
	        mp.prepare();
	        mp.start();
	        	
	    }
	    catch (Exception ex)
	    {
	    	ex.printStackTrace();
	    	//Log.d("Error Playing File", ex.getMessage());
	    	Toast.makeText(mContext, "cant play file", Toast.LENGTH_LONG).show();
	    }
	    mp.setOnCompletionListener(new OnCompletionListener() 
	    {
				
			@Override
			public void onCompletion(MediaPlayer mp) 
			{
					TextView rTitle = (TextView)findViewById (R.id.reminder_title);
				      Toast.makeText(mContext, "Media Completed", Toast.LENGTH_SHORT).show();
				      snoozeButton.setClickable(true);
				      snoozeButton.setFocusable(true);
				      done.setClickable(true);
				      done.setFocusable(true);
				      done.setVisibility(View.VISIBLE);
				      rTitle.setText("Just Played");
				      bReminder.setText("Play Again");
				        
					
					// TODO Auto-generated method stub
					
			}
		});
        
	}
	/*
	 * This will snooze the alarm and schedules it to the next snoozed time in accordance with the user
	 * preference.
	 */
	public void snoozeAlarm(View view)
	{
		if (timer != null)
			timer.cancel();
		Toast.makeText(mContext, "Alarm Snoozed", Toast.LENGTH_SHORT).show();
		//vibrate = false;
		if (v != null)
		{
			v.cancel();
		}
		if (mMediaPlayer != null)
		{
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		scheduleNextAlarm();
		
	}
	
	/*
	 * This function will change the status of a particular alarm to "Done" in the database and also schedules
	 * it to the next time in accordance with the user preference i.e daily, weekly etc 
	 */
	@SuppressLint("InlinedApi")
	public void changeAlarmStatus (View view)
	{
		boolean val = true;
		String rKey = String.valueOf(currentReminderKey);
		String option = mainTable.getOptions (rKey);
		if (option.equals("0"))
		{
			ContentValues cv = new ContentValues();
			cv.put(MainTable.COLUMN_IS_ALARM_SET, "no");
			cv.put(MainTable.COLUMN_ALARM_STATUS, "disabled");
			mainTable.updateAlarmStatus(cv, Long.valueOf(rKey));
			Intent i = new Intent(mContext, OnAlarmReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(mContext, (int)currentReminderKey, i, PendingIntent.FLAG_CANCEL_CURRENT);
			mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			mAlarmManager.cancel(pi);
			val = false;
			
			ScheduledReminders.rDurations.clear();
			ScheduledReminders.reminderQueue.clear();
			mainTable.fillListAndMap();
			boolean val1 = false;
			if (ScheduledReminders.rDurations.size() > 0)
				Collections.sort(ScheduledReminders.rDurations);
			Intent intent = new Intent (mContext, MainActivity.class);
			if (ScheduledReminders.rDurations.size() > 0)
			{
				val1 = true;
				intent.putExtra("referer", "alarmScreen");
				intent.putExtra("key", findKey(ScheduledReminders.rDurations.get(0)));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			else
			{
				finish();
			}
			
		}
		else if (option.equals("1"))
		{
			snoozeTime = 24L *3600L* 1000L;
		}
		else if (option.equals("2"))
		{
			snoozeTime = 7L * 24L *3600L* 1000L;
		}
		else if (option.equals("3"))
		{
			snoozeTime = 30L * 24L *3600L* 1000L;
		}
		else if (option.equals("4"))
		{
			snoozeTime = 365L * 24L *3600L* 1000L;
		}
		if (val)
			scheduleNextAlarm();
	}
	
	protected void onStop()
	{
		super.onStop();
		Toast.makeText(mContext, "Closing",Toast.LENGTH_LONG).show();
		if (mWakeLock.isHeld())
		{
			mWakeLock.release();
		}
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
