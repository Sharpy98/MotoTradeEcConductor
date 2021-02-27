package com.henryalmeida.mototradeecconductor.retrofit;

import com.henryalmeida.mototradeecconductor.models.FCMBody;
import com.henryalmeida.mototradeecconductor.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAZ29m520:APA91bE6w1O8pqT461GvGILP5uOMMi4DW0QUt86dBWUtacWw_mtHgwHzSXdTmIxpZb4AAaKX4Dwt4foQhShvoZZPR-dT3G9nXtTdHTnGj17XmmbJNKD8-ZqrvgQZG3E9TC9-EyK5_Klx"
    })

    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
