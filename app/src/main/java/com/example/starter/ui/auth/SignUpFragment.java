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
        binding.editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername();
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

        binding.editTextStreetAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateStreetAddress();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editTextCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateCity();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editTextState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editTextZipCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateZipCode();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        signUpViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonSignUp.setEnabled(!isLoading);
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
                String username = binding.editTextUsername.getText().toString().trim();
                String email = binding.editTextEmail.getText().toString().trim();
                String password = binding.editTextPassword.getText().toString();
                String streetAddress = binding.editTextStreetAddress.getText().toString().trim();
                String city = binding.editTextCity.getText().toString().trim();
                String state = binding.editTextState.getText().toString().trim();
                String zipCode = binding.editTextZipCode.getText().toString().trim();
                String phone = binding.editTextPhone.getText().toString().trim();
                
                signUpViewModel.signUp(username, email, password, streetAddress, city, state, zipCode, phone);
            }
        });

        binding.textViewSignIn.setOnClickListener(v -> {
            // Navigate back to login fragment
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (!validateUsername()) {
            isValid = false;
        }

        if (!validateEmail()) {
            isValid = false;
        }

        if (!validatePassword()) {
            isValid = false;
        }

        if (!validateStreetAddress()) {
            isValid = false;
        }

        if (!validateCity()) {
            isValid = false;
        }

        if (!validateState()) {
            isValid = false;
        }

        if (!validateZipCode()) {
            isValid = false;
        }

        if (!validatePhone()) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validateUsername() {
        String username = binding.editTextUsername.getText().toString().trim();
        if (username.isEmpty()) {
            binding.editTextUsername.setError("Username is required");
            return false;
        } else {
            binding.editTextUsername.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = binding.editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.editTextEmail.setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Please enter a valid email address");
            return false;
        } else {
            binding.editTextEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = binding.editTextPassword.getText().toString();
        if (password.isEmpty()) {
            binding.editTextPassword.setError("Password is required");
            return false;
        } else if (password.length() < 6) {
            binding.editTextPassword.setError("Password must be at least 6 characters");
            return false;
        } else {
            binding.editTextPassword.setError(null);
            return true;
        }
    }

    private boolean validateStreetAddress() {
        String streetAddress = binding.editTextStreetAddress.getText().toString().trim();
        if (streetAddress.isEmpty()) {
            binding.editTextStreetAddress.setError("Street address is required");
            return false;
        } else {
            binding.editTextStreetAddress.setError(null);
            return true;
        }
    }

    private boolean validateCity() {
        String city = binding.editTextCity.getText().toString().trim();
        if (city.isEmpty()) {
            binding.editTextCity.setError("City is required");
            return false;
        } else {
            binding.editTextCity.setError(null);
            return true;
        }
    }

    private boolean validateState() {
        String state = binding.editTextState.getText().toString().trim();
        if (state.isEmpty()) {
            binding.editTextState.setError("State is required");
            return false;
        } else {
            binding.editTextState.setError(null);
            return true;
        }
    }

    private boolean validateZipCode() {
        String zipCode = binding.editTextZipCode.getText().toString().trim();
        if (zipCode.isEmpty()) {
            binding.editTextZipCode.setError("ZIP code is required");
            return false;
        } else {
            binding.editTextZipCode.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phone = binding.editTextPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            binding.editTextPhone.setError("Phone number is required");
            return false;
        } else {
            binding.editTextPhone.setError(null);
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 