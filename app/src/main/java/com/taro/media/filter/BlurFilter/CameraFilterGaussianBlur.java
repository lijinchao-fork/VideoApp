package com.taro.media.filter.BlurFilter;

import android.content.Context;

import com.taro.media.filter.FilterGroup;
import com.taro.media.filter.CameraFilter;


public class CameraFilterGaussianBlur extends FilterGroup<CameraFilter> {

    public CameraFilterGaussianBlur(Context context, float blur) {
        super();
        addFilter(new CameraFilterGaussianSingleBlur(context, blur, false));
        addFilter(new CameraFilterGaussianSingleBlur(context, blur, true));
    }
}
