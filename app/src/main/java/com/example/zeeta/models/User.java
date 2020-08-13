package com.example.zeeta.models;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {


    private String email;
    private String user_id;
    private String username;
    private String avatar;
    private String phoneNumber;
    private boolean newUser;
    private String rating;
    private double wallet;


    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();
        avatar = in.readString();
        phoneNumber = in.readString();
        newUser = in.readByte() != 0;
        rating = in.readString();
        wallet = in.readDouble();
    }


    public User(String email, String user_id, String username, String avatar, String phoneNumber, boolean newUser, String rating, double wallet) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
        this.newUser = newUser;
        this.rating = rating;
        this.wallet = wallet;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public User(String email, String user_id, String username, String avatar, String phoneNumber) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    public User() {

    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(user_id);
        dest.writeString(username);
        dest.writeString(avatar);
        dest.writeString(phoneNumber);
        dest.writeByte((byte) (newUser ? 1 : 0));
        dest.writeString(rating);
        dest.writeDouble(wallet);
    }
}

