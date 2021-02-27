package com.henryalmeida.mototradeecconductor.providiers;

import com.henryalmeida.mototradeecconductor.models.FCMBody;
import com.henryalmeida.mototradeecconductor.models.FCMResponse;
import com.henryalmeida.mototradeecconductor.retrofit.IFCMApi;
import com.henryalmeida.mototradeecconductor.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider( ) {
    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
