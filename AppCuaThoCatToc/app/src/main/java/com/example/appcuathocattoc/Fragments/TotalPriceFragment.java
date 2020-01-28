package com.example.appcuathocattoc.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcuathocattoc.Adapter.MyConfirmShoppingItemAdapter;
import com.example.appcuathocattoc.Common.Common;
import com.example.appcuathocattoc.Model.BarberServices;
import com.example.appcuathocattoc.Model.CartItem;
import com.example.appcuathocattoc.Model.EventBus.DismissFromBottomSheetEvent;
import com.example.appcuathocattoc.Model.FCMResponse;
import com.example.appcuathocattoc.Model.FCMSendData;
import com.example.appcuathocattoc.Model.Invoice;
import com.example.appcuathocattoc.Model.MyToken;
import com.example.appcuathocattoc.R;
import com.example.appcuathocattoc.Retrofit.IFCMService;
import com.example.appcuathocattoc.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TotalPriceFragment extends BottomSheetDialogFragment {

    Unbinder unbinder;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;
    @BindView(R.id.recycler_view_shopping)
    RecyclerView recycler_view_shopping;

    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_barber_name)
    TextView txt_barber_name;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;
    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    HashSet<BarberServices> servicesAdded;
    //List<ShoppingItem> shoppingItemList;

    IFCMService ifcmService;

    AlertDialog dialog;

    String image_url;

    private static TotalPriceFragment instance;


    public static TotalPriceFragment getInstance() {
        return instance == null ? new TotalPriceFragment():instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_total_price, container, false);

        unbinder = ButterKnife.bind(this, itemView);

        init();

        initView();

        getBundle(getArguments());

        setInformation();

        return itemView;

    }

    private void setInformation() {
        txt_salon_name.setText(Common.selectedSalon.getName());
        txt_barber_name.setText(Common.currentBarber.getName());
        txt_time.setText(Common.convertTimeSlotToString(Common.currentBookingInformation.getSlot().intValue()));
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

        if (servicesAdded.size() > 0)
        {
            //Thêm vào trong chip group
            int i = 0;
            for (BarberServices services:servicesAdded)
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.chip_item, null);
                chip.setText(services.getName());
                chip.setTag(i);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        servicesAdded.remove((int)v.getTag());
                        chip_group_services.removeView(v);

                        calculatePrice();
                    }
                });
                chip_group_services.addView(chip);
                i++;
            }
        }

        if (Common.currentBookingInformation.getCartItemList() != null)
        {
            if (Common.currentBookingInformation.getCartItemList().size() > 0)
            {
                MyConfirmShoppingItemAdapter adapter = new MyConfirmShoppingItemAdapter(getContext(),
                        Common.currentBookingInformation.getCartItemList());
                recycler_view_shopping.setAdapter(adapter);
            }

            calculatePrice();
        }

    }

    private double calculatePrice() {
        double price = Common.DEFAULT_PRICE;
        for (BarberServices services:servicesAdded)
            price+=services.getPrice();
        if (Common.currentBookingInformation.getCartItemList() != null)
        {
            for (CartItem cartItem: Common.currentBookingInformation.getCartItemList())
                price+=(cartItem.getProductPrice()*cartItem.getProductQuantity());

        }


        txt_total_price.setText(new StringBuilder(Common.MONEY_SIGN)
        .append(price));

        return price;
    }

    private void getBundle(Bundle arguments) {
        this.servicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>(){}.getType());

//        this.shoppingItemList = new Gson()
//                .fromJson(arguments.getString(Common.SHOPPING_LIST),
//                        new TypeToken<List<ShoppingItem>>(){}.getType());

        image_url = arguments.getString(Common.IMAGE_DOWNLOADABLE_URL);
    }

    private void initView() {

        recycler_view_shopping.setHasFixedSize(true);
        recycler_view_shopping.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                //Update bookingInformation set done=true
                DocumentReference bookingSet = FirebaseFirestore.getInstance()
                        .collection("AllSalon")
                        .document(Common.district_name)
                        .collection("Branch")
                        .document(Common.selectedSalon.getSalonID())
                        .collection("Barbers")
                        .document(Common.currentBarber.getBarberId())
                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                        .document(Common.currentBookingInformation.getBookingId());

                bookingSet
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    if (task.getResult().exists())
                                    {
                                        //Update
                                        Map<String, Object> dataUpdate = new HashMap<>();
                                        dataUpdate.put("done", true);
                                        bookingSet.update(dataUpdate)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    //Nếu udpate xong thì tạo hóa đơn
                                                    createInvoice();
                                                }
                                            }
                                        });

                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void createInvoice() {
        //Tạo hóa đơn
        CollectionReference invoiceRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.district_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonID())
                .collection("Invoices");

        Invoice invoice = new Invoice();

        invoice.setBarberId(Common.currentBarber.getBarberId());
        invoice.setBarberName(Common.currentBarber.getName());

        invoice.setSalonId(Common.selectedSalon.getSalonID());
        invoice.setSalonName(Common.selectedSalon.getName());
        invoice.setSalonAddress(Common.selectedSalon.getAddress());

        invoice.setCustomerName(Common.currentBookingInformation.getCustomerName());
        invoice.setCutomerPhone(Common.currentBookingInformation.getCustomerPhone());

        invoice.setImageUrl(image_url);

        invoice.setBarberServices(new ArrayList<BarberServices>(servicesAdded));
        invoice.setShoppingItemList(Common.currentBookingInformation.getCartItemList());
        invoice.setFinalPrice(calculatePrice());

        invoiceRef.document()
                .set(invoice)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            sendNotificationUpdateToUser(Common.currentBookingInformation.getCustomerPhone());
                            Toasty.success(getContext(), "Success!").show();
                        }
                    }
                });

    }

    private void sendNotificationUpdateToUser(String customerPhone) {
        //Lấy token của user
        FirebaseFirestore.getInstance()
                .collection("Tokens")
                .whereEqualTo("userPhone",customerPhone)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0)
                        {
                            MyToken myToken = new MyToken();
                            for (DocumentSnapshot tokenSnapShot:task.getResult())
                                myToken = tokenSnapShot.toObject(MyToken.class);

                            //Tạo notification để gửi
                            FCMSendData fcmSendData = new FCMSendData();
                            Map<String,String> dataSend = new HashMap<>();
                            dataSend.put("update_done", "true");
                            //Thông tin cho Rating
                            dataSend.put(Common.RATING_DISTRICT_KEY, Common.district_name);
                            dataSend.put(Common.RATING_SALON_ID, Common.selectedSalon.getSalonID());
                            dataSend.put(Common.RATING_SALON_NAME, Common.selectedSalon.getName());
                            dataSend.put(Common.RATING_BARBER_ID, Common.currentBookingInformation.getBarberId());


                            fcmSendData.setTo(myToken.getToken());
                            fcmSendData.setData(dataSend);

                            ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.newThread())
                                    .subscribe(new Consumer<FCMResponse>() {
                                        @Override
                                        public void accept(FCMResponse fcmResponse) throws Exception {
                                            dialog.dismiss();
                                            dismiss();
                                            //Event Bus
                                            EventBus.getDefault()
                                                    .postSticky(new DismissFromBottomSheetEvent(true));

                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    }
                });
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(getContext())
                .setCancelable(false).build();
        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);
    }
}
