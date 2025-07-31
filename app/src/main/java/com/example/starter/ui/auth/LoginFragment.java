package com.example.starter.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.starter.R;
import com.example.starter.auth.AuthManager;
import com.example.starter.databinding.FragmentLoginBinding;
import com.example.starter.model.User;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupUI();
        setupObservers();
        setupClickListeners();

        return root;
    }

    private void setupUI() {
        // Add text change listeners for real-time validation
        binding.editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        loginViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonLogin.setEnabled(!isLoading);
            binding.buttonSignUp.setEnabled(!isLoading);
        });

        loginViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.textViewError.setVisibility(View.VISIBLE);
                binding.textViewError.setText(error);
            } else {
                binding.textViewError.setVisibility(View.GONE);
            }
        });

        loginViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                // Navigate back to the previous screen
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.buttonLogin.setOnClickListener(v -> {
            if (validateForm()) {
                String email = binding.editTextEmail.getText().toString().trim();
                String password = binding.editTextPassword.getText().toString();
                loginViewModel.login(email, password);
            }
        });

        binding.buttonSignUp.setOnClickListener(v -> {
            // Navigate to sign up fragment
            Navigation.findNavController(requireView()).navigate(R.id.action_login_to_signup);
        });

        binding.textViewForgotPassword.setOnClickListener(v -> {
            // TODO: Implement forgot password functionality
            Toast.makeText(requireContext(), "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (!validateEmail()) {
            isValid = false;
        }

        if (!validatePassword()) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validateEmail() {
        String email = binding.editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.setError("Please enter a valid email address");
            return false;
        } else {
            binding.textInputLayoutEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = binding.editTextPassword.getText().toString();
        if (password.isEmpty()) {
            binding.textInputLayoutPassword.setError("Password is required");
            return false;
        } else if (password.length() < 6) {
            binding.textInputLayoutPassword.setError("Password must be at least 6 characters");
            return false;
        } else {
            binding.textInputLayoutPassword.setError(null);
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 