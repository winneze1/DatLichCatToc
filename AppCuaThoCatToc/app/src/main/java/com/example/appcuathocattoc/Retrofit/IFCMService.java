package com.example.appcuathocattoc.Retrofit;

import com.example.appcuathocattoc.Model.FCMResponse;
import com.example.appcuathocattoc.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    //
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAgMGiCyE:APA91bGXGLVxpToofppiHkpixJlGEGJm6IJ8jrLWxGwCH4KqxkkTsEefHjCDh9Qi0lA3u7M34_D0GcDDEj8eMFjAxb9h9Ka3jZQGAYtAeOvTe-g73MhvC7cUQ2qHWLY1pnXXY_zJbpkV"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
