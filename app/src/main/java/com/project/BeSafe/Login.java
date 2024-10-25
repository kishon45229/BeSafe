package com.project.BeSafe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private boolean isPasswordVisible = false;
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkInternetConnectivity();

        //Start background location detection
        Intent serviceIntent = new Intent(this, ForegroundLocationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);

        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);
        TextView forgotPassword = findViewById(R.id.forgotPasswordButton);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        ImageButton passwordEye = findViewById(R.id.password_eye);
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

        //Check if user's credentials are remembered
        SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            String savedEmail = sharedPreferences.getString("email", "");
            String savedPassword = sharedPreferences.getString("password", "");
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }

        //Handle login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (isValidCredentials(email, password)) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        if (user != null && user.isEmailVerified()) {
                                            Toast.makeText(Login.this, "Welcome ", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "Login success");

                                            //Handle remember me functionality
                                            if (rememberMeCheckBox.isChecked()) {
                                                //Save email and password if Remember Me is checked
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean("rememberMe", true);
                                                editor.putString("email", email);
                                                editor.putString("password", password);
                                                editor.apply();
                                                Log.d(TAG, "Email and password will show when login");
                                            } else {
                                                //Clear saved credentials if Remember Me is unchecked
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.clear().apply();
                                                Log.d(TAG, "Email and password won't show when login");
                                            }

                                            //Check if user details exist in the database
                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                                            userRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        //Handle if user details exist in the database
                                                        Log.d(TAG, "User details are found, moving to Dashboard activity");
                                                        Intent dashboardIntent = new Intent(Login.this, Dashboard.class);
                                                        startActivity(dashboardIntent);
                                                        finish();
                                                    } else {
                                                        //Handle if no user details found
                                                        Log.e(TAG, "User details are not found, moving to AddPersonalDetails activity");
                                                        Intent addPersonalDetailsIntent = new Intent(Login.this, AddPersonalDetails.class);
                                                        startActivity(addPersonalDetailsIntent);
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    //Handle any errors in checking user credentials
                                                    Log.e(TAG, "Error checking user details in database: " + databaseError.getMessage());
                                                    Toast.makeText(Login.this, "Error checking user details", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            //Handle if email is not verified
                                            Toast.makeText(Login.this, "Please verify your email first", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Email not verified yet");

                                            Intent verifyEmailIntent = new Intent(Login.this, VerifyEmail.class);
                                            startActivity(verifyEmailIntent);
                                            finish();
                                        }
                                    } else {
                                        //Handle if login fails
                                        Exception e = task.getException();
                                        if (e != null) {
                                            String errorMessage = e.getMessage();
                                            if (errorMessage != null) {
                                                Toast.makeText(Login.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                                Log.e(TAG, "Login failed", e);
                                            } else {
                                                Toast.makeText(Login.this, "Login failed: Unknown error", Toast.LENGTH_LONG).show();
                                                Log.e(TAG, "Unknown error occurred when login");
                                            }
                                        }
                                    }
                                }
                            });
                } else {
                    //When user enter invalid credentials, show error message
                    Toast.makeText(Login.this, "Please enter a valid email and password", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Invalid credentials entered by the user");
                }
            }
        });

        //Handle signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Signup button clicked");
                Intent signupIntent = new Intent(Login.this, Signup.class);
                startActivity(signupIntent);
                finish();
            }
        });

        //Handle forgot password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Forgot password link clicked");
                Intent forgotPasswordIntent = new Intent(Login.this, ForgotPassword.class);
                startActivity(forgotPasswordIntent);
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
        //Move cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    //Check if email and password are not empty and follow basic format validation
    private boolean isValidCredentials(String email, String password) {
        return !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !password.isEmpty();
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
