package com.project.BeSafe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EmergencyServiceFinder {
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    private static final String API_KEY = "API_KEY";
    private PlacesApiService placesApi;

    public EmergencyServiceFinder() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        placesApi = retrofit.create(PlacesApiService.class);
    }

    //Get nearby hospital details
    public void findNearbyHospital(double lat, double lng, int radius, String type, Callback<PlacesResponse> callback) {
        Call<PlacesResponse> call = placesApi.getNearbyPlaces(
                lat + "," + lng,
                radius,
                type,
                "hospital",
                API_KEY
        );
        call.enqueue(callback);
    }

    //Get nearby fire service details
    public void findNearbyFireService(double lat, double lng, int radius, String type, Callback<PlacesResponse> callback) {
        Call<PlacesResponse> call = placesApi.getNearbyPlaces(
                lat + "," + lng,
                radius,
                type,
                "fire, fire station, fire station department, fire and rescue",
                API_KEY
        );
        call.enqueue(callback);
    }

    //Get nearby police
    public void findNearbyPolice(double lat, double lng, int radius, String type, Callback<PlacesResponse> callback) {
        Call<PlacesResponse> call = placesApi.getNearbyPlaces(
                lat + "," + lng,
                radius,
                type,
                "police",
                API_KEY
        );
        call.enqueue(callback);
    }

    public void getPlaceDetails(String placeId, Callback<PlaceDetailsResponse> callback) {
        Call<PlaceDetailsResponse> call = placesApi.getPlaceDetails(placeId, API_KEY);
        call.enqueue(callback);
    }

}
