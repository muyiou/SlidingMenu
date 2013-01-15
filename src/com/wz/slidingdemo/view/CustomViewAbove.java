package com.wz.slidingdemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.wz.slidingdemo.view.SlidingMenu.OnClosedListener;
import com.wz.slidingdemo.view.SlidingMenu.OnClosedListener;
import com.wz.slidingdemo.view.SlidingMenu.OnOpenedListener;

/*
 * event.getX范围为0--480
 * ScrollX范围为0-900;900 = 480 * 2 -60
 * 
 */

/**
 * 
 *
 */
public class CustomViewAbove extends RelativeLayout {

	private static final boolean USE_CACHE = false;

	private static final int MAX_SETTLE_DURATION = 600; // ms
	private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t + 1.0f;
		}
	};

	private Window mLeftMenu;
	private Window rightMenu;
	private Content mContent;

	private int mCurItem;
	private Scroller mScroller;

	private int mShadowWidth;
	private Drawable mLeftShadowDrawable;

	private boolean mScrollingCacheEnabled;

	private boolean mIsBeingDragged;
	// private boolean mIsUnableToDrag;
	private int mTouchSlop;
	private float mInitialMotionX;
	/**
	 * Position of the last motion event.
	 */
	private float mLastMotionX;
	private float mLastMotionY;
	/**
	 * ID of the active pointer. This is used to retain consistency during
	 * drags/flings if multiple pointers are used.
	 */
	protected int mActivePointerId = INVALID_POINTER;
	/**
	 * Sentinel value for no current active pointer. Used by
	 * {@link #mActivePointerId}.
	 */
	private static final int INVALID_POINTER = -1;
	
	/**
	 * Determines speed during touch scrolling
	 */
	protected VelocityTracker mVelocityTracker;
	private int mMinimumVelocity;
	protected int mMaximumVelocity;
	private int mFlingDistance;

	private boolean mLastTouchAllowed = false;
	private final int mSlidingMenuThreshold = 20;
	private CustomViewLeft mCustomViewLeft;
	private CustomViewRight mCustomViewRight;
	private OnPageChangeListener mOnPageChangeListener;
	private OnPageChangeListener mInternalPageChangeListener;
	
	// private OnCloseListener mCloseListener;
	// private OnOpenListener mOpenListener;
	private OnClosedListener mClosedListener;
	private OnOpenedListener mOpenedListener;

	// private int mScrollState = SCROLL_STATE_IDLE;

	/**
	 * Callback interface for responding to changing state of the selected page.
	 */
	public interface OnPageChangeListener {

		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels);

		public void onPageSelected(int position);

	}

	public static class SimpleOnPageChangeListener implements
			OnPageChangeListener {

		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		public void onPageSelected(int position) {
		}

		public void onPageScrollStateChanged(int state) {
		}

	}

	private class Window extends FrameLayout {

		public Window(Context context) {
			super(context);
		}

		public boolean onTouchEvent(MotionEvent event) {
			return false;
		}

		public boolean onInterceptTouchEvent(MotionEvent event) {
			return false;
		}

	}

	public class Content extends FrameLayout {
		public Content(Context context) {
			super(context);
		}

		public boolean onTouchEvent(MotionEvent event) {
			return super.onTouchEvent(event);
		}

		public boolean onInterceptTouchEvent(MotionEvent event) {
			return super.onInterceptTouchEvent(event);
		}
	}

	public CustomViewAbove(Context context) {
		this(context, null);
	}

	public CustomViewAbove(Context context, AttributeSet attrs) {
		this(context, attrs, true);
	}

	public CustomViewAbove(Context context, AttributeSet attrs, boolean isAbove) {
		super(context, attrs);
		initCustomViewAbove(isAbove);
	}
	
	private boolean leftOpened = false;
	private boolean rightOpened = false;

	void initCustomViewAbove(boolean isAbove) {
		
		setWillNotDraw(false);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setFocusable(true);
		final Context context = getContext();
		mScroller = new Scroller(context, sInterpolator);
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat
				.getScaledPagingTouchSlop(configuration);
		
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		setInternalPageChangeListener(new SimpleOnPageChangeListener() {

			public void onPageSelected(int position) {
				if (mCustomViewLeft != null) {
					
					switch (position) {
					case 0:
						mCustomViewLeft.setVisibility(View.VISIBLE);
						mCustomViewLeft.setChildrenEnabled(true);
						mCustomViewRight.setVisibility(View.GONE);
						mCustomViewRight.setChildrenEnabled(false);
						leftOpened = true;
						rightOpened = false;
						
						break;
					case 1:
						mCustomViewLeft.setChildrenEnabled(false);
						leftOpened = false;
						rightOpened = false;
						break;
					case 2:
						mCustomViewRight.setVisibility(View.VISIBLE);
						mCustomViewRight.setChildrenEnabled(true);
						mCustomViewLeft.setChildrenEnabled(false);
						mCustomViewLeft.setVisibility(View.GONE);
						leftOpened = false;
						rightOpened = true;
						break;
					}
				}
			}

		});

		final float density = context.getResources().getDisplayMetrics().density;
		mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);

		mLeftMenu = new Window(getContext());
		super.addView(mLeftMenu);
		rightMenu = new Window(getContext());
		super.addView(rightMenu);
		mContent = new Content(getContext());
		super.addView(mContent);
	}

	public void setCurrentItem(int item) {
		setCurrentItemInternal(item, true, false);
	}
	
	public void setCurrentItem(int item, boolean smoothScroll) {
		setCurrentItemInternal(item, smoothScroll, false);
	}

	public int getCurrentItem() {
		return mCurItem;
	}

	void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
		setCurrentItemInternal(item, smoothScroll, always, 0);
	}

	void setCurrentItemInternal(int item, boolean smoothScroll, boolean always,
			int velocity) {
		if (!always && mCurItem == item && mLeftMenu != null && mContent != null) {
			setScrollingCacheEnabled(false);
			return;
		}
		if (item < 0) {
			item = 0;
		} else if (item  > 3) {
			item = 1;
		}
		final boolean dispatchSelected = mCurItem != item;
		mCurItem = item;
		final int destX = getDestScrollX(mCurItem);
		if (dispatchSelected && mOnPageChangeListener != null) {
			mOnPageChangeListener.onPageSelected(item);
		}
		if (dispatchSelected && mInternalPageChangeListener != null) {
			mInternalPageChangeListener.onPageSelected(item);
		}
		if (smoothScroll) {
			smoothScrollTo(destX, 0, velocity);
		} else {
			completeScroll();
			scrollTo(destX, 0);
		}
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mOnPageChangeListener = listener;
	}

	public void setOnOpenedListener(OnOpenedListener l) {
		mOpenedListener = l;
	}

	public void setOnClosedListener(OnClosedListener l) {
		mClosedListener = l;
	}

	/**
	 * Set a separate OnPageChangeListener for internal use by the support
	 * library.
	 * 
	 * @param listener
	 *            Listener to set
	 * @return The old listener that was set, if any.
	 */
	OnPageChangeListener setInternalPageChangeListener(
			OnPageChangeListener listener) {
		OnPageChangeListener oldListener = mInternalPageChangeListener;
		mInternalPageChangeListener = listener;
		return oldListener;
	}

	/**
	 * Set the margin between pages.
	 * 
	 * @param shadowWidth
	 *            Distance between adjacent pages in pixels
	 * @see #getShadowWidth()
	 * @see #setShadowDrawable(Drawable)
	 * @see #setShadowDrawable(int)
	 */
	public void setShadowWidth(int shadowWidth) {
		mShadowWidth = shadowWidth;
		invalidate();
	}

	/**
	 * Return the margin between pages.
	 * 
	 * @return The size of the margin in pixels
	 */
	public int getShadowWidth() {
		return mShadowWidth;
	}

	/**
	 * Set a drawable that will be used to fill the margin between pages.
	 * 
	 * @param d
	 *            Drawable to display between pages
	 */
	public void setLeftShadowDrawable(Drawable d) {
		mLeftShadowDrawable = d;
		refreshDrawableState();
		setWillNotDraw(false);
		invalidate();
	}

	private Drawable mRightShadowDrawable;
	
	/**
	 * Set a drawable that will be used to fill the margin between pages.
	 * 
	 * @param resId
	 *            Resource ID of a drawable to display between pages
	 */
	public void setLeftShadowDrawable(int resId) {
		setLeftShadowDrawable(getContext().getResources().getDrawable(resId));
	}
	public void setRightShadowDrawable(int resId) {
		mRightShadowDrawable = getContext().getResources().getDrawable(resId);
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return super.verifyDrawable(who) || who == mLeftShadowDrawable;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		final Drawable d = mLeftShadowDrawable;
		if (d != null && d.isStateful()) {
			d.setState(getDrawableState());
		}
	}

	// We want the duration of the page snap animation to be influenced by the
	// distance that
	// the screen has to travel, however, we don't want this duration to be
	// effected in a
	// purely linear fashion. Instead, we use this method to moderate the effect
	// that the distance
	// of travel has on the overall snap duration.
	float distanceInfluenceForSnapDuration(float f) {
		f -= 0.5f; // center the values about 0.
		f *= 0.3f * Math.PI / 2.0f;
		return (float) FloatMath.sin(f);
	}

	public int getDestScrollX() {
		if (isLeftMenuOpen()) {
			return getLeftWidth();
		} else {
			return 0;
		}
	}
	
	private int rigtViewOffset = 60;//it`s default

	public int getDestScrollX(int page) {
		switch (page) {
		case 0:
			return mContent.getPaddingLeft();
		case 1:
			return mContent.getLeft();
		case 2:
			int l = getChildAt(2).getRight() - rigtViewOffset;
			return l;
		}
		return 0;
	}

	public int getContentLeft() {
		return mContent.getLeft() + mContent.getPaddingLeft();
	}
	
	public int getContentRight(){
		return mContent.getRight() + mContent.getPaddingRight();
	}

	public int getChildLeft(int i) {
		if (i <= 0)
			return 0;
		return getChildWidth(i - 1) + getChildLeft(i - 1);
	}

	public int getChildRight(int i) {
		return getChildLeft(i) + getChildWidth(i);
	}

	public boolean isRightMenuOpen(){
		return getCurrentItem() == 2;
	}
	
	public boolean isLeftMenuOpen() {
		return getCurrentItem() == 0;
	}

	public int getCustomWidth() {
		int i = isLeftMenuOpen() ? 0 : 1;
		return getChildWidth(i);
	}

	public int getChildWidth(int i) {
		if (i <= 0) {
			return getLeftWidth();
		} else {
			return getChildAt(i).getWidth();
		}
	}

	public int getLeftWidth() {
		if (mCustomViewLeft == null) {
			return 0;
		} else {
			return mCustomViewLeft.getWidth();
		}
	}

	private float getRightLeft() {
		return mCustomViewRight.getLeft();
	}

	void smoothScrollTo(int x, int y) {
		smoothScrollTo(x, y, 0);
	}

	/**
	 * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
	 * 
	 * @param x
	 *            the number of pixels to scroll by on the X axis
	 * @param y
	 *            the number of pixels to scroll by on the Y axis
	 * @param velocity
	 *            the velocity associated with a fling, if applicable. (0
	 *            otherwise)
	 */
	void smoothScrollTo(int x, int y, int velocity) {
		if (getChildCount() == 0) {
			setScrollingCacheEnabled(false);
			return;
		}
		int sx = getScrollX();
		int sy = getScrollY();
		int dx = x - sx;
		int dy = y - sy;
		if (dx == 0 && dy == 0) {
			completeScroll();
			if (isLeftMenuOpen()) {
				if (mOpenedListener != null)
					mOpenedListener.onOpened();
			} else {
				if (mClosedListener != null)
					mClosedListener.onClosed();
			}
			return;
		}

		setScrollingCacheEnabled(true);
		final int width = getCustomWidth();
		final int halfWidth = width / 2;
		final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
		final float distance = halfWidth + halfWidth
				* distanceInfluenceForSnapDuration(distanceRatio);

		int duration = 0;
		velocity = Math.abs(velocity);
		if (velocity > 0) {
			duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
		} else {
			final float pageDelta = (float) Math.abs(dx)
					/ (width + mShadowWidth);
			duration = (int) ((pageDelta + 1) * 100);
			duration = MAX_SETTLE_DURATION;
		}
		duration = Math.min(duration, MAX_SETTLE_DURATION);

		mScroller.startScroll(sx, sy, dx, dy, duration);
		invalidate();
	}

	protected void setMenu(View v) {
		mLeftMenu.addView(v);
	}

	public void setRightMenu(View v) {
		rightMenu.addView(v);		
	}

	public void setContent(View v) {
		if (mContent.getChildCount() > 0) {
			mContent.removeAllViews();
		}
		mContent.addView(v);
	}
	
	public void setCustomViewRight(CustomViewRight v){
		mCustomViewRight = v;
	}

	public void setCustomViewLeft(CustomViewLeft cvb) {
		mCustomViewLeft = cvb;
	}
	
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(width, height);

		final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width);
		final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0,
				height);
		mContent.measure(contentWidth, contentHeight);

		final int menuWidth = getChildMeasureSpec(widthMeasureSpec, 0,
				getLeftWidth());
		mLeftMenu.measure(menuWidth, contentHeight);
		rightMenu.measure(menuWidth, contentHeight);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if(oldw == 0){
			super.onSizeChanged(w, h, oldw, oldh);
			// Make sure scroll position is set correctly.
			if (w != oldw) {
				// [ChrisJ] - This fixes the onConfiguration change for orientation
				// issue..
				// maybe worth having a look why the recomputeScroll pos is screwing
				// up?
				completeScroll();
				scrollTo(getChildLeft(mCurItem), getScrollY());
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = r - l;
		final int height = b - t;

		int contentLeft = getChildLeft(1);
		mLeftMenu.layout(0, 0, width, height);
		rightMenu.layout(0, 0, width, height);
		mContent.layout(contentLeft, 0, contentLeft + width, height);
	}

	public void setAboveOffset(int i) {
		mContent.setPadding(i, mContent.getPaddingTop(),
				mContent.getPaddingRight(), mContent.getPaddingBottom());
	}

	@Override
	public void computeScroll() {
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				int oldX = getScrollX();
				int oldY = getScrollY();
				int x = mScroller.getCurrX();
				int y = mScroller.getCurrY();

				if (oldX != x || oldY != y) {
					scrollTo(x, y);
					pageScrolled(x);
				}

				// Keep on drawing until the animation has finished.
				invalidate();
				return;
			}
		}

		// Done with scroll, clean up state.
		completeScroll();
	}

	private void pageScrolled(int xpos) {
		final int widthWithMargin = getChildWidth(mCurItem) + mShadowWidth;
		final int position = xpos / widthWithMargin;
		final int offsetPixels = xpos % widthWithMargin;
		final float offset = (float) offsetPixels / widthWithMargin;

		onPageScrolled(position, offset, offsetPixels);
	}

	protected void onPageScrolled(int position, float offset, int offsetPixels) {
		if (mOnPageChangeListener != null) {
			mOnPageChangeListener
					.onPageScrolled(position, offset, offsetPixels);
		}
		if (mInternalPageChangeListener != null) {
			mInternalPageChangeListener.onPageScrolled(position, offset,
					offsetPixels);
		}
	}

	private void completeScroll() {
		if ( mScroller.isFinished()) {
			// Done with scroll, no longer want to cache view drawing.
			setScrollingCacheEnabled(false);
			mScroller.abortAnimation();
			if (isLeftMenuOpen()) {
				if (mOpenedListener != null)
					mOpenedListener.onOpened();
			} else {
				if (mClosedListener != null)
					mClosedListener.onClosed();
			}
		}
	}

	protected int mTouchMode = SlidingMenu.TOUCHMODE_MARGIN;
	private int mTouchModeLeft = SlidingMenu.TOUCHMODE_MARGIN;

	public void setTouchMode(int i) {
		mTouchMode = i;
	}

	public int getTouchMode() {
		return mTouchMode;
	}

	protected void setTouchModeLeft(int i) {
		mTouchModeLeft = i;
	}

	protected int getTouchModeLeft() {
		return mTouchModeLeft;
	}

	private boolean thisTouchAllowed(MotionEvent ev) {
		int x = (int) (ev.getX() + mScrollX);
		if (isLeftMenuOpen()) {
			switch (mTouchModeLeft) {
			case SlidingMenu.TOUCHMODE_FULLSCREEN:
				return true;
			case SlidingMenu.TOUCHMODE_NONE:
				return false;
			case SlidingMenu.TOUCHMODE_MARGIN:
				return x >= getContentLeft();
			default:
				return false;
			}
		}
		else if(rightOpened){
			//TODO：这个是边距
			return ev.getX() <=  slideOffset;
		}
		else {
			switch (mTouchMode) {
			case SlidingMenu.TOUCHMODE_FULLSCREEN:
				return true;
			case SlidingMenu.TOUCHMODE_NONE:
				return false;
			case SlidingMenu.TOUCHMODE_MARGIN:
				int pixels = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, mSlidingMenuThreshold,
						getResources().getDisplayMetrics());
				int left = getContentLeft();
				return (x >= left && x <= pixels + left);
			default:
				return false;
			}
		}
	}

	//TODO:可以在这里进行设置是否允许左边或者右边slide
	private boolean thisSlideAllowed(float dx) {
		if (leftOpened) {
			 return   dx < 0;
		}else if(rightOpened){
			
		 return  dx > 0;
		}
		else if(leftEnable && dx > 0){
			return true;
		}else if(rightEnable && dx < 0){
			return true;
		}
		return false;
	}

	private boolean mIsUnableToDrag;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
		if(!leftEnable && !rightEnable){
			return false;
		}

		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			mIsBeingDragged = false;
			mIsUnableToDrag = false;
			mActivePointerId = INVALID_POINTER;
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			return false;
		}

		if (action != MotionEvent.ACTION_DOWN) {
			if (mIsBeingDragged)
				return true;
			else if (mIsUnableToDrag)
				return false;
		}

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int activePointerId = mActivePointerId;
			
			if (activePointerId == INVALID_POINTER)
				break;

			final int pointerIndex = MotionEventCompat.findPointerIndex(ev,
					activePointerId);
			if (pointerIndex == -1) {
				mActivePointerId = INVALID_POINTER;
				break;
			}
			final float x = MotionEventCompat.getX(ev, pointerIndex);
			final float dx = x - mLastMotionX;
			
			final float xDiffAbs = Math.abs(dx);
			final float y = MotionEventCompat.getY(ev, pointerIndex);
			final float yDiffAbs = Math.abs(y - mLastMotionY);
			if (xDiffAbs > mTouchSlop && xDiffAbs > yDiffAbs && thisSlideAllowed(dx)) {
				mIsBeingDragged = true;
				mLastMotionX = x;
				setScrollingCacheEnabled(true);
			} else if (yDiffAbs > mTouchSlop) {
				mIsUnableToDrag = true;
			}
			break;

		case MotionEvent.ACTION_DOWN:
			mActivePointerId = ev.getAction()
					& ((Build.VERSION.SDK_INT >= 8) ? MotionEvent.ACTION_POINTER_ID_MASK
							: MotionEvent.ACTION_POINTER_ID_MASK);
			mLastMotionX = mInitialMotionX = MotionEventCompat.getX(ev,
					mActivePointerId);
			
			mLastMotionY = MotionEventCompat.getY(ev, mActivePointerId);
			if (thisTouchAllowed(ev)) {
				mIsBeingDragged = false;
				mIsUnableToDrag = false;
				
				if (isLeftMenuOpen() && mInitialMotionX > getLeftWidth() ||
					isRightMenuOpen() && mInitialMotionX < getRightLeft())
					return true;
			} else {
				mIsUnableToDrag = true;
			}
			break;
		case MotionEventCompat.ACTION_POINTER_UP:
			break;
		}

		if (!mIsBeingDragged) {
			if (mVelocityTracker == null) {
				mVelocityTracker = VelocityTracker.obtain();
			}
			mVelocityTracker.addMovement(ev);
		}
		
		return mIsBeingDragged;
	}

	private boolean forLeft = false;
	private boolean forRight = false;;
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!mIsBeingDragged && !mLastTouchAllowed && !thisTouchAllowed(ev)) {
			//这里判断，如果应该给子类，那就返回
			return false;
		}
		
		final int action = ev.getAction();

		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_OUTSIDE) {
			mLastTouchAllowed = false;
		} else {
			mLastTouchAllowed = true;
		}

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		switch (action & MotionEventCompat.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			//如果左边或者右边已经打开，点击直接返回
			completeScroll();

			// Remember where the motion event started
			mLastMotionX = mInitialMotionX = ev.getX();
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			break;
		case MotionEvent.ACTION_MOVE:
			
			if (!mIsBeingDragged) {
				final int pointerIndex = MotionEventCompat.findPointerIndex(ev,
						mActivePointerId);
				if (pointerIndex == -1) {
					mActivePointerId = INVALID_POINTER;
					break;
				}
				final float x = MotionEventCompat.getX(ev, pointerIndex);
				
				final int deltaX = (int) (x - mLastMotionX);
				
				final float xDiffAbs = Math.abs(x - mLastMotionX);
				final float y = MotionEventCompat.getY(ev, pointerIndex);
				final float yDiffAbs = Math.abs(y - mLastMotionY);
				
				if (xDiffAbs > mTouchSlop && xDiffAbs > yDiffAbs && thisSlideAllowed(deltaX)) {
					mIsBeingDragged = true;
					mLastMotionX = x;
					setScrollingCacheEnabled(true);
				}
			}
			if (mIsBeingDragged) {
				// Scroll to follow the motion event
				final int activePointerIndex = MotionEventCompat
						.findPointerIndex(ev, mActivePointerId);
				if (activePointerIndex == -1) {
					mActivePointerId = INVALID_POINTER;
					break;
				}
				final float x = MotionEventCompat.getX(ev, activePointerIndex);
				final float deltaX = mLastMotionX - x;
				mLastMotionX = x;
				float oldScrollX = getScrollX();
				float scrollX = oldScrollX + deltaX;
				mLastMotionX += scrollX - (int) scrollX;
				
				//主要为了在滑动center的时候，控制后面背景的显示
				if(!leftOpened && !rightOpened){
					if(!forRight && !forLeft){
						if(deltaX > 0){
							mCustomViewLeft.setVisibility(View.GONE);
							mCustomViewRight.setVisibility(View.VISIBLE);
							forRight = true;
						}else if(deltaX < 0){
							mCustomViewLeft.setVisibility(View.VISIBLE);
							mCustomViewRight.setVisibility(View.GONE);
							forLeft = true;
						}
					}
				}
				//这里420是个界限，如果是forLeft，就不能大于420
				if(forLeft && scrollX < windowWidthInPix - slideOffset){
					scrollTo((int) scrollX, getScrollY());
				}
				if(forRight && scrollX > windowWidthInPix - slideOffset){
					scrollTo((int) scrollX, getScrollY());
				}
				if(leftOpened || rightOpened){
					scrollTo((int) scrollX, getScrollY());
				}
				
				pageScrolled((int) scrollX);
			}
			break;
		case MotionEvent.ACTION_UP:
			forLeft = false;
			forRight = false;
			
			if (mIsBeingDragged) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(
						velocityTracker, mActivePointerId);
				final int widthWithMargin = getChildWidth(mCurItem)
						+ mShadowWidth;
				final int scrollX = getScrollX();
				final int currentPage = scrollX / widthWithMargin;
				final float pageOffset = (float) (scrollX % widthWithMargin)
						/ widthWithMargin;
				final int activePointerIndex = MotionEventCompat
						.findPointerIndex(ev, mActivePointerId);
				float x = 0;
				try{
					 x = MotionEventCompat.getX(ev, activePointerIndex);
				}catch(Exception e){
					e.printStackTrace();
//					endDrag();
					return false;
				}
				
				final int totalDelta = (int) (x - mInitialMotionX);
				int nextPage = determineTargetPage(currentPage, pageOffset,
						initialVelocity, totalDelta);
				
				//TODO:fix，有时候会从第三个页面直接跳到第一个页面
				if(Math.abs(nextPage - getCurrentItem()) > 1){
					nextPage = 1;
				}
				setCurrentItemInternal(nextPage, true, true, initialVelocity);

				mActivePointerId = INVALID_POINTER;
				endDrag();
			} else  {
				// close the menu
				setCurrentItem(1);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			if (mIsBeingDragged) {
				setCurrentItemInternal(mCurItem, true, true);
				mActivePointerId = INVALID_POINTER;
				endDrag();
			}
			break;
		case MotionEventCompat.ACTION_POINTER_DOWN: {
			final int index = MotionEventCompat.getActionIndex(ev);
			final float x = MotionEventCompat.getX(ev, index);
			mLastMotionX = x;
			mActivePointerId = MotionEventCompat.getPointerId(ev, index);
			break;
		}
		case MotionEventCompat.ACTION_POINTER_UP:
			mLastMotionX = MotionEventCompat.getX(ev,
					MotionEventCompat.findPointerIndex(ev, mActivePointerId));
			break;
		}
		if (mActivePointerId == INVALID_POINTER)
			mLastTouchAllowed = false;
		return true;
	}

	private float mScrollScale;

	public float getScrollScale() {
		return mScrollScale;
	}

	public void setScrollScale(float f) {
		if (f < 0 && f > 1)
			throw new IllegalStateException(
					"ScrollScale must be between 0 and 1");
		mScrollScale = f;
	}

	@Override
	public void scrollTo(int x, int y) {
		//这个会移动中间的view
		super.scrollTo(x, y);
		mScrollX = x;
//		if (mCustomViewLeft != null && mEnabled) {
			// TODO:这里左边的会移动
//			mCustomViewLeft.scrollTo((int) (x * mScrollScale), y);
//		}
		if (mLeftShadowDrawable != null || mSelectorDrawable != null)
			invalidate();
	}

	private int determineTargetPage(int currentPage, float pageOffset,
			int velocity, int deltaX) {
		int targetPage;
		if (Math.abs(deltaX) > mFlingDistance
				&& Math.abs(velocity) > mMinimumVelocity) {
			targetPage = velocity > 0 ? currentPage : currentPage + 1;
		} else {
			targetPage = (int) (currentPage + pageOffset + 0.5f);
		}
		return targetPage;
	}

	protected float getPercentOpen() {
		return (getLeftWidth() - mScrollX) / getLeftWidth();
	}

