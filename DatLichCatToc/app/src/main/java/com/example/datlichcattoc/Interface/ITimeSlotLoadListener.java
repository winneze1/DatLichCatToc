package com.example.datlichcattoc.Interface;

import com.example.datlichcattoc.Model.TimeSlot;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList);
    void onTimeSlotLoadFail(String message);
    void onTimeSlotLoadEmpty();
}
