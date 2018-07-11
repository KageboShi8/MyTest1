package com.example.kageboshi.contacts_debug.http;

import com.example.kageboshi.contacts_debug.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {
    private static RetrofitService retrofitService;
    private static Retrofit retrofit;

    public static RetrofitService getInstance() {
        if (null == retrofitService) {
            synchronized (RetrofitFactory.class){
                if (null==retrofitService){
                    retrofitService=getInstanceRetrofit().create(RetrofitService.class);
                }
            }
        }
        return retrofitService;
    }


    private static Retrofit getInstanceRetrofit(){
        if (null==retrofit){
            synchronized (RetrofitFactory.class){
                if (null==retrofit){
                    retrofit=new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}
