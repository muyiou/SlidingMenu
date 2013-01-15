package com.wz.slidingdemo.view;

import com.wz.slidingdemo.view.SlidingMenu.CanvasTransformer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class CustomViewRight extends CustomViewAbove{

	private CustomViewAbove mViewAbove;
	private CanvasTransformer mTransformer;
	private boolean mChildrenEnabled;
	
	public CustomViewRight(Context context) {
		this(context, null);
	}
	
	public CustomViewRight(Context context, AttributeSet attrs) {
		super(context, attrs, false);
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
		int i = isRightMenuOpen()? 2 : 1;
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
		super.setRightMenu(v);
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
