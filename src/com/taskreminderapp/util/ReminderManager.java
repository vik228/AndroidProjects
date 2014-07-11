package com.taskreminderapp.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.taskreminderapp.component.OnAlarmReceiver;
import com.taskreminderapp.db.MainTable;
import com.taskreminderapp.fragment.TaskReminderListFragment;

@SuppressLint("SimpleDateFormat")
public class ReminderManager 
{
	private Context mContext;
	private AlarmManager mAlarmManager;

	public ReminderManager(Context context) 
	{
		mContext = context;
		//mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}
	/*
	 * this function sets the reminder...It sorts the alarm times in ascending order and sets the alarm
	 * to the time closest to the current time... 
	 */
	public void setReminder(long alarmTime, long alarmId, boolean toUpdate) 
	{
		/*
		 * Schedule the first one...
		 */
		Date newDate = new Date (alarmTime);
		DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
		String displayDate = df1.format(newDate);
		Intent i = new Intent(mContext, OnAlarmReceiver.class);
		i.putExtra("reminderKey", alarmId);
		PendingIntent pi = PendingIntent.getBroadcast(mContext,(int)alarmId, i, PendingIntent.FLAG_CANCEL_CURRENT);
		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		mAlarmManager.set(AlarmManager.RTC_WAKEUP,alarmTime , pi);
		ContentValues cv = new ContentValues();
		cv.put(MainTable.COLUMN_IS_ALARM_SET, "yes");
		new MainTable(mContext).updateAlarmStatus(cv, alarmId);
		
	} 
	

}
