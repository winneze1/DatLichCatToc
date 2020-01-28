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

import com.example.adminbarber.Adapter.MyBarberAdapter;
import com.example.adminbarber.Common.Common;
import com.example.adminbarber.Common.MySwipeHelper;
import com.example.adminbarber.Common.SpacesItemDecoration;
import com.example.adminbarber.Interface.IBarberLoadListener;
import com.example.adminbarber.Interface.IOnLoadCountBarber;
import com.example.adminbarber.Model.Barber;
import com.example.adminbarber.Model.District;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class BarberListActivity extends AppCompatActivity implements IBarberLoadListener, IOnLoadCountBarber {

    @BindView(R.id.txt_barber_count)
    TextView txt_barber_count;

    @BindView(R.id.recycler_barber)
    RecyclerView recycler_barber;

    IOnLoadCountBarber iOnLoadCountBarber;
    IBarberLoadListener iBarberLoadListener;

    List<Barber> BL;

    AlertDialog dialog;

    Context context;

    CollectionReference barberRef;

    @BindView(R.id.btn_add_barber)
    FloatingActionButton btn_add_barber;

    @OnClick(R.id.btn_add_barber)
    void addNewBaber(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Add new salon");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_add_barber, null);
        EditText edt_barber_name = (EditText) itemView.findViewById(R.id.edt_barber_name);
        EditText edt_barber_pass = (EditText) itemView.findViewById(R.id.edt_barber_pass);
        EditText edt_barber_username = (EditText) itemView.findViewById(R.id.edt_barber_username);

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
                AddData.put("name",edt_barber_name.getText().toString());
                AddData.put("password",edt_barber_pass.getText().toString());
                AddData.put("rating",0);
                AddData.put("ratingTimes", 0);
                AddData.put("username",edt_barber_username.getText().toString());

                barberRef = FirebaseFirestore.getInstance()
                        .collection("AllSalon")
                        .document(Common.district_name)
                        .collection("Branch")
                        .document(Common.selectedSalon.getSalonID())
                        .collection("Barbers");

                barberRef
                        .add(AddData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(context, "Added new barber", Toast.LENGTH_SHORT).show();
                                loadBarberBaseOnSalon(Common.selectedSalon.getSalonID());
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
        setContentView(R.layout.activity_barber_list);

        context = this;

        ButterKnife.bind(this);

        initView();

        init();

        loadBarberBaseOnSalon(Common.selectedSalon.getSalonID());
    }

    private void loadBarberBaseOnSalon(String salonID) {
        dialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.district_name)
                .collection("Branch")
                .document(salonID)
                .collection("Barbers")
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iBarberLoadListener.onBarberLoadFailed(e.getMessage());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            List<Barber> salons = new ArrayList<>();
                            iOnLoadCountBarber.onLoadCountBarberSuccess(task.getResult().size());
                            for (DocumentSnapshot salonSnapShot:task.getResult())
                            {
                                Barber barber = salonSnapShot.toObject(Barber.class);
                                barber.setBarberId(salonSnapShot.getId());
                                salons.add(barber);
                            }
                            iBarberLoadListener.onBarberLoadSuccess(salons);
                            BL = salons;
                        }
                    }
                });

    }

    private void initView() {
        recycler_barber.setHasFixedSize(true);
        recycler_barber.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_barber.addItemDecoration(new SpacesItemDecoration(4));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(this, recycler_barber, 50) {
            @Override
            public void instantiateMybutton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(context, "Update", 28, 0, Color.parseColor("#560027"),
                        pos -> {
                            if (BL != null){
                                Common.currentBarber = BL.get(pos);
                                showUpdateDialog();
                            }
                        }));

                buf.add(new MyButton(context, "Delete", 28, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            if (BL != null){
                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                                builder.setTitle("Delete")
                                        .setMessage("Do you wanna delete this?")
                                        .setNegativeButton("Cancel",((dialogInterface, i) -> dialogInterface.dismiss()))
                                        .setPositiveButton("Delete",((dialogInterface, i) -> {
                                            Common.currentBarber = BL.remove(pos);
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
        barberRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.district_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonID())
                .collection("Barbers");

        barberRef
                .document(Common.currentBarber.getBarberId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadBarberBaseOnSalon(Common.selectedSalon.getSalonID());
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

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_barber, null);
        EditText edt_barber_name = (EditText) itemView.findViewById(R.id.edt_barber_name);
        EditText edt_barber_pass = (EditText) itemView.findViewById(R.id.edt_barber_pass);
        EditText edt_barber_rating = (EditText) itemView.findViewById(R.id.edt_rating);
        EditText edt_barber_rating_times = (EditText) itemView.findViewById(R.id.edt_rating_times);
        EditText edt_barber_username = (EditText) itemView.findViewById(R.id.edt_barber_username);

        //data
        edt_barber_name.setText(new StringBuilder()
                .append(Common.currentBarber.getName()));
        edt_barber_pass.setText(new StringBuilder()
                .append(Common.currentBarber.getPassword()));
        edt_barber_rating.setText(new StringBuilder()
                .append(Common.currentBarber.getRating()));
        edt_barber_rating_times.setText(new StringBuilder()
                .append(Common.currentBarber.getRatingTimes()));
        edt_barber_username.setText(new StringBuilder()
                .append(Common.currentBarber.getUsername()));


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
                double rating = Double.parseDouble(edt_barber_rating.getText().toString());
                long ratingTimes = Long.parseLong(edt_barber_rating_times.getText().toString());
                updateData.put("name",edt_barber_name.getText().toString());
                updateData.put("password",edt_barber_pass.getText().toString());
                updateData.put("rating",rating);
                updateData.put("ratingTimes",ratingTimes);
                updateData.put("username",edt_barber_username.getText().toString());
                updateBaber(updateData);
            }
        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateBaber(Map<String, Object> updateData) {
        barberRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.district_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonID())
                .collection("Barbers");

        barberRef
                .document(Common.currentBarber.getBarberId())
                .update(updateData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(context, "Update successful", Toast.LENGTH_SHORT).show();
                            loadBarberBaseOnSalon(Common.selectedSalon.getSalonID());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(BarberListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iBarberLoadListener = this;
        iOnLoadCountBarber = this;
    }



    @Override
    public void onBarberLoadSuccess(List<Barber> barberList) {
        if (barberList != null)
        {
            MyBarberAdapter barberAdapter = new MyBarberAdapter(this, barberList);
            recycler_barber.setAdapter(barberAdapter);

            dialog.dismiss();
        }


    }

    @Override
    public void onBarberLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onLoadCountBarberSuccess(int count) {
        txt_barber_count.setText(new StringBuilder("All Barber(")
                .append(count)
                .append(")"));
    }
}
