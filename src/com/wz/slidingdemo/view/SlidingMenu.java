package com.wz.slidingdemo.view;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.wz.slidingdemo.R;
import com.wz.slidingdemo.view.CustomViewAbove.OnPageChangeListener;

public class SlidingMenu extends RelativeLayout {

	/** Constant value for use with setTouchModeAbove(). Allows the SlidingMenu to be opened with a swipe
	 * gesture on the screen's margin
	 */
	public static final int TOUCHMODE_MARGIN = 0;
	
	/** Constant value for use with setTouchModeAbove(). Allows the SlidingMenu to be opened with a swipe
	 * gesture anywhere on the screen
	 */
	public static final int TOUCHMODE_FULLSCREEN = 1;
	
	/** Constant value for use with setTouchModeAbove(). Denies the SlidingMenu to be opened with a swipe
	 * gesture
	 */
	public static final int TOUCHMODE_NONE = 2;

	private CustomViewAbove mViewAbove;
	private CustomViewLeft mViewLeft;
	private CustomViewRight mViewRight;
	
	private CanvasTransformer mTransformer;
	
	public SlidingMenu getSlidingMenu(){
		return this;
	}
	
	private OnOpenListener mOpenListener;
	
	private OnCloseListener mCloseListener;

	/**
     * Attach a given SlidingMenu to a given Activity
     *
     * @param activity the Activity to attach to
     * @param sm the SlidingMenu to be attached
     * @param slidingTitle whether the title is slid with the above view
     */
    public static void attachSlidingMenu(Activity activity, SlidingMenu sm, boolean slidingTitle) {
		if (sm.getParent() != null)
			throw new IllegalStateException("SlidingMenu cannot be attached to another view when" +
					" calling the static method attachSlidingMenu");

		if (slidingTitle) {
			// get the window background
			TypedArray a = activity.getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowBackground});
			int background = a.getResourceId(0, 0);
			a.recycle();
			// move everything into the SlidingMenu
			ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
			ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
			decor.removeAllViews();
			// save ActionBar themes that have transparent assets
			decorChild.setBackgroundResource(background);
			sm.setContent(decorChild);
			decor.addView(sm);
		} else {
			// take the above view out of
			ViewGroup content = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
			View above = content.getChildAt(0);
			content.removeAllViews();
			sm.setContent(above);
			content.addView(sm, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
	}

	public interface OnOpenListener {
		
		public void onOpen();
	}

	public interface OnOpenedListener {
		
		public void onOpened();
	}

	public interface OnCloseListener {
		
		public void onClose();
	}
	public interface OnClosedListener {
		public void onClosed();
	}

	/**
	 * The Interface CanvasTransformer.
	 */
	public interface CanvasTransformer {
		
		/**
		 * Transform canvas.
		 *
		 * @param canvas the canvas
		 * @param percentOpen the percent open
		 */
		public void transformCanvas(Canvas canvas, float percentOpen);
	}
	public void setCanvasTransformer(CanvasTransformer t) {
		mTransformer = t;
	}

	/**
	 * Instantiates a new SlidingMenu.
	 *
	 * @param context the associated Context
	 */
	public SlidingMenu(Context context) {
		this(context, null);
	}

	/**
	 * Instantiates a new SlidingMenu.
	 *
	 * @param context the associated Context
	 * @param attrs the attrs
	 */
	public SlidingMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Instantiates a new SlidingMenu.
	 *
	 * @param context the associated Context
	 * @param attrs the attrs
	 * @param defStyle the def style
	 */
	public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutParams leftParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mViewLeft = new CustomViewLeft(context);
		addView(mViewLeft, leftParams);
		
		//TODO:add
		LayoutParams rightParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mViewRight = new CustomViewRight(context);
		addView(mViewRight, rightParams);
		
		LayoutParams aboveParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		aboveParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mViewAbove = new CustomViewAbove(context);
		addView(mViewAbove, aboveParams);
		
		// register the CustomViewLeft2 with the CustomViewAbove
		mViewAbove.setCustomViewLeft(mViewLeft);
		mViewLeft.setCustomViewAbove(mViewAbove);
		
		//TODO:add
		mViewAbove.setCustomViewRight(mViewRight);
		mViewRight.setCustomViewAbove(mViewAbove);
		
		
		
		mViewAbove.setOnPageChangeListener(new OnPageChangeListener() {
			public static final int POSITION_OPEN = 0;
			public static final int POSITION_CLOSE = 1;

			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) { }

			public void onPageSelected(int position) {
				if (position == POSITION_OPEN && mOpenListener != null) {
					mOpenListener.onOpen();
				} else if (position == POSITION_CLOSE && mCloseListener != null) {
					mCloseListener.onClose();
				}
			}
		});

		
		// now style everything!
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);
		// set the above and left views if defined in xml
		int viewAbove = ta.getResourceId(R.styleable.SlidingMenu_viewAbove, -1);
		if (viewAbove != -1)
			setContent(viewAbove);
		int viewLeft = ta.getResourceId(R.styleable.SlidingMenu_viewLeft, -1);
		if (viewLeft != -1)
			setLeftMenu(viewLeft);
		int touchModeAbove = ta.getInt(R.styleable.SlidingMenu_touchModeAbove, TOUCHMODE_FULLSCREEN);
		setTouchModeAbove(touchModeAbove);
		int touchModeLeft = ta.getInt(R.styleable.SlidingMenu_touchModeLeft, TOUCHMODE_MARGIN);
		setTouchModeLeft(touchModeLeft);

		int offsetLeft = (int) ta.getDimension(R.styleable.SlidingMenu_leftOffset, -1);
		int widthLeft = (int) ta.getDimension(R.styleable.SlidingMenu_leftWidth, -1);
		if (offsetLeft != -1 && widthLeft != -1)
			throw new IllegalStateException("Cannot set both leftOffset and leftWidth for a SlidingMenu");
		else if (offsetLeft != -1)
			setLeftOffset(offsetLeft);
		else if (widthLeft != -1)
			setLeftWidth(widthLeft);
		else
			setLeftOffset(0);
		float scrollOffsetLeft = ta.getFloat(R.styleable.SlidingMenu_leftScrollScale, 0.33f);
		setLeftScrollScale(scrollOffsetLeft);
		int shadowRes = ta.getResourceId(R.styleable.SlidingMenu_shadowDrawable, -1);
		if (shadowRes != -1) {
			setLeftShadowDrawable(shadowRes);
		}
		int shadowWidth = (int) ta.getDimension(R.styleable.SlidingMenu_shadowWidth, 0);
		//TODO:add
		if(shadowWidth == 0){
			shadowWidth = 10;
		}
		setShadowWidth(shadowWidth);
		boolean fadeEnabled = ta.getBoolean(R.styleable.SlidingMenu_leftFadeEnabled, true);
		setFadeEnabled(fadeEnabled);
		float fadeDeg = ta.getFloat(R.styleable.SlidingMenu_leftFadeDegree, 0.66f);
		setFadeDegree(fadeDeg);
		boolean selectorEnabled = ta.getBoolean(R.styleable.SlidingMenu_selectorEnabled, false);
		setSelectorEnabled(selectorEnabled);
		int selectorRes = ta.getResourceId(R.styleable.SlidingMenu_selectorDrawable, -1);
		if (selectorRes != -1)
			setSelectorDrawable(selectorRes);
		ta.recycle();
	}
	
	public void setBehindCanvasTransformer(CanvasTransformer t) {
		setCanvasTransformer(t);
	}

	public void setContent(int res) {
		setContent(LayoutInflater.from(getContext()).inflate(res, null));
	}

	public void setContent(View view) {
		mViewAbove.setContent(view);
		mViewAbove.invalidate();
		showAbove();
	}
	
	private CustomViewTop topView;
	
	public void setTopView(CustomViewTop topView){
		this.topView = topView;
	}
	
	//TODO:draw scale
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mTransformer != null && topView != null) {
			canvas.save();
			mTransformer.transformCanvas(canvas, topView.getPercentOpen());
			super.dispatchDraw(canvas);
			canvas.restore();
		} else
			super.dispatchDraw(canvas);
	}
	
	
	public void setLeftMenu(int res) {
		setLeftMenu(LayoutInflater.from(getContext()).inflate(res, null));
	}

	public void setLeftMenu(View v) {
		mViewLeft.setMenu(v);
		mViewLeft.invalidate();
	}
	
	//TODO:add
	public void setRightMenu(View v){
		mViewRight.setRightMenu(v);
		mViewRight.invalidate();
	}

	/**
	 * Opens the menu and shows the left view.
	 */
