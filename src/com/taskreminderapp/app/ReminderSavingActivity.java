package com.taskreminderapp.app;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.taskreminderapp.db.MainTable;
import com.taskreminderapp.util.AppUtil;
import com.taskreminderapp.util.ReminderManager;

public class ReminderSavingActivity extends FragmentActivity {
	private EditText mTaskTitleView;
	private EditText mTaskDescView;
	private static Button mTimeView;
	private static Button mDateView;
	private String mTaskTitle;
	private String mTaskDesc;
	private static String mTime;
	private static String mDate;
	private String mTaskFile;
	private ReminderApplication application;
	private RadioButton daily;
	private RadioButton weekly;
	private RadioButton monthly;
	private RadioButton yearly;
	private RadioButton once;
	private RadioGroup rGroup;
	private Context mContext;
	private Date date = null;
	private long alarmTime;
	private Button mBrowseTone;
	boolean toUpdate = false;
	long remId;
	String alarmStatus = "enabled";
	String referer = "unknown";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd:hh:mm:ss";
	private String toneUri = "notSelected";
	private String isAlarmSet = "no";
	MediaPlayer mp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder_saving);
		setupActionBar();
		mTimeView = (Button) findViewById(R.id.btn_time);
		mDateView = (Button) findViewById(R.id.btn_date);
		mTaskTitleView = (EditText) findViewById(R.id.edtTaskTitle);
		mTaskDescView = (EditText) findViewById(R.id.edtTaskDesc);
		int rowId = getIntent().getIntExtra(AppUtil.ROW_ID, -1);
		mTaskFile = getIntent().getStringExtra(AppUtil.FILE);
		application = (ReminderApplication) getApplication();
		daily = (RadioButton) findViewById(R.id.daily);
		weekly = (RadioButton) findViewById(R.id.weekly);
		monthly = (RadioButton) findViewById(R.id.monthly);
		yearly = (RadioButton) findViewById(R.id.yearly1);
		once = (RadioButton) findViewById(R.id.once);
		mBrowseTone = (Button)findViewById (R.id.browse_ringtone);
		mContext = this;
		if (savedInstanceState != null)
		{
			mTaskTitleView.setText(savedInstanceState.getString("title"));
			mTaskDescView.setText(savedInstanceState.getString("desc"));
			mDateView.setText(savedInstanceState.getString("date"));
			mTimeView.setText(savedInstanceState.getString("time"));
		}
		yearly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked)
				{
					once.setChecked(false);
					daily.setChecked(false);
					weekly.setChecked(false);
					monthly.setChecked(false);
				}
			}
		});
		
		rGroup = (RadioGroup)findViewById(R.id.rgb_reminder);
		//RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(rGroup.getCheckedRadioButtonId());
		rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(checkedId);
				if (checkedRadioButton.isChecked())
					yearly.setChecked(false);
				
			}
		});
		if (rowId != -1) 
		{
			loadTaskData(rowId);
		}
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			referer = extras.getString("referer");
			if (referer == null)
				referer = "unknown";
			if (referer != null && referer.equals("TaskReminderListFragment"))
			{
				remId = extras.getLong("id");
				MainTable mTable = new MainTable (this);
				ArrayList<String> al = mTable.getDataAt(String.valueOf(remId));
				mTaskFile = al.get(0);
				Log.d("taskFile",mTaskFile);
				mTaskTitle = al.get(1);
				mTaskDesc = al.get(2);
				String options = al.get(3);
				if (options.equals("0"))
						once.setChecked(true);
				else if (options.equals("1"))
					daily.setChecked(true);
				else if (options.equals("2"))
						weekly.setChecked(true);
				else if (options.equals("3"))
					monthly.setChecked(true);
				else if (options.equals("4"))
					yearly.setChecked(true);
				String alTime = al.get(5);
				alarmTime = Long.parseLong(alTime);
				mDate = al.get(6);
				mTime = al.get(7);
				mTaskTitleView.setText(mTaskTitle);
				mTaskDescView.setText(mTaskDesc);
				mDateView.setText(mDate);
				mTimeView.setText(mTime);
				alarmStatus = al.get(4);
				toneUri = al.get(8);
				if (!toneUri.equals("notSelected"))
				{
					Uri u = Uri.parse(toneUri);
					getAndSetFileName(u);
				}
				toUpdate = true;
				
			
			}
		}
		

	}

	private void loadTaskData(int rowId) 
	{
		// TODO Auto-generated method stub

	}
	
	/*
	 * This method shows the time picker Dialog
	 */

	public void showTimePickerDialog(View v) 
	{
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), "timePicker");
	}

	public void onCancelClick(View view) 
	{
		if (!toUpdate)
		{
			File file = new File(application.getRecordStorageDirectory()
					.getAbsolutePath() + "/" + mTaskFile);
			boolean deleted = file.delete();
			Toast.makeText(this, "" + deleted, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent (mContext, MainActivity.class );
			startActivity(intent);
			finish();
		}
		else
		{
			if (mp != null)
			{
				mp.stop();
				mp.release();
				mp = null;
				
			}
			Intent intent = new Intent (mContext, MainActivity.class );
			startActivity(intent);
			finish();
		}
	}

	public void showDatePickerDialog(View v) 
	{
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}
	
	/**
	 * {@link R.layout.activity_reminder_saving }
	 * @see http://developer.android.com/guide/topics/ui/controls/button.html#ClickListener
	 * @param view
	 */
	
	public void onSaveClick(View view) 
	{
		boolean cancel = false;
		mTaskTitle = mTaskTitleView.getText().toString().trim();
		mTaskDesc = mTaskDescView.getText().toString().trim();
		String s = getReminderOption();
		if (TextUtils.equals(mDateView.getText().toString(), "Date")) 
		{
			Toast.makeText(this, "Please Select Date", Toast.LENGTH_SHORT).show();
			cancel = true;
		}
		else if (TextUtils.equals(mTimeView.getText().toString(), "Time")) 
		{
			Toast.makeText(this, "Please Select Time", Toast.LENGTH_SHORT).show();
			cancel = true;
		}
		else
		{
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT,Locale.getDefault());
			try 
			{
				String date1 = mDate +":"+ mTime;
				
				//date = dateTimeFormat.parse( mDate + " " + mTime);
				date = dateTimeFormat.parse(date1);
				Log.d("pasedTime", date1);
				Log.d("pasedTime1", date.toString());
			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Date d = Calendar.getInstance().getTime();
			Log.d("currentDateAndTime", d.toString());
	    	Calendar c = Calendar.getInstance();
	    	c.setTime(d);
	    	c.set(Calendar.SECOND, 0);
	    	c.set(Calendar.MILLISECOND, 0);
	    	long currentTime = c.getTimeInMillis();
	    	
	    	//Getting the alarm time...
	    	//Log.d("alarmTime", date.toString());
			Calendar calendar=Calendar.getInstance();
			calendar.setTime(date);
			alarmTime = calendar.getTimeInMillis();
			MainTable m1 = new MainTable (mContext);
			if (alarmTime < currentTime)
			{
				//Log.d("alarmTime", String.valueOf(alarmTime));
				//Log.d("currentTime", String.valueOf(currentTime));
				Toast.makeText(mContext, "Please select a future date ", Toast.LENGTH_LONG).show();
				cancel = true;
			}
			else if (m1.getNumberOfAlarms(String.valueOf(alarmTime)) > 0 && (!toUpdate))
			{
				Toast.makeText(mContext, "Max limit reached.Please select different time ", Toast.LENGTH_LONG).show();
				cancel = true;
			}
			
		}
		if (!cancel)
		{
			saveReminder(s);
			NavUtils.navigateUpFromSameTask(this);
		}
		else
		{
			Toast.makeText(this, "Please select a valid date or Time", Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * 
	 * @param s{{@link String} 
	 * Saving Reminder to the database 
	 * and setting alarm for getting alert;
	 */
	private void saveReminder(String s) 
	{
		long mRowId = 0;
		ContentValues values = new ContentValues();
		values.put(MainTable.COLUMN_NAME_TASK_FILE_PATH, mTaskFile);
		values.put(MainTable.COLUMN_NAME_TASK_TITLE, mTaskTitle);
		values.put(MainTable.COLUMN_NAME_TAKS_DESC, mTaskDesc);
		values.put(MainTable.COLUMN_NAME_TASK_REMINDER_OPTION, s);
		values.put(MainTable.COLUMN_ALARM_STATUS, alarmStatus);
		values.put(MainTable.COLUMN_NAME_TAKS_DATETIME,String.valueOf(alarmTime));
		values.put(MainTable.COLUMN_NAME_TAKS_DATE, mDate);
		values.put(MainTable.COLUMN_NAME_TAKS_TIME, mTime);
		values.put(MainTable.COLUMN_ALARM_RINGTONE_URI, toneUri);
		values.put(MainTable.COLUMN_IS_ALARM_SET, isAlarmSet);
		MainTable rTable = new MainTable (this);
		if (toUpdate)
		{
			rTable.updateAlarmStatus(values, remId);
			mRowId = remId;
		}
		else
		{
			mRowId= rTable.save(values);
		}
		if (!alarmStatus.equals("disabled"))
			new ReminderManager(mContext).setReminder(alarmTime,mRowId, toUpdate);
	}

	private String getReminderOption() 
	{
		String s = "1";
		if (once.isChecked())
			s = "0";
		if (daily.isChecked()) 
		{
			s = "1";
		}
		if (weekly.isChecked()) 
		{
			s = "2";
		}
		if (monthly.isChecked()) 
		{
			s = "3";
		}
		if (yearly.isChecked()) 
		{
			s = "4";
			
		}
		return s;
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

	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener 
	{
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) 
			{
				// Use the current time as the default values for the picker
				final Calendar c = Calendar.getInstance();
				int hour = c.get(Calendar.HOUR_OF_DAY);
				int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
				return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
			}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
		{
			// Do something with the time chosen by the user
			mTimeView.setText(String.format(Locale.getDefault(), "%02d:%02d",hourOfDay, minute));
			mTime = String.format(Locale.getDefault(), "%02d:%02d:00",hourOfDay, minute);
			Log.d("TAG", mTime);
		}
	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener 
	{

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) 
		{
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) 
		{
			mDateView.setText(String.format(Locale.getDefault(),"%02d-%02d-%d", day, (month + 1), year));
			mDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year,(month + 1), day);
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (!referer.equals("TaskReminderListFragment") && keyCode == KeyEvent.KEYCODE_BACK)
		{
			File file = new File(application.getRecordStorageDirectory()
					.getAbsolutePath() + "/" + mTaskFile);
			boolean deleted = file.delete();
			Toast.makeText(this, "" + "deleted", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent (mContext, MainActivity.class );
			startActivity(intent);
			finish();
		}
		else if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Intent intent = new Intent (mContext, MainActivity.class );
			startActivity(intent);
			finish();
		}
		return true;
	}
	
	public void browseRingtone(View v)
	{
		final Dialog dialog = new Dialog (this);
		dialog.setContentView(R.layout.file_chooser);
		dialog.show();
		Button browse = (Button)dialog.findViewById(R.id.galery_browser);
		browse.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent ();
				intent.setType("audio/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Ringtone"), 200);
				dialog.dismiss();
				
			}
		});
	}
	
	private void getAndSetFileName (Uri uri)
	{
		String toneName = "tone";
		String scheme = uri.getScheme();
		if (scheme.equals("file"))
			toneName = uri.getLastPathSegment();
		else if (scheme.equals("content"))
		{
			String[] proj = {MediaStore.Audio.Media.TITLE};
			 Cursor cursor = this.getContentResolver().query(uri, proj, null, null, null);
			 if (cursor != null && cursor.getCount() != 0)
			 {
				 int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
				 cursor.moveToFirst();
				 toneName = cursor.getString(columnIndex);
			 }
				 
		}
		mBrowseTone.setText(toneName);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 200)
		{
			if (resultCode == RESULT_OK)
			{
				Uri RingtoneUri = data.getData();
				//String path = RingtoneUri.getPath();
				toneUri = RingtoneUri.toString();
				getAndSetFileName (RingtoneUri);
				Toast.makeText(mContext, toneUri, Toast.LENGTH_LONG).show();
				
			}
			
		}
	}
	public void onConfigurationChanged (Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	public void listenReminderAgain (View v)
	{
		mp = new MediaPlayer ();
		File file = new File(Environment.getExternalStorageDirectory(),"Record");
	    String path = file.toString();
	    String filePath = "file://"+path+"/"+mTaskFile;
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
				    Toast.makeText(mContext, "Media Completed", Toast.LENGTH_SHORT).show();
				      // TODO Auto-generated method stub
					
			}
		});
	    
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) 
	{
		  super.onSaveInstanceState(savedInstanceState);
		  savedInstanceState.putString("title", mTaskTitle);
		  savedInstanceState.putString("desc", mTaskDesc);
		  savedInstanceState.putString("date", mDate);
		  savedInstanceState.putString("time", mTime);
	}
	
}
