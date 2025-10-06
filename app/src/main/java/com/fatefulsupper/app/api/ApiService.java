package com.fatefulsupper.app.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface ApiService {

    @FormUrlEncoded
    @POST("api/register")
    Call<Map<String, Object>> register(
            @Field("userid") String userid,
            @Field("password") String password,
            @Field("email") String email,
            @Field("username") String username,
            @Field("phone") String phone,
            @Field("role") String role
    );
    @FormUrlEncoded
    @POST("api/login")
    Call<Map<String, Object>> login(
            @Field("userid") String userid,
            @Field("password") String password
    );


}
