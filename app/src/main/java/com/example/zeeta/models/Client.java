package com.example.zeeta.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Client implements Parcelable {


    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }


        @Override
        public Client[] newArray(int size) {
            return new Client[size];
        }
    };
    private String client_location;
    private String username;
    private String phoneNumber;

    public Client(String client_location, String username, String phoneNumber) {
        this.client_location = client_location;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    protected Client(Parcel in) {
        client_location = in.readString();
        username = in.readString();
        phoneNumber = in.readString();
    }

    public String getClient_location() {
        return client_location;
    }

    public void setClient_location(String client_location) {
        this.client_location = client_location;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @NonNull
    @Override
    public String toString() {
        return "Client{" +
                ", username='" + username + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", clientLocation='" + client_location + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }


}
