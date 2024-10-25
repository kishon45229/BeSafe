package com.project.BeSafe;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class PlaceSearchUtils {
    private static final String TAG = "PlaceSearchUtils";
    private final String apiKey;
    private PlacesClient placesClient;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1; // Define your permission request code
    private final Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final PlaceSearchListener searchListener;

    public PlaceSearchUtils(Context context, PlaceSearchListener listener) {
        this.context = context;
        this.searchListener = listener;
        apiKey = getGooglePlacesAPIKey(context);
        if (apiKey != null) {
            Places.initialize(context, apiKey);
            placesClient = Places.createClient(context);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        } else {
            Log.e(TAG, "Google Places API key not found.");
        }
    }

    private String getGooglePlacesAPIKey(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            if (bundle != null) {
                return bundle.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void searchNearbyPlaces(String[] placeTypes) {
        if (apiKey == null) {
            Log.e(TAG, "Google Places API key not found.");
            return;
        }
        if (placeTypes == null || placeTypes.length == 0) {
            Log.e(TAG, "No place types provided for search");
            return;
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
                placesClient.findCurrentPlace(request).addOnSuccessListener((response) -> {
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        // Handle each nearby place
                        Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place search failed: " + apiException.getStatusCode());
                        if (searchListener != null) {
                            searchListener.onError("Place search failed: " + apiException.getStatusCode());
                        }
                    } else {
                        Log.e(TAG, "Place search failed: " + exception.getMessage());
                        if (searchListener != null) {
                            searchListener.onError("Place search failed: " + exception.getMessage());
                        }
                    }
                });
            } else {
                Log.e(TAG, "No location available");
                if (searchListener != null) {
                    searchListener.onError("No location available");
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get last location: " + e.getMessage());
            if (searchListener != null) {
                searchListener.onError("Failed to get last location: " + e.getMessage());
            }
        });
    }

    public interface PlaceSearchListener {
        void onPlacesFound(List<Place> places);

        void onError(String message);
    }
}