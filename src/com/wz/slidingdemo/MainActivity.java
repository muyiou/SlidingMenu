package com.wz.slidingdemo;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;

import com.wz.slidingdemo.view.SlidingMenu;

public class MainActivity extends SlidingActivity {

    private SlidingMenu mSlider;
    private int mWidthPixels;
    
    /*
     * slide offset for rightView and leftView in pixels
     */
    private final static int SLIDE_OFFEST = 60;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSlider = this.getSlidingMenu();

        setViewAttrs();
    }

    private void setViewAttrs() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.main_center_fragment);

        this.setLeftContentView(R.layout.main_left_fragment);
        mSlider.setLeftOffset(SLIDE_OFFEST);
        mSlider.setLeftShadowDrawable(R.drawable.left_shadow);
        mSlider.setLeftMenuEnable(true);

        this.setRightContentView(R.layout.main_right_fragment);
        mSlider.setRightOffset(SLIDE_OFFEST);
        mSlider.setRightShadowDrawable(R.drawable.right_shadow);
        mSlider.setRightMenuEnable(true);

        mSlider.setShadowWidth(15);
        mSlider.setWindowWidth(getWindowPix(), SLIDE_OFFEST);
    }
    
    /**
     * 得到屏幕宽度
     * @return
     */
    private int  getWindowPix() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int widthPixels = dm.widthPixels;
//        int heightPixels = dm.heightPixels;
//        float density = dm.density;
        return mWidthPixels;
    }
}
