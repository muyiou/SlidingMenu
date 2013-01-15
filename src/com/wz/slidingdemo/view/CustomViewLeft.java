package com.wz.slidingdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.wz.slidingdemo.view.SlidingMenu.CanvasTransformer;

public class CustomViewLeft extends CustomViewAbove {

	//private static final String TAG = "CustomViewLeft";

	private CustomViewAbove mViewAbove;
	private CanvasTransformer mTransformer;
	private boolean mChildrenEnabled;
	
	public CustomViewLeft(Context context) {
		this(context, null);
	}

	public CustomViewLeft(Context context, AttributeSet attrs) {
		super(context, attrs, false);
		setUpViews();
	}

	private void setUpViews() {

	}

	public void setCustomViewAbove(CustomViewAbove customViewAbove) {
		mViewAbove = customViewAbove;
		mViewAbove.setTouchModeLeft(mTouchMode);
	}

	public void setTouchMode(int i) {
		mTouchMode = i;
		if (mViewAbove != null)
			mViewAbove.setTouchModeLeft(i);
	}

	public void setCanvasTransformer(CanvasTransformer t) {
		mTransformer = t;
	}

	public int getChildLeft(int i) {
		return 0;
	}

	@Override
	public int getCustomWidth() {
		int i = isLeftMenuOpen()? 0 : 1;
		return getChildWidth(i);
	}

	@Override
	public int getChildWidth(int i) {
		if (i <= 0) {
			return getLeftWidth();
		} else {
			return getChildAt(i).getMeasuredWidth();
		}
	}

	public int getLeftWidth() {
		ViewGroup.LayoutParams params = getLayoutParams();
		return params.width;
	}

	@Override
	public void setContent(View v) {
		super.setMenu(v);
	}

	public void setChildrenEnabled(boolean enabled) {
		mChildrenEnabled = enabled;
	}
	
	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		if (mTransformer != null)
			invalidate();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		return !mChildrenEnabled;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return false;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mTransformer != null) {
			canvas.save();
			mTransformer.transformCanvas(canvas, mViewAbove.getPercentOpen());
			super.dispatchDraw(canvas);
			canvas.restore();
		} else
			super.dispatchDraw(canvas);
	}

}
