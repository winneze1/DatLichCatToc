package com.example.appcuathocattoc.Interface;

import com.example.appcuathocattoc.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> salonList);
    void onBranchLoadFailed(String message);
}
