package com.example.starter.network;

import com.example.starter.models.AuthResponse;
import com.example.starter.models.ChatAPIMessage;
import com.example.starter.models.Tool;
import com.example.starter.models.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    String BASE_URL = "https://starter-ios-app-backend.onrender.com/";

    // Authentication endpoints
    @POST("signup")
    Call<ResponseBody> signup(@Body Map<String, Object> signupData);

    @POST("login")
    Call<AuthResponse> login(@Body Map<String, String> loginData);

    // User endpoints
    @GET("users")
    Call<List<User>> getUsers(@Header("Authorization") String authorization);

    // Tool endpoints
    @GET("tools")
    Call<List<Tool>> getTools();

    @Multipart
    @POST("tools")
    Call<ResponseBody> createTool(
            @Header("Authorization") String authorization,
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("description") RequestBody description,
            @Part("owner_id") RequestBody ownerId,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("created_at") RequestBody createdAt,
            @Part MultipartBody.Part image
    );

    @Multipart
    @PUT("tools/{id}")
    Call<ResponseBody> updateTool(
            @Path("id") int toolId,
            @Header("Authorization") String authorization,
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("description") RequestBody description,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("updated_at") RequestBody updatedAt,
            @Part MultipartBody.Part image
    );

    @DELETE("tools/{id}")
    Call<ResponseBody> deleteTool(
            @Path("id") int toolId,
            @Header("Authorization") String authorization
    );

    // Chat endpoints
    @GET("chats")
    Call<List<ChatAPIMessage>> getChats(@Header("Authorization") String authorization);

    @Multipart
    @POST("chats")
    Call<ChatAPIMessage> sendMessage(
            @Header("Authorization") String authorization,
            @Part("recipient_id") RequestBody recipientId,
            @Part("message") RequestBody message,
            @Part("tool_id") RequestBody toolId,
            @Part MultipartBody.Part image
    );

    @PUT("chats/{id}")
    Call<ChatAPIMessage> editMessage(
            @Path("id") int messageId,
            @Header("Authorization") String authorization,
            @Body Map<String, String> messageData
    );

    // Image upload endpoint
    @Multipart
    @POST("upload-image")
    Call<Map<String, String>> uploadImage(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part image
    );
} 