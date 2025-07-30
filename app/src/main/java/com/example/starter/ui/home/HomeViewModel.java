package com.example.starter.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starter.model.Tool;
import com.example.starter.model.User;
import com.example.starter.network.ApiClient;
import com.example.starter.network.ApiService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<List<Tool>> mTools;
    private final MutableLiveData<List<User>> mUsers;
    private final MutableLiveData<Boolean> mIsLoading;
    private final MutableLiveData<String> mError;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mTools = new MutableLiveData<>();
        mUsers = new MutableLiveData<>();
        mIsLoading = new MutableLiveData<>();
        mError = new MutableLiveData<>();

        mIsLoading.setValue(false);
        
        loadTools();
        loadUsers();
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<Tool>> getTools() {
        return mTools;
    }

    public LiveData<List<User>> getUsers() {
        return mUsers;
    }

    public LiveData<Boolean> getIsLoading() {
        return mIsLoading;
    }

    public LiveData<String> getError() {
        return mError;
    }

    public User getUserById(int userId) {
        List<User> users = mUsers.getValue();
        if (users != null) {
            for (User user : users) {
                if (user.getId() == userId) {
                    return user;
                }
            }
        }
        return null;
    }

    public void loadTools() {
        mIsLoading.setValue(true);
        mError.setValue(null);

        // Create OkHttpClient with extended timeout for slow connections
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS) // 2 minutes connect timeout
                .readTimeout(120, TimeUnit.SECONDS)    // 2 minutes read timeout
                .writeTimeout(120, TimeUnit.SECONDS)   // 2 minutes write timeout
                .build();

        // Create a temporary Retrofit instance with extended timeout
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Use the temporary service for this call
        ApiService tempService = retrofit.create(ApiService.class);
        Call<List<Tool>> call = tempService.getTools();
        
        call.enqueue(new Callback<List<Tool>>() {
            @Override
            public void onResponse(Call<List<Tool>> call, Response<List<Tool>> response) {
                mIsLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    mTools.setValue(response.body());
                } else {
                    mError.setValue("Failed to load tools");
                }
            }

            @Override
            public void onFailure(Call<List<Tool>> call, Throwable t) {
                mIsLoading.setValue(false);
                mError.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void loadUsers() {
        // Create OkHttpClient with extended timeout for slow connections
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS) // 2 minutes connect timeout
                .readTimeout(120, TimeUnit.SECONDS)    // 2 minutes read timeout
                .writeTimeout(120, TimeUnit.SECONDS)   // 2 minutes write timeout
                .build();

        // Create a temporary Retrofit instance with extended timeout
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Use the temporary service for this call
        ApiService tempService = retrofit.create(ApiService.class);
        Call<List<User>> call = tempService.getUsers();
        
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mUsers.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // Don't set error for users since tools are more important
            }
        });
    }

    public void retry() {
        loadTools();
        loadUsers();
    }
}