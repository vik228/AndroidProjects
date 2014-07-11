package com.taskreminderapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "Alerts.db";
	private static final int DATABASE_VERSION = 10;


	public DBHelper(Context context) {
		// calls the super constructor, requesting the default cursor factory.
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * 
	 * Creates the underlying database with table name and column names taken
	 * from the NotePad class.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + MainTable.TABLE_NAME + " ("
				+ MainTable._ID + " INTEGER PRIMARY KEY autoincrement ,"
				+ MainTable.COLUMN_NAME_TASK_FILE_PATH + " TEXT,"
				+ MainTable.COLUMN_NAME_TASK_TITLE + " TEXT,"
				+ MainTable.COLUMN_NAME_TAKS_DESC + " TEXT,"
				+ MainTable.COLUMN_NAME_TASK_REMINDER_OPTION+ " TEXT,"
				+ MainTable.COLUMN_ALARM_STATUS + " TEXT,"
				+ MainTable.COLUMN_NAME_TAKS_DATETIME+ " TEXT,"
				+ MainTable.COLUMN_NAME_TAKS_DATE+ " TEXT,"
				+ MainTable.COLUMN_NAME_TAKS_TIME+ " TEXT,"
				+ MainTable.COLUMN_ALARM_RINGTONE_URI+ " TEXT,"
				+ MainTable.COLUMN_IS_ALARM_SET + " TEXT);");
	}

	/**
	 * 
	 * Demonstrates that the provider must consider what happens when the
	 * underlying datastore is changed. In this sample, the database is upgraded
	 * the database by destroying the existing data. A real application should
	 * upgrade the database in place.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// Kills the table and existing data
		db.execSQL("DROP TABLE IF EXISTS "+MainTable.TABLE_NAME);

		// Recreates the database with a new version
		onCreate(db);
	}
}