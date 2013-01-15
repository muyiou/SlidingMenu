package com.wz.slidingdemo;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.wz.slidingdemo.view.SlidingMenu;
import com.wz.slidingdemo.view.TopContainer;

public class SlidingActivityHelper {

	private Activity mActivity;

	private SlidingMenu mSlidingMenu;
	private View mViewAbove;
	
	private View mViewLeft;
	
	private View mViewRight;
	
	private boolean mBroadcasting = false;

	private boolean mOnPostCreateCalled = false;
	
	private boolean mEnableSlide = true;
	
	private TopContainer topView;

	public SlidingActivityHelper(Activity activity) {
		mActivity = activity;
	}

	public void onCreate(Bundle savedInstanceState) {
		mSlidingMenu = (SlidingMenu) LayoutInflater.from(mActivity).inflate(R.layout.slidingmenumain, null);
		
//		topView = (TopView) LayoutInflater.from(mActivity).inflate(R.layout.test_top_view, null);
//		mSlidingMenu = topView.sm;
	}

	public void onPostCreate(Bundle savedInstanceState) {

		mOnPostCreateCalled = true;

		// get the window background
		TypedArray a = mActivity.getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowBackground});
		int background = a.getResourceId(0, 0);
		a.recycle();

		//TODO:等Slidingmenu加载好孩子后
		if (mEnableSlide) {
			// move everything into the SlidingMenu
			ViewGroup decor = (ViewGroup) mActivity.getWindow().getDecorView();
			ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
			// save ActionBar themes that have transparent assets
			decorChild.setBackgroundResource(background);
			decor.removeView(decorChild);
			mSlidingMenu.setContent(decorChild);
			
			ViewGroup parent = (ViewGroup) mSlidingMenu.getParent();
			if (parent != null)
				parent.removeAllViews();
			
			decor.addView(mSlidingMenu);
			

			
			//TOTO:如果希望在当前页面之上再套一层view，请去掉下面代码的注释
//          ViewGroup dector = (ViewGroup)mActivity.getWindow().getDecorView();
//			if(dector.getChildAt(0) instanceof SlidingMenu){
//				SlidingMenu sm = (SlidingMenu) dector.getChildAt(0);
//				
//				if(sm.getParent() != null){
//					((ViewGroup) sm.getParent()).removeAllViews();
//				}
//				
//				TopContainer topView = (TopContainer) LayoutInflater.from(mActivity).inflate(R.layout.test_top_view, null);
//				topView.setSm(mActivity,sm);
//				
//				dector.removeAllViews();
//				dector.addView(topView);
//			}
			
		} else {
			// take the above view out of
			ViewGroup parent = (ViewGroup) mViewAbove.getParent();
			if (parent != null) {
				parent.removeView(mViewAbove);
			}
			// save people from having transparent backgrounds
			if (mViewAbove.getBackground() == null) {
				mViewAbove.setBackgroundResource(background);
			}
			mSlidingMenu.setContent(mViewAbove);
			parent.addView(mSlidingMenu, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
	}

	public void setSlidingActionBarEnabled(boolean slidingActionBarEnabled) {
		if (mOnPostCreateCalled)
			throw new IllegalStateException("enableSlidingActionBar must be called in onCreate.");
		mEnableSlide = slidingActionBarEnabled;
	}

	public View findViewById(int id) {
		View v;
		if (mSlidingMenu != null) {
			v = mSlidingMenu.findViewById(id);
			if (v != null)
				return v;
		}
		return null;
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("menuOpen", mSlidingMenu.isLeftShowing());
	}

	public void registerAboveContentView(View v, LayoutParams params) {
		if (!mBroadcasting)
			mViewAbove = v;
	}

	public void setContentView(View v) {
		mBroadcasting = true;
		mActivity.setContentView(v);
	}

	public void setLeftContentView(View view, LayoutParams layoutParams) {
		mViewLeft = view;
		mSlidingMenu.setLeftMenu(mViewLeft);
	}

	public void setRightContentView(View view,LayoutParams params){
		mViewRight = view;
		mSlidingMenu.setRightMenu(mViewRight);
	}
	
	public SlidingMenu getSlidingMenu() {
		return mSlidingMenu;
	}
	
	public TopContainer getTopView() {
		return topView;
	}

	public void toggle() {
		if (mSlidingMenu.isLeftShowing()) {
			showAbove();
		} else {
			showLeft();
		}
	}

	public void showAbove() {
		mSlidingMenu.showAbove();
	}

	public void showLeft() {
//		mSlidingMenu.showLeft();
	}

}
