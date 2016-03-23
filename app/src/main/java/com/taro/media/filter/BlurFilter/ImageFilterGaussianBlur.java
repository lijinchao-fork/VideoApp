package com.taro.media.filter.BlurFilter;

import android.content.Context;

import com.taro.media.filter.FilterGroup;
import com.taro.media.filter.CameraFilter;

public class ImageFilterGaussianBlur extends FilterGroup<CameraFilter> {

    public ImageFilterGaussianBlur(Context context, float blur) {
        super();
        addFilter(new ImageFilterGaussianSingleBlur(context, blur, false));
        addFilter(new ImageFilterGaussianSingleBlur(context, blur, true));
    }
}
