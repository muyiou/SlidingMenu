package com.wz.slidingdemo.fragment;


import com.wz.slidingdemo.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainCenterFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.main_left_layout, container,
                false);
        return v;
    }

}
