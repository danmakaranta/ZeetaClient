package com.example.zeeta;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zeeta.data.GeneralJobData;
import com.example.zeeta.data.StaffFound;
import com.example.zeeta.models.PolylineData;
import com.example.zeeta.models.RequestInformation;
import com.example.zeeta.models.User;
import com.example.zeeta.models.WorkerLocation;
import com.example.zeeta.services.LocationService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import static com.example.zeeta.util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener {

    private static final String TAG = "MapActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 14f;
    Location currentLocation;
    Intent serviceIntent;
    public LocationManager locationManager;
    //firestore access for cloud storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private Boolean serviceProviderAcceptanceStatus;
    private long hourlyRate = 0;
    private String serviceRendered = "";
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private WorkerLocation mWorkerLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean markerPinned;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    //widget sections
    private EditText mSearchText;
    private ArrayList<Marker> markerList = new ArrayList<Marker>();
    private FirebaseFirestore mDb;
    private String staffOccupation = "";
    private LatLngBounds mMapBoundary;
    private ArrayList<WorkerLocation> mUserLocations = new ArrayList<>();
    private ArrayList<String> selectedServices;
    private GeoApiContext mGeoApiContext;
    private Handler mHandler = new Handler();
    public Criteria criteria;
    //vars
    private @ServerTimestamp
    Date clientTimeStamp, staffTimeStamp;
    private ArrayList selectedServiceData;
    private GeoFire geoFire;
    private double RADIUS;
    private boolean serviceFound;
    private ArrayList<StaffFound> keysFound;
    private GeoQuery geoQuery;
    private DocumentReference staffTime;
    private DocumentReference acceptance;
    private Marker mSelectedMarker = null;
    private ArrayList<String> keyIDs;
    private DatabaseReference mdatabaseRef;
    public String bestProvider;
    DocumentReference clientRequest;
    private int tyingNum;
    private String serviceProviderPhone;
    private String getServiceProviderName;
    private GeoPoint serviceProviderLocation;
    private GeoPoint pickupLocation;
    private GeoPoint destination;
    private String locality = "StateNotFound";
    private @ServerTimestamp
    Timestamp timeStamp;

    //for adding a custom marker,
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId){

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Bitmap my_image;

    // reset all variables for service
    @Override
    public void onBackPressed() {
        serviceFound = false;
        selectedServices.clear();
        serviceProviderAcceptanceStatus = false;
        mMap.clear();
        startActivity(new Intent(getApplicationContext(), Request.class));
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                    mWorkerLocation.setGeoPoint(geoPoint);
                    Log.d(TAG, "geopoint set.");
                    mWorkerLocation.setTimeStamp(null);
                    //saveWokerLocation();

                }
            }
        });

    }

    boolean markerTracker;

    private void getWorkerDetails() {

        if (mWorkerLocation == null) {
            mWorkerLocation = new WorkerLocation();
            DocumentReference userRef = mDb.collection("Worker location")
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        User user = task.getResult().toObject(User.class);
                        mWorkerLocation.setUser(user);
                        ((UserClient) getApplicationContext()).setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        } else {
            getLastKnownLocation();
        }

    }

    public String getProfession(String id) {


        DocumentReference proffession2 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            proffession2 = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(id);
        }


        DocumentReference proffession = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            proffession = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(id);
        }


        proffession2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    String jobType = (String) doc.get("profession");

                    if (jobType == null) {
                        Log.d(TAG, "No data found ");
                    } else {
                        staffOccupation = null;
                        Log.d(TAG, "Profession: " + jobType);
                        staffOccupation = jobType;
                    }
                }

            }
        });

        /*proffession.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    String aiki = (String) doc.get("profession");
                    if (aiki == null) {
                        Log.d(TAG, "No data found ");
                    } else {
                        staffOccupation = null;
                        Log.d(TAG, "Profession: " + aiki);
                        staffOccupation = aiki;
                    }
                }
            }

        });*/
        id = null;
        return staffOccupation;
    }

    private void calculateDirections(GeoPoint gp) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                gp.getLatitude(),
                gp.getLongitude()
        );
        Log.d(TAG, "calculateDirections: finished calculating directions.");
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
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

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready here");
        //Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // getDeviceLocation();

        if (mLocationPermissionGranted) {
            getDeviceLocation();
          /*  mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);// remove the set location button from the screen*/
            // init();
        }
        mMap.clear();
        mMap.setOnPolylineClickListener(this);

    }

    private void getUserLocations(String id) {

        DocumentReference clientL = FirebaseFirestore.getInstance()
                .collection("AbujaOnline")
                .document(id); // testing with an already dummy location data

        clientL.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "getUserLocationss: successful at accessing the client location.");
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null) {
                        GeoPoint geoPoint = doc.getGeoPoint("geoPoint");
                        //check to see if we have a latitude and longitude of the client for the cloud database
                        Log.d(TAG, "Latitude " + geoPoint.getLatitude());
                        Log.d(TAG, "Longitude " + geoPoint.getLongitude());

                        calculateDirections(geoPoint);

                    } else {
                        Log.d(TAG, "Document is null for location ");
                    }


                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "getUserLocationss: unsuccessful at accessing the client location.");
                    }
                });

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        getDeviceLocation();
        RADIUS = 20;
        serviceFound = false;
        keyIDs = new ArrayList();
        keysFound = new ArrayList<StaffFound>();
        serviceProviderAcceptanceStatus = false;

        tyingNum = 0;


        setContentView(R.layout.activity_map);
        selectedServiceData = new ArrayList();

        serviceIntent = new Intent(MapActivity.this, LocationService.class);


        mDb = FirebaseFirestore.getInstance();

        markerPinned = false;
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_api_key))
                    .build();
        }

        //initialize and assign variables for the bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        //set home icon selected
        bottomNavigationView.setSelectedItemId(R.id.home_button);
        //perform itemselectedlistener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home_button:
                        return true;
                    case R.id.jobs_button:
                        startActivity(new Intent(getApplicationContext(), Jobs.class).putExtra("RequestedServices", selectedServices));
                        overridePendingTransition(0, 0);
                        //getUserLocations();
                        return true;
                    case R.id.dashboard_button:
                        startActivity(new Intent(getApplicationContext(), DashBoard.class).putExtra("RequestedServices", selectedServices));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.sos_button:
                        /*startActivity(new Intent(getApplicationContext(), DashBoard.class));
                        overridePendingTransition(0, 0);
                        return true;   */

                }
                return false;
            }
        });

        executeService();

    }

    private String requestedService;

    private void executeService() {
        selectedServices = (ArrayList<String>) getIntent().getSerializableExtra("RequestedServices");
        if (selectedServices != null && selectedServices.size() >= 1) {

            for (int i = 0; i <= selectedServices.size() - 1; i++) {

                String serv = null;
                serv = "" + selectedServices.get(i);
                requestedService = serv;

                if (currentLocation != null) {

                }
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(locality).child(serv);
                geoFire = new GeoFire(ref);
                serviceFound = false;
                RADIUS = 20;
                getClientRequest(); //get the service, if found, pin it to map with custom marker
            }

        }

    }

    private boolean markerContains(String key) {
        markerTracker = false;
        int counter = 0;
        while (!markerTracker && markerList.size() > 0) {
            String tempKey = "";
            if (counter <= markerList.size() - 1) {
                tempKey = markerList.get(counter).getId().toString();
            }

            if (tempKey.equalsIgnoreCase(key)) {
                markerTracker = true;
            }
            if (counter < markerList.size()) {
                counter = counter + 1;
            } else {
                markerTracker = true;
            }

        }
        return markerTracker;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (checkMapServices()) {
            if (mLocationPermissionGranted) {

            } else {
                getLocationPermission();
            }
        }
        executeService();
    }

    private boolean checkMapServices() {
        if (isServicesOk()) {
            return isMapsEnabled();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isServicesOk() {
        Log.d(TAG, "isServicesOk: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is ok and the user can make a map request
            Log.d(TAG, "isServicesOk: Google play services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOk: an error occured but we can fix it");

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void init() {
        geolocate();
       /* mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == event.ACTION_DOWN || event.getAction() == event.KEYCODE_ENTER) {

                }

                return false;
            }
        });*/


    }

    private void geolocate() {
        String searchString ="hmedix";
        // create a geocoder object
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.d(TAG, "geolocate: input was wrong");
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            //now move the camera to the location
            //  moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }

    }

    private void getClientRequest() {
        getDeviceLocation();

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null) {
                        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), RADIUS);

                    } else {
                        geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), RADIUS);
                    }

                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {

                            String tempProf = "key";
                            keysFound.add(new StaffFound(key, new LatLng(location.latitude, location.longitude), tempProf));
                            if (engaged(key)) {

                            } else {
                                if (markerList.size() <= 0) {

                                    Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(requestedService));
                                    staffMarker.setTag(key);
                                    //choose icon type for transport or other service
                                    if (requestedService.equalsIgnoreCase("Taxi") || requestedService.equalsIgnoreCase("Trycycle(Keke)")) {
                                        staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.car64));
                                    } else {
                                        staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_directions_walk_black_24dp));
                                    }
                                    moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");
                                    markerList.add(staffMarker);
                                } else {
                                    if (markerContains(key)) {

                                    } else {
                                        Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(requestedService));
                                        staffMarker.setTag(key);
                                        if (requestedService.equalsIgnoreCase("Taxi") || requestedService.equalsIgnoreCase("Trycycle(Keke)")) {
                                            staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.car64));
                                        } else {
                                            staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_directions_walk_black_24dp));
                                        }
                                        moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");
                                        markerList.add(staffMarker);
                                    }
                                }
                            }


                        }

                        @Override
                        public void onKeyExited(String key) {


                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            Log.d("OnGeoQueryReady", "OnGeoQueryReady called");

                            if (tyingNum <= 20) {
                                Log.d("Counter for GeoQuery", "Counting how many times geoQuery is called: " + tyingNum);
                                getClientRequest();
                                tyingNum++;
                            }

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    });

                }
            }
        });
    }

    private void moveCamera(LatLng latlng, float zoom, String title) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

    }

    private void resetSelectedMarker(){
        if(mSelectedMarker != null){
            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }

    private void removeTripMarkers(){
        for(Marker marker: mTripMarkers){
            marker.remove();
        }
    }

    private void initMap() {// for initializing the map
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;

                initMap();// if the location permission is granted
            } else {
                Log.d(TAG, "getLocationPermission: Location permission failed");
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSIONS_REQUEST_CODE);
            }

        } else {
            Log.d(TAG, "getLocationPermission: Location permission failed");
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0) {// that means some kind of permission was granted
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermission: permission request failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermission: permission granted");
                    mLocationPermissionGranted = true;
                    //initialize our map
                    initMap();

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    // getWorkerDetails();
                } else {
                    getLocationPermission();
                }
            }
        }

    }

    private void startLocationService() {
        Log.d(TAG, "startLocationService: Start of location service method");

        if (!isLocationServiceRunning()) {
            startService(serviceIntent);

        } else {
            stopService(serviceIntent);// stop location updates from here
        }
    }

    private void stopLocationUpdates() {
        stopService(serviceIntent);
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.zeeta.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolyLinesData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){

                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.blue2));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Route #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration
                        ));

                mTripMarkers.add(marker);

                marker.showInfoWindow();
            }
            else{
                polylineData.getPolyline().setColor(R.color.darkGrey);
                polylineData.getPolyline().setZIndex(0);
            }
        }

    }

    private void calculateDirections(Marker marker) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {

                Log.d(TAG, "onResult: successfully retrieved directions.");
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
    }

    private void getDeviceLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        Log.d(TAG, "getDeviceLocation: getting the device current location");


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {// check first to see if the permission is granted
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            currentLocation = location;
                            locality = getLocality();

                            //move camera to current location on map
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My location");

                        }
                    }

                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException:" + e.getMessage());
        }

    }


    public boolean engaged(String id) {

        DocumentReference serviceProviderStatus = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("Request").document("ongoing");
        final boolean[] acceptance = new boolean[1];

        serviceProviderStatus.addSnapshotListener(new EventListener<DocumentSnapshot>() {

            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                assert documentSnapshot != null;
                if (documentSnapshot.exists()) {
                   /* String temp = documentSnapshot.getString("accepted");
                    if (temp != null) {
                        if (temp.equalsIgnoreCase("accepted") || temp.equalsIgnoreCase("awaiting")) {
                            serviceProviderAcceptanceStatus = true;
                            acceptance[0] = true;
                            Log.d("Statusss:", "Statusessss: " + temp + id);
                        }
                    }*/

                } else {
                    serviceProviderAcceptanceStatus = false;
                    acceptance[0] = false;
                }

            }
        });
        //return serviceProviderAcceptanceStatus;
        return acceptance[0];

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        try {

            getServiceProviderDetails(marker.getTag().toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean stillOnline(String id) {

        staffTime = FirebaseFirestore.getInstance()
                .collection("AbujaOnline")
                .document(id);

        staffTime.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                DocumentSnapshot doc = task.getResult();
                Timestamp timestamp = doc.getTimestamp("timeStamp");

                assert timestamp != null;
                Log.d(TAG, "Time on server is: " + timestamp.getSeconds());
            }
        });

        return true;
    }

    private void sendRideRequest(String id, GeoPoint pickup, GeoPoint destination, Long amount) {
        final int[] counter = {0};
        timeStamp = Timestamp.now();
        DocumentReference clientRequest = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("RideData").document(Objects.requireNonNull("ongoing"));

        JourneyInfo journeyInfo = new JourneyInfo(pickup, destination, FirebaseAuth.getInstance().getUid(), "+3920329", (long) 0, timeStamp, (long) amount, false, false, false);

        clientRequest.set(journeyInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(MapActivity.this, "Your Ride request has been sent! please hold for driver", Toast.LENGTH_LONG).show();
                    clientRequest.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            counter[0] = counter[0] + 1;
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                Log.d(TAG, "Current data: " + documentSnapshot.getData());
                                Boolean accepted = documentSnapshot.getBoolean("accepted");
                                if (accepted) {

                                    Toast.makeText(MapActivity.this, "Your Driver is on the way!", Toast.LENGTH_LONG).show();
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !accepted) {

                                    if (counter[0] > 1) {
                                        //clear your request since it has been declined
                                        clientRequest.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.w(TAG, "Ride Request declined, clear request data.");
                                            }
                                        });
                                        Toast.makeText(MapActivity.this, "Your request have been declined, please choose another Vehicle", Toast.LENGTH_LONG).show();

                                    }

                                }

                            }
                        }
                    });
                }
            }
        });

    }


    private void sendClientRequest(String id) {

        DocumentReference clientRequest = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("Request").document("ongoing");

        RequestInformation requestData = new RequestInformation(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), FirebaseAuth.getInstance().getUid(), "Awaiting");

        clientRequest.set(requestData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Your request has been sent, please hold on!", Toast.LENGTH_LONG).show();
                    listenForUpdate(id);
                    Log.e(TAG, "sendClientRequest: customer request sent!");
                } else {
                    Log.e(TAG, "sendClientRequest: error sending customer request!");
                }
            }
        });

    }

    private void listenForUpdate(String id) {

        DocumentReference acceptanceUpdate = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("Request").document("ongoing"); // testi
        acceptanceUpdate.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    if (documentSnapshot.getString("accepted").equals("Accepted")) {
                        Toast.makeText(MapActivity.this, "Your request have been accepted! Hold on for Client!", Toast.LENGTH_LONG).show();

                        setJobDataOnCloud(id);

                    } else if (documentSnapshot.getString("accepted").equals("Declined")) {
                        //clear your request since it has been declined
                        acceptanceUpdate.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.w(TAG, "Request not accepted, clear any outstanding request data.");
                            }
                        });

                        Toast.makeText(MapActivity.this, "Your request have been declined, please choose another service provider", Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG, "A change has been effected on this doc");
                }

            }
        });
    }


    private void setJobDataOnCloud(String id) {
        DocumentReference spJobData = null;
        DocumentReference jobData = null;
        jobData = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(FirebaseAuth.getInstance().getUid()).collection("JobData").document(id);
        spJobData = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("JobData").document(FirebaseAuth.getInstance().getUid());

        jobData.set(new GeneralJobData(pickupLocation, destination, serviceProviderLocation, id, serviceProviderPhone, getServiceProviderName, (long) 0, (long) 1000, true,
                false, false, serviceRendered, timeStamp, "Awaiting", (long) 0))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("setJobData", "Job data set");
                    }
                });
        spJobData.set(new GeneralJobData(pickupLocation, destination, serviceProviderLocation, FirebaseAuth.getInstance().getUid(), serviceProviderPhone, getServiceProviderName, (long) 0, (long) 1000, true,
                false, false, serviceRendered, timeStamp, "Awaiting", (long) 0))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("setJobData", "Job data set");
                    }
                });

        /*//reset the variables for next request
        jobData = null;
        serviceProviderPhone = null;
        getServiceProviderName = null;
        serviceRendered = "";
        hourlyRate = 0;*/

    }


    private void getServiceProviderDetails(String id) throws IOException {

        // custom dialog
        final Dialog dialog = new Dialog(MapActivity.this);
        ImageView imageView = findViewById(R.id.serviceProviderPic);

        mdatabaseRef = FirebaseDatabase.getInstance().getReference(id);
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child(id + ".jpg");


        DocumentReference serviceProviderDetails = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            serviceProviderDetails = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(id);
        }
        String email, number, name, rating;

        serviceProviderDetails.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    serviceProviderPhone = "";
                    getServiceProviderName = "";
                    hourlyRate = 0;
                    serviceRendered = "";
                    DocumentSnapshot doc = task.getResult();
                    String email = (String) doc.get("email");
                    String number = (String) doc.get("phoneNumber");
                    String name = (String) doc.get("name");
                    String rating = (String) doc.get("rating");
                    String jobType = (String) doc.get("profession");
                    Long hourly = (Long) doc.get("hourlyRate");

                    serviceProviderPhone = number;
                    getServiceProviderName = name;
                    hourlyRate = hourly;
                    serviceRendered = jobType;

                    assert hourly != null;
                    double hourlyRate = hourly.doubleValue();

                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.service_provider);
                    dialog.setTitle("Send Request?");

                    // set the custom dialog components - text, image and button
                    ImageView pic = (ImageView) dialog.findViewById(R.id.serviceProviderPic);
                    TextView textName = (TextView) dialog.findViewById(R.id.serviceProviderName);
                    textName.setText("Name: " + name);
                    TextView textEmail = (TextView) dialog.findViewById(R.id.serviceProviderEmail);
                    textEmail.setText("Email: " + email);
                    TextView textJobType = (TextView) dialog.findViewById(R.id.serviceProviderJobType);
                    textJobType.setText("Job Type: " + jobType);
                    TextView textPhoneNumber = (TextView) dialog.findViewById(R.id.serviceProviderPhoneNumber);
                    textPhoneNumber.setText("Phone Number: " + number);
                    TextView textRating = (TextView) dialog.findViewById(R.id.serviceProviderRating);
                    textRating.setText("Rating: " + rating);
                    TextView hourlyTxt = dialog.findViewById(R.id.serviceProviderRate);
                    hourlyTxt.setText("Hourly Rate: N" + hourlyRate);

                    mdatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            dataSnapshot.getValue();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    Button btnYes = (Button) dialog.findViewById(R.id.buttonYes);
                    Button btnNo = (Button) dialog.findViewById(R.id.buttonNo);
                    dialog.show();

                    btnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (engaged(id)) {
                                Toast.makeText(MapActivity.this, "The Service provider is engaged already", Toast.LENGTH_LONG).show();

                            } else {
                                //setJobDataOnCloud(id);
                                sendClientRequest(id);
                            }
                            dialog.dismiss();
                        }
                    });

                    btnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                }
            }

        });
        mdatabaseRef.removeValue();

    }


    public String getLocality() {

        Location location = currentLocation;
        String state = "";
        Log.d("testinglocation", "location latitude" + location.getLatitude());
        if (location.getLongitude() >= 7 && location.getLongitude() < 8) {// abuja's longitude
            if (location.getLatitude() >= 9 && location.getLatitude() < 9.5) {// abuja's latitude
                state = state + "Abuja";
            }
        } else if (location.getLongitude() >= 3 && location.getLongitude() < 4) {// lagos longitude
            if (location.getLatitude() >= 6 && location.getLatitude() < 7) {// lagos latitude
                state = state + "Lagos";
            }
        } else if (location.getLongitude() >= 9 && location.getLongitude() < 10) {// Bauchi longitude
            if (location.getLatitude() >= 10 && location.getLatitude() < 11) {// Bauchi latitude
                state = state + "Bauchi";
            }
        } else if (location.getLongitude() >= 7 && location.getLongitude() < 8) {// kaduna longitude
            if (location.getLatitude() >= 10 && location.getLatitude() < 11) {// kaduna latitude
                state = state + "Kaduna";
            }
        } else if (location.getLongitude() <= -122.0 && location.getLongitude() > -123.0) {// GooglePlex longitude
            Log.d("stage1", "passed longitude");
            if (location.getLatitude() >= 37.0 && location.getLatitude() < 38.0) {// GooglePlex latitude
                state = state + "GooglePlex";
            }
        } else {
            state = state + "StateNotFound";
        }

        return state;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
