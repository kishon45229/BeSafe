package com.project.BeSafe;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapUtils {
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private Marker currentMarker;
    private GoogleMap mMap;
    private final Context context;
    private final EditText streetAddressEditText;
    private final EditText cityEditText;
    private final EditText districtEditText;

    public MapUtils(Context context, EditText streetAddressEditText, EditText cityEditText, EditText districtEditText) {
        this.context = context;
        this.streetAddressEditText = streetAddressEditText;
        this.cityEditText = cityEditText;
        this.districtEditText = districtEditText;
    }

    public void showMap(FragmentActivity activity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        } else {
            showMapInternal(activity);
        }
    }

    private void showMapInternal(FragmentActivity activity) {
        FrameLayout mapContainer = activity.findViewById(R.id.map_container);
        mapContainer.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.map_container, mapFragment).commit();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.getUiSettings().setMapToolbarEnabled(true);
                    // Set default location to Sri Lanka
                    LatLng sriLanka = new LatLng(7.8731, 80.7718);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 7));

                    // Add a marker click listener to allow users to select a location
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(@NonNull LatLng latLng) {
                            if (currentMarker != null) {
                                currentMarker.remove();
                            }
                            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                            getAddressFromLocation(latLng.latitude, latLng.longitude);

                            mapContainer.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                String city = address.getLocality();
                String district = address.getSubAdminArea();

                streetAddressEditText.setText(streetAddress);
                cityEditText.setText(city);
                districtEditText.setText(district);
            } else {
                Toast.makeText(context, "Unable to get address from location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder I/O Exception: " + e.getMessage());
            Toast.makeText(context, "Unable to get address from location", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMapInternal((FragmentActivity) context);
            } else {
                Toast.makeText(context, "Location permission is required to set location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
