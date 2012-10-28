package com.soerboe.mastermind;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class Game extends Activity {
	public int mNumPegs;
	public int mNumColors;
	private int[] mCorrectPegs;
	private int mNumAnswers = 1;
	private Answer mCurrentAnswer;
	private int mLevel;
	private int mZoom = 1;
	private boolean mFinished = false;

	private Dialog mDialog;
	private LinearLayout mAnswerBoard;
	private Button mButtonDone;
	private ScrollView mScrollView;
	private LinearLayout mBaseLayout;
	private Solution mSolution;

	public static final int LEVEL_TOO_EASY = 0;
	public static final int LEVEL_VERY_EASY = 1;
	public static final int LEVEL_EASY = 2;
	public static final int LEVEL_MEDIUM = 3;
	public static final int LEVEL_HARD = 4;
	public static final int LEVEL_VERY_HARD = 5;

	private static final int PEG_PICKER = 0;
	private static final int REGISTER_HIGHSCORE = 1;
//	private static final String TAG = "MasterMind";
	private static final int MIN_ZOOM = 1;
	private static int MAX_ZOOM;

	public static final int[] PEGS = {
		R.drawable.blue,
		R.drawable.red,
		R.drawable.green,
		R.drawable.yellow,
		R.drawable.purple,
		R.drawable.cyan,
		R.drawable.black,
		R.drawable.white,
		R.drawable.orange
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean hasCurrentGame = getIntent().getBooleanExtra("HAS_CURRENT_GAME", false);
		if (hasCurrentGame) {
			loadCurrentGameInfo();
//			if (loadCurrentGameInfo () == false) {
//				CurrentGameDatabaseHandler db = new CurrentGameDatabaseHandler(getApplicationContext());
//				db.clear();
//				this.finish();
//			}
		} else {
			mNumPegs = getIntent().getIntExtra("PEGS", 4);
			mNumColors = getIntent().getIntExtra("COLORS", 6);
			mLevel = getIntent().getIntExtra("LEVEL", 3);
			boolean duplicates = getIntent().getBooleanExtra("DUPLICATES", true);
			selectSolution(duplicates);
		}
		
		if (mNumPegs == 4)
			MAX_ZOOM = 9;
		else
			MAX_ZOOM = 5;

		setContentView(R.layout.game);

		mBaseLayout = (LinearLayout) findViewById(R.id.BaseLayout);
		mAnswerBoard = (LinearLayout) findViewById(R.id.LayoutAnswerBoard);
		mScrollView = (ScrollView) findViewById(R.id.ScrollView);
		mButtonDone = (Button) findViewById(R.id.btnDone);
		mButtonDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkAnswer();
			}
		});

		if (hasCurrentGame) {
			mBaseLayout.post (new Runnable () {
				@Override
				public void run() {
					loadCurrentGameAnswers();
				}
			});
		} else {
			/* Add first answer view */
			mBaseLayout.post(new Runnable() {
				@Override
				public void run() {
					mCurrentAnswer = new Answer(mAnswerBoard.getContext(),
							Game.this, mAnswerBoard.getWidth(), 1);
					mAnswerBoard.addView(mCurrentAnswer);
					mCurrentAnswer.create();
				}
			});
		}
				
		/* Create solution area */
		mBaseLayout.post(new Runnable() {
			@Override
			public void run() {
				mSolution = new Solution(mAnswerBoard.getContext(),
						Game.this, mAnswerBoard.getWidth(), mCorrectPegs);
				LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT, 0f);
				mBaseLayout.addView(mSolution, 0, l);
			}
		});
	}
	
	@Override
	public void onPause () {
		super.onPause();
		CurrentGameDatabaseHandler db = new CurrentGameDatabaseHandler(getApplicationContext());
		db.clear();
		
		if (mFinished) {
			return;
		}
		
		db.addInfo(mNumPegs, mNumColors, mLevel, mZoom, mCorrectPegs);
		db.add (mAnswerBoard);
	}
	
	@Override
	public void onBackPressed() {
		if (mFinished) {
			super.onBackPressed();
			return;
		}
		
	    new AlertDialog.Builder(this)
	        .setTitle("Exit game")
	        .setMessage("Are you sure you want to exit this game?")
	        .setNegativeButton("No", null)
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

	            public void onClick(DialogInterface arg0, int arg1) {
	            	mFinished = true;
	                Game.super.onBackPressed();
	            }
	        }).create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.game_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.copy_row:
	            copyPreviousRow();
	            return true;
	        case R.id.clear_row:
	            clearRow();
	            return true;
	        case R.id.zoom_out:
	        	if (mZoom < MAX_ZOOM) {
	        		mZoom += 2;
	        		handleZoom();
	        	}
	        	
	        	return true;
	        case R.id.zoom_in:
	        	if (mZoom > MIN_ZOOM) {
	        		mZoom -= 2;
	        		handleZoom();
	        	}
	        	
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		if (id == PEG_PICKER) {
			View layout = inflater.inflate(R.layout.picker,
					(ViewGroup) findViewById(R.id.grid));
			GridView gridview = (GridView) layout.findViewById(R.id.grid);

			int num = 2;
			if (mNumColors % 4 == 0)
				num = 4;
			else if (mNumColors % 3 == 0)
				num = 3;

			gridview.setNumColumns(num);
			gridview.setAdapter(new PegAdapter(this));

			gridview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					mCurrentAnswer.setPeg(position);
					mDialog.dismiss();
				}
			});

			builder.setView(layout);
			mDialog = builder.create();
			
		} else if (id == REGISTER_HIGHSCORE) {
			View layout = inflater.inflate(R.layout.register_highscore,
					(ViewGroup) findViewById(R.id.baseRegisterHighscore));
			Button add = (Button) layout.findViewById(R.id.btnRegisterHighscore);
			Button cancel = (Button) layout.findViewById(R.id.btnCancelHighscore);
			final EditText name = (EditText) layout.findViewById(R.id.textHighscore);
			
			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});
			
			add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					HighscoreDatabaseHandler db = new HighscoreDatabaseHandler(getApplicationContext());
					if (name.toString().equals("")) {
						Toast.makeText(Game.this.getApplicationContext(), getString(R.string.no_name), Toast.LENGTH_SHORT).show();
						return;
					}
					
					db.addHighscore(name.getText().toString(), mNumAnswers, mLevel);
					mDialog.dismiss();
				}
			});
			
			builder.setTitle(R.string.add_to_highscore);
			builder.setView(layout);
			mDialog = builder.create();
			
		} else {
			mDialog = null;
		}

		return mDialog;
	}
	
	private void handleZoom () {
		for (int i = 0; i < mAnswerBoard.getChildCount(); i++)
			((Answer) mAnswerBoard.getChildAt(i)).zoom();

		mSolution.zoom();
	}
	
	private void clearRow () {
		if (mFinished)
			return;
		
		mCurrentAnswer.clear ();
	}
	
	private void copyPreviousRow () {
		if (mNumAnswers == 1 || mFinished)
			return;
		
		Answer pre = (Answer) mAnswerBoard.getChildAt(mNumAnswers-2);
		mCurrentAnswer.copy(pre);
	}

	private void selectSolution(boolean allowDuplicates) {
		mCorrectPegs = new int[mNumPegs];
		Random r = new Random();

		if (allowDuplicates) {
			for (int i = 0; i < mNumPegs; i++)
				mCorrectPegs[i] = r.nextInt(mNumColors);
		} else {
			boolean[] usedColors = new boolean[mNumColors];

			for (int i = 0; i < mNumPegs; i++)
				usedColors[i] = false;

			int i = 0;
			while (i < mNumPegs) {
				int color = r.nextInt(mNumColors);
				if (!usedColors[color]) {
					mCorrectPegs[i++] = color;
					usedColors[color] = true;
				}
			}
		}
	}

	private void checkAnswer() {
		if (mCurrentAnswer != null) {
			if (!mCurrentAnswer.isComplete()) {
				Toast.makeText(this, getString(R.string.notComplete), Toast.LENGTH_SHORT).show();
				return;
			} else if (mFinished) {
				Toast.makeText(this, "Game is finished!", Toast.LENGTH_LONG).show();
				return;
			} else {
				mCurrentAnswer.showKeyPegs(mCorrectPegs);

				if (mCurrentAnswer.isCorrectAnswer(mCorrectPegs)) {
					Toast.makeText(this, "HUZZA!", Toast.LENGTH_LONG).show();
					mFinished = true;
					mSolution.showSolution();
					showDialog(REGISTER_HIGHSCORE);

					return;
				}
			}
		}

		mCurrentAnswer = new Answer(mAnswerBoard.getContext(), this,
				mAnswerBoard.getWidth(), ++mNumAnswers);

		mAnswerBoard.addView(mCurrentAnswer);
		mCurrentAnswer.create();

		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}
	
	public int getNumPegs() {
		return mNumPegs;
	}

	public int getNumColors() {
		return mNumColors;
	}

	public int getLevel() {
		return mLevel;
	}

	public int getZoom() {
		return mZoom;
	}

	public boolean isFinished() {
		return mFinished;
	}

	private boolean loadCurrentGameInfo () {
		CurrentGameDatabaseHandler db = new CurrentGameDatabaseHandler(getApplicationContext());
		int [] info = db.getInfo();
		if (info == null)
			return false;
		
		mNumPegs = info[0];
		mNumColors = info [1];
		mLevel = info[2];
		mZoom = info[3];
		mCorrectPegs = new int[mNumPegs];
		for (int i = 4; i < info.length; i++)
			mCorrectPegs[i-4] = info[i];
		return true;
	}
	
	private boolean loadCurrentGameAnswers () {
		CurrentGameDatabaseHandler db = new CurrentGameDatabaseHandler(getApplicationContext());
		
		List<int[]> list = db.getAnswers();
		if (list == null)
			return false;
		
		for (int i = 0; i < list.size(); i++) {
			mCurrentAnswer = new Answer(mAnswerBoard.getContext(),
					Game.this, mAnswerBoard.getWidth(), i+1);
			mAnswerBoard.addView(mCurrentAnswer);
			mCurrentAnswer.create();
			mCurrentAnswer.setPegs (list.get(i));
			
			if (i < list.size() - 1) {
				mCurrentAnswer.showKeyPegs(mCorrectPegs);
			}
		}
		
		mNumAnswers = list.size();
		return true;
	}

	public void showPegPicker() {
		showDialog(PEG_PICKER);
	}

	private int scalePixels(int dps) {
		float scale = getApplicationContext().getResources()
				.getDisplayMetrics().density;
		return (int) (dps * scale + 0.5f);
	}

	private class PegAdapter extends BaseAdapter {
		private Context mContext;

		public PegAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return mNumColors;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(
						scalePixels(60), scalePixels(60)));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource(PEGS[position]);
			return imageView;
		}

	}
}