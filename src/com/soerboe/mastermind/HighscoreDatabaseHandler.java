package com.soerboe.mastermind;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HighscoreDatabaseHandler extends SQLiteOpenHelper {
	private final static String DB_NAME = "HighScoreDb";
	private final static int DB_VERSION = 1;
	private final static String TABLE_NAME = "highscore";
	private final static String KEY_NAME = "name";
	private final static String KEY_SCORE = "score";
	private final static String KEY_LEVEL = "level";
	private final static String KEY_DATE = "date";
	
	private Context context;
	
// TABLE: highscore
// | name  | text    |
// | score | integer |
// | level | integer |
// | date  | text    |
	
	public HighscoreDatabaseHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + 
				   KEY_NAME + " TEXT PRIMARY KEY," + 
				   KEY_SCORE + " INTEGER," + 
				   KEY_LEVEL + " INTEGER," +
				   KEY_DATE + " TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
		//TODO change this behavior
	}
	
	public void addHighscore (String name, int score, int level) {
		SQLiteDatabase db = getWritableDatabase();
		
		Format formatter = new SimpleDateFormat("dd MMM yyyy");
		Date now = new Date();
		String date = formatter.format(now);
		
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_SCORE, score);
		values.put(KEY_LEVEL, level);
		values.put(KEY_DATE, date);
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	
	public void putHighscoreIntoViewGroup (ViewGroup group, int level) {
		int counter = 0;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"SELECT * FROM " + TABLE_NAME + 
				" WHERE " + KEY_LEVEL + "='" + level + "'" +
				" ORDER BY " + KEY_SCORE + " ASC", null);
		
		if (cursor != null && !cursor.moveToFirst()) {
			db.close();
			return;
		}
		
		do {
			View item = inflater.inflate(R.layout.highscore_item, null);
			item.setBackgroundColor(Color.BLACK);
			if (counter % 2 == 0)
				item.getBackground().setAlpha(30);
			else
				item.getBackground().setAlpha(60);
				
			TextView name = (TextView) item.findViewById(R.id.highscore_name);
			name.setText(cursor.getString(0));
			TextView date = (TextView) item.findViewById(R.id.highscore_date);
			date.setText(cursor.getString(3));
			TextView score = (TextView) item.findViewById(R.id.highscore_score);
			score.setText("" + cursor.getInt(1));
			group.addView(item);
			counter++;
		} while (cursor.moveToNext());
		
		db.close();
	}
}
