package com.project.BeSafe;

import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WitnessDetailsManager {
    private static final String TAG = "WitnessDetailsManager";
    private final DatabaseReference mDatabase;

    public WitnessDetailsManager() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void loadWitnessDetails(CheckBox witnessDetailsCheckBox, TextView userNameTextView, TextView userMobileNoTextView, Object currentUser) {
        witnessDetailsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (currentUser != null) {
                        String userId = ((FirebaseUser) currentUser).getUid();
                        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String firstName = snapshot.child("firstName").getValue(String.class);
                                    if (firstName != null) {
                                        userNameTextView.setText(firstName);
                                    } else {
                                        Log.d(TAG, "First name is null");
                                    }

                                    String mobileNo = snapshot.child("mobileNo").getValue(String.class);
                                    if (mobileNo != null) {
                                        userMobileNoTextView.setText(mobileNo);
                                    } else {
                                        Log.d(TAG, "Mobile number is null");
                                    }
                                } else {
                                    Log.d(TAG, "User data snapshot does not exist");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "Database error: " + error.getMessage());
                            }
                        });
                    } else {
                        Log.d(TAG, "No current user");
                    }
                }
            }
        });
    }
}
