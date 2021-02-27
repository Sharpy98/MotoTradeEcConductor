package com.henryalmeida.mototradeecconductor.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleApi {

    @GET
    Call<String> getDirection(@Url String url);
}
