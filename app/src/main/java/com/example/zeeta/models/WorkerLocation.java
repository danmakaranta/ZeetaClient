package com.example.zeeta.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class WorkerLocation implements Parcelable {

    public static final Creator<WorkerLocation> CREATOR = new Creator<WorkerLocation>() {
        @Override
        public WorkerLocation createFromParcel(Parcel in) {
            return new WorkerLocation(in);
        }

        @Override
        public WorkerLocation[] newArray(int size) {
            return new WorkerLocation[size];
        }
    };
    private GeoPoint geoPoint;
    private @ServerTimestamp
    Date timeStamp;
    private User user;
    private Boolean online;

    protected WorkerLocation(Parcel in) {
        user = in.readParcelable(User.class.getClassLoader());
    }

    public WorkerLocation(User user, GeoPoint geo_point, Date timestamp) {
        this.user = user;
        this.geoPoint = geo_point;
        this.timeStamp = timestamp;
    }

    public WorkerLocation(User user, GeoPoint geo_point, Date timestamp, boolean online) {
        this.user = user;
        this.geoPoint = geo_point;
        this.timeStamp = timestamp;
        this.online = online;
    }

    public WorkerLocation() {

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

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "WorkerLocation{" +
                "geoPoint=" + geoPoint +
                ", timeStamp='" + timeStamp + '\'' +
                ", user=" + user +
                ", onlieStatus=" + online +
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
