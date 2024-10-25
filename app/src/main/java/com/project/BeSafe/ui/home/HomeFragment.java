package com.project.BeSafe.ui.home;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.BeSafe.EmergencyServiceFinder;
import com.project.BeSafe.LocationUtils;
import com.project.BeSafe.PlaceDetailsResponse;
import com.project.BeSafe.PlacesAdapter;
import com.project.BeSafe.PlacesResponse;
import com.project.BeSafe.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final int INITIAL_VISIBLE_ITEMS = 2;
    private DatabaseReference mDatabase;
    private TextView currentLocation;
    private LocationUtils locationUtils;
    private EmergencyServiceFinder emergencyServiceFinder;
    private RecyclerView servicesRecyclerView;
    private PlacesAdapter servicesAdapter;
    private List<PlacesResponse.Result> allServices;
    private Spinner serviceTypeSpinner;
    private String selectedServiceType;
    Button loadMoreButton;
    private LocationCallback locationCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        currentLocation = view.findViewById(R.id.myLocation);
        servicesRecyclerView = view.findViewById(R.id.servicesRecyclerView);
        serviceTypeSpinner = view.findViewById(R.id.serviceTypeSpinner);

        //Setup RecyclerView
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        servicesAdapter = new PlacesAdapter(getActivity(), new ArrayList<>());
        servicesRecyclerView.setAdapter(servicesAdapter);

        //Initialize LocationUtils and EmergencyServiceFinder
        locationUtils = new LocationUtils(getActivity());
        emergencyServiceFinder = new EmergencyServiceFinder();

        //Setup service type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.service_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(adapter);
        serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedServiceType = parent.getItemAtPosition(position).toString();
                //Fetch services based on selected type
                fetchNearbyServices();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        //Get current Firebase user and retrieve user data
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String firstName = snapshot.child("firstName").getValue(String.class);
                        if (firstName != null) {
                            TextView username = view.findViewById(R.id.welcomeUser);
                            username.setText("Hi, " + firstName);
                        } else {
                            Log.d(TAG, "First name is null");
                        }
                    } else {
                        Log.e(TAG, "User data snapshot does not exist");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        } else {
            Log.e(TAG, "No current user");
        }

        //Handle Load More button
        loadMoreButton = view.findViewById(R.id.loadMoreButton);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllServices();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Start location updates when fragment starts
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        //Start location updates using LocationUtils
        locationCallback = new MyLocationCallback();
        locationUtils.startLocationUpdates(locationCallback, getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        //Stop location updates when fragment stops
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        //Stop location updates using LocationUtils
        locationUtils.stopLocationUpdates();
    }

    //Handle current location of the user
    private void updateCurrentLocationText(Location location) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0);
                if (addressString != null) {
                    currentLocation.setText(addressString);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed", e);
        }
    }

    //Handle location updates
    private class MyLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (location != null) {
                    updateCurrentLocationText(location);
                    fetchNearbyServices();
                } else {
                    currentLocation.setText("Location unavailable");
                }
            }
        }
    }

    private void fetchNearbyServices() {
        if (selectedServiceType == null || selectedServiceType.isEmpty()) {
            return;
        }

        locationUtils.getLastKnownLocation(getActivity(), location -> {
            if (location == null) {
                Log.e(TAG, "Location is null");
                return;
            }

            //Determine service type and make API call to fetch nearby services
            String type;

            if (Objects.equals(selectedServiceType, "Police Station")) {
                emergencyServiceFinder.findNearbyPolice(location.getLatitude(), location.getLongitude(), 5000, "police", new Callback<PlacesResponse>() {
                    @Override
                    public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                        if (response.isSuccessful()) {
                            PlacesResponse placesResponse = response.body();
                            if (placesResponse != null && placesResponse.results != null) {
                                allServices = filterOutUnwantedPlaces(placesResponse.results);
                                sortPlacesByDistance(allServices, location);
                                displayInitialServices();
                            }
                        } else {
                            Log.e(TAG, "Places API response not successful");
                        }
                    }

                    @Override
                    public void onFailure(Call<PlacesResponse> call, Throwable t) {
                        Log.e(TAG, "Places API request failed", t);
                    }
                });
            }

            if (Objects.equals(selectedServiceType, "Fire Station")) {
                emergencyServiceFinder.findNearbyFireService(location.getLatitude(), location.getLongitude(), 5000, "fire station", new Callback<PlacesResponse>() {
                    @Override
                    public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                        if (response.isSuccessful()) {
                            PlacesResponse placesResponse = response.body();
                            if (placesResponse != null && placesResponse.results != null) {
                                allServices = filterOutUnwantedPlaces(placesResponse.results);
                                sortPlacesByDistance(allServices, location);
                                displayInitialServices();
                            }
                        } else {
                            Log.e(TAG, "Places API response not successful");
                        }
                    }

                    @Override
                    public void onFailure(Call<PlacesResponse> call, Throwable t) {
                        Log.e(TAG, "Places API request failed", t);
                    }
                });
            }

            if (Objects.equals(selectedServiceType, "Hospital")) {
                emergencyServiceFinder.findNearbyHospital(location.getLatitude(), location.getLongitude(), 5000, "hospital", new Callback<PlacesResponse>() {
                    @Override
                    public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                        if (response.isSuccessful()) {
                            PlacesResponse placesResponse = response.body();
                            if (placesResponse != null && placesResponse.results != null) {
                                allServices = filterOutUnwantedPlaces(placesResponse.results);
                                sortPlacesByDistance(allServices, location);
                                displayInitialServices();
                            }
                        } else {
                            Log.e(TAG, "Places API response not successful");
                        }
                    }

                    @Override
                    public void onFailure(Call<PlacesResponse> call, Throwable t) {
                        Log.e(TAG, "Places API request failed", t);
                    }
                });
            }
        });
    }

    //Add place to adapter with updated details
    private void fetchPlaceDetails(PlacesResponse.Result place) {
        emergencyServiceFinder.getPlaceDetails(place.place_id, new Callback<PlaceDetailsResponse>() {
            @Override
            public void onResponse(Call<PlaceDetailsResponse> call, Response<PlaceDetailsResponse> response) {
                if (response.isSuccessful()) {
                    PlaceDetailsResponse detailsResponse = response.body();
                    if (detailsResponse != null) {
                        place.formatted_phone_number = detailsResponse.result.formatted_phone_number;
                    }
                    servicesAdapter.addPlace(place);
                } else {
                    Log.e(TAG, "Place details API response not successful");
                }
            }

            @Override
            public void onFailure(Call<PlaceDetailsResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching place details", t);
            }
        });
    }

    //Handle list to filter unwanted names
    private List<PlacesResponse.Result> filterOutUnwantedPlaces(List<PlacesResponse.Result> places) {
        List<PlacesResponse.Result> filteredPlaces = new ArrayList<>();
        for (PlacesResponse.Result place : places) {
            String placeName = place.name.toLowerCase();
            if (!placeName.contains("medical center") &&
                    !placeName.contains("clinic center") &&
                    !placeName.contains("medical centre") &&
                    !placeName.contains("herbal cure") &&
                    !placeName.contains("nursing center") &&
                    !placeName.contains("unit") &&
                    !placeName.contains("ayurvedic") &&
                    !placeName.contains("ayurveda") &&
                    !placeName.contains("dispensary") &&
                    !placeName.contains("medi") &&
                    !placeName.contains("family") &&
                    !placeName.contains("home") &&
                    !placeName.contains("care") &&
                    !placeName.contains("wellness") &&
                    !placeName.contains("health service") &&
                    !placeName.contains("channeling center")) {
                filteredPlaces.add(place);
            }
        }
        return filteredPlaces;
    }

    //Sort places by distance from current location
    private void sortPlacesByDistance(List<PlacesResponse.Result> places, Location currentLocation) {
        Collections.sort(places, (place1, place2) -> {
            double distanceToPlace1 = calculateDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    place1.geometry.location.lat, place1.geometry.location.lng
            );
            double distanceToPlace2 = calculateDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    place2.geometry.location.lat, place2.geometry.location.lng
            );
            return Double.compare(distanceToPlace1, distanceToPlace2);
        });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    private void displayInitialServices() {
        servicesAdapter.clearPlaces();
        List<PlacesResponse.Result> initialServices = allServices.size() > INITIAL_VISIBLE_ITEMS
                ? allServices.subList(0, INITIAL_VISIBLE_ITEMS)
                : allServices;
        for (PlacesResponse.Result place : initialServices) {
            fetchPlaceDetails(place);
        }
        if (allServices.size() > INITIAL_VISIBLE_ITEMS) {
            loadMoreButton.setVisibility(View.VISIBLE);
        } else {
            loadMoreButton.setVisibility(View.GONE);
        }
    }

    //Fetch details for remaining services
    private void showAllServices() {
        for (PlacesResponse.Result place : allServices.subList(INITIAL_VISIBLE_ITEMS, allServices.size())) {
            fetchPlaceDetails(place);
        }
        loadMoreButton.setVisibility(View.GONE);
    }
}
