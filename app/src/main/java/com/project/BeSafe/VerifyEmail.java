package com.project.BeSafe;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmail extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "VerifyEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        checkInternetConnectivity();

        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Send initial email verification request
        sendEmailVerificationRequest();

        TextView resendEmailButton = findViewById(R.id.resendVerificationEmailButton);
        //Handle resend verification email button
        resendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationEmail();
            }
        });

        //Handle continue button to move to login activity
        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(VerifyEmail.this, Login.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    //Handle sending the initial email verification request
    private void sendEmailVerificationRequest() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            //Check if the user's email is not already verified
            if (!user.isEmailVerified()) {
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //Inform user that verification email was sent
                                    Toast.makeText(VerifyEmail.this, "Verification email sent, please check your inbox.", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Verification email sent");
                                } else {
                                    //Show error message if some error occurred
                                    Toast.makeText(VerifyEmail.this, "Failed to send verification email, please try again later.", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to send verification email", task.getException());
                                }
                            }
                        });
            }
        }
    }

    //Handle resending the verification email
    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Inform user that verification email was resent
                                Toast.makeText(VerifyEmail.this, "Verification email sent, please check your inbox.", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Verification email sent");
                            } else {
                                //Show error message if some error occurred
                                Toast.makeText(VerifyEmail.this, "Failed to send verification email, please try again.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to resend verification email", task.getException());
                            }
                        }
                    });
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