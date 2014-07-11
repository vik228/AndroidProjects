package com.taskreminderapp.fragment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.taskreminderapp.app.MainActivity;
import com.taskreminderapp.app.R;
import com.taskreminderapp.app.ReminderSavingActivity;
import com.taskreminderapp.app.ScheduledReminders;
import com.taskreminderapp.component.OnAlarmReceiver;
import com.taskreminderapp.db.MainTable;
import com.taskreminderapp.util.ReminderManager;

public class TaskReminderListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "AlertFragment";

	// This is the Adapter being used to display the list's data.
	MyAdapter mAdapter;

	// If non-null, this is the current filter the user has provided.
	String mCurFilter;

	// Task we have running to populate the database.
	TaskReminderListFragment t1 = this;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText("No data.");
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new MyAdapter(getActivity());
		setListAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();

		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);

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
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Insert desired behavior here.
		Log.i(TAG, "Item clicked: " + id);
	}

	// These are the rows that we will retrieve.
	static final String[] PROJECTION = new String[] { MainTable._ID,
			MainTable.COLUMN_NAME_TASK_FILE_PATH,
			MainTable.COLUMN_NAME_TASK_TITLE, MainTable.COLUMN_NAME_TAKS_DESC,
			MainTable.COLUMN_NAME_TAKS_DATETIME,
			MainTable.COLUMN_NAME_TASK_REMINDER_OPTION};

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cl = new CursorLoader(getActivity(),
				MainTable.CONTENT_URI, PROJECTION, null, null, null);

		return cl;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	public class MyAdapter extends CursorAdapter {

		public MyAdapter(Context context) {
			super(context, null, false);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) 
		{
			
			// TODO Auto-generated method stub
			final Intent i = new Intent (context, ReminderSavingActivity.class);
			i.putExtra("referer", "TaskReminderListFragment");
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			String displayDate = "";
			displayDate = getDisplayDate(cursor, displayDate);
			final long remId = cursor.getLong(cursor.getColumnIndex(MainTable._ID));
			i.putExtra("id", remId);
			if (TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(MainTable.COLUMN_NAME_TASK_TITLE)))) 
			{
				viewHolder.title.setText(displayDate);
				viewHolder.desc.setVisibility(View.GONE);
			} 
			else 
			{
				viewHolder.title.setText(cursor.getString(cursor.getColumnIndex(MainTable.COLUMN_NAME_TASK_TITLE)));
				viewHolder.desc.setVisibility(View.VISIBLE);
				viewHolder.desc.setText(displayDate);
			}
			viewHolder.editButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.d("ID", String.valueOf(remId));
					startActivity (i);
					getActivity().finish();
					
				}
			});
			final Context c = context;
			final MainTable m1 = new MainTable (context);
			String status = m1.getAlarmStatus (remId);
			final Cursor c1 = cursor;
			
			viewHolder.delete.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					final Intent i = new Intent (c, OnAlarmReceiver.class);
					PendingIntent pi = PendingIntent.getBroadcast(c, (int)remId, i, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager mAlarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
					mAlarmManager.cancel(pi);
					File file = new File(Environment.getExternalStorageDirectory(),"Record");
				    String path = file.toString();
				    //String filePath = "file://"+path+"/"+c1.getString(c1.getColumnIndex(MainTable.COLUMN_NAME_TASK_FILE_PATH));
				    File f1 = new File(path);
					boolean deleted = f1.delete();
					Log.d("deleted", String.valueOf(deleted));
					m1.deleteAlarm(remId);
					m1.fillListAndMap();
					if (ScheduledReminders.rDurations.size() > 0)
					{
						Collections.sort(ScheduledReminders.rDurations);
						String alarmTime = ScheduledReminders.rDurations.get(0);
						String alarmKey = findKey(alarmTime);
						new ReminderManager(mContext).setReminder(Long.parseLong(alarmTime), Long.parseLong(alarmKey), false);
						getLoaderManager().restartLoader(0, null, t1);
					}
					getLoaderManager().restartLoader(0, null, t1);
					
				}
			});
			if (status.equals("enabled"))
				viewHolder.toggleButton.setChecked(true);
			else
				viewHolder.toggleButton.setChecked(false);
			final ToggleButton tb = viewHolder.toggleButton;
			tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					final Intent i = new Intent (c, OnAlarmReceiver.class);
					PendingIntent pi = PendingIntent.getBroadcast(c, (int)remId, i, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager mAlarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
					mAlarmManager.cancel(pi);
					if (!isChecked)
					{
						Log.d("disabling alaem", Boolean.toString(isChecked));
						ContentValues cv = new ContentValues ();
						cv.put(MainTable.COLUMN_ALARM_STATUS, "disabled");
						cv.put(MainTable.COLUMN_IS_ALARM_SET, "no");
						m1.updateAlarmStatus(cv, remId);
						
					}
					else
					{
						Log.d("Enabling alarm", Boolean.toString(isChecked));
						ContentValues cv = new ContentValues ();
						cv.put(MainTable.COLUMN_ALARM_STATUS, "enabled");
						cv.put(MainTable.COLUMN_IS_ALARM_SET, "no");
						m1.updateAlarmStatus(cv, remId);
					}
					m1.fillListAndMap();
					if (ScheduledReminders.rDurations.size() > 0)
					{
						Collections.sort(ScheduledReminders.rDurations);
						String alarmTime = ScheduledReminders.rDurations.get(0);
						String alarmKey = findKey(alarmTime);
						new ReminderManager(mContext).setReminder(Long.parseLong(alarmTime), Long.parseLong(alarmKey), false);
					}
				}
			});

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
			ViewHolder holder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(context);

			/*
			 * Creates a new View by inflating the specified layout file. The
			 * root ViewGroup is the root of the layout file.
			 */
			View layoutView = inflater.inflate(R.layout.list_row_item, null);
			holder.title = (TextView) layoutView
					.findViewById(android.R.id.text1);

			holder.desc = (TextView) layoutView
					.findViewById(android.R.id.text2);
			// Sets the layoutView's tag to be the same as the ViewHolder
			// tag.
			holder.editButton = (Button)layoutView.findViewById(R.id.editReminder);
			layoutView.setTag(holder);
			
			holder.toggleButton = (ToggleButton)layoutView.findViewById (R.id.enable_disable_alarm);
			
			holder.delete = (Button)layoutView.findViewById(R.id.delete_reminder);
			return layoutView;
		}
	}

	static class ViewHolder {
		Button delete;
		ToggleButton toggleButton;
		Button editButton;
		TextView title;
		TextView desc;
	}

	private String getDisplayDate(Cursor cursor, String displayDate) 
	{
		String mTableDate = cursor.getString(cursor.getColumnIndex(MainTable.COLUMN_NAME_TAKS_DATETIME));
		long displayDate1 = Long.parseLong(mTableDate);
		Date newDate = new Date (displayDate1);
		DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
		displayDate = df1.format(newDate);
		return displayDate;
		
	}

}
