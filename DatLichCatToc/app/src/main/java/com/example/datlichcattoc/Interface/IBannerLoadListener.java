package com.example.datlichcattoc.Interface;

import com.example.datlichcattoc.Model.Banner;

import java.util.List;

public interface IBannerLoadListener {
    void onBannerLoadSuccess(List<Banner> banners);
    void onBannerLoadFail(String message);
}
