package com.taskreminderapp.app;

import com.taskreminderapp.component.OnAlarmReceiver;
import com.taskreminderapp.db.MainTable;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Intent intent;
	AlarmManager mAlarmManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d("size", String.valueOf(ScheduledReminders.rDurations.size()));
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			String referer = extras.getString("referer");
			if (referer.equals("alarmScreen"))
			{
				String rTime = ScheduledReminders.rDurations.get(0);
				String rKey = extras.getString("key");
				Intent i = new Intent (this, OnAlarmReceiver.class);
				i.putExtra("reminderKey", Long.parseLong(rKey));
				PendingIntent pi = PendingIntent.getBroadcast(this, (int) Long.parseLong(rKey), i, PendingIntent.FLAG_CANCEL_CURRENT);
				mAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
				mAlarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(rTime), pi);
				ContentValues cv = new ContentValues();
				cv.put(MainTable.COLUMN_IS_ALARM_SET, "yes");
				new MainTable(this).updateAlarmStatus(cv, Long.valueOf(rKey));
				finish();
			}
		}
	}
	
	/**
	 * {@link R.layout.activity_main }
	 * @see http://developer.android.com/guide/topics/ui/controls/button.html#ClickListener
	 * @param view
	 */
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_start_recording:
			intent=new Intent(this,RecordActivity.class);
			break;
		case R.id.btn_reminder:
			intent=new Intent(this,ReminderListActivity.class);
			break;
		case R.id.btn_settings:
			intent=new Intent(this,SettingsActivity.class);
			break;
		}
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onConfigurationChanged (Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	public void onBackPressed()
	{
		Toast.makeText(this, "Closing App", Toast.LENGTH_LONG).show();
		finish();
	}
	

}
