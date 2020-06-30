package com.example.zeeta;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.example.zeeta.data.GeneralJobData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Objects;

import androidx.annotation.NonNull;

public class RideInformaitonLoader extends AsyncTaskLoader<GeneralJobData> {


    GeneralJobData journeyInfo = null;
    private DocumentReference rideInformation = null;
    private GeoPoint pickupLocation;
    private String customerName = "";
    private GeoPoint destination;
    private String customerID;
    private String customerPhoneNumber;
    private Long distanceCovered;
    private boolean arrived;
    private String status;
    private Long amount;
    private String accepted;
    private Boolean started;
    private Boolean ended;
    private @ServerTimestamp
    Timestamp timeStamp;

    public RideInformaitonLoader(Context context) {
        super(context);
    }


    public GeneralJobData getRideInformation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            rideInformation = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection("Request").document("ongoing");

        }

        if (rideInformation != null) {
            rideInformation.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();

                        pickupLocation = doc.getGeoPoint("serviceLocation");
                        destination = doc.getGeoPoint("destination");
                        customerID = doc.getString("serviceID");
                        distanceCovered = doc.getLong("distanceCovered");
                        customerPhoneNumber = doc.getString("customerPhoneNumber");
                        amount = doc.getLong("amount");
                        started = doc.getBoolean("started");
                        accepted = doc.getString("accepted");
                        ended = doc.getBoolean("ended");
                        timeStamp = doc.getTimestamp("timeStamp");
                        Log.d("data valid", "data:.." + timeStamp);
                        try {// nothing more but to slow down execution a bit to get results before proceeding
                            Thread.sleep(2000);
                        } catch (InterruptedException excp) {
                            excp.printStackTrace();
                        }

                        journeyInfo = new GeneralJobData(pickupLocation, destination,
                                null, customerID, customerPhoneNumber, customerName,
                                distanceCovered, amount, accepted, started, ended, "ser", timeStamp, status, (long) 0, false, false);

                    }
                }
            });
        }
        if (journeyInfo != null) {
            return journeyInfo;
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.d("recursion error", "recursive error: " + e.getMessage());
            }
            return getRideInformation();
        }

    }

    @Override
    public GeneralJobData loadInBackground() {
        return getRideInformation();
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}
