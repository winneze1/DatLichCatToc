package com.example.datlichcattoc.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datlichcattoc.Adapter.HomeSliderAdapter;
import com.example.datlichcattoc.Adapter.LookbookAdapter;
import com.example.datlichcattoc.BookingActivity;
import com.example.datlichcattoc.CartActivity;
import com.example.datlichcattoc.Database.CartDataSource;
import com.example.datlichcattoc.Database.CartDatabase;
import com.example.datlichcattoc.Database.LocalCartDataSource;
import com.example.datlichcattoc.Helper.LocaleHelper;
import com.example.datlichcattoc.HistoryActivity;
import com.example.datlichcattoc.HomeActivity;
import com.example.datlichcattoc.Interface.IBookingInfoLoadListener;
import com.example.datlichcattoc.Interface.IBookingInformationChangeListener;
import com.example.datlichcattoc.Interface.ICountItemInCartListener;
import com.example.datlichcattoc.Model.BookingInformation;
import com.example.datlichcattoc.Common.Common;
import com.example.datlichcattoc.Interface.IBannerLoadListener;
import com.example.datlichcattoc.Interface.ILookbookLoadListener;
import com.example.datlichcattoc.MainActivity;
import com.example.datlichcattoc.Model.Banner;
import com.example.datlichcattoc.Model.User;
import com.example.datlichcattoc.R;
import com.example.datlichcattoc.Service.PicassoImageLoadingService;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ss.com.bannerslider.Slider;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements
        IBannerLoadListener,
        ILookbookLoadListener,
        IBookingInfoLoadListener,
        IBookingInformationChangeListener {

    private Unbinder unbinder;


    AlertDialog dialog;

    CartDataSource cartDataSource;



    @BindView(R.id.notification_badge)
    NotificationBadge notificationBadge;

    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;

    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;


    @BindView(R.id.card_booking_info)
    CardView card_booking_info;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_barber)
    TextView txt_salon_barber;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_time_remain)
    TextView txt_time_remain;


    @BindView(R.id.txt_booking)
    TextView txt_booking;
    @BindView(R.id.txt_cart)
    TextView txt_cart;
    @BindView(R.id.txt_history)
    TextView txt_history;
    @BindView(R.id.txt_notification)
    TextView txt_notification;
    @BindView(R.id.btn_change_language)
    ImageView btn_change_language;
    @BindView(R.id.txt_member_type)
    TextView txt_member_type;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(LocaleHelper.onAttach(context,"en"));
    }

    @OnClick(R.id.btn_change_language)
    void showLanguage(){
        onShowLanguage();
    }

    private void onShowLanguage() {
        btn_change_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), btn_change_language);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.language_en)
                        {
                            Paper.book().write("language","en");
                            updateView((String)Paper.book().read("language"));
                        }
                        else if (menuItem.getItemId() == R.id.language_vi)
                        {
                            Paper.book().write("language","vi");
                            updateView((String)Paper.book().read("language"));
                        }
                        else if (menuItem.getItemId() == R.id.language_ja)
                        {
                            Paper.book().write("language","ja");
                            updateView((String)Paper.book().read("language"));
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void updateView(String language) {
            Context context = LocaleHelper.setLocale(getActivity(),language);
            Resources resources = context.getResources();

            txt_booking.setText(resources.getString(R.string.booking));
            txt_cart.setText(resources.getString(R.string.cart));
            txt_history.setText(resources.getString(R.string.history));
            txt_notification.setText(resources.getString(R.string.notification));
            txt_member_type.setText(resources.getString(R.string.memberType));

            Common.resouceLang = resources;

    }


    @OnClick(R.id.card_view_cart)
    void openCartActivity(){
        startActivity(new Intent(getActivity(), CartActivity.class));
    }

    @OnClick(R.id.card_view_history)
    void openHistoryActivity(){
        startActivity(new Intent(getActivity(), HistoryActivity.class));
    }

    @OnClick(R.id.btn_delete_booking)
    void deleteBooking()
    {
        deleteBookingFromBarber(false);
    }

    @OnClick(R.id.btn_change_booking)
    void changeBooking()
    {
        changeBookingFromUser();
    }

    private void changeBookingFromUser() {
        //hien dialog de cofirm la muon thay doi
        androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Yo!")
                .setMessage("Do you really want to change the booking information?\nBecause we will delete your old booking information\n" +
                        "If your please to do so, just press OK")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBookingFromBarber(true);
                        //Khi set true thi no se thuc hien xoa booking va goi lai activity booking
                    }
                });

        confirmDialog.show();
    }

    private void deleteBookingFromBarber(boolean isChange) {
        /*de xoa booking user thi can xoa booking cua tho cat toc
          Sau do moi xoa booking cua user duoc
          rui xoa event trong lich
        * */

        //Load Common.currentBooking de lay du lieu tu BookingInfomation
        if(Common.currentBooking != null)
        {

            dialog.show();

            //Lay gui lieu booking tu collection Barbers
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.currentBooking.getDistrictBook())
                    .collection("Branch")
                    .document(Common.currentBooking.getSalonID())
                    .collection("Barbers")
                    .document(Common.currentBooking.getBarberId())
                    .collection(Common.convertTimeStampToStringKey(Common.currentBooking.getTimestamp()))
                    .document(Common.currentBooking.getSlot().toString());
            //Khi da co document, xoa document do
            barberBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Sau khi xoa booking cua Barber
                    //Xoa tiep cua user
                    deleteBookingFromUser(isChange);
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Current booking must not be null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBookingFromUser(boolean isChange) {
        //lay du lieu tu user
        if (!TextUtils.isEmpty(Common.currentBookingId))
        {
            DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking")
                    .document(Common.currentBookingId);

            //Xoa
            userBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Sau khi da xoa booking tu "User", delete tiep cua Lich
                    //Can luu Uri cua event ma add vao truoc do
                    Paper.init(getActivity());

                    if (Paper.book().read(Common.EVENT_URI_CACHE) != null)
                    {
                        String eventString = Paper.book().read(Common.EVENT_URI_CACHE).toString();
                        Uri eventUri = null;
                        if (eventString != null && !TextUtils.isEmpty(eventString)){
                            eventUri = Uri.parse(eventString);
                        }
                        if (eventUri != null){
                            getActivity().getContentResolver().delete(eventUri, null, null);
                        }
                    }
                    Toast.makeText(getActivity(), "Success delete booking!", Toast.LENGTH_SHORT).show();


                    //Refresh
                    loadUserBooking();

                    //Kiem tra isChange -> goi tu nut change, ban toi thang interface
                    //Yeu cua no khoi dong lai thang Booking
                    if (isChange)
                        iBookingInformationChangeListener.onBookingInformationChange();

                    dialog.dismiss();
                }
            });
        }
        else
        {
            dialog.dismiss();
            Toast.makeText(getContext(), "Booking information ID must not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @BindView(R.id.card_view_booking)
    CardView card_view_booking;
    @OnClick(R.id.card_view_booking)
    void booking()
    {
        if (card_view_booking.isEnabled()){
            startActivity(new Intent(getActivity(), BookingActivity.class));
        }
    }

    @OnClick(R.id.exit_app)
    void onLogOutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(Common.resouceLang.getString(R.string.title))
                .setMessage(Common.resouceLang.getString(R.string.message))
                .setNegativeButton(Common.resouceLang.getString(R.string.cancelBtn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(Common.resouceLang.getString(R.string.acceptBtn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Common.currentUser = null;
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @OnClick(R.id.layout_user_information)
    void onUpdateUser(){
        showUpdateDialog(Common.currentUser.getPhoneNumber());
    }

    BottomSheetDialog bottomSheetDialog;
    CollectionReference userRef = FirebaseFirestore.getInstance()
            .collection("User");
    private void showUpdateDialog(final String phoneNumber)
    {

        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setTitle("Update");
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information, null);

        Button btn_update = (Button)sheetView.findViewById(R.id.btn_update);
        final TextInputEditText edt_name = (TextInputEditText)sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edt_address = (TextInputEditText)sheetView.findViewById(R.id.edt_address);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edt_name.getText().length() > 0 && edt_address.getText().length() > 0
                && !edt_name.getText().toString().trim().equals("") && !edt_address.getText().toString().trim().equals(""))
                {
                    btn_update.setEnabled(true);
                }
                else if (edt_address.getText().toString().trim().equals(""))
                {
                    btn_update.setEnabled(false);
                }
                else if (edt_name.getText().toString().trim().equals(""))
                {
                    btn_update.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        edt_name.addTextChangedListener(textWatcher);
        edt_address.addTextChangedListener(textWatcher);

        edt_name.setText(new StringBuilder()
                .append(Common.currentUser.getName()));
        edt_address.setText(new StringBuilder()
                .append(Common.currentUser.getAddress()));

        btn_update.setOnClickListener(v -> {
            Map<String, Object> docUser = new HashMap<>();
            docUser.put("name",edt_name.getText().toString());
            docUser.put("address", edt_address.getText().toString());
            userRef.document(phoneNumber)
                    .update(docUser)
                    .addOnSuccessListener(aVoid -> {
                        txt_user_name.setText(edt_name.getText().toString());
                        bottomSheetDialog.dismiss();
                    }).addOnFailureListener(e -> {
                        bottomSheetDialog.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    //FireStore
    CollectionReference bannerRef, lookbookRef;

    //Interface
    IBannerLoadListener iBannerLoadListener;
    ILookbookLoadListener iLookbookLoadListener;
    IBookingInfoLoadListener iBookingInfoLoadListener;
    IBookingInformationChangeListener iBookingInformationChangeListener;

    ListenerRegistration userBookingListener = null;
    com.google.firebase.firestore.EventListener<QuerySnapshot> userBookingEvent = null;

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");


    }


    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
        countCartItem();
    }

    private void loadUserBooking() {
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        //Lay ngay hien tai
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        //chon thong tin booking tu firebase voi bien done=false va thoi gian = hom nay
        userBooking
                .whereGreaterThanOrEqualTo("timestamp", toDayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1) //lay 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            if (!task.getResult().isEmpty())
                            {
                                for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult())
                                {
                                    BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                                    iBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation, queryDocumentSnapshot.getId());
                                    card_view_booking.setEnabled(false);
                                    break; //thoat khi load xong
                                }
                            }
                            else
                            {
                                iBookingInfoLoadListener.onBookingInfoLoadEmpty();
                                card_view_booking.setEnabled(true);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage());
            }
        });

        //Sau khi userBooking đã được assign data (collections)
        //Tạo 1 realtime listener
        if (userBookingEvent != null) // nếu userBookingEvent đã được init
        {
           if (userBookingListener == null) //Chỉ thêm vào nếu userBookingListener = null
           {
               //Thêm vào 1 lần
               userBookingListener = userBooking
                       .addSnapshotListener(userBookingEvent);
           }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        //Init
        Slider.init(new PicassoImageLoadingService());
        iBannerLoadListener = this;
        iLookbookLoadListener = this;
        iBookingInfoLoadListener = this;
        iBookingInformationChangeListener = this;

        //Kiem tra xem user da dang nhap chua
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            //Log.d("test",Common.currentUser.getName());
            setUserInformation();
            loadBanner();
            loadLookbook();
            initRealTimeUserBooking(); // Phải Khai báo trên thằng loadUserBooking();
            loadUserBooking();
            countCartItem();
            loadLanguage();
        }else
        {
            loadBanner();
            loadLookbook();
        }

        return view;
    }

    private void loadLanguage() {
        Paper.init(getActivity());
        String language = Paper.book().read("language");
        if (language == null)
            Paper.book().write("language","en");
        else
            updateView((String)Paper.book().read("language"));
    }

    private void initRealTimeUserBooking() {
        //Bug loop zzzzzzzzzzzzzzzzzzz
        if (userBookingEvent == null) //Chỉ cần init event nếu event là null
        {
            userBookingEvent = new EventListener<QuerySnapshot>(){

                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    //Trong event, khi bắn thì sẽ gọi loadUserBooking lần nữa
                    //Để load lại hết booking information
                    loadUserBooking();
                }
            };
        }
    }

    private void countCartItem() {
        //DatabaseUtils.countItemInCart(cartDatabase, this);
        cartDataSource.countItemInCart(Common.currentUser.getPhoneNumber())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        notificationBadge.setText(String.valueOf(integer));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadLookbook() {
        lookbookRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> lookbooks = new ArrayList<>();
                        if (task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot bannerSnapShot:task.getResult())
                            {
                                Banner banner = bannerSnapShot.toObject(Banner.class);
                                lookbooks.add(banner);
                            }
                            iLookbookLoadListener.onLookbookLoadSuccess(lookbooks);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iLookbookLoadListener.onLookbookLoadFail(e.getMessage());
            }
        });
    }

    private void loadBanner() {
        bannerRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> banners = new ArrayList<>();
                        if (task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot bannerSnapShot:task.getResult())
                            {
                                Banner banner = bannerSnapShot.toObject(Banner.class);
                                banners.add(banner);
                            }
                            iBannerLoadListener.onBannerLoadSuccess(banners);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBannerLoadListener.onBannerLoadFail(e.getMessage());
            }
        });
    }

    private void setUserInformation() {
        layout_user_information.setVisibility(View.VISIBLE);
        Log.d("Hello","1");
        txt_user_name.setText(Common.currentUser.getName());
    }

    @Override
    public void onLookbookLoadSuccess(List<Banner> banners) {
        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookbookAdapter(getActivity(),banners));
    }

    @Override
    public void onLookbookLoadFail(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerLoadFail(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));
    }


    @Override
    public void onBookingInfoLoadEmpty() {
        card_booking_info.setVisibility(View.GONE);
    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String bookingId) {

        Common.currentBooking = bookingInformation;
        Common.currentBookingId = bookingId;

        txt_salon_address.setText(bookingInformation.getSalonAddress());
        txt_salon_barber.setText(bookingInformation.getBarberName());
        txt_time.setText(bookingInformation.getTime());
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(), 0).toString();

        txt_time_remain.setText(dateRemain);

        card_booking_info.setVisibility(View.VISIBLE); //hien lich ra

        dialog.dismiss();
    }

    @Override
    public void onBookingInfoLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInformationChange() {
        //khoi dong lai thang activity Booking
        startActivity(new Intent(getActivity(),BookingActivity.class));
    }


    @Override
    public void onDestroy() {
        if (userBookingListener != null)
            userBookingListener.remove();
        super.onDestroy();
    }
}
