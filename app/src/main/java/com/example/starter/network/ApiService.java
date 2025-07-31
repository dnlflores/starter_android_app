package com.example.starter.network;

import com.example.starter.model.Tool;
import com.example.starter.model.User;
import com.example.starter.ui.auth.LoginViewModel.LoginResponse;
import com.example.starter.ui.auth.SignUpViewModel.SignUpResponse;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("tools")
    Call<List<Tool>> getTools();
    
    @GET("tools/{id}")
    Call<Tool> getTool(@Path("id") int toolId);
    
    @GET("users")
    Call<List<User>> getUsers();
    
    @GET("users/{id}")
    Call<User> getUser(@Path("id") int userId);
    
    @POST("auth/login")
    Call<LoginResponse> login(@Body RequestBody body);
    
    @POST("auth/signup")
    Call<SignUpResponse> signUp(@Body RequestBody body);
}