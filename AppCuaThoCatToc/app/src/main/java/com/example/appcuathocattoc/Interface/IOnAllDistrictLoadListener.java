package com.example.appcuathocattoc.Interface;

import com.example.appcuathocattoc.Model.District;

import java.util.List;

public interface IOnAllDistrictLoadListener {
    void onAllDistrictLoadSuccess(List<District> districtList);
    void onAllDistrictLoadFailed(String message);
}
