package com.example.appcuathocattoc.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.appcuathocattoc.Model.Barber;
import com.example.appcuathocattoc.Model.BookingInformation;
import com.example.appcuathocattoc.Model.MyToken;
import com.example.appcuathocattoc.Model.Salon;
import com.example.appcuathocattoc.R;
import com.example.appcuathocattoc.Service.MyFCMService;
import com.example.appcuathocattoc.StaffHomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Formatter;

import io.paperdb.Paper;

public class Common {
    public static final Object DISABLE_TAG = "DISABLE";
    public static final int TIME_SLOT_TOTAL = 21;
    public static final String LOGGED_KEY = "LOGGED";
    public static final String DISTRICT_KEY = "DISTRICT";
    public static final String SALON_KEY = "SALON";
    public static final String BARBER_KEY = "BARBER";
    public static final String TITLE_KEY = "title";
    public static final String CONTENT_KEY = "content";
    public static final int MAX_NOTIFICATION_PER_LOAD = 10;
    public static final String SERVICES_ADDED = "SERVICES_ADDED";
    public static final double DEFAULT_PRICE = 30; //Gía mặc định khi ko thêm item hay services
    public static final String MONEY_SIGN = "$";
    public static final String SHOPPING_LIST = "SHOPPING_LIST_ITEMS";
    public static final String IMAGE_DOWNLOADABLE_URL = "DOWNLOADABLE_URL";
    public static final String RATING_DISTRICT_KEY = "RATING_DISTRICT";
    public static final String RATING_SALON_ID = "RATING_SALON";
    public static final String RATING_SALON_NAME = "RATING_SALON_NAME";
    public static final String RATING_BARBER_ID = "RATING_BARBER_ID";
    public static String district_name = "";
    public static Salon selectedSalon;
    public static Barber currentBarber;
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");;
    public static Calendar bookingDate = Calendar.getInstance();
    public static BookingInformation currentBookingInformation;

    public static String convertTimeSlotToString(int slot) {
        switch(slot) {
            case 0:
                return "9:00-9:30";
            case 1:
                return "9:30-10:00";
            case 2:
                return "10:00-10:30";
            case 3:
                return "10:30-11:00";
            case 4:
                return "11:00-11:30";
            case 5:
                return "11:30-12:00";
            case 6:
                return "12:00-12:30";
            case 7:
                return "12:30-13:00";
            case 8:
                return "13:00-13:30";
            case 9:
                return "13:30-14:00";
            case 10:
                return "14:00-14:30";
            case 11:
                return "14:30-15:00";
            case 12:
                return "15:00-15:30";
            case 13:
                return "15:30-16:00";
            case 14:
                return "16:00-16:30";
            case 15:
                return "16:30-17:00";
            case 16:
                return "17:00-17:30";
            case 17:
                return "17:30-18:00";
            case 18:
                return "18:00-18:30";
            case 19:
                return "18:30-19:00";
            case 20:
                return "19:00-19:30";
            default:
                return "Closed";
        }
    }

    public static void showNotification(Context context, int notification_id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(
                    context,
                    notification_id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

        String NOTIFICATION_CHANNEL_ID = "dat_lich_cat_toc_barber_app";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "App Cua Tho Cat Toc", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Staff app");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        //

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();

        notificationManager.notify(notification_id,notification);
    }

    public static String formatShoppingItemName(String name) {
        return name.length() > 13 ? new StringBuilder(name.substring(0, 10)).append("...").toString() : name;
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String result = null;
        if (fileUri.getScheme().equals("content"))
        {
            Cursor cursor = contentResolver.query(fileUri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }finally {
                cursor.close();
            }
        }
        if (result == null)
        {
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }

        return result;
    }

    public static enum TOKEN_TYPE {
        CLIENT,
        BARBER,
        MANAGER
    }

    public static void updateToken(Context context, String token) {
        //Kiểm tra xem nếu user còn login
        //Nếu còn thì store token của user
        Paper.init(context);
        String user = Paper.book().read(Common.LOGGED_KEY);
        if (user != null)
        {
            if (!TextUtils.isEmpty(user))
            {
                MyToken myToken = new MyToken();
                myToken.setToken(token);
                myToken.setTokenType(TOKEN_TYPE.BARBER); // vì đây là app của thợ cắt tóc
                myToken.setUserPhone(user);

                //Submit to Firestore
                FirebaseFirestore.getInstance()
                        .collection("Tokens")
                        .document(user)
                        .set(myToken)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            }
        }
    }
}
