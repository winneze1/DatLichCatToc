package com.example.adminbarber.Interface;

import com.example.adminbarber.Model.Barber;

import java.util.List;

public interface IBarberLoadListener {
    void onBarberLoadSuccess(List<Barber> barberList);
    void onBarberLoadFailed(String message);
}
