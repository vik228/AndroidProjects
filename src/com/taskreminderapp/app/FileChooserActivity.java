package com.taskreminderapp.app;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class FileChooserActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_chooser);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_chooser, menu);
		return true;
	}

}
