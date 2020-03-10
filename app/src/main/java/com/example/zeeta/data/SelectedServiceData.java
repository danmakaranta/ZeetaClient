package com.example.zeeta.data;

import com.firebase.geofire.GeoLocation;
import com.google.firebase.firestore.GeoPoint;

public class SelectedServiceData {
    private String id;//for the found staff id
    private GeoLocation geoLocation;//for the location data

    public SelectedServiceData(String id, GeoLocation geoLocation) {
        this.id = id;
        this.geoLocation = geoLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
