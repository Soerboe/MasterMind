package com.soerboe.mastermind;

import android.content.Context;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Answer extends RelativeLayout {
	private int mWidth;
	private int mAnswerNumber;
	private int mCodePegsSize;
	private Game mGame;
	private ImageView mKeyPegs[];
	private ImageView mCodePegs[];
	private RelativeLayout mAnswerSlots;
	private int[] mSelectedPegs;
	private int mCurrPeg;
	private boolean mLocked = false;
	
	private final static double CODEPEGS_PERCENT = 0.75;

	public Answer (Context context, Game game, int width, int number) {
		super(context);
		mGame = game;
		mWidth = width;
		mAnswerNumber = number;
		
		int numPegs = mGame.getNumPegs();
		mKeyPegs = new ImageView[numPegs];
		mCodePegs = new ImageView[numPegs];
		mSelectedPegs = new int[numPegs];
		
		for (int i = 0; i < numPegs; i++) {
			mKeyPegs[i] = new ImageView(getContext());
			mCodePegs[i] = new ImageView(getContext());
			mSelectedPegs[i] = -1;
		}
	}
	
	public void create() {
		int id = 1;
		int numPegs = mGame.getNumPegs();
		mCodePegsSize = (int) (mWidth * CODEPEGS_PERCENT);
		int numberWidth = scalePixels(25);
		int codePegsPadding = scalePixels(3) * mGame.getZoom();
		int keyPegsPadding = scalePixels(3) * mGame.getZoom() / 2;
		int codePegSize = mCodePegsSize / (numPegs) - codePegsPadding;
		int keyPegSize = (int) (codePegSize / 2.75);
		
		ViewGroup.LayoutParams l = getLayoutParams();
		l.width = ViewGroup.LayoutParams.FILL_PARENT;
		l.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		
		/* Create number */
		TextView number = new TextView(getContext());
		number.setText("" + mAnswerNumber);
		number.setGravity(Gravity.CENTER);
		number.setWidth(numberWidth);
		number.setId(id++);
		LayoutParams numberLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT);
		numberLayout.addRule(RelativeLayout.CENTER_VERTICAL);
		numberLayout.setMargins(0, 0, keyPegsPadding, 0);
		addView(number, numberLayout);
		
		/* Create slots for key pegs*/
		mAnswerSlots = new RelativeLayout(getContext());
		mAnswerSlots.setId(id++);
		RelativeLayout.LayoutParams answerLayout = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT);
		answerLayout.addRule(RelativeLayout.CENTER_VERTICAL);
		answerLayout.addRule(RelativeLayout.RIGHT_OF, number.getId());
		
		for (int i = 0; i < numPegs; i++) {
			if (!mLocked)
				mKeyPegs[i].setImageResource(R.drawable.empty);
			mKeyPegs[i].setId(id++);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					keyPegSize, 
					keyPegSize);
			
			if (!(i == 0 || i == numPegs - numPegs/2))
				lp.addRule(RelativeLayout.RIGHT_OF, mKeyPegs[i-1].getId());
			
			if (i >= numPegs - numPegs/2)
				lp.addRule(RelativeLayout.BELOW, mKeyPegs[i - numPegs/2].getId());
			
			mAnswerSlots.addView(mKeyPegs[i], lp);
		}
		
		addView (mAnswerSlots, answerLayout);

		/* Create code pegs slots */
		for (int i = 0; i < numPegs; i++) {
			if (mSelectedPegs[i] == -1)
				mCodePegs[i].setImageResource(R.drawable.empty);
			mCodePegs[i].setId(id++);
			mCodePegs[i].setTag(i);
			mCodePegs[i].setBackgroundResource(R.drawable.peg_background);
			
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					codePegSize,
					codePegSize);
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
			lp.setMargins(codePegsPadding, scalePixels(15), 0, 0);
			
			mCodePegs[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mLocked)
						return;
					
					mCurrPeg = (Integer) v.getTag ();
					mGame.showPegPicker();
				}
			});

			mCodePegs[i].setOnLongClickListener(new OnLongClickListener () {
				@Override
				public boolean onLongClick(View v) {
					if (mLocked)
						return true;
					
					mCurrPeg = (Integer) v.getTag();
					mCodePegs[mCurrPeg].setImageResource(R.drawable.empty);
					mSelectedPegs[mCurrPeg] = -1;
					mCodePegs[mCurrPeg].performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
					return true;
				}
			});

			if (i == 0)
				lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, mCodePegs[i].getId());
			else
				lp.addRule(RelativeLayout.LEFT_OF, mCodePegs[i-1].getId());
			
			addView(mCodePegs[i], lp);
		}
	}
	
	public void zoom () {
		for (ImageView view : mCodePegs) {
			removeView(view);
		}
		
		mAnswerSlots.removeAllViews();
		removeView(mAnswerSlots);
		
		create();
	}
	
	public void clear () {
		for (int i = 0; i < mGame.getNumPegs(); i++) {
			mSelectedPegs[i] = -1;
			mCodePegs[i].setImageResource(R.drawable.empty);
		}
	}
	
	public void copy (Answer cpy) {
		for (int i = 0; i < mGame.getNumPegs(); i++) {
			mCurrPeg = i;
			setPeg(cpy.getSelectedPegs()[i]);
		}
	}
	
	public int[] getSelectedPegs () {
		return mSelectedPegs;
	}
	
	public int getAnswerNumber () {
		// starts on 1, not 0
		return mAnswerNumber;
	}
	
	public boolean isComplete () {
		for (int i = 0; i < mGame.getNumPegs(); i++) {
			if (mSelectedPegs[i] == -1)
				return false;
		}

		return true;
	}
	
	public void showKeyPegs (int[] correctPegs) {
		int keyPeg = 0;
		int numPegs = mGame.getNumPegs();
		boolean[] used = new boolean[numPegs];
		
		for (int i = 0; i < numPegs; i++)
			used[i] = false;
		
		/* Check for correct pegs */
		for (int i = 0; i < numPegs; i++) {
			if (mSelectedPegs[i] == correctPegs[i]) {
				mKeyPegs[keyPeg++].setImageResource(R.drawable.correct_peg);
				used[i] = true;
			}
		}
		
		/* Check for correct color */
		for (int i = 0; i < numPegs; i++) {
			if (mSelectedPegs[i] != correctPegs[i]) {
				for (int j = 0; j < numPegs; j++) {
					if (j != i && mSelectedPegs[i] == correctPegs[j] && !used[j]) {
						mKeyPegs[keyPeg++].setImageResource(R.drawable.correct_color);
						used[j] = true;
						break;
					}
				}
			}
		}
		
		lock();
	}
	
	private void lock () {
		mLocked = true;
		
		for (int i = 0; i < mGame.getNumPegs(); i++)
			mCodePegs[i].setBackgroundResource(0);
	}
	
	public boolean isLocked () {
		return mLocked;
	}
	
	public void setPeg (int index) {
		mCodePegs[mCurrPeg].setImageResource(Game.PEGS[index]);
		mSelectedPegs[mCurrPeg] = index;
	}
	
	public void setPegs (int[] pegs) {
		if (mGame.getNumPegs() != pegs.length)
			return;
		
		for (int i = 0; i < mGame.getNumPegs(); i++) {
			if (pegs[i] != -1)
				mCodePegs[i].setImageResource(Game.PEGS[pegs[i]]);
			mSelectedPegs[i] = pegs[i];
		}
	}
	
	public boolean isCorrectAnswer (int[] fasit) {
		boolean correct = true;
		
		for (int i = 0; i < mGame.getNumPegs(); i++) {
			if (mSelectedPegs[i] != fasit[i]) {
				correct = false;
				break;
			}
		}
		
		return correct;
	}
	
	private int scalePixels(int dps) {
		float scale = getContext().getResources().getDisplayMetrics().density;
		return (int) (dps * scale + 0.5f);
	}
}
