package com.example.zeeta;

import android.net.Uri;

public class ModelForImage {
    private Uri imageUri;
    private String title, description;

    public ModelForImage(Uri imageUri, String title, String description) {
        this.imageUri = imageUri;
        this.title = title;
        this.description = description;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
