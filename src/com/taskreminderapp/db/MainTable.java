package com.taskreminderapp.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.taskreminderapp.app.ScheduledReminders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class MainTable implements BaseColumns{
	 // This class cannot be instantiated
	private SQLiteDatabase database;
	private DBHelper helper;
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss";
    public MainTable (Context context)
    {
    	helper = new DBHelper (context);
    }
    
    private void open ()
    {
    	database = helper.getWritableDatabase();
    }
    
    private void close()
    {
    	helper.close();
    }
    public static final String TABLE_NAME = "tasktable";
    public static final String AUTHORITY = "com.taskreminderapp.app.PROVIDER";
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/tasktable");

    /**
     * The content URI base for a single row of data. Callers must
     * append a numeric row id to this Uri to retrieve a row
     */
    public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse("content://" + AUTHORITY + "/tasktable/");

    /**
     * The MIME type of {@link #CONTENT_URI}.
     */
    public static final String CONTENT_TYPE
            = "vnd.android.cursor.dir/com.taskreminderapp.app";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
     */
    public static final String CONTENT_ITEM_TYPE
            = "vnd.android.cursor.item/com.taskreminderapp.app";
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "_id DESC";

    /**
     * Column name for the single column holding our data.
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_TASK_FILE_PATH = "file";
    public static final String COLUMN_NAME_TASK_REMINDER_OPTION = "reminder_option";
    public static final String COLUMN_NAME_TASK_TITLE = "task_title";
    public static final String COLUMN_NAME_TAKS_DESC = "task_desc";
    public static final String COLUMN_NAME_TAKS_DATETIME = "task_date_time";
    public static final String COLUMN_NAME_TAKS_DATE = "date";
    public static final String COLUMN_NAME_TAKS_TIME = "time";
    public static final String COLUMN_ALARM_STATUS = "status";
    public static final String COLUMN_ALARM_RINGTONE_URI = "toneUri";
    public static final String COLUMN_IS_ALARM_SET = "isalarmset";
    
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT,Locale.getDefault()); 
	Date date = null;
    
    public long save(ContentValues reminderDetails)
    {
    	open();
    	long insertId = database.insert(TABLE_NAME, null, reminderDetails);
    	close();
    	return insertId;
    }
    public ArrayList <String> getDataAt (String rowid)
    {
    	ArrayList <String> row = new ArrayList<String> ();
    	open();
    	Cursor c = database.rawQuery("select * from " + TABLE_NAME , null);
    	c.moveToFirst();
    	while (!c.isAfterLast())
    	{
    		if (c.getString(0).equals(rowid))
    		{
    			row.add(c.getString(1));
    			row.add(c.getString(2));
    			row.add(c.getString(3));
    			row.add(c.getString(4));
    			row.add(c.getString(5));
    			row.add(c.getString(6));
    			row.add(c.getString(7));
    			row.add(c.getString(8));
    			row.add(c.getString(9));
    			row.add(c.getString(10));
    			break;
    		}
    		c.moveToNext();
    		
    	}
    	close();
    	return row;
    }
    
    public String getOptions(String id)
    {
    	String option = null;
    	open();
    	Cursor c = database.rawQuery("select * from " + TABLE_NAME + " where " + _ID + " = " + id, null);
    	c.moveToFirst();
    	option = c.getString(4);  
    	close();
    	return option;
    }
    
    public void updateReminderWithKey (String key, String time, String mTime, String mDate)
    {
    	ContentValues cv = new ContentValues ();
    	cv.put(COLUMN_NAME_TAKS_DATETIME, time);
    	cv.put(COLUMN_NAME_TAKS_DATE, mDate);
    	cv.put(COLUMN_NAME_TAKS_TIME, mTime);
    	
    	open();
    	database.update(TABLE_NAME, cv, _ID + " = '" + key +"'", null);
    	close();
    }
    
   
    /*
     * This function will fill the structures in ScheduleReminder class..
     */
    public void fillListAndMap ()
    {
    	Date d = Calendar.getInstance().getTime();
    	Calendar c = Calendar.getInstance();
    	c.setTime(d);
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MILLISECOND, 0);
    	long currentTimeInLong = c.getTimeInMillis();
    	ScheduledReminders.rDurations.clear();
    	ScheduledReminders.reminderQueue.clear();
    	open();
    	Cursor c1 = database.rawQuery("select * from " + TABLE_NAME, null);
    	c1.moveToFirst();
    	while (!c1.isAfterLast())
    	{
    		long time1 = Long.parseLong(c1.getString(6));
    		String alarmStatus = c1.getString(c1.getColumnIndex(MainTable.COLUMN_ALARM_STATUS));
    		Log.d("diff", String.valueOf(currentTimeInLong-time1));
    		if (alarmStatus.equals("disabled"))
    		{
    				c1.moveToNext();
    				continue;
    		}
    		
    		if (time1 < currentTimeInLong)
    		{
    			Log.d("CurrentTimeInLong", String.valueOf(currentTimeInLong));
    			Log.d("alarmTime", c1.getString(6));
    			
    			c1.moveToNext();
    			continue;
    			
    		}
    		ScheduledReminders.rDurations.add(c1.getString(6));
    		ScheduledReminders.reminderQueue.put(c1.getString(0), c1.getString(6));
    		c1.moveToNext();
    		
    	}
    	close();
    }

	public void updateAlarmStatus(ContentValues cv, long remId) {
		// TODO Auto-generated method stub
		open();
		database.update (MainTable.TABLE_NAME, cv, MainTable._ID + " = '" + remId +"'", null);
		close();
		
	}

	public String getAlarmStatus(long remId) {
		// TODO Auto-generated method stub
		open();
		Cursor c = database.rawQuery("select " + COLUMN_ALARM_STATUS + " from " + TABLE_NAME + " where " + _ID + " = " + remId , null);
		c.moveToFirst();
		String status = c.getString(c.getColumnIndex(MainTable.COLUMN_ALARM_STATUS));
		close();
		return status;
		
		
	}
	
	public int getNumberOfAlarms (String alarmTime)
	{
		open();
		Cursor c = database.rawQuery("select * from " + TABLE_NAME + " where " + COLUMN_NAME_TAKS_DATETIME + " = " + alarmTime, null);
		c.moveToFirst();
		int numRows = c.getCount();
		close();
		return numRows;
	}
	
	public void deleteAlarm (long key)
	{
		open();
		database.delete(TABLE_NAME, _ID + " = " + key, null);
		close();
	}

	public String getAlarmUri(String currentReminderKey) 
	{
		// TODO Auto-generated method stub
		String remUri = null;
		open();
		Cursor c1 = database.rawQuery("select " + COLUMN_ALARM_RINGTONE_URI + " from " + TABLE_NAME + " where " + _ID + " = " + currentReminderKey, null);
		c1.moveToFirst();
		remUri = c1.getString(c1.getColumnIndex(COLUMN_ALARM_RINGTONE_URI));
		close();
		return remUri;
	}
	
	public int getNumberOfSetAlarm ()
	{
		int cnt = 0;
		open();
		Cursor c1 = database.rawQuery("select * from " + TABLE_NAME + " where " + MainTable.COLUMN_IS_ALARM_SET + " = 'yes'", null );
		c1.moveToFirst();
		cnt = c1.getCount();
		close();
		Log.d("number of set alarms", String.valueOf(cnt));
		return cnt;
	}
    
}
