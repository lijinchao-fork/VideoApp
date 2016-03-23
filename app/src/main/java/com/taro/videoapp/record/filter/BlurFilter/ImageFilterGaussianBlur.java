package com.taro.videoapp.record.filter.BlurFilter;

import android.content.Context;

import com.taro.videoapp.record.filter.CameraFilter;
import com.taro.videoapp.record.filter.FilterGroup;

public class ImageFilterGaussianBlur extends FilterGroup<CameraFilter> {

    public ImageFilterGaussianBlur(Context context, float blur) {
        super();
        addFilter(new ImageFilterGaussianSingleBlur(context, blur, false));
        addFilter(new ImageFilterGaussianSingleBlur(context, blur, true));
    }
}
