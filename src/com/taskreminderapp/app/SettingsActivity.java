package com.taskreminderapp.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity 
{
	MediaPlayer mMediaPlayer = null;
	static List <String> entriesValues;
	static List <String> entriesList;
	final Context mContext = this;
	Dialog d;
	Uri selectedToneUri = null;
	SharedPreferences myPrefs;
	SharedPreferences.Editor myPrefsEdit;
	public static void fillRingtoneList (ListPreference lp, RingtoneManager r)
	{
		entriesList = new ArrayList <String> ();
		entriesValues = new ArrayList <String> ();
		Cursor c = r.getCursor();
		while (!c.isAfterLast())
		{
			String rToneName = c.getString(1);
			entriesList.add(rToneName);
			entriesValues.add(rToneName);
			c.moveToNext();
		}
		CharSequence[] entries = entriesList.toArray(new CharSequence[entriesList.size()] );
		CharSequence[] entryValues = entriesValues.toArray(new CharSequence[entriesValues.size()]);
		lp.setEntries(entries);
		lp.setEntryValues(entryValues);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// Ringtone code start..
		addPreferencesFromResource(R.xml.prefs);
		ListPreference ringToneChanger = (ListPreference)findPreference ("ringtone");
		RingtoneManager ringToneManager = new RingtoneManager (this);
		fillRingtoneList(ringToneChanger, ringToneManager);
		if (ringToneChanger.getValue() == null)
		{
			ringToneChanger.setValueIndex(0);
		}
		CharSequence defaultRingTone = ringToneChanger.getEntry();
		ringToneChanger.setSummary(defaultRingTone);
		ringToneChanger.getValue();
		
		ringToneChanger.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() 
		{
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) 
			{
				// TODO Auto-generated method stub
				preference.setDefaultValue(newValue);
				String defaultValue = (String)newValue;
				preference.setSummary(defaultValue);
				return true;
			}
		});
		
		EditTextPreference snoozeDuration = (EditTextPreference)findPreference ("snooze_value");
		CharSequence defaultSnoozeDuration = snoozeDuration.getText();
		snoozeDuration.setDefaultValue(defaultSnoozeDuration);
		snoozeDuration.setSummary(defaultSnoozeDuration + " mins ");
		snoozeDuration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() 
		{
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) 
			{
				// TODO Auto-generated method stub
				//playSound (mContext, getAlarmUri(newValue.toString()));
				preference.setDefaultValue(newValue);
				preference.setSummary((String)newValue + " mins ");
				return true;
			}
		});
		
		
	}

	
}
