package com.example.zeeta;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.zeeta.models.User;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

class ClientLocation implements Parcelable {


    public static final Creator<ClientLocation> CREATOR = new Creator<ClientLocation>() {
        @Override
        public ClientLocation createFromParcel(Parcel in) {
            return new ClientLocation(in);
        }

        @Override
        public ClientLocation[] newArray(int size) {
            return new ClientLocation[size];
        }
    };
    private GeoPoint geoPoint;
    private @ServerTimestamp
    Date timeStamp;
    private User user;

    public ClientLocation(User user, GeoPoint geo_point, Date timestamp) {
        this.user = user;
        this.geoPoint = geo_point;
        this.timeStamp = timestamp;
    }

    public ClientLocation() {

    }

    public ClientLocation(Parcel in) {

        user = in.readParcelable(User.class.getClassLoader());
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ClientLocation{" +
                "geoPoint=" + geoPoint +
                ", timeStamp='" + timeStamp + '\'' +
                ", user=" + user +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
    }
}
