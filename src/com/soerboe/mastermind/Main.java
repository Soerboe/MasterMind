package com.soerboe.mastermind;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Main extends Activity {
//	private static final String TAG = "MasterMind";
	private static final int SELECT_LEVEL = 0;
	private static final String KEY_LEVEL = "level";
	private static String[] mLevelNames;
	private static final String[] mDescriptions = {
		"4 pegs\n4 colors\nNo duplicates",
		"4 pegs\n2 colors\nDuplicates allowed",
		"4 pegs\n6 colors\nNo duplicates",
		"4 pegs\n6 colors\nDuplicates allowed",
		"6 pegs\n9 colors\nNo duplicates",
		"6 pegs\n9 colors\nDuplicates allowed"
	};
	private static final int[] mNumPegsInLevel = {4, 4, 4, 4, 6, 6};
	private static final int[] mNumColorsInLevel = {4, 2, 6, 6, 9, 9};
	private static final boolean[] mDuplicateInLevel = {false, true, false, true, false, true};

	private Dialog mDialog;
	private View selectLevelLayout;
	private SeekBar seekbar;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLevelNames = getResources().getStringArray(R.array.level_names);
        setContentView(R.layout.main);
        Button newGame = (Button) findViewById(R.id.btnNewGame);
        newGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Main.this.showDialog(SELECT_LEVEL);
			}
		});
        
        Button highscore = (Button) findViewById(R.id.btnHighscore);
        highscore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, Highscore.class);
				Main.this.startActivity(intent);
			}
		});
    }
    
    @Override
    protected void onResume () {
    	super.onResume();
    	
    	CurrentGameDatabaseHandler db = new CurrentGameDatabaseHandler(getApplicationContext());
    	if (db.hasCurrentGame()) {
    		Intent intent = new Intent(Main.this, Game.class);
    		intent.putExtra("HAS_CURRENT_GAME", true);
			Main.this.startActivity(intent);
    	}
    }
    
    @Override
    protected void onSaveInstanceState (Bundle outState) {
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    protected Dialog onCreateDialog (int id) {
    	if (id == SELECT_LEVEL) {
            AlertDialog.Builder builder;
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);     
            selectLevelLayout = inflater.inflate(R.layout.select_level,(ViewGroup) findViewById(R.id.SelectLevelBaseLayout));     

            seekbar = (SeekBar) selectLevelLayout.findViewById(R.id.levels);
            Button createGame = (Button) selectLevelLayout.findViewById(R.id.btnCreateGame);
            
            seekbar.setMax(mLevelNames.length-1);
    	    SharedPreferences settings = getPreferences(0);
            seekbar.setProgress(settings.getInt(KEY_LEVEL, Game.LEVEL_TOO_EASY));
            seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener () {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					changeOnSeekBarChange(progress);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
            	
            });
            
            createGame.setOnClickListener(new OnClickListener () {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Main.this, Game.class);
					int level = seekbar.getProgress();
					intent.putExtra("PEGS", mNumPegsInLevel[level]);
					intent.putExtra("COLORS", mNumColorsInLevel[level]);
					intent.putExtra("DUPLICATES", mDuplicateInLevel[level]);
					intent.putExtra("LEVEL", level);
					mDialog.dismiss();
					SharedPreferences settings = Main.this.getPreferences(0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putInt(KEY_LEVEL, level);
					editor.commit();
					Main.this.startActivity(intent);
				}
            });


            builder = new AlertDialog.Builder(this); 
            builder.setTitle(getString(R.string.select_level));
            builder.setView(selectLevelLayout);     
            mDialog = builder.create();
            changeOnSeekBarChange(seekbar.getProgress());
    	} else {
    		mDialog = null;
    	}
    	
    	return mDialog;
    }
    
    private void changeOnSeekBarChange (int index) {
        TextView levelName = (TextView) selectLevelLayout.findViewById(R.id.level_name);
        levelName.setText(mLevelNames[index]);
        TextView description = (TextView) selectLevelLayout.findViewById(R.id.description);
        description.setText(mDescriptions[index]);
    }
}