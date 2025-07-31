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
import com.example.starter.databinding.FragmentSignUpBinding;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private SignUpViewModel signUpViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupUI();
        setupObservers();
        setupClickListeners();

        return root;
    }

    private void setupUI() {
        // Add text change listeners for real-time validation
        binding.editTextFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFirstName();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editTextLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateLastName();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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

        binding.editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        signUpViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonSignUp.setEnabled(!isLoading);
            binding.buttonLogin.setEnabled(!isLoading);
        });

        signUpViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.textViewError.setVisibility(View.VISIBLE);
                binding.textViewError.setText(error);
            } else {
                binding.textViewError.setVisibility(View.GONE);
            }
        });

        signUpViewModel.getSignUpSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                // Navigate back to login
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.buttonSignUp.setOnClickListener(v -> {
            if (validateForm()) {
                String firstName = binding.editTextFirstName.getText().toString().trim();
                String lastName = binding.editTextLastName.getText().toString().trim();
                String email = binding.editTextEmail.getText().toString().trim();
                String password = binding.editTextPassword.getText().toString();
                
                signUpViewModel.signUp(firstName, lastName, email, password);
            }
        });

        binding.buttonLogin.setOnClickListener(v -> {
            // Navigate back to login fragment
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (!validateFirstName()) {
            isValid = false;
        }

        if (!validateLastName()) {
            isValid = false;
        }

        if (!validateEmail()) {
            isValid = false;
        }

        if (!validatePassword()) {
            isValid = false;
        }

        if (!validateConfirmPassword()) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validateFirstName() {
        String firstName = binding.editTextFirstName.getText().toString().trim();
        if (firstName.isEmpty()) {
            binding.textInputLayoutFirstName.setError("First name is required");
            return false;
        } else {
            binding.textInputLayoutFirstName.setError(null);
            return true;
        }
    }

    private boolean validateLastName() {
        String lastName = binding.editTextLastName.getText().toString().trim();
        if (lastName.isEmpty()) {
            binding.textInputLayoutLastName.setError("Last name is required");
            return false;
        } else {
            binding.textInputLayoutLastName.setError(null);
            return true;
        }
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

    private boolean validateConfirmPassword() {
        String password = binding.editTextPassword.getText().toString();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString();
        
        if (confirmPassword.isEmpty()) {
            binding.textInputLayoutConfirmPassword.setError("Please confirm your password");
            return false;
        } else if (!password.equals(confirmPassword)) {
            binding.textInputLayoutConfirmPassword.setError("Passwords do not match");
            return false;
        } else {
            binding.textInputLayoutConfirmPassword.setError(null);
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 