//	private float getRightPercentOpen() {
//		return (mScrollX - getLeftWidth())/getLeftWidth()
//	}
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		// Draw the margin drawable if needed.
		if (mShadowWidth > 0 && mLeftShadowDrawable != null) {
			final int left = getContentLeft() - mShadowWidth;
			mLeftShadowDrawable
					.setBounds(left, 0, left + mShadowWidth, getHeight());
			mLeftShadowDrawable.draw(canvas);
		}
		
		if(mShadowWidth > 0 && mRightShadowDrawable != null){
			final int right = getContentRight();
			mRightShadowDrawable.setBounds(right, 0, right + mShadowWidth, getHeight());
			mRightShadowDrawable.draw(canvas);
		}
		
		if(getChildAt(0).getVisibility() == 0){
			onDrawLeftFade(canvas, getPercentOpen());
		}
		if(getChildAt(2).getVisibility() == 0){
			onDrawRightFade(canvas,getPercentOpen());
		}

		if (mSelectorEnabled)
			onDrawMenuSelector(canvas, getPercentOpen());
	}

	/**
	 * Pads our content window so that it fits within the system windows.
	 * 
	 * @param insets
	 *            The insets by which we need to offset our view.
	 * @return True since we handled the padding change.
	 */
	@Override
	protected boolean fitSystemWindows(Rect insets) {

		if (mContent != null) {
			int leftPadding = mContent.getPaddingLeft() + insets.left;
			int rightPadding = mContent.getPaddingRight() + insets.right;
			int topPadding = insets.top;
			int bottomPadding = insets.bottom;
			mContent.setPadding(leftPadding, topPadding, rightPadding,
					bottomPadding);
			return true;
		}

		return super.fitSystemWindows(insets);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	// variables for drawing
	private float mScrollX = 0.0f;
	private float mFadeDegree = 1.0f;
	private final Paint mLeftFadePaint = new Paint();
	// for the indicator
	private boolean mSelectorEnabled = true;
	private Bitmap mSelectorDrawable;
	private View mSelectedView;

	/*
	 * 绘制左边渐变效果
	 */
	private void onDrawLeftFade(Canvas canvas, float openPercent) {
		final int alpha = (int) (mFadeDegree * 255 * Math.abs(1 - openPercent));
		if (alpha > 0) {
			mLeftFadePaint.setColor(Color.argb(alpha, 0, 0, 0));
			canvas.drawRect(-slideOffset, 0, getContentLeft() , getHeight(),
					mLeftFadePaint);
		}
	}

	/*
	 * 绘制右边渐变效果
	 */
	private void onDrawRightFade(Canvas canvas, float percentOpen) {
		int rightAlpha = (int) (mFadeDegree * 255 * Math.abs(1 + percentOpen));
		mLeftFadePaint.setColor(Color.argb(rightAlpha, 0, 0, 0));
		canvas.drawRect(getContentRight(), 0, getContentRight() + 480 , getHeight(),
				mLeftFadePaint);
	}

	private void onDrawMenuSelector(Canvas canvas, float openPercent) {
//		if (mSelectorDrawable != null && mSelectedView != null) {
//			String tag = (String) mSelectedView.getTag(R.id.selected_view);
//			if (tag.equals(TAG + "SelectedView")) {
//				int right = getChildLeft(1);
//				int left = (int) (right - mSelectorDrawable.getWidth()
//						* openPercent);
//
//				canvas.save();
//				canvas.clipRect(left, 0, right, getHeight());
//				canvas.drawBitmap(mSelectorDrawable, left, getSelectedTop(),
//						null);
//				canvas.restore();
//			}
//		}
	}

	public void setLeftFadeEnabled(boolean b) {
	}

	public void setLeftFadeDegree(float f) {
		if (f > 1.0f || f < 0.0f)
			throw new IllegalStateException(
					"The LeftFadeDegree must be between 0.0f and 1.0f");
		mFadeDegree = f;
	}

	public void setSelectorEnabled(boolean b) {
		mSelectorEnabled = b;
	}

	public void setSelectedView(View v) {
		if (mSelectedView != null) {
//			mSelectedView.setTag(R.id.selected_view, null);
			mSelectedView = null;
		}
		if (v.getParent() != null) {
			mSelectedView = v;
//			mSelectedView.setTag(R.id.selected_view, TAG + "SelectedView");
			invalidate();
		}
	}

	public void setSelectorBitmap(Bitmap b) {
		mSelectorDrawable = b;
		refreshDrawableState();
	}

	private void endDrag() {
		mIsBeingDragged = false;
		mIsUnableToDrag = false;
		mLastTouchAllowed = false;

		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private void setScrollingCacheEnabled(boolean enabled) {
		if (mScrollingCacheEnabled != enabled) {
			mScrollingCacheEnabled = enabled;
			if (USE_CACHE) {
				final int size = getChildCount();
				for (int i = 0; i < size; ++i) {
					final View child = getChildAt(i);
					if (child.getVisibility() != GONE) {
						child.setDrawingCacheEnabled(enabled);
					}
				}
			}
		}
	}

	private int windowWidthInPix ;
	private int slideOffset;
	
	public void setWindowWidth(int windowWidthInPix, int slideOffest) {
		this.windowWidthInPix = windowWidthInPix;
		this.slideOffset = slideOffest;
	}

	private boolean leftEnable = false;
	private boolean rightEnable = false;
	
	public void setRightEnable(boolean enable) {
		// TODO Auto-generated method stub
		rightEnable = enable;
	}

	public void setLeftEnable(boolean enable) {
		// TODO Auto-generated method stub
		leftEnable = enable;
	}

	public void setSlidingMenu(SlidingMenu sm) {
	}

	/**
	 * Tests scrollability within child views of v given a delta of dx.
	 * 
	 * @param v
	 *            View to test for horizontal scrollability
	 * @param checkV
	 *            Whether the view v passed should itself be checked for
	 *            scrollability (true), or just its children (false).
	 * @param dx
	 *            Delta scrolled in pixels
	 * @param x
	 *            X coordinate of the active touch point
	 * @param y
	 *            Y coordinate of the active touch point
	 * @return true if child views of v can be scrolled by delta of dx.
	 */
//	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
//		if (v instanceof ViewGroup) {
//			final ViewGroup group = (ViewGroup) v;
//			final int scrollX = v.getScrollX();
//			final int scrollY = v.getScrollY();
//			final int count = group.getChildCount();
//			// Count backwards - let topmost views consume scroll distance
//			// first.
//			for (int i = count - 1; i >= 0; i--) {
//				final View child = group.getChildAt(i);
//				if (x + scrollX >= child.getLeft()
//						&& x + scrollX < child.getRight()
//						&& y + scrollY >= child.getTop()
//						&& y + scrollY < child.getBottom()
//						&& canScroll(child, true, dx,
//								x + scrollX - child.getLeft(), y + scrollY
//										- child.getTop())) {
//					return true;
//				}
//			}
//		}
//
//		return checkV && ViewCompat.canScrollHorizontally(v, -dx);
//	}

}
