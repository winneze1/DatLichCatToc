package com.example.appcuathocattoc.Interface;

import com.example.appcuathocattoc.Model.BookingInformation;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList);
    void onTimeSlotLoadFail(String message);
    void onTimeSlotLoadEmpty();
}
