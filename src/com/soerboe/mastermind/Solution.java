package com.soerboe.mastermind;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Solution extends RelativeLayout {
	private int mWidth;
	private int mCodePegsSize;
	private Game mGame;
	private ImageView mCodePegs[];
	private int[] mCorrectPegs;
	
	private final static double CODEPEGS_PERCENT = 0.75;
	
	public Solution (Context context, Game game, int width, int[] correctPegs) {
		super(context);
		mGame = game;
		mWidth = width;
		mCorrectPegs = correctPegs;
		
		mCodePegs = new ImageView[mGame.getNumPegs()];
		
		for (int i = 0; i < mGame.getNumPegs(); i++) {
			mCodePegs[i] = new ImageView(getContext());
		}
		
		setPadding(0, scalePixels(5), scalePixels(5), scalePixels(5));
		
		setBackgroundColor(Color.BLACK);
		getBackground().setAlpha(40);
		
		showSlots();
	}
	
	public void zoom () {
		for (ImageView view : mCodePegs)
			removeView(view);
		
		showSlots ();
	}
	
	public void showSolution () {
		for (int i = 0; i < mGame.getNumPegs(); i++) {
			mCodePegs[i].setImageResource(Game.PEGS[mCorrectPegs[i]]);
		}
	}
	
	private void showSlots () {
		int id = 1;
		int numPegs = mGame.getNumPegs();
		mCodePegsSize = (int) (mWidth * CODEPEGS_PERCENT);
		int codePegsPadding = scalePixels(3) * mGame.getZoom();
		int codePegSize = mCodePegsSize / numPegs - codePegsPadding;

		/* Create code pegs slots */
		for (int i = 0; i < numPegs; i++) {
			mCodePegs[i].setImageResource(R.drawable.solution_slot);
			mCodePegs[i].setId(id++);
			
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					codePegSize,
					codePegSize);
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
			lp.setMargins(codePegsPadding, 0, 0, 0);

			if (i == 0)
				lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, mCodePegs[i].getId());
			else
				lp.addRule(RelativeLayout.LEFT_OF, mCodePegs[i-1].getId());
			
			addView(mCodePegs[i], lp);
		}
	}
	
	private int scalePixels(int dps) {
		float scale = getContext().getResources().getDisplayMetrics().density;
		return (int) (dps * scale + 0.5f);
	}
}
