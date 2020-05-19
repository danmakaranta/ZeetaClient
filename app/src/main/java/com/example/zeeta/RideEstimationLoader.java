package com.example.zeeta;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.Distance;

public class RideEstimationLoader extends AsyncTaskLoader<String> {
    private GeoPoint pickup, destination;
    private String TAG = "RIDEESTIMATION";
    private long distanceCovered;
    private GeoApiContext mDirectionApi = new GeoApiContext.Builder()
            .apiKey("AIzaSyB9nZYenhhs6M8MEXs4xqBYDmaiPpMP4mQ")
            .build();

    public RideEstimationLoader(Context context, GeoPoint pickup, GeoPoint destination) {
        super(context);
        this.pickup = pickup;
        this.destination = destination;
    }

    @Override
    public String loadInBackground() {
        long dist = calculateDistance(pickup, destination);
        int pricePerKm = 0;
        if (dist > 1) {
            pricePerKm = getFairPrice(dist);
        }
        String est = "N" + (dist / 1000) * pricePerKm;
        Log.d(TAG, "Price estimation " + est);
        return est;
    }

    private int getFairPrice(long dist) {
        int price = 0;

        if (dist <= 10) {
            price = 100;
            return price;
        } else if (dist <= 20) {
            price = 60;
            return price;
        } else if (dist <= 30) {
            price = 55;
            return price;
        } else if (dist <= 40) {
            price = 60;
            return price;
        } else if (dist <= 50) {
            price = 75;
            return price;
        } else {
            price = 90;
            return price;
        }

    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    private long calculateDistance(GeoPoint pickupL, GeoPoint dest) {
        Log.d("check", "check calculatedistance");

        com.google.maps.model.LatLng destinations = new com.google.maps.model.LatLng(
                dest.getLatitude(),
                dest.getLongitude()
        );

        DirectionsApiRequest directions = new DirectionsApiRequest(mDirectionApi);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        pickupL.getLatitude(),
                        pickupL.getLongitude()
                )
        );

        final Distance[] distance = new Distance[1];
        final long[] dista = new long[1];

        directions.destination(destinations).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {

                if (result.routes[0].legs[0].distance.inMeters > 5) {
                    distanceCovered = result.routes[0].legs[0].distance.inMeters;

                }
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: distanceCVREL:" + distanceCovered);
                Log.d(TAG, "calculateDirections: geocodedWayPointzz: " + result.geocodedWaypoints[0].toString());
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("CalculateDirectionsETA", "calculateDistance: Failed to get distance: " + e.getMessage());
            }
        });
        if (distanceCovered < 1) {
            Log.d(TAG, "calculateDirections: Recursively:");
            return calculateDistance(pickup, destination);
        } else {
            Log.d(TAG, "calculateDirections: distanceCVRET:" + distanceCovered);
            return distanceCovered;
        }


    }


}