//	public void showLeft() {
//		showLeft(true);
//	}
	
	/**
	 * Opens the menu and shows the left view.
	 *
	 * @param animate true to animate the transition, false to ignore animation
	 */
	public void showLeft(boolean animate) {
		mViewAbove.setCurrentItem(0, animate);
	}

	public void showRight(boolean animate) {
		mViewAbove.setCurrentItem(2, animate);
	}

	/**
	 * Closes the menu and shows the above view.
	 */
	public void showAbove() {
		showAbove(true);
	}
	
	/**
	 * Closes the menu and shows the above view.
	 *
	 * @param animate true to animate the transition, false to ignore animation
	 */
	public void showAbove(boolean animate) {
		mViewAbove.setCurrentItem(1, animate);
	}

	public boolean isLeftShowing() {
		return mViewAbove.getCurrentItem() == 0;
	}
	public boolean isRightShowing() {
		return mViewAbove.getCurrentItem() == 2;
	}

	/**
	 * Gets the left offset.
	 *
	 * @return The margin on the right of the screen that the left view scrolls to
	 */
	public int getLeftOffset() {
		return ((RelativeLayout.LayoutParams)mViewLeft.getLayoutParams()).rightMargin;
	}

	/**
	 * Sets the left offset. in pixels
	 *
	 * @param i The margin, in pixels, on the right of the screen that the left view scrolls to.
	 */
	public void setLeftOffset(int i) {
		RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams)mViewLeft.getLayoutParams());
		int bottom = params.bottomMargin;
		int top = params.topMargin;
		int left = params.leftMargin;
		params.setMargins(left, top, i, bottom);
		OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				showAbove();
				mViewAbove.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		};
		mViewAbove.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
		mViewAbove.requestLayout();
	}

	/*
	 * 右边的menu距离左边为 i 
	 */
	public void setRightOffset(int i){
		RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams)mViewRight.getLayoutParams());
		int bottom = params.bottomMargin;
		int top = params.topMargin;
		int right = params.rightMargin;
		params.setMargins(i, top, right, bottom);
		OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				showAbove();
				mViewAbove.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		};
		mViewAbove.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
		mViewAbove.requestLayout();
	}
	
	/**
	 * Sets the left offset.
	 *
	 * @param resID The dimension resource id to be set as the left offset.
	 * The menu, when open, will leave this width margin on the right of the screen.
	 */
	public void setLeftOffsetRes(int resID) {
		int i = (int) getContext().getResources().getDimension(resID);
		setLeftOffset(i);
	}
	
	/**
	 * Sets the above offset.
	 *
	 * @param i the new above offset, in pixels
	 */
	public void setAboveOffset(int i) {
//		RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams)mViewAbove.getLayoutParams());
//		int bottom = params.bottomMargin;
//		int top = params.topMargin;
//		int right = params.rightMargin;
//		params.setMargins(i, top, right, bottom);
//		this.requestLayout();
		mViewAbove.setAboveOffset(i);
	}
		
	/**
	 * Sets the above offset.
	 *
	 * @param resID The dimension resource id to be set as the above offset.
	 */
	public void setAboveOffsetRes(int resID) {
		int i = (int) getContext().getResources().getDimension(resID);
		setAboveOffset(i);
	}

	/**
	 * Sets the left width.
	 *
	 * @param i The width the Sliding Menu will open to, in pixels
	 */
	public void setLeftWidth(int i) {
		int width;
		Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		try {
			Class<?> cls = Display.class;
			Class<?>[] parameterTypes = {Point.class};
			Point parameter = new Point();
			Method method = cls.getMethod("getSize", parameterTypes);
			method.invoke(display, parameter);
			width = parameter.x;
		} catch (Exception e) {
			width = display.getWidth();
		}
		setLeftOffset(width-i);
	}
	
	/**
	 * Sets the left width.
	 *
	 * @param res The dimension resource id to be set as the left width offset.
	 * The menu, when open, will open this wide.
	 */
	public void setLeftWidthRes(int res) {
		int i = (int) getContext().getResources().getDimension(res);
		setLeftWidth(i);
	}

	/**
	 * Gets the left scroll scale.
	 *
	 * @return The scale of the parallax scroll
	 */
	public float getLeftScrollScale() {
		return mViewAbove.getScrollScale();
	}

	/**
	 * Sets the left scroll scale.
	 *
	 * @param f The scale of the parallax scroll (i.e. 1.0f scrolls 1 pixel for every
	 * 1 pixel that the above view scrolls and 0.0f scrolls 0 pixels)
	 */
	public void setLeftScrollScale(float f) {
		mViewAbove.setScrollScale(f);
	}

	/**
	 * Sets the left canvas transformer.
	 *
	 * @param t the new left canvas transformer
	 */
	public void setLeftCanvasTransformer(CanvasTransformer t) {
		mViewLeft.setCanvasTransformer(t);
	}

	/**
	 * Gets the touch mode above.
	 *
	 * @return the touch mode above
	 */
	public int getTouchModeAbove() {
		return mViewAbove.getTouchMode();
	}

	/**
	 * Controls whether the SlidingMenu can be opened with a swipe gesture.
	 * Options are {@link #TOUCHMODE_MARGIN TOUCHMODE_MARGIN}, {@link #TOUCHMODE_FULLSCREEN TOUCHMODE_FULLSCREEN},
	 * or {@link #TOUCHMODE_NONE TOUCHMODE_NONE}
	 *
	 * @param i the new touch mode
	 */
	public void setTouchModeAbove(int i) {
		if (i != TOUCHMODE_FULLSCREEN && i != TOUCHMODE_MARGIN
				&& i != TOUCHMODE_NONE) {
			throw new IllegalStateException("TouchMode must be set to either" +
					"TOUCHMODE_FULLSCREEN or TOUCHMODE_MARGIN or TOUCHMODE_NONE.");
		}
		mViewAbove.setTouchMode(i);
	}

	/**
	 * Returns whether the menu can be swiped to close
	 *
	 * @return the touch mode left, either {@link #TOUCHMODE_MARGIN TOUCHMODE_MARGIN}, {@link #TOUCHMODE_FULLSCREEN TOUCHMODE_FULLSCREEN},
	 * or {@link #TOUCHMODE_NONE TOUCHMODE_NONE}
	 */
	public int getTouchModeLeft() {
		return mViewLeft.getTouchMode();
	}

	/**
	 * Controls whether the SlidingMenu can be closed with a swipe gesture.
	 * Options are {@link #TOUCHMODE_MARGIN TOUCHMODE_MARGIN}, {@link #TOUCHMODE_FULLSCREEN TOUCHMODE_FULLSCREEN},
	 * or {@link #TOUCHMODE_NONE TOUCHMODE_NONE}
	 *
	 * @param i the new touch mode
	 */
	public void setTouchModeLeft(int i) {
		if (i != TOUCHMODE_FULLSCREEN && i != TOUCHMODE_MARGIN
				&& i != TOUCHMODE_NONE) {
			throw new IllegalStateException("TouchMode must be set to either" +
					"TOUCHMODE_FULLSCREEN or TOUCHMODE_MARGIN or TOUCHMODE_NONE.");
		}
		mViewLeft.setTouchMode(i);
	}

	/**
	 * Sets the shadow drawable.
	 *
	 * @param resId the resource ID of the new shadow drawable
	 */
	public void setLeftShadowDrawable(int resId) {
		mViewAbove.setLeftShadowDrawable(resId);
	}
	public void setRightShadowDrawable(int resId) {
		mViewAbove.setRightShadowDrawable(resId);
	}
	
	/**
	 * Sets the shadow drawable.
	 *
	 * @param d the new shadow drawable
	 */
	public void setLeftShadowDrawable(Drawable d) {
		mViewAbove.setLeftShadowDrawable(d);
	}

	/**
	 * Sets the shadow width.
	 *
	 * @param resId The dimension resource id to be set as the shadow width.
	 */
	public void setShadowWidthRes(int resId) {
		setShadowWidth((int)getResources().getDimension(resId));
	}

	public void setShadowWidth(int pixels) {
		mViewAbove.setShadowWidth(pixels);
	}

	/**
	 * Enables or disables the SlidingMenu's fade in and out
	 *
	 * @param b true to enable fade, false to disable it
	 */
	public void setFadeEnabled(boolean b) {
		mViewAbove.setLeftFadeEnabled(b);
	}

	/**
	 * Sets how much the SlidingMenu fades in and out. Fade must be enabled, see
	 * {@link #setFadeEnabled(boolean) setFadeEnabled(boolean)}
	 *
	 * @param f the new fade degree, between 0.0f and 1.0f
	 */
	public void setFadeDegree(float f) {
		mViewAbove.setLeftFadeDegree(f);
	}

	/**
	 * Enables or disables whether the selector is drawn
	 *
	 * @param b true to draw the selector, false to not draw the selector
	 */
	public void setSelectorEnabled(boolean b) {
		mViewAbove.setSelectorEnabled(true);
	}

	/**
	 * Sets the selected view. The selector will be drawn here
	 *
	 * @param v the new selected view
	 */
	public void setSelectedView(View v) {
		mViewAbove.setSelectedView(v);
	}

	/**
	 * Sets the selector drawable.
	 *
	 * @param res a resource ID for the selector drawable
	 */
	public void setSelectorDrawable(int res) {
		mViewAbove.setSelectorBitmap(BitmapFactory.decodeResource(getResources(), res));
	}

	/**
	 * Sets the selector drawable.
	 *
	 * @param b the new selector bitmap
	 */
	public void setSelectorBitmap(Bitmap b) {
		mViewAbove.setSelectorBitmap(b);
	}

	/**
	 * 适配不同的分辨率的屏幕
	 * @param windowWidthPix
	 * @param slideOffest
	 */
	public void setWindowWidth(int windowWidthPix, int slideOffest) {
		mViewAbove.setWindowWidth(windowWidthPix,slideOffest);
	}

	/**
	 * Sets the OnOpenListener. {@link OnOpenListener#onOpen() OnOpenListener.onOpen()} will be called when the SlidingMenu is opened
	 *
	 * @param listener the new OnOpenListener
	 */
	public void setOnOpenListener(OnOpenListener listener) {
		//mViewAbove.setOnOpenListener(listener);
		mOpenListener = listener;
	}

	/**
	 * Sets the OnCloseListener. {@link OnCloseListener#onClose() OnCloseListener.onClose()} will be called when the SlidingMenu is closed
	 *
	 * @param listener the new setOnCloseListener
	 */
	public void setOnCloseListener(OnCloseListener listener) {
		//mViewAbove.setOnCloseListener(listener);
		mCloseListener = listener;
	}

	/**
	 * Sets the OnOpenedListener. {@link OnOpenedListener#onOpened() OnOpenedListener.onOpened()} will be called after the SlidingMenu is opened
	 *
	 * @param listener the new OnOpenedListener
	 */
	public void setOnOpenedListener(OnOpenedListener listener) {
		mViewAbove.setOnOpenedListener(listener);
	}

	/**
	 * Sets the OnClosedListener. {@link OnClosedListener#onClosed() OnClosedListener.onClosed()} will be called after the SlidingMenu is closed
	 *
	 * @param listener the new OnClosedListener
	 */
	public void setOnClosedListener(OnClosedListener listener) {
		mViewAbove.setOnClosedListener(listener);
	}

	private boolean mChildrenEnabled = true;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		return !mChildrenEnabled;
	}
	
	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}
	public void setChildrenEnabled(boolean enabled) {
		mChildrenEnabled = enabled;
	}
	
	//下面是保存用户状态
	public static class SavedState extends BaseSavedState {
		
		private final boolean mLeftShowing;

		public SavedState(Parcelable superState, boolean isLeftShowing) {
			super(superState);
			mLeftShowing = isLeftShowing;
		}

		/* (non-Javadoc)
		 * @see android.view.AbsSavedState#writeToParcel(android.os.Parcel, int)
		 */
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeByte(mLeftShowing ? (byte)1 : 0);
		}

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        
		private SavedState(Parcel in) {
			super(in);
			mLeftShowing = in.readByte()!=0;
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View#onSaveInstanceState()
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState, isLeftShowing());
		return ss;
	}

	/* (non-Javadoc)
	 * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState ss = (SavedState)state;
		super.onRestoreInstanceState(ss.getSuperState());

//		if (ss.mLeftShowing) {
//			showLeft();
//		} else {
//			showAbove();
//		}
	}

	/* (non-Javadoc)
	 * @see android.view.ViewGroup#fitSystemWindows(android.graphics.Rect)
	 */
	@Override
	protected boolean fitSystemWindows(Rect insets) {

        int leftPadding = getPaddingLeft() + insets.left;
        int rightPadding = getPaddingRight() + insets.right;
        int topPadding = insets.top;
        int bottomPadding = insets.bottom;
        this.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);

		return super.fitSystemWindows(insets);
	}

	/**
	 * @param enable :true enable right slide ,false will disable right slibe , default is false
	 */
	public void setRightMenuEnable(boolean enable) {
		mViewAbove.setRightEnable(enable);
	}
	
	/**
	 * @param enable :true enable left slide ,false will disable left slibe
	 */
	public void setLeftMenuEnable(boolean enable){
		mViewAbove.setLeftEnable(enable);
	}

	public void setRightInVisiable() {
		mViewRight.setVisibility(View.INVISIBLE);
	}

	public void setLeftInVisiable() {
		mViewLeft.setVisibility(View.INVISIBLE);
	}
}