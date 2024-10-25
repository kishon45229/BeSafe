package com.project.BeSafe.ui.report_incident;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.BeSafe.DatePickerUtils;
import com.project.BeSafe.IncidentReport;
import com.project.BeSafe.LocationUtils;
import com.project.BeSafe.MapUtils;
import com.project.BeSafe.R;
import com.project.BeSafe.TimePickerUtils;
import com.project.BeSafe.WitnessDetailsManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportIncidentFragment extends Fragment {
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private EditText streetAddressEditText, cityEditText, userNameTextView, userMobileNoTextView, districtEditText, dobPicker, timePicker, injuredChildren, injuredAdults, childFatalities, adultFatalities, propertyDamage, description, additionalComments;
    private Spinner spinnerIncidentType;
    private DatabaseReference mDatabase;
    private MapUtils mapUtils;
    private LocationUtils locationUtils;
    private RadioGroup severityRadioGroup;
    private TextView selectedMediaTextView;
    private final ArrayList<Uri> selectedMediaUris = new ArrayList<>();
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100, MAX_MEDIA_SIZE_MB = 20;
    private static final int PICK_MEDIA_REQUEST = 1;
    private static final String TAG = "ReportIncidentFragment";

    public ReportIncidentFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reportincident, container, false);

        locationUtils = new LocationUtils(getActivity());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        spinnerIncidentType = view.findViewById(R.id.spinnerIncidentType);

        streetAddressEditText = view.findViewById(R.id.streetAddress);
        cityEditText = view.findViewById(R.id.city);
        districtEditText = view.findViewById(R.id.district);
        TextView setLocationButton = view.findViewById(R.id.setLocation);

        CheckBox setCurrentLocation = view.findViewById(R.id.setCurrentLocation);
        mapUtils = new MapUtils(getActivity(), streetAddressEditText, cityEditText, districtEditText);

        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_LOCATION);
                } else {
                    mapUtils.showMap(getActivity());
                }
            }
        });
        /*
        setLocationButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            } else {
                mapUtils.showMap(getActivity());
            }
        });*/

        setCurrentLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        });

        dobPicker = view.findViewById(R.id.date);
        //dobPicker.setOnClickListener(v -> DatePickerUtils.showDatePickerDialog(getActivity(), dobPicker));
        dobPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerUtils.showDatePickerDialog(getActivity(), dobPicker);
            }
        });

        timePicker = view.findViewById(R.id.time);
        //timePicker.setOnClickListener(v -> TimePickerUtils.showTimePickerDialog(getActivity(), timePicker));
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerUtils.showTimePickerDialog(getActivity(), timePicker);
            }
        });

        severityRadioGroup = view.findViewById(R.id.severityRadioBtnGroup);

        injuredChildren = view.findViewById(R.id.numInjuredChildren);
        injuredAdults = view.findViewById(R.id.numInjuredAdults);
        childFatalities = view.findViewById(R.id.numChildFatalities);
        adultFatalities = view.findViewById(R.id.numAdultFatalities);

        propertyDamage = view.findViewById(R.id.propertyDamage);

        description = view.findViewById(R.id.description);

        Button selectMediaButton = view.findViewById(R.id.buttonAttach);
        selectedMediaTextView = view.findViewById(R.id.selectedMediaTextView);

        selectMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                else {
                    openMediaPicker();

                }
            }
        });

        /*
        selectMediaButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                Log.d(TAG,"permisson");
            }
            else {
                Log.d(TAG,"run");
                openMediaPicker();

            }
        });*/

        WitnessDetailsManager witnessDetailsManager = new WitnessDetailsManager();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        CheckBox witnessDetailsCheckBox = view.findViewById(R.id.witnessDetailsCheckBox);
        userNameTextView = view.findViewById(R.id.name);
        userMobileNoTextView = view.findViewById(R.id.contactDetails);

        witnessDetailsManager.loadWitnessDetails(witnessDetailsCheckBox, userNameTextView, userMobileNoTextView, currentUser);

        additionalComments = view.findViewById(R.id.additionalComments);

        Button uploadButton = view.findViewById(R.id.submitButton);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    // Proceed with uploading the data
                    uploadDataToDatabase();
                }
                else {
                    // Show a toast message indicating validation failed
                    Toast.makeText(getActivity(), "Please correct the errors before submitting", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*
        uploadButton.setOnClickListener(v -> {
            if (validateFields()) {
                // Proceed with uploading the data
                uploadDataToDatabase();
            }
            else {
                // Show a toast message indicating validation failed
                Toast.makeText(getActivity(), "Please correct the errors before submitting", Toast.LENGTH_SHORT).show();
            }
        });*/

        return view;
    }

    //Handle location and storage permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openMediaPicker();
            } else {
                Toast.makeText(getActivity(), "Permission required to access media files", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(getActivity(), "Permission required to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openMediaPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/* video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        mediaPickerLauncher.launch(intent);
    }

    //Select media
    private final ActivityResultLauncher<Intent> mediaPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri mediaUri = data.getClipData().getItemAt(i).getUri();
                            selectedMediaUris.add(mediaUri);
                        }
                    } else if (data.getData() != null) {
                        Uri mediaUri = data.getData();
                        selectedMediaUris.add(mediaUri);
                    }

                    if (!validateMediaSize()) {
                        selectedMediaUris.clear();
                        Toast.makeText(getActivity(), "Selected media exceeds the 20MB limit. Please select smaller files.", Toast.LENGTH_SHORT).show();
                    } else {
                        updateSelectedMediaTextView();
                    }
                }
            }
    );

    //Update media name
    private void updateSelectedMediaTextView() {
        StringBuilder sb = new StringBuilder();
        for (Uri uri : selectedMediaUris) {
            sb.append(getFileName(uri)).append("\n");
        }
        selectedMediaTextView.setText(sb.toString());
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1 && result != null) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private boolean validateMediaSize() {
        long totalSize = 0;
        for (Uri uri : selectedMediaUris) {
            totalSize += getFileSize(uri);
        }
        totalSize = totalSize / (1024 * 1024);
        if (totalSize > MAX_MEDIA_SIZE_MB) {
            Toast.makeText(getActivity(), "Selected media exceeds the 20MB limit", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private long getFileSize(Uri uri) {
        Cursor returnCursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        long size = returnCursor.getLong(sizeIndex);
        returnCursor.close();
        return size;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        } else {
            locationUtils.startLocationUpdates(new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            updateLocationFields(location);
                        }
                    }
                }
            }, getActivity());
        }
    }

    private void stopLocationUpdates() {
        locationUtils.stopLocationUpdates();
    }

    private void updateLocationFields(Location location) {
        if (location != null) {
            Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String streetAddress = address.getThoroughfare();
                    String city = address.getLocality();
                    String district = address.getSubAdminArea();

                    if (streetAddress != null) {
                        streetAddressEditText.setText(streetAddress);
                    }
                    if (city != null) {
                        cityEditText.setText(city);
                    }
                    if (district != null) {
                        districtEditText.setText(district);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (spinnerIncidentType.getSelectedItemPosition() == 0) {
            Toast.makeText(getActivity(), "Please select an incident type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate street address
        if (streetAddressEditText.getText().toString().trim().isEmpty()) {
            streetAddressEditText.setError("Street address is required");
            isValid = false;
        }

        // Validate city
        if (cityEditText.getText().toString().trim().isEmpty()) {
            cityEditText.setError("City is required");
            isValid = false;
        }

        // Validate district
        if (districtEditText.getText().toString().trim().isEmpty()) {
            districtEditText.setError("District is required");
            isValid = false;
        }

        // Validate date of birth (DOB)
        if (dobPicker.getText().toString().trim().isEmpty()) {
            dobPicker.setError("Date of birth is required");
            isValid = false;
        }

        // Validate time
        if (timePicker.getText().toString().trim().isEmpty()) {
            timePicker.setError("Time is required");
            isValid = false;
        }

        int selectedSeverityId = severityRadioGroup.getCheckedRadioButtonId();
        if (selectedSeverityId == -1) {
            Toast.makeText(getActivity(), "Please select the severity of the incident", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate number of injured children
        if (injuredChildren.getText().toString().trim().isEmpty()) {
            injuredChildren.setError("Number of injured children is required");
            isValid = false;
        }

        // Validate number of injured adults
        if (injuredAdults.getText().toString().trim().isEmpty()) {
            injuredAdults.setError("Number of injured adults is required");
            isValid = false;
        }

        // Validate number of child fatalities
        if (childFatalities.getText().toString().trim().isEmpty()) {
            childFatalities.setError("Number of child fatalities is required");
            isValid = false;
        }

        // Validate number of adult fatalities
        if (adultFatalities.getText().toString().trim().isEmpty()) {
            adultFatalities.setError("Number of adult fatalities is required");
            isValid = false;
        }

        // Validate property damage
        if (propertyDamage.getText().toString().trim().isEmpty()) {
            propertyDamage.setError("Property damage is required");
            isValid = false;
        }

        // Validate description
        if (description.getText().toString().trim().isEmpty()) {
            description.setError("Description is required");
            isValid = false;
        }

        if (userNameTextView.getText().toString().trim().isEmpty()) {
            userNameTextView.setError("Name is required");
            isValid = false;
        }

        if (userMobileNoTextView.getText().toString().trim().isEmpty()) {
            userMobileNoTextView.setError("Contact details are required");
            isValid = false;
        }

        return isValid;
    }

    private void uploadDataToDatabase() {
        if (selectedMediaUris.isEmpty()) {
            Toast.makeText(getActivity(), "No media selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String incidentType = spinnerIncidentType.getSelectedItem().toString().trim();
        String streetAddress = streetAddressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String district = districtEditText.getText().toString().trim();
        String dob = dobPicker.getText().toString().trim();
        String time = timePicker.getText().toString().trim();

        int selectedSeverityId = severityRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedSeverityRadioButton = requireView().findViewById(selectedSeverityId);
        String severity = selectedSeverityRadioButton != null ? selectedSeverityRadioButton.getText().toString().trim() : "";

        String injuredChildrenCount = injuredChildren.getText().toString().trim();
        String injuredAdultsCount = injuredAdults.getText().toString().trim();
        String childFatalitiesCount = childFatalities.getText().toString().trim();
        String adultFatalitiesCount = adultFatalities.getText().toString().trim();
        String propertyDamageValue = propertyDamage.getText().toString().trim();
        String descriptionValue = description.getText().toString().trim();

        String userName = userNameTextView.getText().toString().trim();
        String userMobileNo = userMobileNoTextView.getText().toString().trim();

        String additionalComment = additionalComments.getText().toString().trim();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String incidentId = mDatabase.child("incidents").push().getKey();

            IncidentReport incidentReport = new IncidentReport(
                    userId, streetAddress, city, district, dob, time, incidentType, severity,
                    injuredChildrenCount, injuredAdultsCount, childFatalitiesCount,
                    adultFatalitiesCount, propertyDamageValue, descriptionValue, userName,
                    userMobileNo, additionalComment
            );

            if (incidentId != null) {
                mDatabase.child("incidents").child(incidentId).setValue(incidentReport)
                        .addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                //Toast.makeText(getActivity(), "Incident report submitted successfully", Toast.LENGTH_SHORT).show();
                                if (selectedMediaUris.isEmpty()) {
                                    Toast.makeText(getActivity(), "Please attach atleast one photo/video of the incident!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("incident_media").child(incidentId);
                                for (Uri uri : selectedMediaUris) {
                                    String fileName = getFileName(uri);
                                    StorageReference fileRef = storageReference.child(fileName);
                                    fileRef.putFile(uri)
                                            .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                                mDatabase.child("incidents").child(incidentId).child("media").push().setValue(downloadUri.toString())
                                                        .addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getActivity(), "Report uploaded successfully", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getActivity(), "Failed to submit incident report", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }))
                                            .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to upload media file: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                                clearForm();
                            }
                            else {
                                Toast.makeText(getActivity(), "Failed to submit incident report", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        else {
            Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        streetAddressEditText.setText("");
        cityEditText.setText("");
        districtEditText.setText("");
        dobPicker.setText("");
        timePicker.setText("");
        spinnerIncidentType.setSelection(0);
        severityRadioGroup.clearCheck();
        injuredChildren.setText("");
        injuredAdults.setText("");
        childFatalities.setText("");
        adultFatalities.setText("");
        propertyDamage.setText("");
        description.setText("");
        userNameTextView.setText("");
        userMobileNoTextView.setText("");
        additionalComments.setText("");
    }
}

