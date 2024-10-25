package com.project.BeSafe;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AddPersonalDetails extends AppCompatActivity {
    private static final String DEFAULT_PHONE_NUMBER = "+94 ", TAG = "Signup";
    private EditText dobPicker, nicNo;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpersonaldetails);

        checkInternetConnectivity();

        //Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        EditText firstNameEditText = findViewById(R.id.firstName);
        EditText lastNameEditText = findViewById(R.id.lastName);
        dobPicker = findViewById(R.id.dob);
        nicNo = findViewById(R.id.nic);
        EditText mobileNumberEditText = findViewById(R.id.mobileNumber);
        Button SubmitButton = findViewById(R.id.submitButton);

        //Set input filters for name fields to restrict input to letters only
        firstNameEditText.setFilters(new InputFilter[]{new NameInputFilter()});
        lastNameEditText.setFilters(new InputFilter[]{new NameInputFilter()});

        //Set default phone number and input filter for mobile number field
        mobileNumberEditText.setText(DEFAULT_PHONE_NUMBER);
        mobileNumberEditText.setSelection(mobileNumberEditText.getText().length());
        mobileNumberEditText.setFilters(new InputFilter[]{new SriLankaPhoneNumberFilter()});

        //Handle summit button
        SubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData(null);
            }
        });
    }

    //Handle input filter to allow only letters in name fields
    private static class NameInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Character.isLetter(c) && builder.length() < 10) {
                    builder.append(c);
                }
            }
            boolean hasChanged = (builder.length() != end - start);
            return hasChanged ? builder.toString() : null;
        }
    }

    //Show date picker dialog for date of birth input
    public void showDatePickerDialog(View v) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        dobPicker.setText(selectedDate);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    //Validate NIC number
    public boolean isNICValid(String nicNumber) {
        String nicNumberStr = String.valueOf(nicNumber);
        String firstTwoDigitsStr = nicNumberStr.substring(0, 2);
        int firstTwoDigits = Integer.parseInt(firstTwoDigitsStr);

        if (firstTwoDigits >= 40 && firstTwoDigits <= 99) {
            return isOldNICValid(String.valueOf(nicNumber));
        } else {
            return isNewNICValid(String.valueOf(nicNumber));
        }
    }

    //Validate old NIC format
    public boolean isOldNICValid(String nic) {
        if (nic.length() != 10) {
            nicNo.setError("Invalid NIC number");
            return false;
        }

        String numericPart = nic.substring(0, 9);
        char letterPart = nic.charAt(9);

        if (!numericPart.matches("\\d{9}")) {
            nicNo.setError("Invalid NIC number");
            return false;
        }

        int birthYear = Integer.parseInt(numericPart.substring(0, 2));
        int dayNumber = Integer.parseInt(numericPart.substring(2, 5));
        int serialNumber = Integer.parseInt(numericPart.substring(5, 8));

        if (birthYear < 0 || birthYear > 99 || dayNumber < 1 || dayNumber > 366 || serialNumber < 1 || serialNumber > 999) {
            nicNo.setError("Invalid NIC number");
            return false;
        }

        if (letterPart != 'V' && letterPart != 'X') {
            nicNo.setError("Invalid NIC number");
            return false;
        }

        return true;
    }

    //Validate new NIC format
    public boolean isNewNICValid(String nic) {
        if (nic.length() != 12) {
            nicNo.setError("Invalid NIC number0");
            return false;
        }

        String districtCode = nic.substring(0, 2);
        String birthYear = nic.substring(2, 6);
        String genderCode = nic.substring(6, 8);
        String serialNumber = nic.substring(8, 12);

        if (!districtCode.matches("[A-Za-z0-9]{2}") || !birthYear.matches("\\d{4}") || !genderCode.matches("\\d{2}") || !serialNumber.matches("\\d{4}")) {
            nicNo.setError("Invalid NIC number");
            return false;
        }

        return true;
    }

    //Handle input filter for Sri Lankan phone numbers
    private static class SriLankaPhoneNumberFilter implements InputFilter {
        private static final int COUNTRY_CODE_LENGTH = DEFAULT_PHONE_NUMBER.length(), SPACE_POSITION = COUNTRY_CODE_LENGTH;

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if ((dstart < SPACE_POSITION && dend >= SPACE_POSITION) || (dstart == SPACE_POSITION && dend == SPACE_POSITION - 1)) {
                return DEFAULT_PHONE_NUMBER + " ";
            }

            if (dstart < COUNTRY_CODE_LENGTH && dend == COUNTRY_CODE_LENGTH) {
                return DEFAULT_PHONE_NUMBER;
            }

            if (dstart < COUNTRY_CODE_LENGTH || dend < COUNTRY_CODE_LENGTH) {
                return DEFAULT_PHONE_NUMBER + " ";
            }

            if (source.length() > 0 && source.charAt(0) == '0' && dstart == 0) {
                return "";
            }

            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }

            if (dest.length() - (dend - dstart) + source.length() > 13) {
                return "";
            }

            if (!dest.toString().startsWith(DEFAULT_PHONE_NUMBER)) {
                return DEFAULT_PHONE_NUMBER;
            }

            return null;
        }
    }

    //Handle validation and upload user data
    public void uploadData(View view) {
        String firstName = ((EditText) findViewById(R.id.firstName)).getText().toString().trim();
        String lastName = ((EditText) findViewById(R.id.lastName)).getText().toString().trim();
        String dob = dobPicker.getText().toString().trim();
        String nic = nicNo.getText().toString().trim();
        String mobileNumber = ((EditText) findViewById(R.id.mobileNumber)).getText().toString().trim();

        if (!isNICValid(nic)) {
            return;
        }

        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty() || nic.isEmpty() || mobileNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        checkNICExists(nic, new OnCheckExistsListener() {
            @Override
            public void onCheckCompleted(boolean nicExists) {
                if (nicExists) {
                    Toast.makeText(AddPersonalDetails.this, "NIC already exists", Toast.LENGTH_SHORT).show();
                } else {
                    checkMobileNumberExists(mobileNumber, new OnCheckExistsListener() {
                        @Override
                        public void onCheckCompleted(boolean mobileNumberExists) {
                            if (mobileNumberExists) {
                                Toast.makeText(AddPersonalDetails.this, "Mobile Number already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("AddPersonalDetails", "Uploading data...");
                                //Upload user details to Firebase
                                uploadUserDetailsToFirebase(firstName, lastName, dob, nic, mobileNumber);
                            }
                        }
                    });
                }
            }
        });
    }

    //Check if NIC exists in the database
    private void checkNICExists(String nic, OnCheckExistsListener listener) {
        mDatabase.child("users").orderByChild("nicNo").equalTo(nic).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onCheckCompleted(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                listener.onCheckCompleted(false);
            }
        });
    }

    //Check if Mobile Number exists in the database
    private void checkMobileNumberExists(String mobileNumber, OnCheckExistsListener listener) {
        mDatabase.child("users").orderByChild("mobileNo").equalTo(mobileNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onCheckCompleted(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                listener.onCheckCompleted(false);
            }
        });
    }

    // Listener interface for checking if NIC or Mobile Number exists
    private interface OnCheckExistsListener {
        void onCheckCompleted(boolean exists);
    }

    //Upload user details to Firebase
    private void uploadUserDetailsToFirebase(String firstName, String lastName, String dob, String nic, String mobileNumber) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();

            UsersData userDetails = new UsersData(firstName, lastName, dob, nic, mobileNumber, email);
            mDatabase.child("users").child(currentUser.getUid()).setValue(userDetails)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //Handle upload success
                            Toast.makeText(AddPersonalDetails.this, "Details uploaded successfully!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User data uploaded success");

                            Intent dashboardIntent = new Intent(AddPersonalDetails.this, Dashboard.class);
                            startActivity(dashboardIntent);
                            finish();
                        } else {
                            Toast.makeText(AddPersonalDetails.this, "Failed to upload details. Please try again.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to upload");
                        }
                    });
        } else {
            Log.e(TAG, "Failed to get current user.");
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
