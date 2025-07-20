package com.example.starter.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.example.starter.models.AuthResponse;
import com.example.starter.models.ChatAPIMessage;
import com.example.starter.models.Tool;
import com.example.starter.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {
    private static NetworkManager instance;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private Context context;
    
    // Shared preferences keys
    private static final String PREFS_NAME = "StarterPrefs";
    private static final String AUTH_TOKEN_KEY = "authToken";
    private static final String USERNAME_KEY = "username";
    private static final String USER_ID_KEY = "userId";

    private NetworkManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        setupRetrofit();
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context);
        }
        return instance;
    }

    private void setupRetrofit() {
        // Create logging interceptor for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttp client
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Create Gson with custom configuration
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // Authentication methods
    public void signup(String username, String email, String password, String street, 
                      String city, String state, String zip, String phone, 
                      NetworkCallback<Boolean> callback) {
        Map<String, Object> signupData = new HashMap<>();
        signupData.put("username", username);
        signupData.put("email", email);
        signupData.put("password", password);
        signupData.put("address", street);
        signupData.put("city", city);
        signupData.put("state", state);
        signupData.put("zip", zip);
        signupData.put("phone", phone);

        apiService.signup(signupData).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                callback.onSuccess(response.code() == 201);
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void login(String username, String password, NetworkCallback<Boolean> callback) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);

        apiService.login(loginData).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    saveAuthToken(authResponse.getToken());
                    saveUsername(username);
                    
                    // Fetch current user info to get user ID
                    fetchCurrentUserInfo(callback);
                } else {
                    callback.onSuccess(false);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private void fetchCurrentUserInfo(NetworkCallback<Boolean> originalCallback) {
        fetchUsers(new NetworkCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                // Find current user by username
                String currentUsername = getUsername();
                if (users != null && currentUsername != null) {
                    for (User user : users) {
                        if (currentUsername.equals(user.getUsername())) {
                            saveUserId(user.getId());
                            break;
                        }
                    }
                }
                originalCallback.onSuccess(true);
            }

            @Override
            public void onError(String error) {
                // Even if we can't get user info, login was successful
                originalCallback.onSuccess(true);
            }
        });
    }

    public void logout() {
        clearAuthToken();
        clearUsername();
        clearUserId();
    }

    // User methods
    public void fetchUsers(NetworkCallback<List<User>> callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onError("No auth token available");
            return;
        }

        apiService.getUsers("Bearer " + token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch users");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Tool methods
    public void fetchTools(NetworkCallback<List<Tool>> callback) {
        apiService.getTools().enqueue(new Callback<List<Tool>>() {
            @Override
            public void onResponse(Call<List<Tool>> call, Response<List<Tool>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch tools");
                }
            }

            @Override
            public void onFailure(Call<List<Tool>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createTool(String name, String price, String description, int ownerId,
                          Double latitude, Double longitude, Bitmap image,
                          NetworkCallback<Boolean> callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onError("No auth token available");
            return;
        }

        // Format price to one decimal place
        String formattedPrice = String.format("%.1f", Double.parseDouble(price));

        // Create form data parts
        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), formattedPrice);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody ownerIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(ownerId));
        RequestBody latBody = RequestBody.create(MediaType.parse("text/plain"), 
                latitude != null ? String.valueOf(latitude) : "");
        RequestBody lngBody = RequestBody.create(MediaType.parse("text/plain"), 
                longitude != null ? String.valueOf(longitude) : "");
        RequestBody createdAtBody = RequestBody.create(MediaType.parse("text/plain"), 
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new java.util.Date()));

        MultipartBody.Part imagePart = null;
        if (image != null) {
            imagePart = createImagePart(image, "image");
        }

        apiService.createTool("Bearer " + token, nameBody, priceBody, descBody, ownerIdBody, 
                latBody, lngBody, createdAtBody, imagePart)
                .enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                callback.onSuccess(response.code() == 201);
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateTool(int toolId, String name, String price, String description,
                          Double latitude, Double longitude, Bitmap image,
                          NetworkCallback<Boolean> callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onError("No auth token available");
            return;
        }

        // Format price to one decimal place
        String formattedPrice = String.format("%.1f", Double.parseDouble(price));

        // Create form data parts
        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), formattedPrice);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody latBody = RequestBody.create(MediaType.parse("text/plain"), 
                latitude != null ? String.valueOf(latitude) : "");
        RequestBody lngBody = RequestBody.create(MediaType.parse("text/plain"), 
                longitude != null ? String.valueOf(longitude) : "");
        RequestBody updatedAtBody = RequestBody.create(MediaType.parse("text/plain"), 
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new java.util.Date()));

        MultipartBody.Part imagePart = null;
        if (image != null) {
            imagePart = createImagePart(image, "image");
        }

        apiService.updateTool(toolId, "Bearer " + token, nameBody, priceBody, descBody, 
                latBody, lngBody, updatedAtBody, imagePart)
                .enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                callback.onSuccess(response.code() == 200 || response.code() == 204);
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteTool(int toolId, NetworkCallback<Boolean> callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onError("No auth token available");
            return;
        }

        apiService.deleteTool(toolId, "Bearer " + token).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                callback.onSuccess(response.code() == 204 || response.code() == 200);
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Chat methods
    public void fetchChats(NetworkCallback<List<ChatAPIMessage>> callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onError("No auth token available");
            return;
        }

        apiService.getChats("Bearer " + token).enqueue(new Callback<List<ChatAPIMessage>>() {
            @Override
            public void onResponse(Call<List<ChatAPIMessage>> call, Response<List<ChatAPIMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch chats");
                }
            }

            @Override
            public void onFailure(Call<List<ChatAPIMessage>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void sendMessage(int recipientId, String message, Integer toolId, Bitmap image,
                           NetworkCallback<ChatAPIMessage> callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onError("No auth token available");
            return;
        }

        RequestBody recipientIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(recipientId));
        RequestBody messageBody = RequestBody.create(MediaType.parse("text/plain"), message);
        RequestBody toolIdBody = null;
        if (toolId != null) {
            toolIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(toolId));
        }

        MultipartBody.Part imagePart = null;
        if (image != null) {
            imagePart = createImagePart(image, "image");
        }

        apiService.sendMessage("Bearer " + token, recipientIdBody, messageBody, toolIdBody, imagePart)
                .enqueue(new Callback<ChatAPIMessage>() {
            @Override
            public void onResponse(Call<ChatAPIMessage> call, Response<ChatAPIMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to send message");
                }
            }

            @Override
            public void onFailure(Call<ChatAPIMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void editMessage(int messageId, String newMessage, NetworkCallback<ChatAPIMessage> callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onError("No auth token available");
            return;
        }

        Map<String, String> messageData = new HashMap<>();
        messageData.put("message", newMessage);

        apiService.editMessage(messageId, "Bearer " + token, messageData)
                .enqueue(new Callback<ChatAPIMessage>() {
            @Override
            public void onResponse(Call<ChatAPIMessage> call, Response<ChatAPIMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to edit message");
                }
            }

            @Override
            public void onFailure(Call<ChatAPIMessage> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Image upload
    public void uploadImage(Bitmap image, NetworkCallback<String> callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onError("No auth token available");
            return;
        }

        MultipartBody.Part imagePart = createImagePart(image, "image");

        apiService.uploadImage("Bearer " + token, imagePart).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().get("imageUrl");
                    callback.onSuccess(imageUrl);
                } else {
                    callback.onError("Failed to upload image");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Helper methods
    private MultipartBody.Part createImagePart(Bitmap bitmap, String partName) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
        return MultipartBody.Part.createFormData(partName, "image.jpg", requestFile);
    }

    // Token management
    private void saveAuthToken(String token) {
        sharedPreferences.edit().putString(AUTH_TOKEN_KEY, token).apply();
    }

    private String getAuthToken() {
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null);
    }

    private void clearAuthToken() {
        sharedPreferences.edit().remove(AUTH_TOKEN_KEY).apply();
    }

    private void saveUsername(String username) {
        sharedPreferences.edit().putString(USERNAME_KEY, username).apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(USERNAME_KEY, "Guest");
    }

    private void clearUsername() {
        sharedPreferences.edit().remove(USERNAME_KEY).apply();
    }

    private void saveUserId(int userId) {
        sharedPreferences.edit().putInt(USER_ID_KEY, userId).apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt(USER_ID_KEY, -1);
    }

    private void clearUserId() {
        sharedPreferences.edit().remove(USER_ID_KEY).apply();
    }

    public boolean isLoggedIn() {
        return getAuthToken() != null && !getAuthToken().isEmpty();
    }

    // Callback interface
    public interface NetworkCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
} 