package com.example.zeeta;

import com.google.firebase.database.Exclude;

public class Upload {
    private String mName;
    private String mImageUrl;
    private String imageDescription;
    private String mKey;

    public Upload(String mName, String mImageUrl, String imageDescription, String mKey) {
        this.mName = mName;
        this.mImageUrl = mImageUrl;
        this.imageDescription = imageDescription;
        this.mKey = mKey;
    }

    public Upload(String mName, String mImageUrl, String imageDescription) {
        this.mName = mName;
        this.mImageUrl = mImageUrl;
        this.imageDescription = imageDescription;

        if (mName.trim().equals("")) {
            mName = "No Name";
        }
        if (imageDescription.trim().equals("")) {
            imageDescription = "No description";
        }

    }

    public Upload() {
        //empty constructor needed
    }

    @Exclude
    public String getmKey() {
        return mKey;
    }

    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

}
