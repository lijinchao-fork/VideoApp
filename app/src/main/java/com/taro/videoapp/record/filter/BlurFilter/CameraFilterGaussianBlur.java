package com.taro.videoapp.record.filter.BlurFilter;

import android.content.Context;

import com.taro.videoapp.record.filter.CameraFilter;
import com.taro.videoapp.record.filter.FilterGroup;


public class CameraFilterGaussianBlur extends FilterGroup<CameraFilter> {

    public CameraFilterGaussianBlur(Context context, float blur) {
        super();
        addFilter(new CameraFilterGaussianSingleBlur(context, blur, false));
        addFilter(new CameraFilterGaussianSingleBlur(context, blur, true));
    }
}
