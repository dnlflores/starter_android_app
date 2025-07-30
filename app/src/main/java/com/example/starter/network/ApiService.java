package com.example.starter.network;

import com.example.starter.model.Tool;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("tools")
    Call<List<Tool>> getTools();
    
    @GET("tools/{id}")
    Call<Tool> getTool(@Path("id") int toolId);
}