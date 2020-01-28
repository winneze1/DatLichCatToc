package com.example.adminbarber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.adminbarber.Adapter.MyDistrictAdapter;
import com.example.adminbarber.Common.Common;
import com.example.adminbarber.Common.MySwipeHelper;
import com.example.adminbarber.Common.SpacesItemDecoration;
import com.example.adminbarber.Interface.IOnAllDistrictLoadListener;
import com.example.adminbarber.Model.District;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements IOnAllDistrictLoadListener {

    Context context;

    @BindView(R.id.recycler_district)
    RecyclerView recycler_district;


    CollectionReference allSalonCollection;

    IOnAllDistrictLoadListener iOnAllDistrictLoadListener;

    EventListener<QuerySnapshot> eventListener = null;

    List<District> dL;

    MyDistrictAdapter adapter;

    AlertDialog dialog;

    @BindView(R.id.btn_add)
    FloatingActionButton btn_add;

    @OnClick(R.id.btn_add)
    void addNewDistrict()
    {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Add new district");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_district, null);
        EditText edt_district_name = (EditText) itemView.findViewById(R.id.edt_district_name);

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> AddedData = new HashMap<>();
                AddedData.put("name",edt_district_name.getText().toString());

                allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");

                allSalonCollection
                        .document(edt_district_name.getText().toString())
                        .set(AddedData)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Success Add Data", Toast.LENGTH_SHORT).show();
                                loadAllDistrictFromFireStore();
                            }
                        });

            }
        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        ButterKnife.bind(this);

        initView();

        init();

        loadAllDistrictFromFireStore();


    }



    private void loadAllDistrictFromFireStore() {
        dialog.show();

        allSalonCollection.get()
                .addOnFailureListener(e -> {
                    iOnAllDistrictLoadListener.onAllDistrictLoadFailed(e.getMessage());
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        List<District> districts = new ArrayList<>();
                        for (DocumentSnapshot districtSnapShot:task.getResult())
                        {
                            District district = districtSnapShot.toObject(District.class);
                            district.setName(districtSnapShot.getId());
                            districts.add(district);
                        }
                        iOnAllDistrictLoadListener.onAllDistrictLoadSuccess(districts);
                        dL = districts;
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
        recycler_district.setLayoutManager(new GridLayoutManager(this, 1));
        recycler_district.addItemDecoration(new SpacesItemDecoration(4));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(this, recycler_district, 200) {
            @Override
            public void instantiateMybutton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(context, "Update", 28, 0, Color.parseColor("#560027"),
                        pos -> {
                            if (dL != null){
                                Common.district_name = dL.get(pos).getName();
                                showUpdateDialog();
                            }
                        }));

                buf.add(new MyButton(context, "Delete", 28, 0, Color.parseColor("#9b0000"),
                        pos -> {
                            if (dL != null){
                                Common.district_name = dL.get(pos).getName();
                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                                builder.setTitle("Delete")
                                        .setMessage("Do you wanna delete this?")
                                        .setNegativeButton("Cancel",((dialogInterface, i) -> dialogInterface.dismiss()))
                                        .setPositiveButton("Delete",((dialogInterface, i) -> {
                                            Common.district_name = dL.remove(pos).getName();
                                            deleteDistrict();
                                        }));
                                androidx.appcompat.app.AlertDialog deleteDialog = builder.create();
                                deleteDialog.show();
                            }
                        }));
            }
        };
    }

    private void deleteDistrict() {
        allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");

        allSalonCollection
                .document(Common.district_name)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            loadAllDistrictFromFireStore();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_district, null);
        EditText edt_district_name = (EditText) itemView.findViewById(R.id.edt_district_name);
        edt_district_name.setText(new StringBuilder().append(Common.district_name));

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("name",edt_district_name.getText().toString());
                updateDisrict(updateData);
            }
        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateDisrict(Map<String, Object> updateData) {
        allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");

        allSalonCollection
                .document(Common.district_name)
                .update(updateData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            loadAllDistrictFromFireStore();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onAllDistrictLoadSuccess(List<District> districtList) {
        if (districtList != null)
        {
            adapter = new MyDistrictAdapter(this,districtList);
            recycler_district.setAdapter(adapter);
        }
        dialog.dismiss();
    }

    @Override
    public void onAllDistrictLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}
