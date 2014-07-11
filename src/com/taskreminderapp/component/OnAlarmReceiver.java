package com.taskreminderapp.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.util.Log;

import com.taskreminderapp.app.ReminderActivity;

public class OnAlarmReceiver extends BroadcastReceiver
{

	private static final String TAG = ComponentInfo.class.getCanonicalName();

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		Bundle extras = intent.getExtras();
		long key = extras.getLong("reminderKey");
		Log.d(TAG, "Received wake up from alarm manager.");
		Intent intentone = new Intent(context.getApplicationContext(), ReminderActivity.class);
		intentone.putExtra("reminderKey", key);
		intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intentone);

	}
}
