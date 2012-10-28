package com.soerboe.mastermind;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class Highscore extends Activity {
	private static final String KEY_IDX = "index";
	private Spinner levels;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.highscore);
		
		levels = (Spinner) findViewById(R.id.levelsSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.level_names, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    levels.setAdapter(adapter);

	    SharedPreferences settings = getPreferences(0);
	    levels.setSelection(settings.getInt(KEY_IDX, 0));
	    
	    levels.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				LinearLayout list = (LinearLayout) findViewById(R.id.highScoreList);
				HighscoreDatabaseHandler db = new HighscoreDatabaseHandler(Highscore.this.getApplicationContext());
				list.removeAllViews();
				db.putHighscoreIntoViewGroup (list, arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	@Override
	public void onStop () {
		super.onStop();
		
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(KEY_IDX, levels.getSelectedItemPosition());
		editor.commit();
	}
}
