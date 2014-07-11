package com.taskreminderapp.app;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class ScheduledReminders 
{
	/*
	 * These two structures will store all the alarm set..so that we don't have to query the database repeatedly.
	 * The ArrayList will hold the alarm times in millisecond. The map will be to used to map alarm times with 
	 * unique IDs.
	 */
	public static SortedMap <String, String> reminderQueue = new TreeMap<String, String>();
	public static ArrayList <String> rDurations = new ArrayList<String>();
}
