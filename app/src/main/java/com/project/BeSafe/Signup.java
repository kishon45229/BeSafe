package com.project.BeSafe;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false, isConfirmPasswordVisible = false;
    private EditText passwordEditText, confirmPasswordEditText;
    private static final String TAG = "Signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        checkInternetConnectivity();

        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        EditText emailEditText = findViewById(R.id.email);
        emailEditText.addTextChangedListener(new EmailValidator(emailEditText));

        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);

        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);

        ImageButton passwordEye = findViewById(R.id.password_eye);
        ImageButton confirmPasswordEye = findViewById(R.id.confirmPassword_eye);

        //Handle password visibility when the eye icon is clicked
        passwordEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();

                if (isPasswordVisible) {
                    passwordEye.setImageResource(R.drawable.baseline_visibility_24);
                    Log.d(TAG, "Password visibility changed to visible");
                } else {
                    passwordEye.setImageResource(R.drawable.baseline_visibility_off_24);
                    Log.d(TAG, "Password visibility changed to hidden");
                }
            }
        });

        //Handle confirm password visibility when the eye icon is clicked
        confirmPasswordEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConfirmPasswordVisibility();

                if (isPasswordVisible) {
                    passwordEye.setImageResource(R.drawable.baseline_visibility_24);
                    Log.d(TAG, "Password visibility changed to visible");
                } else {
                    passwordEye.setImageResource(R.drawable.baseline_visibility_off_24);
                    Log.d(TAG, "Password visibility changed to hidden");
                }
            }
        });

        //Handle using TextWatcher to validate password field as the user types
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Do nothing before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PasswordValidator.isPasswordValid(passwordEditText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Do nothing after text changes
            }
        });

        //Handle using TextWatcher to validate password field as the user types
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Do nothing before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PasswordValidator.doPasswordsMatch(passwordEditText, confirmPasswordEditText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Do nothing after text changes
            }
        });

        //Handle signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                //Validate email, password, and confirm password fields
                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Password is required");
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    confirmPasswordEditText.setError("Please confirm your password");
                    return;
                }

                //Check if passwords match
                if (!PasswordValidator.doPasswordsMatch(passwordEditText, confirmPasswordEditText)) {
                    return;
                }

                //Create a new user with Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Registration success
                                    Log.d(TAG, "Registration success");

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        Log.d(TAG, "Moving the user to VerifyEmail activity");
                                        Intent verifyEmailIntent = new Intent(Signup.this, VerifyEmail.class);
                                        startActivity(verifyEmailIntent);
                                        finish();
                                    }
                                } else {
                                    //Handle registration failed
                                    Log.e(TAG, "Registration failed", task.getException());
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                                    Toast.makeText(Signup.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        //Handle login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login button clicked");
                Intent loginIntent = new Intent(Signup.this, Login.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    //Handle password show/hide
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            //If the password is currently visible hide it
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isPasswordVisible = false;
            Log.d(TAG, "Password visibility changed to hidden");
        } else {
            //If the password is currently hidden show it
            passwordEditText.setTransformationMethod(null);
            isPasswordVisible = true;
            Log.d(TAG, "Password visibility changed to visible");
        }

        // Move cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    //Handle confirm password show/hide
    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            //If the password is currently visible hide it
            confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isConfirmPasswordVisible = false;
            Log.d(TAG, "Confirm password visibility changed to hidden");
        } else {
            //If the password is currently visible hide it
            confirmPasswordEditText.setTransformationMethod(null);
            isConfirmPasswordVisible = true;
            Log.d(TAG, "Confirm password visibility changed to visible");
        }

        // Move cursor to the end of the text
        confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
    }

    //Handle email validation
    private static class EmailValidator implements TextWatcher {
        private final EditText editText;

        public EmailValidator(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            String email = editText.getText().toString().trim();
            if (!isValidEmail(email)) {
                editText.setError("Invalid email address");
            } else {
                editText.setError(null);
            }
        }

        //Check if email is valid
        private boolean isValidEmail(String email) {
            if (TextUtils.isEmpty(email)) {
                return false;
            } else {
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                Pattern pattern = Pattern.compile(emailPattern);
                Matcher matcher = pattern.matcher(email);
                return matcher.matches();
            }
        }
    }

    //Handle password validation
    private static class PasswordValidator {
        // Validate password strength
        public static boolean isPasswordValid(EditText passwordField) {
            String password = passwordField.getText().toString();

            if (password.length() < 8 || password.length() > 14) {
                passwordField.setError("Password must be between 8 and 14 characters long");
                return false;
            }

            if (!password.matches(".*[@$!%*?&].*")) {
                passwordField.setError("Password must contain at least one special character (@$!%*?&)");
                return false;
            }

            if (!password.matches(".*[a-z].*")) {
                passwordField.setError("Password must contain at least one lowercase letter");
                return false;
            }

            if (!password.matches(".*[A-Z].*")) {
                passwordField.setError("Password must contain at least one uppercase letter");
                return false;
            }

            if (!password.matches(".*\\d.*")) {
                passwordField.setError("Password must contain at least one digit");
                return false;
            }

            passwordField.setError(null);
            return true;
        }

        //Handle if passwords don't match
        public static boolean doPasswordsMatch(EditText passwordField, EditText confirmPasswordField) {
            String password = passwordField.getText().toString();
            String confirmPassword = confirmPasswordField.getText().toString();

            if (!password.equals(confirmPassword)) {
                confirmPasswordField.setError("Passwords do not match");
                return false;
            }

            confirmPasswordField.setError(null);
            return true;
        }
    }

    //Check internet connection available or not
    private void checkInternetConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show();
            Log.e(TAG, "No internet connection available");
        } else {
            Log.d(TAG, "Internet connection is available");
        }
    }
}