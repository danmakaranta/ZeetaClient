package com.example.zeeta.data;

import com.google.android.gms.maps.model.LatLng;

public class StaffFound {
    private String id;//staff id
    private LatLng location;
    private String profession;

    public StaffFound(String id, LatLng location, String profession) {
        this.id = id;
        this.location = location;
        this.profession = profession;
    }

    public StaffFound(String id, LatLng location) {
        this.id = id;
        this.location = location;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
