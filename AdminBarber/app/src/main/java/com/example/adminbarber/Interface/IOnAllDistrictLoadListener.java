package com.example.adminbarber.Interface;

import com.example.adminbarber.Model.District;

import java.util.List;

public interface IOnAllDistrictLoadListener {
    void onAllDistrictLoadSuccess(List<District> districtList);
    void onAllDistrictLoadFailed(String message);
}
