package com.example.datlichcattoc.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.datlichcattoc.Common.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;

public class MyFCMService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this, s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        //dataSend.put("update_done", "true");
        if (remoteMessage.getData() != null)
        {
            if (remoteMessage.getData().get("update_done") != null)
            {
                updateLastBooking();
                Map<String,String> dataReceived = remoteMessage.getData();
                Paper.init(this);
                Paper.book().write(Common.RATING_INFORMATION_KEY, new Gson().toJson(dataReceived));
            }

            if (remoteMessage.getData().get(Common.TITLE_KEY) != null &&
                    remoteMessage.getData().get(Common.CONTENT_KEY) !=null)
            {
                Common.showNotification(this, new Random().nextInt(),
                        remoteMessage.getData().get(Common.TITLE_KEY),
                        remoteMessage.getData().get(Common.CONTENT_KEY),
                        null);
            }
        }

    }

    private void updateLastBooking() {
        //cần lấy user đã login
        //Vì app có thể chạy ở background nên cần lấy từ db paper

        CollectionReference userBooking;
        //nếu app đang chạy
        if (Common.currentUser != null)
        {
            userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking");
        }
        else
        {
            //Nếu app không chạy
            Paper.init(this);
            String user = Paper.book().read(Common.LOGGED_KEY);
            Log.d("My Phone homie", ""+user);
            userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(user)
                    .collection("Booking");
        }

        //Kiểm tra bằng cách lấy ngày hiện tại
        //Chỉ hoạt đông cho ngày hiện tại bởi vì appointment của ngày đó chỉ hiện trong ngày đó và 2 ngày tiếp theo
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MINUTE, 0);

        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        userBooking
                .whereGreaterThanOrEqualTo("timestamp",timestamp) //chỉ lấy ngày mà bookinginfo bằng với thời gian hôm nay hoặc 2 ngày tiếp
                .whereEqualTo("done",false) //và trường filed done là false
                .limit(1)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            if (task.getResult().size() > 0)
                            {
                                //Update
                                DocumentReference userBookingCurrentDocument = null;
                                for (DocumentSnapshot documentSnapshot:task.getResult())
                                {
                                    userBookingCurrentDocument = userBooking.document(documentSnapshot.getId());
                                }
                                if (userBookingCurrentDocument != null)
                                {
                                    Map<String, Object> dataUpdate = new HashMap<>();
                                    dataUpdate.put("done",true);
                                    userBookingCurrentDocument.update(dataUpdate)
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }
}
