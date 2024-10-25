package com.project.BeSafe;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesApiService {
    @GET("nearbysearch/json")
    Call<PlacesResponse> getNearbyPlaces(
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("type") String type,
            @Query("keyword") String keyword,
            @Query("key") String apiKey
    );

    @GET("details/json")
    Call<PlaceDetailsResponse> getPlaceDetails(
            @Query("place_id") String placeId,
            @Query("key") String apiKey
    );
}
