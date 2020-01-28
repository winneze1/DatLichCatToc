package com.example.datlichcattoc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.example.datlichcattoc.Adapter.MyViewPagerAdapter;
import com.example.datlichcattoc.Common.Common;
import com.example.datlichcattoc.Common.NonSwipeViewPager;
import com.example.datlichcattoc.Model.Barber;
import com.example.datlichcattoc.Model.EventBus.BarberDoneEvent;
import com.example.datlichcattoc.Model.EventBus.ConfirmBookingEvent;
import com.example.datlichcattoc.Model.EventBus.DisplayTimeSlotEvent;
import com.example.datlichcattoc.Model.EventBus.EnableNextButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class BookingActivity extends AppCompatActivity {

//    LocalBroadcastManager localBroadcastManager;
    AlertDialog dialog;
    CollectionReference barberRef;

    @BindView(R.id.step_view)
    StepView stepView;
    @BindView(R.id.view_pager)
    NonSwipeViewPager viewPager;
    @BindView(R.id.btn_previous_step)
    Button btn_previous_step;
    @BindView(R.id.btn_next_step)
    Button btn_next_step;

    //Event
    @OnClick(R.id.btn_previous_step)
    void previousStep(){
        if(Common.step == 3 || Common.step > 0)
        {
            Common.step--;
            viewPager.setCurrentItem(Common.step);
            if (Common.step < 3) //lun hien thi nut next khi step <3
            {
                btn_next_step.setEnabled(true);
                setColorButton();
            }
        }
    }
    @OnClick(R.id.btn_next_step)
    void nextClick(){
        if(Common.step < 3 || Common.step == 0)
        {
            Common.step++;//Tang cac buoc len
            if(Common.step == 1) // sau khi chon salon
            {
                if (Common.currentSalon != null){
                    loadBarberBySalon(Common.currentSalon.getSalonID());
                }
            }
            else if(Common.step == 2) // Chon thoi gian
            {
                if (Common.currentBarber != null)
                    loadTimeSlotOfBarber(Common.currentBarber.getBarberId());
            }
            else if(Common.step == 3) // Confirm
            {
                if (Common.currentTimeSlot != -1)
                    confirmBooking();
            }
            viewPager.setCurrentItem(Common.step);
        }
    }

    private void confirmBooking() {
        //Gui broadcast den fragment4
//        Intent intent = new Intent(Common.KEY_CONFIRM_BOOKING);
//        localBroadcastManager.sendBroadcast(intent);


        EventBus.getDefault().postSticky(new ConfirmBookingEvent(true));
    }

    private void loadTimeSlotOfBarber(String barberId) {
        //gui local broadcast toi fragment buoc 3
        //Intent intent = new Intent(Common.KEY_DISPLAY_TIME_SLOT);
        //localBroadcastManager.sendBroadcast(intent);

        EventBus.getDefault().postSticky(new DisplayTimeSlotEvent(true));
    }

    private void loadBarberBySalon(String salonID) {
        dialog.show();

        //Chon tat ca cac tho cat toc cua cua hang
        ///AllSalon/Bình Thạnh/Branch/lQfFD266n5pUGlDMWoNn/Barbers
        if (!TextUtils.isEmpty(Common.district)){
            barberRef = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.district)
                    .collection("Branch")
                    .document(salonID)
                    .collection("Barbers");

            barberRef.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<Barber> barbers = new ArrayList<>();
                            for (QueryDocumentSnapshot barberSnapShot:task.getResult())
                            {
                                Barber barber = barberSnapShot.toObject(Barber.class);
                                barber.setPassword(""); //Khong nen lay pass o app client
                                barber.setBarberId(barberSnapShot.getId());//Lay id cua tho

                                barbers.add(barber);
                            }
                            //Gui broadcast toi trang booking2 de tai Recycler
                            //Intent intent = new Intent(Common.KEY_BARBER_LOAD_DONE);
                            //intent.putParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE, barbers);
                            //localBroadcastManager.sendBroadcast(intent);

                            EventBus.getDefault()
                                    .postSticky(new BarberDoneEvent(barbers));

                            dialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                        }
                    });

        }

    }


    //Event Bus convert
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void buttonNextReceiver(EnableNextButton event)
    {
        int step = event.getStep();
        if(step == 1)
            Common.currentSalon = event.getSalon();
        else if (step == 2)
            Common.currentBarber = event.getBarber();
        else if (step == 3)
            Common.currentTimeSlot = event.getTimeSlot();


        btn_next_step.setEnabled(true);
        setColorButton();
    }

    /*@Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        super.onDestroy();
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        ButterKnife.bind(BookingActivity.this);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        //localBroadcastManager = LocalBroadcastManager.getInstance(this);
        //localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));

        setupStepView();
        setColorButton();

        //View
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4);//co 4 fragment nen can giu state cua 4 trang nay, hem thi no xoa het khi back.
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                //Hien Cac buoc
                stepView.go(i, true);
                if ( i == 0)
                    btn_previous_step.setEnabled(false);
                else
                    btn_previous_step.setEnabled(true);
                //disable next button
                btn_next_step.setEnabled(false);
                setColorButton();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }
    // set mau cho button
    private void setColorButton() {
        if (btn_next_step.isEnabled())
        {
            btn_next_step.setBackgroundResource(R.color.colorButton);
        }
        else
        {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }

        if (btn_previous_step.isEnabled())
        {
            btn_previous_step.setBackgroundResource(R.color.colorButton);
        }
        else
        {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }
    }

    private void setupStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Salon");
        stepList.add("Barber");
        stepList.add("Time");
        stepList.add("Confirm");
        stepView.setSteps(stepList);
    }

    //Event Bus


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
