package com.example.starter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.starter.network.NetworkManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnGoToSignup;
    private ProgressBar progressBar;
    private TextView tvError;
    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize NetworkManager
        networkManager = NetworkManager.getInstance(this);

        // Check if user is already logged in
        if (networkManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToSignup = findViewById(R.id.btn_go_to_signup);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        btnGoToSignup.setOnClickListener(v -> navigateToSignup());
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show loading state
        setLoadingState(true);
        hideError();

        // Perform login
        networkManager.login(username, password, new NetworkManager.NetworkCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success) {
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        showError("Invalid username or password");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showError("Login failed: " + error);
                });
            }
        });
    }

    private void navigateToSignup() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnGoToSignup.setEnabled(!loading);
        etUsername.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
} 