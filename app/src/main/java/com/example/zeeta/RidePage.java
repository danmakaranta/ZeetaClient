package com.example.zeeta;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zeeta.data.GeneralJobData;
import com.example.zeeta.models.PolylineData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class RidePage extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String TAG = "RIDE_PAGE";
    private static final float DEFAULT_ZOOM = 15f;
    private static final long LOCATION_UPDATE_INTERVAL = 3000;
    public Criteria criteria;
    public String bestProvider;
    int PERMISSION_ALL = 1;
    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE};
    Location pickupLocation, serviceProviderLocation, destination;
    Button pickupRiderBtn;
    private String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE};
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted = true;
    private GeoApiContext mGeoApiContext;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LoaderManager loaderManager;
    private Button endRide;
    private GeneralJobData journeyInfo;
    Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    Ringtone ringtone;
    private TextView wait_timer;
    private TextView waitingTxt;
    private Button callDriver;
    private Button cancelRideBtn;
    private DocumentReference rideInformation = null;
    private DatabaseReference ref = null;
    private CountDownTimer waitCountDownTimer;
    private boolean timerRunning = false;
    private long timeInMillis = 300000;
    private DocumentReference clientRideRequest;
    private MarkerOptions markerOption;
    private MarkerOptions driverMarkerOption;
    private Marker driverMarker;
    private boolean arrivalNotification = false;
    private boolean startedJourneyNotification = false;
    private boolean endedJourneyNotification = false;
    private double amountToBePaid;
    private boolean passengerCanceled = false;
    private boolean callbackPresent = false;
    private Handler mHandler;
    private Runnable mRunnable;
    private String locality;
    private Handler locationHandler = new Handler();
    private Runnable locationRunnable;
    private Dialog paymentOptionsDialog;
    private ProgressDialog cancelRideProgressDialog;

    public static boolean callPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_page);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        callDriver = (Button) findViewById(R.id.call_driver);
        cancelRideBtn = findViewById(R.id.cancel_ride);
        wait_timer = findViewById(R.id.wait_timer);
        waitingTxt = findViewById(R.id.waittxt);
        wait_timer.setVisibility(View.INVISIBLE);
        waitingTxt.setVisibility(View.INVISIBLE);
        markerOption = new MarkerOptions();
        driverMarkerOption = new MarkerOptions();

        cancelRideProgressDialog = new ProgressDialog(this);
        cancelRideProgressDialog.setMessage("Cancelling Ride....");

        serviceProviderLocation = new Location(LocationManager.GPS_PROVIDER);
        pickupLocation = new Location(LocationManager.GPS_PROVIDER);
        destination = new Location(LocationManager.GPS_PROVIDER);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
        ringtone.setStreamType(AudioManager.STREAM_RING);

        new CountDownTimer(1000, 3000) {
            @Override
            public void onTick(long millisUntilFinished) {
                journeyInfo = (GeneralJobData) getIntent().getParcelableExtra("RideData");
                locality = getIntent().getStringExtra("locality");
                assert journeyInfo != null;
                double workerLongitude = getIntent().getDoubleExtra("servicePLongitude", 0.0);
                double workerLatitude = getIntent().getDoubleExtra("servicePLatitude", 0.0);
                Log.d(TAG, "ID from intent: Location " + workerLatitude + ", " + workerLongitude);
                LatLng spLatLng = new LatLng(workerLatitude, workerLongitude);
                serviceProviderLocation.setLongitude(workerLongitude);
                serviceProviderLocation.setLatitude(workerLatitude);
                Log.d(TAG, "ID from intent: Location " + serviceProviderLocation);
                double pickupLongitude = getIntent().getDoubleExtra("pickupLongitude", 0.0);
                double pickupLatitude = getIntent().getDoubleExtra("pickupLatitude", 0.0);
                pickupLocation.setLatitude(pickupLatitude);
                pickupLocation.setLongitude(pickupLongitude);
                double destinationLongitude = getIntent().getDoubleExtra("destinationLongitude", 0.0);
                double destinationLatitude = getIntent().getDoubleExtra("destinationLatitude", 0.0);
                destination.setLongitude(destinationLongitude);
                destination.setLatitude(destinationLatitude);
                amountToBePaid = getIntent().getDoubleExtra("amountToBePaid", 0.0);

                clientRideRequest = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(Objects.requireNonNull(journeyInfo.getServiceID())).collection("Request").document("ongoing");
            }

            @Override
            public void onFinish() {
                if (serviceProviderLocation != null) {
                    new getDeviceLocationAsync().execute();
                    listenforUpdateAndRespond();
                }
            }
        }.start();

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_directions_api_key))
                    .build();
        }

        cancelRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                passengerCanceled = true;
                cancelRideProgressDialog.show();
                cancelRide();
            }
        });

        callDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!callPermissions(RidePage.this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(RidePage.this, PERMISSIONS, PERMISSION_ALL);
                }
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", journeyInfo.getPhoneNumber(), null));
                startActivity(intent);
            }
        });


    }

    private void listenforUpdateAndRespond() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            rideInformation = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(journeyInfo.getServiceID()).collection("Request").document("ongoing");
        }

        clientRideRequest.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                if (documentSnapshot.exists()) {
                    boolean canceledRide = documentSnapshot.getBoolean("cancelRide");
                    boolean arrived = documentSnapshot.getBoolean("arrived");
                    boolean journeyStarted = documentSnapshot.getBoolean("started");
                    boolean journeyEnded = documentSnapshot.getBoolean("ended");

                    try {// nothing more but to slow down execution a bit to get results before proceeding
                        Thread.sleep(2000);
                    } catch (InterruptedException excp) {
                        excp.printStackTrace();
                    }
                    if (canceledRide && !passengerCanceled) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(RidePage.this);
                        builder.setMessage("We are SORRY, your driver canceled, please choose another vehicle?")
                                .setCancelable(false);
                        final AlertDialog alert = builder.create();

                        new CountDownTimer(3000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                alert.show();
                                rideInformation.update("status", "Canceled").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            cancelRide();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFinish() {

                            }
                        }.start();
                    }
                    if (arrived && !arrivalNotification) {
                        arrivalNotification = true;
                        notifyRider();

                    } else if (journeyStarted && !startedJourneyNotification) {
                        startedJourneyNotification = true;
                        cancelRideBtn.setEnabled(false);
                        stopTimer();
                        Toast.makeText(RidePage.this, "Your journey has started.", Toast.LENGTH_LONG).show();
                    }

                    if (journeyEnded && !endedJourneyNotification) {
                        endedJourneyNotification = true;
                        new CountDownTimer(3000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                Toast.makeText(RidePage.this, "You have arrived your destination.", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFinish() {
                                finish();
                            }
                        }.start();

                    }

                }
            }
        });

    }


    private void paymentOptions() {


    }

    private void payWithCard() {
        paymentOptionsDialog.dismiss();
        startActivity(new Intent(getApplicationContext(), CreditCardLayout.class));
    }

    private void notifyRider() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(RidePage.this);
        builder.setMessage("Your driver has arrived")
                .setCancelable(false);
        final AlertDialog alert = builder.create();
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                alert.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.setVolume((float) 1.0);
                }
                ringtone.play();
            }

            @Override
            public void onFinish() {
                alert.dismiss();
                if (ringtone.isPlaying()) {
                    ringtone.stop();
                }
            }
        }.start();
        wait_timer.setVisibility(View.VISIBLE);
        waitingTxt.setVisibility(View.VISIBLE);
        startTimer();
    }


    private void startTimer() {
        wait_timer.setVisibility(View.VISIBLE);
        waitCountDownTimer = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeInMillis = millisUntilFinished;
                updateTime();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                timeInMillis = 300000;
                waitingTxt.setText("Waiting fee added!");
                amountToBePaid = amountToBePaid + 100;
                Toast.makeText(RidePage.this, "A waiting fee has been added!", Toast.LENGTH_LONG).show();
            }
        }.start();
        timerRunning = true;
    }

    private void updateTime() {
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;

        String timeformated = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        wait_timer.setText(timeformated);
    }

    private void stopTimer() {
        if (timerRunning) {
            waitCountDownTimer.cancel();
            timerRunning = false;
            timeInMillis = 300000;
            //payWaitingFee = false;
        }
        wait_timer.setVisibility(View.INVISIBLE);
        waitingTxt.setVisibility(View.INVISIBLE);
    }

    private void cancelRide() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            rideInformation = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(journeyInfo.getServiceID()).collection("Request").document("ongoing");
        }

        DocumentReference customersJobDataOncloud = FirebaseFirestore.getInstance()
                .collection("Customers").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection("JobData").document(journeyInfo.getServiceID());

        rideInformation.update("cancelRide", true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    rideInformation.update("status", "Canceled");
                    customersJobDataOncloud.set((new GeneralJobData(journeyInfo.getServiceLocation(), journeyInfo.getDestination(), null, journeyInfo.getServiceID(),
                            journeyInfo.getPhoneNumber(), journeyInfo.getName(), (long) 0, (long) 0, "Accepted",
                            false, false, journeyInfo.getServiceRendered(), journeyInfo.getTimeStamp(), "Completed", (long) 0, true, true, "Cash"))).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (cancelRideProgressDialog.isShowing()) {
                                    cancelRideProgressDialog.dismiss();
                                }
                                finish();
                            }
                        }
                    });
                }
            }
        });

    }

    //for adding a custom marker, in Zeeta's case its a sign of a worker going in the direction
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void initMap() {// for initializing the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.rideMap);
        assert mapFragment != null;
        mapFragment.getMapAsync(RidePage.this);
    }


    private void calculateDirections(Location fromLocation, GeoPoint gp) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                gp.getLatitude(),
                gp.getLongitude()
        );


        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        fromLocation.getLatitude(),
                        fromLocation.getLongitude()
                )
        );

        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                Log.d(TAG, "onResult: successfully retrieved directions.");
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());
            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if (mPolyLinesData.size() > 0) {
                    for (PolylineData polylineData : mPolyLinesData) {
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }

                double duration = 999999999;
                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(R.color.blue2);
                    polyline.setClickable(true);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                    // highlight the fastest route and adjust camera
                    double tempDuration = route.legs[0].duration.inSeconds;
                    if (tempDuration < duration) {
                        duration = tempDuration;
                        onPolylineClick(polyline);
                    }


                }
            }
        });
    }

    private void removeTripMarkers() {
        for (Marker marker : mTripMarkers) {
            marker.remove();
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for (PolylineData polylineData : mPolyLinesData) {
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {

                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.blue2));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );
                //Marker driverMarker = mMap.addMarker(driverMarkerOption);
                //Marker marker = mMap.addMarker(markerOption);
                //marker.showInfoWindow();
                driverMarker.showInfoWindow();
            } else {
                polylineData.getPolyline().setColor(R.color.darkGrey);
                polylineData.getPolyline().setZIndex(0);
            }
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnPolylineClickListener(this);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        initMap();
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Request For Location Permission")
                        .setMessage("This app is requesting for a Location permission. Allow?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(RidePage.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }


    private void showPointerOnMap(final double latitude, final double longitude) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.rideMap);
        assert mapFragment != null;
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng latLng = new LatLng(latitude, longitude);
                mMap = googleMap;
                /*driverMarkerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.tdcar))
                        .position(latLng).title("Driver");*/

                driverMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tdcar))
                        .anchor(0.5f, 0.5f)
                        .rotation(120)
                        .title("Driver"));

                // googleMap.addMarker(driverMarkerOption);
                //googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                // Updates the location and zoom of the MapView
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                googleMap.moveCamera(cameraUpdate);
            }
        });
    }


    private void showPassengerOnMap(final double latitude, final double longitude) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.rideMap);
        assert mapFragment != null;
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng latLng = new LatLng(latitude, longitude);
                mMap = googleMap;
               /* mMap.addMarker(markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.passenger))
                        .anchor(0.0f, 1.0f)
                        .position(latLng).title("You"));*/
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.passenger))
                        .anchor(0.0f, 0.0f)
                        .title("You")).showInfoWindow();

                //googleMap.addMarker(markerOption);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);

            }
        });
    }

    private void updateMarkerRunnable() {
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        if (!callbackPresent) {
            locationHandler.postDelayed(locationRunnable = new Runnable() {
                @Override
                public void run() {
                    updateMarker();
                    locationHandler.postDelayed(locationRunnable, 3000);
                }
            }, 3000);
            callbackPresent = true;
        }


    }

    private void updateMarker() {

        DocumentReference locationRef;

        locationRef = FirebaseFirestore.getInstance()
                .collection(locality)
                .document(journeyInfo.getServiceID());

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getGeoPoint("geoPoint") != null) {
                        GeoPoint gp = task.getResult().getGeoPoint("geoPoint");
                        assert gp != null;
                        LatLng latLng = new LatLng(gp.getLatitude(), gp.getLongitude());
                        driverMarkerOption.position(latLng);
                    }
                }

            }
        });

    }

    @SuppressLint("StaticFieldLeak")
    public class getDeviceLocationAsync extends AsyncTask<String, String, String> {
        public double lati = 0.0;
        public double longi = 0.0;

        public LocationManager mLocationManager;

        @Override
        protected void onPreExecute() {
            checkLocationPermission();
        }

        @Override
        protected void onPostExecute(String s) {
            //move camera to current location on map
            if (serviceProviderLocation != null) {
                showPointerOnMap(serviceProviderLocation.getLatitude(), serviceProviderLocation.getLongitude());
                showPassengerOnMap(pickupLocation.getLatitude(), pickupLocation.getLongitude());
                GeoPoint gp = new GeoPoint(pickupLocation.getLatitude(), pickupLocation.getLongitude());
                calculateDirections(serviceProviderLocation, gp);
                /*if (driverMarkerOption != null) {
                    updateMarkerRunnable();
                }*/
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected String doInBackground(String... strings) {

            return null;
        }
    }


}
