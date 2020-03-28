package com.example.zeeta.models;

import com.google.firebase.firestore.GeoPoint;

public class RequestInformation {
    private GeoPoint geoPoint;
    private String id;
    private String accepted;

    public RequestInformation(GeoPoint geoPoint, String id, String accepted) {
        this.geoPoint = geoPoint;
        this.id = id;
        this.accepted = accepted;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }
}
