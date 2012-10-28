package com.soerboe.mastermind;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.ViewGroup;

public class CurrentGameDatabaseHandler extends SQLiteOpenHelper {
	private static final String DB_NAME = "currentGame";
	private static final int DB_VERSION = 2;
	private static final String TABLE_ANSWERS = "answers";
	private static final String TABLE_INFO = "info";
	private static final String KEY_POS = "pos";
	private static final String KEY_COLORS = "colors";
	private static final String KEY_NUMPEGS = "num_pegs";
	private static final String KEY_NUMCOLORS = "num_colors";
	private static final String KEY_LEVEL = "level";
	private static final String KEY_ZOOM = "zoom";
	private static final String KEY_SOLUTION = "solution";

	
//	TABLE: answers
//	| pos	  | integer |
//	| colors  | text    |
	
//	TABLE: info
//	| num_pegs    | integer |
//	| num_colors  | integer |
//	| level		  | integer |
//  | zoom        | integer |
//	| solution    | text    |

	public CurrentGameDatabaseHandler (Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_INFO + "(" + 
				KEY_NUMPEGS + " INTEGER PRIMARY KEY," +
				KEY_NUMCOLORS + " INTEGER," +
				KEY_LEVEL + " INTEGER," +
				KEY_ZOOM + " INTEGER," +
				KEY_SOLUTION + " TEXT)");
		db.execSQL("CREATE TABLE " + TABLE_ANSWERS + "(" + 
				KEY_POS + " INTEGER PRIMARY KEY," +
				KEY_COLORS + " TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWERS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO);
		onCreate(db);
	}
	
	public void addInfo (int numPegs, int numColors, int level, int zoom, int [] solution) {
		SQLiteDatabase db = getWritableDatabase();
		
		String s = "";
		for (int p : solution)
			s += "" + p + " ";
		
		ContentValues values = new ContentValues();
		values.put(KEY_NUMPEGS, numPegs);
		values.put(KEY_NUMCOLORS, numColors);
		values.put(KEY_LEVEL, level);
		values.put(KEY_ZOOM, zoom);
		values.put(KEY_SOLUTION, s);
		
		db.insert(TABLE_INFO, null, values);
		db.close();
	}
	
	public void add (Answer ans) {
		SQLiteDatabase db = getWritableDatabase();
		int [] pegs = ans.getSelectedPegs();
		String s = "";
		for (int p : pegs)
			s += "" + p + " ";
		
	    ContentValues values = new ContentValues();
	    values.put(KEY_POS, ans.getAnswerNumber());
	    values.put(KEY_COLORS, s);
	 
	    db.insert(TABLE_ANSWERS, null, values);
	    db.close();
	}
	
	public void add (ViewGroup view) {
		SQLiteDatabase db = getWritableDatabase();
		
		for (int i = 0; i < view.getChildCount(); i++) {
			Answer ans = (Answer) view.getChildAt(i);
			int [] pegs = ans.getSelectedPegs();
			String s = "";
			for (int p : pegs)
				s += "" + p + " ";
			
		    ContentValues values = new ContentValues();
		    values.put(KEY_POS, ans.getAnswerNumber());
		    values.put(KEY_COLORS, s);
		 
		    db.insert(TABLE_ANSWERS, null, values);
		}
		
		db.close();
	}
	
	public List<int[]> getAnswers () {
		List<int[]> list = new ArrayList<int[]>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ANSWERS +
				" ORDER BY " + KEY_POS + " ASC", null);
		
		if (cursor != null && cursor.moveToFirst()) {
			do {
				String[] sarr = cursor.getString(1).split(" ");
				int [] pegs = new int[sarr.length];
				for (int i = 0; i < sarr.length; i++)
					pegs[i] = Integer.parseInt(sarr[i]);
				list.add(pegs);
			} while (cursor.moveToNext());
		}
		
		cursor.close();
		db.close();
		return list;
	}
	
	public int[] getInfo() {
		int[] arr = null;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INFO, null);
		
		if (cursor != null && cursor.moveToFirst()) {
			int numPegs = cursor.getInt(0);
			arr = new int[numPegs+4];
			arr[0] = numPegs;
			arr[1] = cursor.getInt(1);
			arr[2] = cursor.getInt(2);
			arr[3] = cursor.getInt(3);
			String[] sarr = cursor.getString(4).split(" ");
			for (int i = 4; i < arr.length; i++)
				arr[i] = Integer.parseInt(sarr[i-4]);
		}
		
		cursor.close();
		db.close();
		return arr;
	}
	
	public void clear () {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_ANSWERS, null, null);
		db.delete(TABLE_INFO, null, null);
		db.close();
	}
	
	public boolean hasCurrentGame () {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ANSWERS, null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count != 0;
	}

}
