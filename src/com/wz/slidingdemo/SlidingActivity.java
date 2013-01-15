package com.wz.slidingdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.wz.slidingdemo.view.SlidingMenu;
import com.wz.slidingdemo.view.TopContainer;

public class SlidingActivity extends FragmentActivity implements SlidingActivityBase {

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
	}

	private SlidingActivityHelper mHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new SlidingActivityHelper(this);
		mHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		mHelper.onPostCreate(savedInstanceState);
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v != null)
			return v;
		return mHelper.findViewById(id);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//		
//		Log.d("onSaveInstanceState ");
//		mHelper.onSaveInstanceState(outState);
	}

	@Override
	public void setContentView(int id) {
		setContentView(getLayoutInflater().inflate(id, null));
	}

	@Override
	public void setContentView(View v) {
		setContentView(v, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	@Override
	public void setContentView(View v, LayoutParams params) {
		super.setContentView(v, params);
		mHelper.registerAboveContentView(v, params);
	}

	public void setLeftContentView(int id) {
		setLeftContentView(getLayoutInflater().inflate(id, null));
	}

	public void setLeftContentView(View v) {
		setLeftContentView(v, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	public void setLeftContentView(View v, LayoutParams params) {
		mHelper.setLeftContentView(v, params);
	}

	public SlidingMenu getSlidingMenu() {
		return mHelper.getSlidingMenu();
	}
	public TopContainer getTopView() {
		return mHelper.getTopView();
	}

	public void toggle() {
		mHelper.toggle();
	}

	public void showAbove() {
		mHelper.showAbove();
	}

	public void showLeft() {
		mHelper.showLeft();
	}

	public void setSlidingActionBarEnabled(boolean b) {
		mHelper.setSlidingActionBarEnabled(b);
	}

	@Override
	public void setRightContentView(View view, LayoutParams layoutParams) {
		mHelper.setRightContentView(view, layoutParams);
	}

	@Override
	public void setRightContentView(View view) {
		setRightContentView(view, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	@Override
	public void setRightContentView(int id) {
		setRightContentView(getLayoutInflater().inflate(id, null));
	}

	@Override
	public void showRight() {
//		mHelper.showRight();
	}
	

}
