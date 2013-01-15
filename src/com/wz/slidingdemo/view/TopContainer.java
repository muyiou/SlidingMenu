package com.wz.slidingdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.wz.slidingdemo.R;
import com.wz.slidingdemo.view.SlidingMenu.CanvasTransformer;
public class TopContainer extends RelativeLayout {

	public SlidingMenu sm;
	public CustomViewTop mViewTop;

	private CanvasTransformer transFormer;
	public TopContainer(Context context) {
		this(context, null);
	}

	public TopContainer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TopContainer(Context context, AttributeSet attrs, int i) {
		super(context, attrs, i);
		transFormer = new CanvasTransformer(){

			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				//TODO：在4.0上有时候不会缩放的动画不会执行
				float scale = (float) ((percentOpen*0.1 + 0.9));
				canvas.scale(scale, scale, canvas.getWidth()/2, canvas.getHeight()/2);
			}
			
		};
	}

	public void setContent(int res) {

		setContent(LayoutInflater.from(getContext()).inflate(res, null));
	}

	public void setContent(View view) {
		mViewTop.setContent(view);
		mViewTop.invalidate();
		showAbove();
	}

	public void showAbove() {
		showAbove(true);
	}

	/**
	 * Closes the menu and shows the above view.
	 * 
	 * @param animate
	 *            true to animate the transition, false to ignore animation
	 */
	public void showAbove(boolean animate) {
		mViewTop.setCurrentItem(1, animate);
	}

	public void setSm(Context context, SlidingMenu sm) {
		this.sm = sm;

		LayoutParams aboveParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		mViewTop = new CustomViewTop(context);

		mViewTop.setCustomViewBehind(sm);

		if (sm.getParent() != null) {
			((ViewGroup) sm.getParent()).removeAllViews();
		}
		
		View recipeView = LayoutInflater.from(context).inflate(
				R.layout.recipe_detail_fragment, null);
		
		mViewTop.setContent(recipeView);
		
		mViewTop.setMenu(sm);
		
		if (sm.getParent() != null) {
			((ViewGroup) sm.getParent()).removeAllViews();
		}
		
		addView(this.sm, aboveParams);
		addView(mViewTop, aboveParams);
		this.sm.setTopView(mViewTop);
		this.sm.setBehindCanvasTransformer(transFormer);
	}

	public void showRecipe() {
		//TODO：to show the top view
		mViewTop.setCurrentItem(1);
	}

}
