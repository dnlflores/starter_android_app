package com.example.starter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.starter.network.NetworkManager;

public class SignUpActivity extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword;
    private EditText etStreet, etCity, etState, etZip, etPhone;
    private Button btnSignUp, btnGoToLogin;
    private ProgressBar progressBar;
    private TextView tvError;
    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize NetworkManager
        networkManager = NetworkManager.getInstance(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Account information fields
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        
        // Address information fields
        etStreet = findViewById(R.id.et_street);
        etCity = findViewById(R.id.et_city);
        etState = findViewById(R.id.et_state);
        etZip = findViewById(R.id.et_zip);
        etPhone = findViewById(R.id.et_phone);
        
        // Buttons and UI elements
        btnSignUp = findViewById(R.id.btn_signup);
        btnGoToLogin = findViewById(R.id.btn_go_to_login);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> performSignUp());
        btnGoToLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void performSignUp() {
        // Get all input values
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String zip = etZip.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate required fields
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(street)) {
            etStreet.setError("Street address is required");
            etStreet.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(city)) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(state)) {
            etState.setError("State is required");
            etState.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(zip)) {
            etZip.setError("ZIP code is required");
            etZip.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        // Show loading state
        setLoadingState(true);
        hideError();

        // Perform signup
        networkManager.signup(username, email, password, street, city, state, zip, phone,
                new NetworkManager.NetworkCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                runOnUiThread(() -> {
                    if (success) {
                        // After successful signup, automatically log in the user
                        performAutoLogin(username, password);
                    } else {
                        setLoadingState(false);
                        showError("Sign up failed. Username might already exist.");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showError("Sign up failed: " + error);
                });
            }
        });
    }

    private void performAutoLogin(String username, String password) {
        networkManager.login(username, password, new NetworkManager.NetworkCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success) {
                        Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        showError("Account created but login failed. Please try logging in manually.");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showError("Account created but login failed: " + error);
                });
            }
        });
    }

    private void navigateToLogin() {
        finish(); // Go back to login activity
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!loading);
        btnGoToLogin.setEnabled(!loading);
        
        // Disable all input fields during loading
        etUsername.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etStreet.setEnabled(!loading);
        etCity.setEnabled(!loading);
        etState.setEnabled(!loading);
        etZip.setEnabled(!loading);
        etPhone.setEnabled(!loading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
} 