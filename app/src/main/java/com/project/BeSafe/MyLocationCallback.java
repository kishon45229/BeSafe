package com.project.BeSafe;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

public class MyLocationCallback extends LocationCallback {

    @Override
    public void onLocationResult(@NonNull LocationResult locationResult) {
        super.onLocationResult(locationResult);
        // Handle location updates here
        Location location = locationResult.getLastLocation();
        if (location != null) {
            // Process the received location
            // For example:
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            // Do something with latitude and longitude
        }
    }
}
