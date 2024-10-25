package com.project.BeSafe;

import java.util.List;

public class PlacesResponse {
    public List<Result> results;

    public static class Result {
        public Geometry geometry;
        public String name;
        public String vicinity;
        public String place_id; // Add this line
        public String formatted_phone_number; // Add this line
    }

    public static class Geometry {
        public Location location;
    }

    public static class Location {
        public double lat;
        public double lng;
    }
}
