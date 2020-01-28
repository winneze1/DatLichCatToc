package com.example.appcuathocattoc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.appcuathocattoc.Adapter.MyDistrictAdapter;
import com.example.appcuathocattoc.Common.Common;
import com.example.appcuathocattoc.Common.SpacesItemDecoration;
import com.example.appcuathocattoc.Interface.IOnAllDistrictLoadListener;
import com.example.appcuathocattoc.Model.Barber;
import com.example.appcuathocattoc.Model.District;
import com.example.appcuathocattoc.Model.Salon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements IOnAllDistrictLoadListener {

    @BindView(R.id.recycler_district)
    RecyclerView recycler_district;

    CollectionReference allSalonCollection;

    IOnAllDistrictLoadListener iOnAllDistrictLoadListener;
    MyDistrictAdapter adapter;
    AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Dexter.withActivity(this)
                .withPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                })
        .withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                FirebaseInstanceId.getInstance()
                        .getInstanceId()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful())
                        {
                            Common.updateToken(MainActivity.this, task.getResult().getToken());
                            Log.d("My Token", task.getResult().getToken());
                        }
                    }
                });

                Paper.init(MainActivity.this);
                String user = Paper.book().read(Common.LOGGED_KEY);
                if (TextUtils.isEmpty(user))// Nếu user chưa login trước đó
                {
                    setContentView(R.layout.activity_main);

                    ButterKnife.bind(MainActivity.this);

                    initView();

                    init();

                    loadAllDistrictFromFireStore();
                }
                else //Nếu user đã login
                {
                    //Auto login
                    Gson gson = new Gson();
                    Common.district_name = Paper.book().read(Common.DISTRICT_KEY);
                    Common.selectedSalon = gson.fromJson(Paper.book().read(Common.SALON_KEY,""),
                            new TypeToken<Salon>(){}.getType());
                    Common.currentBarber = gson.fromJson(Paper.book().read(Common.BARBER_KEY,""),
                            new TypeToken<Barber>(){}.getType());

                    Intent intent = new Intent(MainActivity.this, StaffHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }).check();

//        FirebaseInstanceId.getInstance()
//                .getInstanceId()
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//            @Override
//            public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                if (task.isSuccessful())
//                {
//                    Common.updateToken(MainActivity.this, task.getResult().getToken());
//                    Log.d("My Token", task.getResult().getToken());
//                }
//            }
//        });
//
//        Paper.init(this);
//        String user = Paper.book().read(Common.LOGGED_KEY);
//        if (TextUtils.isEmpty(user))// Nếu user chưa login trước đó
//        {
//            setContentView(R.layout.activity_main);
//
//            ButterKnife.bind(this);
//
//            initView();
//
//            init();
//
//            loadAllDistrictFromFireStore();
//        }
//        else //Nếu user đã login
//        {
//            //Auto login
//            Gson gson = new Gson();
//            Common.district_name = Paper.book().read(Common.DISTRICT_KEY);
//            Common.selectedSalon = gson.fromJson(Paper.book().read(Common.SALON_KEY,""),
//                    new TypeToken<Salon>(){}.getType());
//            Common.currentBarber = gson.fromJson(Paper.book().read(Common.BARBER_KEY,""),
//                    new TypeToken<Barber>(){}.getType());
//
//            Intent intent = new Intent(this, StaffHomeActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish();
//        }

    }

    private void loadAllDistrictFromFireStore() {
        dialog.show();

        allSalonCollection.get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iOnAllDistrictLoadListener.onAllDistrictLoadFailed(e.getMessage());
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    List<District> districts = new ArrayList<>();
                    for (DocumentSnapshot districtSnapShot:task.getResult())
                    {
                        District district = districtSnapShot.toObject(District.class);
                        districts.add(district);
                    }
                    iOnAllDistrictLoadListener.onAllDistrictLoadSuccess(districts);
                }
            }
        });
    }

    private void init() {
        allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");
        iOnAllDistrictLoadListener = this;
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

    }

    private void initView() {
        recycler_district.setHasFixedSize(true);
        recycler_district.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_district.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onAllDistrictLoadSuccess(List<District> districtList) {
        adapter = new MyDistrictAdapter(this,districtList);
        recycler_district.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onAllDistrictLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}
