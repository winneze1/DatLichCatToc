package com.example.datlichcattoc.Interface;

import com.example.datlichcattoc.Model.Banner;

import java.util.List;

public interface ILookbookLoadListener {
    void onLookbookLoadSuccess(List<Banner> banners);
    void onLookbookLoadFail(String message);
}
