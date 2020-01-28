package com.example.datlichcattoc.Fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.datlichcattoc.Common.Common;
import com.example.datlichcattoc.Database.CartDataSource;
import com.example.datlichcattoc.Database.CartDatabase;
import com.example.datlichcattoc.Database.CartItem;
import com.example.datlichcattoc.Database.LocalCartDataSource;
import com.example.datlichcattoc.Model.BookingInformation;
import com.example.datlichcattoc.Model.EventBus.ConfirmBookingEvent;
import com.example.datlichcattoc.Model.FCMResponse;
import com.example.datlichcattoc.Model.FCMSendData;
import com.example.datlichcattoc.Model.MyNotification;
import com.example.datlichcattoc.Model.MyToken;
import com.example.datlichcattoc.R;
import com.example.datlichcattoc.Retrofit.IFCMApi;
import com.example.datlichcattoc.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BookingStep4Fragment extends Fragment {

    CartDataSource cartDataSource;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    SimpleDateFormat simpleDateFormat;
    Unbinder unbinder;

    IFCMApi ifcmApi;

    AlertDialog dialog;

    @BindView(R.id.txt_booking_barber_text)
    TextView txt_booking_barber_text;
    @BindView(R.id.txt_booking_time_text)
    TextView txt_booking_time_text;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;
    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;
    @BindView(R.id.txt_salon_website)
    TextView txt_salon_website;

    //Event
    @OnClick(R.id.btn_confirm)
    void confirmBooking(){

        dialog.show();

//        DatabaseUtils.getAllCart(CartDatabase.getInstance(getContext()),
//                this);
        compositeDisposable.add(cartDataSource.getAllItemFromCart(Common.currentUser.getPhoneNumber())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {

            //lấy hết item trong cart

            //Timestamp
            //Dung timestamp de loc tat ca cac booking voi ngay lon hon hom nay
            //chi hien thi cho booking trong cac ngay sau
            String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
            String[] convertTime = startTime.split("-"); //tach chuoi gio ra
            //Lay duoc gio dau vd 9:00
            String[] startTimeConvert = convertTime[0].split(":");
            int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // Lay so 9 thui
            int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // lay 00

            Calendar bookingDateWithourHouse = Calendar.getInstance();
            bookingDateWithourHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
            bookingDateWithourHouse.set(Calendar.HOUR_OF_DAY, startHourInt);
            bookingDateWithourHouse.set(Calendar.MINUTE, startMinInt);

            //Tao object timestamp va them vao BookingInfo
            Timestamp timestamp = new Timestamp(bookingDateWithourHouse.getTime());

            //Tao thong tin cho booking
            final BookingInformation bookingInformation = new BookingInformation();

            bookingInformation.setDistrictBook(Common.district);
            bookingInformation.setTimestamp(timestamp);
            bookingInformation.setDone(false); //luon de bien false, boi vi dung bien nay de loc cho cac user duoc hien thi
            bookingInformation.setBarberId(Common.currentBarber.getBarberId());
            bookingInformation.setBarberName(Common.currentBarber.getName());
            bookingInformation.setCustomerName(Common.currentUser.getName());
            bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
            bookingInformation.setSalonID(Common.currentSalon.getSalonID());
            bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
            bookingInformation.setSalonName(Common.currentSalon.getName());
            bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                    .append(" at ")
                    .append(simpleDateFormat.format(bookingDateWithourHouse.getTime())).toString());
            bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));
            bookingInformation.setCartItemList(cartItems); //Thêm các item trong giỏ hàng vào bookinginfo
            Log.d("DataInfo: ", "2");
            Log.d("Datacartsize", ""+bookingInformation.getCartItemList().size());

            //gui thong tin cho tho cat toc
            final DocumentReference bookingDate = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.district)
                    .collection("Branch")
                    .document(Common.currentSalon.getSalonID())
                    .collection("Barbers")
                    .document(Common.currentBarber.getBarberId())
                    .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                    .document(String.valueOf(Common.currentTimeSlot));

            //Ghi data
            bookingDate.set(bookingInformation)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Sau khi confirm booking, thì clear cart
                            //DatabaseUtils.clearCart(CartDatabase.getInstance(getContext()));
                            cartDataSource.clearCart(Common.currentUser.getPhoneNumber());
                            addToUserBooking(bookingInformation);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


        }, throwable -> Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    private void addToUserBooking(BookingInformation bookingInformation) {


        //tao moi collection
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        //Kiem tra neu document da ton tai trong collection
        //Lay ngay hien tai
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        String myTimeString = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertString = myTimeString.split("-");
        String startTime = convertString[0];

        userBooking
                .whereGreaterThanOrEqualTo("timestamp", toDayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1) //lay 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.getResult().isEmpty())
                    {
                        //Ghi data
                        userBooking.document()
                                .set(bookingInformation)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        //tao thong bao
                                        MyNotification myNotification = new MyNotification();
                                        myNotification.setUid(UUID.randomUUID().toString());
                                        myNotification.setTitle("New Booking");
                                        myNotification.setContent("New booking from user "
                                                + Common.currentUser.getName() + " at "
                                                + startTime);
                                        myNotification.setRead(false);//loc cac thong bao voi read la false trong app cho tho cat toc
                                        myNotification.setServerTimestamp(FieldValue.serverTimestamp());

                                        //Submit thong bao toi 'Notifications' collection cua Barbers
                                        FirebaseFirestore.getInstance()
                                                .collection("AllSalon")
                                                .document(Common.district)
                                                .collection("Branch")
                                                .document(Common.currentSalon.getSalonID())
                                                .collection("Barbers")
                                                .document(Common.currentBarber.getBarberId())
                                                .collection("Notifications") //Nếu ko tồn tại thì nó tự động tạo
                                                .document(myNotification.getUid()) //Tạo key
                                                .set(myNotification)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        //Lấy Token dựa trên id barber
                                                        FirebaseFirestore.getInstance()
                                                                .collection("Tokens")
                                                                .whereEqualTo("userPhone", Common.currentBarber.getUsername())
                                                                .limit(1)
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful() && task.getResult().size() > 0)
                                                                        {
                                                                            MyToken myToken = new MyToken();
                                                                            for (DocumentSnapshot tokenSnapShot : task.getResult())
                                                                            {
                                                                                myToken = tokenSnapShot.toObject(MyToken.class);
                                                                            }

                                                                            //Tạo data để gửi
                                                                            FCMSendData sendRequest = new FCMSendData();
                                                                            Map<String, String> dataSend = new HashMap<>();
                                                                            dataSend.put(Common.TITLE_KEY, "New Booking");
                                                                            dataSend.put(Common.CONTENT_KEY, "New booking from user "
                                                                                    + Common.currentUser.getName() + " at "
                                                                                    + startTime);


                                                                            sendRequest.setTo(myToken.getToken());
                                                                            sendRequest.setData(dataSend);

                                                                            compositeDisposable.add(ifcmApi.sendNotification(sendRequest)
                                                                                    .subscribeOn(Schedulers.io())
                                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                                    .subscribe(new Consumer<FCMResponse>() {
                                                                                        @Override
                                                                                        public void accept(FCMResponse fcmResponse) throws Exception {

                                                                                            dialog.dismiss();
//
                                                                                            addToCalendar(Common.bookingDate,
                                                                                                    Common.convertTimeSlotToString(Common.currentTimeSlot));
                                                                                            resetStaticData(); //reset state để ng dùng book lịch mới
                                                                                            getActivity().finish(); //Đóng activity lại
                                                                                            Toasty.success(getContext(), "Success!", Toast.LENGTH_SHORT, true).show();
                                                                                        }
                                                                                    }, new Consumer<Throwable>() {
                                                                                        @Override
                                                                                        public void accept(Throwable throwable) throws Exception {
                                                                                            Log.d("NOTIFICATION ERROR", throwable.getMessage());
                                                                                            dialog.dismiss();
//
                                                                                            addToCalendar(Common.bookingDate,
                                                                                                    Common.convertTimeSlotToString(Common.currentTimeSlot));
                                                                                            resetStaticData(); //reset state để ng dùng book lịch mới
                                                                                            getActivity().finish(); //Đóng activity lại
                                                                                            Toasty.success(getContext(), "Success!", Toast.LENGTH_SHORT, true).show();
                                                                                        }
                                                                                    }));



                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (dialog.isShowing())
                                            dialog.dismiss();

                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else
                    {
                        if (dialog.isShowing())
                            dialog.dismiss();

                        resetStaticData();
                        getActivity().finish(); // dong activity khi thuc hien xong
                        Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                    }
                }
        });

    }

    private void addToCalendar(Calendar bookingDate, String startDate) {
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); //tach chuoi gio ra 9:00 - 9:30
        //Lay duoc gio dau 9:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // Lay so 9 thui
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // lay 00

        //lay gio sau 9:30
        String[] endTimeConvert = convertTime[0].split(":");
        int endHourInt = Integer.parseInt(startTimeConvert[0].trim()); // Lay so 9
        int endMinInt = Integer.parseInt(startTimeConvert[1].trim()); // lay 30

        //lay event bat dau
        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt); // dat gio bat dau cua event
        startEvent.set(Calendar.MINUTE, startMinInt); // dat phut bat dau cua event

        //lay event ket huc
        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt); // dat gio bat dau cua event
        endEvent.set(Calendar.MINUTE, endMinInt); // dat phut bat dau cua event

        //sau khi da set startEvent va endEvent, chuyen no thanh dang chuoi
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calendarDateFormat.format(startEvent.getTime());
        String endEventTime = calendarDateFormat.format(endEvent.getTime());

        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking",
                new StringBuilder("Haircut from ")
        .append(startTime)
        .append(" with ")
        .append(Common.currentBarber.getName())
        .append(" at ")
        .append(Common.currentSalon.getName()).toString(),
                new StringBuilder("Address: ").append(Common.currentSalon.getAddress()).toString());
    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title, String desciption, String location) {
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {
            Date start = calendarDateFormat.parse(startEventTime);
            Date end = calendarDateFormat.parse(endEventTime);

            ContentValues event = new ContentValues();

            //Them vao su kien
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, desciption);
            event.put(CalendarContract.Events.EVENT_LOCATION, location);

            //Thoi gian
            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);


            Uri calendars;
                if (Build.VERSION.SDK_INT >= 8)
                    calendars = Uri.parse("content://com.android.calendar/events");
                else
                    calendars = Uri.parse("content://calendar/events");

            Uri uri_save = getActivity().getContentResolver().insert(calendars, event);
            //luu vao cache
            Paper.init(getActivity());
            Paper.book().write(Common.EVENT_URI_CACHE, uri_save.toString());


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {

        //lay calendar ID mac dinh cua gmail
        String gmailIdCalendar = "";
        String projection[] = {"_id", "calendar_displayName"};
        Uri calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = context.getContentResolver();
        //Chon tat ca calendar

        Cursor managedCursor = contentResolver.query(calendars,projection,null,null,null);
        if (managedCursor.moveToFirst())
        {
            String calName;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                if (calName.contains("@gmail.com"))
                {
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break; // ton tai khi co id
                }

            }while (managedCursor.moveToNext());
            managedCursor.close();
        }

        return gmailIdCalendar;
    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE, 0); //THEM NGAY HIEN Tai
    }


    //=============================================================================
    //EVENT BUS
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void setDataBooking(ConfirmBookingEvent event)
    {
        if (event.isConfirm())
        {
            setData();
        }
    }


    //=============================================================================


    private void setData() {
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        txt_booking_time_text.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
        .append(" at ")
        .append(simpleDateFormat.format(Common.bookingDate.getTime())));

        txt_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());
    }


    static BookingStep4Fragment instance;
    public static BookingStep4Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep4Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ifcmApi = RetrofitClient.getInstance().create(IFCMApi.class);
        //dinh dang ngay khi Confirm
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");


        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false)
                .build();
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_four,container,false);
        unbinder = ButterKnife.bind(this, itemView);

        //init cartDataSource, ko thì sẽ bị null
        //getContext() trả về null
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        return itemView;
    }

}
