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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adminbarber.Adapter.MySalonAdapter;
import com.example.adminbarber.Common.Common;
import com.example.adminbarber.Common.MySwipeHelper;
import com.example.adminbarber.Common.SpacesItemDecoration;
import com.example.adminbarber.Interface.IBranchLoadListener;
import com.example.adminbarber.Interface.IOnLoadCountSalon;
import com.example.adminbarber.Model.Salon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class SalonListActivity extends AppCompatActivity implements IOnLoadCountSalon, IBranchLoadListener {

    Context context;

    @BindView(R.id.txt_salon_count)
    TextView txt_salon_count;

    @BindView(R.id.recycler_salon)
    RecyclerView recycler_salon;


    IOnLoadCountSalon iOnLoadCountSalon;
    IBranchLoadListener iBranchLoadListener;

    CollectionReference branchRef;

    List<Salon> SL;

    AlertDialog dialog;

    @BindView(R.id.btn_add_salon)
    FloatingActionButton btn_add_salon;

    @OnClick(R.id.btn_add_salon)
    void addNewSalon(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Add new salon");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_salon, null);
        EditText edt_salon_address = (EditText) itemView.findViewById(R.id.edt_salon_address);
        EditText edt_salon_name = (EditText) itemView.findViewById(R.id.edt_salon_name);
        EditText edt_salon_open_hours = (EditText) itemView.findViewById(R.id.edt_open_hours);
        EditText edt_salon_phone = (EditText) itemView.findViewById(R.id.edt_salon_phone);
        EditText edt_salon_website = (EditText) itemView.findViewById(R.id.edt_salon_website);

        //data

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> AddData = new HashMap<>();
                AddData.put("address",edt_salon_address.getText().toString());
                AddData.put("name",edt_salon_name.getText().toString());
                AddData.put("openHours",edt_salon_open_hours.getText().toString());
                AddData.put("phone",edt_salon_phone.getText().toString());
                AddData.put("website",edt_salon_website.getText().toString());

                branchRef = FirebaseFirestore.getInstance()
                        .collection("AllSalon")
                        .document(Common.district_name)
                        .collection("Branch");

                branchRef
                        .add(AddData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(context, "Added new salon", Toast.LENGTH_SHORT).show();
                                    loadSalonBaseOnDistrict(Common.district_name);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_salon_list);
        context = this;
        ButterKnife.bind(this);

        initView();

        init();

        loadSalonBaseOnDistrict(Common.district_name);
    }

    private void loadSalonBaseOnDistrict(String name) {
        dialog.show();

        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(name)
                .collection("Branch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            List<Salon> salons = new ArrayList<>();
                            iOnLoadCountSalon.onLoadCountSalonSuccess(task.getResult().size());
                            for (DocumentSnapshot salonSnapShot:task.getResult())
                            {
                                Salon salon = salonSnapShot.toObject(Salon.class);
                                salon.setSalonID(salonSnapShot.getId());
                                salons.add(salon);
                            }
                            iBranchLoadListener.onBranchLoadSuccess(salons);
                            SL = salons;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBranchLoadListener.onBranchLoadFailed(e.getMessage());
            }
        });
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iOnLoadCountSalon = this;
        iBranchLoadListener = this;
    }

    private void initView() {
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_salon.addItemDecoration(new SpacesItemDecoration(8));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(this, recycler_salon, 50) {
            @Override
            public void instantiateMybutton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(context, "Update", 28, 0, Color.parseColor("#560027"),
                        pos -> {
                            if (SL != null){
                                Common.selectedSalon = SL.get(pos);
                                showUpdateDialog();
                            }
                        }));

                buf.add(new MyButton(context, "Delete", 28, 0, Color.parseColor("#9b0000"),
                        pos -> {
                            if (SL != null){
                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                                builder.setTitle("Delete")
                                        .setMessage("Do you wanna delete this?")
                                        .setNegativeButton("Cancel",((dialogInterface, i) -> dialogInterface.dismiss()))
                                        .setPositiveButton("Delete",((dialogInterface, i) -> {
                                            Common.selectedSalon = SL.remove(pos);
                                            deleteSalon();
                                        }));
                                androidx.appcompat.app.AlertDialog deleteDialog = builder.create();
                                deleteDialog.show();
                            }
                        }));
            }
        };
    }

    private void deleteSalon() {
        branchRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.district_name)
                .collection("Branch");

        branchRef
                .document(Common.selectedSalon.getSalonID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadSalonBaseOnDistrict(Common.district_name);
                        Toast.makeText(context, "Delete success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_salon, null);
        EditText edt_salon_address = (EditText) itemView.findViewById(R.id.edt_salon_address);
        EditText edt_salon_name = (EditText) itemView.findViewById(R.id.edt_salon_name);
        EditText edt_salon_open_hours = (EditText) itemView.findViewById(R.id.edt_open_hours);
        EditText edt_salon_phone = (EditText) itemView.findViewById(R.id.edt_salon_phone);
        EditText edt_salon_website = (EditText) itemView.findViewById(R.id.edt_salon_website);

        //data
        edt_salon_address.setText(new StringBuilder()
                .append(Common.selectedSalon.getAddress()));
        edt_salon_name.setText(new StringBuilder()
                .append(Common.selectedSalon.getName()));
        edt_salon_open_hours.setText(new StringBuilder()
                .append(Common.selectedSalon.getOpenHours()));
        edt_salon_phone.setText(new StringBuilder()
                .append(Common.selectedSalon.getPhone()));
        edt_salon_website.setText(new StringBuilder()
                .append(Common.selectedSalon.getWebsite()));


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
                updateData.put("address",edt_salon_address.getText().toString());
                updateData.put("name",edt_salon_name.getText().toString());
                updateData.put("openHours",edt_salon_open_hours.getText().toString());
                updateData.put("phone",edt_salon_phone.getText().toString());
                updateData.put("website",edt_salon_website.getText().toString());
                updateSalon(updateData);
            }
        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateSalon(Map<String, Object> updateData) {


        branchRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.district_name)
                .collection("Branch");

        branchRef
                .document(Common.selectedSalon.getSalonID())
                .update(updateData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(context, "Update successful", Toast.LENGTH_SHORT).show();
                            loadSalonBaseOnDistrict(Common.district_name);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(SalonListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    @Override
    public void onLoadCountSalonSuccess(int count) {
        txt_salon_count.setText(new StringBuilder("All Salon(")
                .append(count)
                .append(")"));
    }

    @Override
    public void onBranchLoadSuccess(List<Salon> branchList) {
        if (branchList != null)
        {
            MySalonAdapter salonAdapter = new MySalonAdapter(this, branchList);
            recycler_salon.setAdapter(salonAdapter);

            dialog.dismiss();
        }
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}
