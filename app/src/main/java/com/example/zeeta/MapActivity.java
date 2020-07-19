package com.example.zeeta;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zeeta.adapters.PlaceAutoSuggestionAdapter;
import com.example.zeeta.data.GeneralJobData;
import com.example.zeeta.data.StaffFound;
import com.example.zeeta.models.PolylineData;
import com.example.zeeta.models.RequestInformation;
import com.example.zeeta.models.WorkerLocation;
import com.example.zeeta.services.LocationService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
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
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.google.maps.model.Distance;
import com.squareup.picasso.Picasso;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import static com.example.zeeta.util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;


public class MapActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<String>, OnMapReadyCallback,
        LocationListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener, GoogleApiClient.OnConnectionFailedListener,
        InternetConnectivityListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "MapActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 14f;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;
    /**
     * GeoDataClient wraps our service connection to Google Play services and provides access
     * to the Google Places API for Android.
     */
    protected GeoDataClient mGeoDataClient;
    AutoCompleteTextView pickET;
    AutoCompleteTextView destinationET;
    Location currentLocation;
    Intent serviceIntent;
    //firestore access for cloud storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    DocumentReference clientRequest;
    boolean markerTracker;
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
    private GeoApiContext mDirectionApi;
    private Handler mHandler = new Handler();
    //vars
    private GeoFire geoFire;
    private double RADIUS = 10;
    private boolean serviceFound;
    private ArrayList<StaffFound> keysFound;
    private DocumentReference staffTime;
    private DocumentReference acceptance;
    private Marker mSelectedMarker = null;
    private ArrayList keyIDs;
    private DatabaseReference mdatabaseRef;
    private int tyingNum;
    //progress bars for geolocation
    private ProgressBar pickUpP;
    private String serviceProviderPhone;
    private GeoPoint serviceProviderLocation;
    private GeoPoint pickupLocation;
    private GeoPoint destination;
    private ProgressBar destinationPBar;
    // custom driverDialog
    private Dialog driverDialog;
    private TextView rideEst;
    private LoaderManager priceLoaderManager;
    private int rideEstimateAmount;
    private double priceEstimate = 0.0;
    private boolean pickupFirstClick = false;
    private boolean destinationFound = false;
    private String locality = "";
    private @ServerTimestamp
    Timestamp timeStamp;
    private String serv = null;
    private long distanceCovered;
    private String serviceProviderName;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
    private TextView mPlaceDetailsText;
    private TextView mPlaceDetailsAttribution;
    private String customerAddress = "";
    private String customerPhoneNumber;
    private Runnable mRunnable;
    private boolean callbackPresent = false;
    private long LOCATION_UPDATE_INTERVAL = 6000;
    private String customerName = "";
    private String destinationText;
    private boolean navOpened = false;
    private Button cancelRequestBtn;
    private DocumentReference acceptanceUpdate;
    private DocumentReference clientRideRequest;
    private DocumentReference customerRequest;
    private NavigationView navigationView;
    private ProgressDialog requestProgressDialog;
    private ProgressDialog nemaProgressDialog;
    private Bitmap my_image;
    private ProgressDialog loadingProgressDialog;
    /**
     * Callback for results from a Places Geo Data Client query that shows the first place result in
     * the details view on screen.
     */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                // Format details of the place for display and show it in a TextView.
                mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                        place.getId(), place.getAddress(), place.getPhoneNumber(),
                        place.getWebsiteUri()));

                // Display the third party attributions if set.
                final CharSequence thirdPartyAttribution = places.getAttributions();
                if (thirdPartyAttribution == null) {
                    mPlaceDetailsAttribution.setVisibility(View.GONE);
                } else {
                    mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
                    mPlaceDetailsAttribution.setText(
                            Html.fromHtml(thirdPartyAttribution.toString()));
                }

                Log.i(TAG, "Place details received: " + place.getName());

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
                return;
            }
        }
    };

    private String requestedService;
    private InternetAvailabilityChecker mInternetAvailabilityChecker;
    private int lookingForTaxi = 0;
    private int lookingForMechanic = 0;
    private GeoQuery geoQueryTricycle;
    private GeoQuery geoQueryMechanic;
    private GeoQuery geoQueryTaxi;
    private GeoQuery geoQueryBikeDelivery;
    private GeneralJobData ridedata;
    private boolean notifiedRiderA = false;
    private ArrayList<String> markerNameList = new ArrayList<String>();
    private boolean taxiSearchInProgress = false;
    private boolean tricycleSearchInProgress = false;
    private int lookingForTricycle = 0;
    private boolean mechanicSearchInProgress = false;
    private boolean deliverySearchInProgress = false;
    private int lookingForDeliveryBikes = 0;

    public static String getLocalityName(Context context, double latitude, double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if (addresses.size() > 0) {
            Log.d("locality", "State of operation " + addresses.get(0).getLocality());
        }
        return addresses.get(0).getLocality();

    }

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    //for adding a custom marker,
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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


    // reset all variables for service
    @Override
    public void onBackPressed() {

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

    private void calculateDirections(GeoPoint gp) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                gp.getLatitude(),
                gp.getLongitude()
        );
        Log.d(TAG, "calculateDirections: finished calculating directions.");
        DirectionsApiRequest directions = new DirectionsApiRequest(mDirectionApi);

        directions.alternatives(false);
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean markerContains(String key) {
        markerTracker = false;
        int counter = 0;
        while (markerList.size() > 0) {
            String tempKey = "";
            if (counter <= markerList.size() - 1) {
                tempKey = markerList.get(counter).getId().toString();
            }

            if (tempKey.equalsIgnoreCase(key)) {
                return true;
            } else {
                return false;
            }

        }
        return markerTracker;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    String servType = marker.getSnippet();
                    LatLng ltl = marker.getPosition();
                    GeoPoint servicePgp = new GeoPoint(ltl.latitude, ltl.longitude);
                    if (servType.equalsIgnoreCase("Taxi") || servType.equalsIgnoreCase("Trycycle(Keke)")) {
                        getZeetaDriver(Objects.requireNonNull(marker.getTag()).toString(), servicePgp);
                    } else {
                        getServiceProviderDetails(Objects.requireNonNull(marker.getTag()).toString());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                navigationView.setVisibility(View.INVISIBLE);
            }
        });
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        new getDeviceLocationAsync().execute();

        mMap.setOnPolylineClickListener(this);

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

    @Override
    protected void onResume() {
        super.onResume();
        updateMarkersRunnable();

        if (checkMapServices()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                initMap();
            }
        }

        if (checkMapServices()) {
            if (mLocationPermissionGranted) {

            } else {
                getLocationPermission();
            }
        }

    }

    private int timeApart(GeoPoint customerL, GeoPoint servidePL) {
        com.google.maps.model.LatLng servicePLocation = new com.google.maps.model.LatLng(
                customerL.getLatitude(),
                customerL.getLongitude()
        );

        if (mDirectionApi == null) {
            mDirectionApi = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_directions_api_key))
                    .build();
        }

        DirectionsApiRequest directions = new DirectionsApiRequest(mDirectionApi);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        servidePL.getLatitude(),
                        servidePL.getLongitude()
                )
        );

        final int[] time = {0};

        directions.destination(servicePLocation).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResult(DirectionsResult result) {
                if (result.routes.length > 0) {
                    time[0] = result.routes[0].legs[0].arrivalTime.getMinute();
                    Log.d(TAG, "calculateDirections: ETA: " + time[0]);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("CalculateDirectionExcp", "calculateDirections ETA: Failed to get directions: " + e.getMessage());
            }
        });
        if (time[0] > 0) {
            return time[0];
        } else {
            return 5;
        }

    }

    private long calculateDistance(GeoPoint pickupL, GeoPoint dest) {
        Log.d("check", "check calculatedistance");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
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

        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {

                if (result.routes[0].legs[0].distance.inMeters > 5) {
                    distanceCovered = result.routes[0].legs[0].distance.inMeters;
                }
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: distanceCV:" + distanceCovered);
                Log.d(TAG, "calculateDirections: geocodedWayPointzz: " + result.geocodedWaypoints[0].toString());
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("CalculateDirectionsETA", "calculateDistance: Failed to get distance: " + e.getMessage());
            }
        });
        if (distanceCovered < 1) {
            return 0;
        } else {
            return distanceCovered;
        }


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        checkLocationPermission();
        new getDeviceLocationAsync().execute();
        updateCustomerDetails();
        requestProgressDialog = new ProgressDialog(this);
        nemaProgressDialog = new ProgressDialog(this);
        requestProgressDialog.setMessage("Sending Request....");
        nemaProgressDialog.setMessage("Searching.....");
        serviceFound = false;
        keyIDs = new ArrayList();
        keysFound = new ArrayList<StaffFound>();
        serviceProviderAcceptanceStatus = false;

        String serviceRerequest = getIntent().getStringExtra("ReRequest");

        InternetAvailabilityChecker.init(this);
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);

        // Construct a GeoDataClient for the Google Places API for Android.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        tyingNum = 0;
        driverDialog = new Dialog(MapActivity.this);
        driverDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        driverDialog.setContentView(R.layout.ride_request_page);
        driverDialog.setTitle("Send Request?");
        rideEst = driverDialog.findViewById(R.id.ride_estimate);
        priceLoaderManager = getLoaderManager();

        if (priceLoaderManager.getLoader(1) != null) {
            priceLoaderManager.initLoader(1, null, MapActivity.this);
        }

        setContentView(R.layout.activity_map);

        serviceIntent = new Intent(MapActivity.this, LocationService.class);

        mDb = FirebaseFirestore.getInstance();

        markerPinned = false;
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_api_key))
                    .build();
        }

        if (mDirectionApi == null) {
            mDirectionApi = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_directions_api_key))
                    .build();
        }

        navigationView = findViewById(R.id.side_navigation);

        //initialize and assign variables for the bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        //set home icon selected
        bottomNavigationView.setSelectedItemId(R.id.home_button);
        //perform itemselectedlistener
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.home_button:
                    navigationView.setVisibility(View.GONE);
                    navOpened = false;
                    return true;
                case R.id.jobs_button:
                    startActivity(new Intent(getApplicationContext(), Jobs.class).putExtra("RequestedServices", selectedServices));
                    return true;
                case R.id.dashboard_button:
                    Intent dashBIntent = new Intent(MapActivity.this, DashBoard.class);
                    dashBIntent.putExtra("rating", "4.5");
                    dashBIntent.putExtra("walletBalance", 5000.0);
                    startActivity(dashBIntent);
                    return true;
                case R.id.services_list:
                    if (navOpened) {
                        navOpened = false;
                        navigationView.setVisibility(View.GONE);
                        bottomNavigationView.setSelectedItemId(R.id.home_button);
                    } else {
                        navOpened = true;
                        navigationView.setVisibility(View.VISIBLE);
                        // bottomNavigationView.setSelectedItemId(R.id.services_list);
                    }
                    return true;
            }
            return false;
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemSelected = item.getItemId();
                switch (itemSelected) {
                    case R.id.taxiService:
                        if (!taxiSearchInProgress) {
                            nemaProgressDialog.show();
                            getTaxi();
                        } else {
                            Toast.makeText(MapActivity.this, "Your search is already in progress", Toast.LENGTH_SHORT).show();
                        }
                        navigationView.setVisibility(View.GONE);
                        return true;
                    case R.id.tricycleService:
                        if (!tricycleSearchInProgress) {
                            nemaProgressDialog.show();
                            getTricycle();
                        } else {
                            Toast.makeText(MapActivity.this, "Your search is already in progress", Toast.LENGTH_SHORT).show();
                        }
                        navigationView.setVisibility(View.GONE);
                        return true;
                    case R.id.nurse:
                        nemaProgressDialog.show();
                        //getNurse();
                        navigationView.setVisibility(View.GONE);
                        return true;
                    case R.id.mechanicService:
                        if (!mechanicSearchInProgress) {
                            nemaProgressDialog.show();
                            getMechanic();
                        } else {
                            Toast.makeText(MapActivity.this, "Your search is already in progress", Toast.LENGTH_SHORT).show();
                        }
                        navigationView.setVisibility(View.GONE);
                        return true;
                    case R.id.bikeDelivery:
                        if (!deliverySearchInProgress) {
                            nemaProgressDialog.show();
                            getDeliveryBikes();
                        } else {
                            Toast.makeText(MapActivity.this, "Your search is already in progress", Toast.LENGTH_SHORT).show();
                        }
                        navigationView.setVisibility(View.GONE);
                        return true;
                    case R.id.logout:
                        //blah .....
                        return true;
                }

                return false;
            }
        });

    }

    private void getTricycle() {

        tricycleSearchInProgress = true;

        DatabaseReference refTricycle = null;
        refTricycle = FirebaseDatabase.getInstance().getReference(locality).child("Tricycle");
        GeoFire geoFireTricycle = null;
        geoFireTricycle = new GeoFire(refTricycle);

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
        GeoFire finalGeoFireTricycle = geoFireTricycle;
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();

                    if (location != null) {
                        geoQueryTricycle = finalGeoFireTricycle.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), RADIUS);

                    } else {
                        geoQueryTricycle = finalGeoFireTricycle.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), RADIUS);
                    }

                    geoQueryTricycle.addGeoQueryEventListener(new GeoQueryEventListener() {

                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            //search for taxi already in progress
                            taxiSearchInProgress = true;

                            keysFound.add(new StaffFound(key, new LatLng(location.latitude, location.longitude), key));
                            if (engaged(key)) {

                            } else {
                                if (markerList.size() <= 0) {
                                    String markerTitle;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        String temp = " " + timeApart(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoPoint(location.latitude, location.longitude)) + " minutes away";

                                        try {// nothing more but to slow down execution a bit to get results before proceeding
                                            Thread.sleep(2000);
                                        } catch (InterruptedException excp) {
                                            excp.printStackTrace();
                                        }
                                        markerTitle = "Tricycle" + temp;
                                    } else {
                                        markerTitle = "Tricycle" + " ...minutes away!";
                                    }
                                    nemaProgressDialog.dismiss();
                                    Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(markerTitle));

                                    staffMarker.setTag(key);
                                    //choose icon type for transport or other service
                                    staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.tricycle));
                                    staffMarker.setSnippet("Tricycle");
                                    moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");// don't forget to remove later
                                    markerList.add(staffMarker);
                                    markerNameList.add(key);
                                    updateMarkersRunnable();
                                    staffMarker.showInfoWindow();
                                } else {
                                    if (!markerNameList.contains(key)) {
                                        nemaProgressDialog.dismiss();
                                        Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(requestedService));
                                        staffMarker.setTag(key);
                                        staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.tricycle));
                                        staffMarker.setSnippet("Tricycle");
                                        moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");
                                        markerList.add(staffMarker);
                                        markerNameList.add(key);
                                        staffMarker.showInfoWindow();
                                        //
                                    }

                                }
                            }

                        }

                        @Override
                        public void onKeyExited(String key) {
                            for (int i = 0; i < markerList.size(); i++) {
                                if (markerList.get(i).getTag() != null) {
                                    if (Objects.requireNonNull(markerList.get(i).getTag()).toString().equalsIgnoreCase(key)) {
                                        markerNameList.remove(key);
                                        if (markerNameList.contains(key)) {
                                            markerNameList.remove(key);
                                            markerList.get(i).remove();
                                        }

                                    }
                                }
                            }
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            Log.d("OnGeoQueryReady", "OnGeoQueryReady called");

                            if (lookingForTricycle <= 5) {
                                getTricycle();
                                lookingForTricycle++;
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

    private void getDeliveryBikes() {
        deliverySearchInProgress = true;

        DatabaseReference refDeliveryService = null;
        refDeliveryService = FirebaseDatabase.getInstance().getReference(locality).child("BikeDelivery");
        GeoFire geoFireBikeDelivery = null;
        geoFireBikeDelivery = new GeoFire(refDeliveryService);

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
        GeoFire finalGeoFireBikeDelivery = geoFireBikeDelivery;
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();

                    if (location != null) {
                        geoQueryBikeDelivery = finalGeoFireBikeDelivery.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), RADIUS);

                    } else {
                        geoQueryBikeDelivery = finalGeoFireBikeDelivery.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), RADIUS);
                    }

                    geoQueryBikeDelivery.addGeoQueryEventListener(new GeoQueryEventListener() {

                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {

                            keysFound.add(new StaffFound(key, new LatLng(location.latitude, location.longitude), key));
                            if (engaged(key)) {

                            } else {
                                if (markerList.size() <= 0) {
                                    String markerTitle;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        String temp = " " + timeApart(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoPoint(location.latitude, location.longitude)) + " minutes away";

                                        try {// nothing more but to slow down execution a bit to get results before proceeding
                                            Thread.sleep(2000);
                                        } catch (InterruptedException excp) {
                                            excp.printStackTrace();
                                        }
                                        markerTitle = "Delivery Bike" + temp;
                                    } else {
                                        markerTitle = "Delivery Bike" + " ...minutes away!";
                                    }
                                    nemaProgressDialog.dismiss();
                                    Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(markerTitle));

                                    staffMarker.setTag(key);
                                    //choose icon type for transport or other service
                                    staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.deliverybike));
                                    staffMarker.setSnippet("Delivery Bike");
                                    moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");// don't forget to remove later
                                    markerList.add(staffMarker);
                                    markerNameList.add(key);
                                    updateMarkersRunnable();
                                    staffMarker.showInfoWindow();
                                } else {
                                    if (!markerNameList.contains(key)) {
                                        nemaProgressDialog.dismiss();
                                        Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(requestedService));
                                        staffMarker.setTag(key);
                                        staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.deliverybike));
                                        staffMarker.setSnippet("Delivery Bike");
                                        moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");
                                        markerList.add(staffMarker);
                                        markerNameList.add(key);
                                        staffMarker.showInfoWindow();
                                        //
                                    }

                                }
                            }

                        }

                        @Override
                        public void onKeyExited(String key) {
                            for (int i = 0; i < markerList.size(); i++) {
                                if (markerList.get(i).getTag() != null) {
                                    if (Objects.requireNonNull(markerList.get(i).getTag()).toString().equalsIgnoreCase(key)) {
                                        markerNameList.remove(key);
                                        if (markerNameList.contains(key)) {
                                            markerNameList.remove(key);
                                            markerList.get(i).remove();
                                        }

                                    }
                                }
                            }
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            Log.d("OnGeoQueryReady", "OnGeoQueryReady called");

                            if (lookingForDeliveryBikes <= 5) {
                                getTricycle();
                                lookingForDeliveryBikes++;
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

    private void resetSelectedMarker() {
        if (mSelectedMarker != null) {
            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }

    private void removeTripMarkers() {
        for (Marker marker : mTripMarkers) {
            marker.remove();
        }
    }

    private void getClientRequest() {

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
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoQuery geoQuery;

                    if (location != null) {
                        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), RADIUS);

                    } else {
                        geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), RADIUS);
                    }


                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {

                            keysFound.add(new StaffFound(key, new LatLng(location.latitude, location.longitude), key));
                            if (engaged(key)) {

                            } else {
                                if (markerList.size() <= 0) {
                                    String markerTitle;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        markerTitle = requestedService + " " + timeApart(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoPoint(location.latitude, location.longitude)) + "minutes away";
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                            }
                                        }, 3000);
                                    } else {
                                        markerTitle = requestedService + " ...minutes away!";
                                    }

                                    Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(markerTitle));

                                    staffMarker.setTag(key);

                                    //choose icon type for transport or other service
                                    if (requestedService.equalsIgnoreCase("Taxi") || requestedService.equalsIgnoreCase("Trycycle(Keke)")) {
                                        staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.car643));
                                        staffMarker.setSnippet(requestedService);
                                    } else {
                                        staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_directions_walk_black_24dp));
                                        staffMarker.setSnippet(requestedService);
                                    }
                                    moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");
                                    markerList.add(staffMarker);
                                    markerNameList.add(key);
                                    updateMarkersRunnable();
                                    staffMarker.showInfoWindow();
                                } else {
                                    if (!markerNameList.contains(key)) {
                                        Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(requestedService));
                                        staffMarker.setTag(key);
                                        if (requestedService.equalsIgnoreCase("Taxi") || requestedService.equalsIgnoreCase("Trycycle(Keke)")) {
                                            staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.car643));
                                            staffMarker.setSnippet(requestedService);
                                        } else {
                                            staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_directions_walk_black_24dp));
                                            staffMarker.setSnippet(requestedService);
                                        }
                                        moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");
                                        markerList.add(staffMarker);
                                        staffMarker.showInfoWindow();

                                    }
                                }
                            }

                        }

                        @Override
                        public void onKeyExited(String key) {
                            for (int i = 0; i < markerList.size(); i++) {
                                if (markerList.get(i).getTag() != null) {
                                    if (Objects.requireNonNull(markerList.get(i).getTag()).toString().equalsIgnoreCase(key)) {
                                        // markerList.get(i).remove();
                                    }
                                }

                            }

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            Log.d("OnGeoQueryReady", "OnGeoQueryReady called");

                            if (tyingNum <= 5) {
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


    private void getTaxi() {


        DatabaseReference refTaxi = null;
        refTaxi = FirebaseDatabase.getInstance().getReference(locality).child("Taxi");
        GeoFire geoFireTaxi = null;
        geoFireTaxi = new GeoFire(refTaxi);

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
        GeoFire finalGeoFireTaxi = geoFireTaxi;
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();

                    if (location != null) {
                        geoQueryTaxi = finalGeoFireTaxi.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), RADIUS);

                    } else {
                        geoQueryTaxi = finalGeoFireTaxi.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), RADIUS);
                    }

                    geoQueryTaxi.addGeoQueryEventListener(new GeoQueryEventListener() {

                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            //search for taxi already in progress
                            taxiSearchInProgress = true;

                            keysFound.add(new StaffFound(key, new LatLng(location.latitude, location.longitude), key));
                            if (engaged(key)) {

                            } else {
                                if (markerList.size() <= 0) {
                                    String markerTitle;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        String temp = " " + timeApart(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoPoint(location.latitude, location.longitude)) + " minutes away";

                                        try {// nothing more but to slow down execution a bit to get results before proceeding
                                            Thread.sleep(2000);
                                        } catch (InterruptedException excp) {
                                            excp.printStackTrace();
                                        }
                                        markerTitle = "Taxi" + temp;
                                    } else {
                                        markerTitle = "Taxi" + " ...minutes away!";
                                    }
                                    nemaProgressDialog.dismiss();
                                    Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(markerTitle));

                                    staffMarker.setTag(key);
                                    //choose icon type for transport or other service
                                    staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.car643));
                                    staffMarker.setSnippet("Taxi");
                                    moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");// don't forget to remove later
                                    markerList.add(staffMarker);
                                    markerNameList.add(key);
                                    updateMarkersRunnable();
                                    staffMarker.showInfoWindow();
                                } else {
                                    if (!markerNameList.contains(key)) {
                                        nemaProgressDialog.dismiss();
                                        Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(requestedService));
                                        staffMarker.setTag(key);
                                        staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.car643));
                                        staffMarker.setSnippet("Taxi");
                                        moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");
                                        markerList.add(staffMarker);
                                        markerNameList.add(key);
                                        staffMarker.showInfoWindow();
                                        updateMarkersRunnable();
                                    }

                                }
                            }

                        }

                        @Override
                        public void onKeyExited(String key) {
                            for (int i = 0; i < markerList.size(); i++) {
                                if (markerList.get(i).getTag() != null) {
                                    if (Objects.requireNonNull(markerList.get(i).getTag()).toString().equalsIgnoreCase(key)) {
                                        markerNameList.remove(key);
                                        if (markerNameList.contains(key)) {
                                            markerNameList.remove(key);
                                            markerList.get(i).remove();
                                        }

                                    }
                                }
                            }
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            Log.d("OnGeoQueryReady", "OnGeoQueryReady called");

                            if (lookingForTaxi <= 5) {
                                getTaxi();
                                lookingForTaxi++;
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

    private void getMechanic() {
        mechanicSearchInProgress = true;

        DatabaseReference refMechanic = null;
        refMechanic = FirebaseDatabase.getInstance().getReference(locality).child("Mechanic");
        GeoFire geoFireMechanic = null;
        geoFireMechanic = new GeoFire(refMechanic);

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
        GeoFire finalGeoFireMechanic = geoFireMechanic;
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();

                    if (location != null) {
                        geoQueryMechanic = finalGeoFireMechanic.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), RADIUS);

                    } else {
                        geoQueryMechanic = finalGeoFireMechanic.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), RADIUS);
                    }


                    geoQueryMechanic.addGeoQueryEventListener(new GeoQueryEventListener() {

                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            Log.d(TAG, "getMechanic() 2:" + key);

                            keysFound.add(new StaffFound(key, new LatLng(location.latitude, location.longitude), key));
                            if (engaged(key)) {

                            } else {
                                if (markerList.size() <= 0) {
                                    String markerTitle;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        String temp = " " + timeApart(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoPoint(location.latitude, location.longitude)) + " minutes away";

                                        try {// nothing more but to slow down execution a bit to get results before proceeding
                                            Thread.sleep(2000);
                                        } catch (InterruptedException excp) {
                                            excp.printStackTrace();
                                        }
                                        markerTitle = "Mechanic" + temp;
                                    } else {
                                        markerTitle = "Mechanic, 5 minutes away!";
                                    }
                                    nemaProgressDialog.dismiss();
                                    Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(markerTitle));

                                    staffMarker.setTag(key);
                                    //choose icon type for transport or other service
                                    staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.mechanic));
                                    staffMarker.setSnippet("Mechanic");
                                    moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");// don't forget to remove later
                                    markerList.add(staffMarker);
                                    markerNameList.add(key);
                                    staffMarker.showInfoWindow();
                                    updateMarkersRunnable();
                                } else {
                                    if (!markerNameList.contains(key)) {
                                        nemaProgressDialog.dismiss();
                                        Marker staffMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(requestedService));
                                        staffMarker.setTag(key);
                                        staffMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.mechanic));
                                        staffMarker.setSnippet("Mechanic");
                                        moveCamera(new LatLng(location.latitude, location.longitude), DEFAULT_ZOOM, "");
                                        markerList.add(staffMarker);
                                        markerNameList.add(key);
                                        staffMarker.showInfoWindow();
                                        updateMarkersRunnable();
                                    }
                                }
                            }

                        }

                        @Override
                        public void onKeyExited(String key) {
                            for (int i = 0; i < markerList.size(); i++) {
                                if (markerList.get(i).getTag() != null) {
                                    if (Objects.requireNonNull(markerList.get(i).getTag()).toString().equalsIgnoreCase(key)) {
                                        markerNameList.remove(key);
                                        if (markerNameList.contains(key)) {
                                            markerNameList.remove(key);
                                            markerList.get(i).remove();
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {

                            if (lookingForMechanic <= 5) {
                                getMechanic();
                                lookingForMechanic++;
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


    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;

                initMap();// if the location permission is granted
                //getDeviceLocation();
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

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Route #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration
                        ));

                mTripMarkers.add(marker);

                //marker.showInfoWindow();
            } else {
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

                    initMap();
                }
            }
        }
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
                            try {
                                locality = null;
                                locality = getLocality();
                                Log.d("Locality", "State" + getLocalityName(MapActivity.this, location.getLatitude(), location.getLongitude()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            customerAddress = getCompleteAddressString(location.getLatitude(), location.getLongitude());

                            Log.d("address", "customer address" + customerAddress);

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
        Log.d(TAG, "Engaged() called for " + id);

        DocumentReference engagementStatus = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id);
        final boolean[] status = new boolean[1];


        engagementStatus.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    boolean engaged = doc.getBoolean("engaged");
                    if (engaged) {

                        status[0] = true;
                    } else {

                        status[0] = false;
                    }
                }
            }
        });

        return status[0];

    }

    private void init() {
        // geolocate();
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == event.ACTION_DOWN || event.getAction() == event.KEYCODE_ENTER) {

                }

                return false;
            }
        });

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();

                Log.w("My Current location", strReturnedAddress.toString());
            } else {
                Log.w("My Current loction", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction", "Cannt get Address!");
        }
        return strAdd;
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

    private void geolocate(String input) {

        String searchString;

        if (input.equalsIgnoreCase("pickup")) {
            searchString = null;
            searchString = pickET.getText().toString();
            Log.d("input", "input is pickup:");
        } else {
            searchString = null;
            searchString = destinationET.getText().toString();
            Log.d("input", "input is destination:");
        }
        // create a geocoder object
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList();
        try {

            list = geocoder.getFromLocationName(searchString, 1);

        } catch (IOException e) {
            Log.d(TAG, "geolocate: input was wrong " + input);
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            if (input.equalsIgnoreCase("pickup")) {
                pickUpP.setVisibility(View.INVISIBLE);

                pickupLocation = new GeoPoint(address.getLatitude(), address.getLongitude());
                pickupFirstClick = false;
                Log.d(TAG, "geolocate: input was right:Pickup " + searchString + pickupLocation);

            } else {
                destinationFound = true;
                destinationPBar.setVisibility(View.INVISIBLE);
                destination = new GeoPoint(address.getLatitude(), address.getLongitude());
                Log.d(TAG, "geolocate: input was right:destination " + searchString + destination);
            }
        }

    }

    public void updateCustomerDetails() {
        DocumentReference customerDetails = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        customerDetails.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    customerPhoneNumber = doc.getString("phoneNumber");
                    customerName = doc.getString("name");
                }
            }
        });

    }


    private void sendClientRequest(String id) {

        DocumentReference clientRequest = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("Request").document("ongoing");
        requestProgressDialog.show();

        RequestInformation requestData = new RequestInformation(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), FirebaseAuth.getInstance().getUid(), "Awaiting");

        clientRequest.set(requestData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    requestProgressDialog.dismiss();
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

        acceptanceUpdate = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("Request").document("ongoing");
        final boolean[] notifyAcceptance = {false};

        acceptanceUpdate.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    if (documentSnapshot.getString("accepted").equals("Accepted") && !notifyAcceptance[0]) {
                        Toast.makeText(MapActivity.this, "Your request have been accepted! Hold on for Service Provider!", Toast.LENGTH_LONG).show();
                        notifyAcceptance[0] = true;
                        cancelRequestBtn.setVisibility(View.VISIBLE);
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

    private void sendRideRequest(String id, GeoPoint jouneyPickup, GeoPoint journeyDestination, Long amountTobePaid, GeoPoint serviceProviderGp, String serviceType) {
        final int[] counter = {0};
        timeStamp = Timestamp.now();

        clientRideRequest = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("Request").document(Objects.requireNonNull("ongoing"));
        customerRequest = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(FirebaseAuth.getInstance().getUid()).collection("Request").document(Objects.requireNonNull("ongoing"));


        DocumentReference spJobData = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("RideData").document(FirebaseAuth.getInstance().getUid());


        final DocumentReference customerjobData = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection("JobData").document(id);


        GeneralJobData generalJobData = new GeneralJobData(jouneyPickup, journeyDestination, serviceProviderGp, FirebaseAuth.getInstance().getUid(),
                customerPhoneNumber, customerName, (long) 0, (long) amountTobePaid, "Awaiting",
                false, false, serviceType, timeStamp, "Awaiting", (long) 0, false, false);
        spJobData.set(generalJobData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                clientRideRequest.set(generalJobData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            driverDialog.dismiss();
                            Toast.makeText(MapActivity.this, "Your Ride request has been sent! please hold for your driver.", Toast.LENGTH_LONG).show();
                            clientRideRequest.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    counter[0] = counter[0] + 1;
                                    if (e != null) {
                                        Log.w(TAG, "Listen failed.", e);
                                        return;
                                    }

                                    if (documentSnapshot != null && documentSnapshot.exists()) {

                                        String accepted = documentSnapshot.getString("accepted");
                                        boolean started = documentSnapshot.getBoolean("started");
                                        boolean arrivedPickUp = documentSnapshot.getBoolean("arrived");
                                        boolean cancelRide = documentSnapshot.getBoolean("cancelRide");
                                        boolean endedRide = documentSnapshot.getBoolean("ended");

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                            }
                                        }, 2000);

                                        if (accepted != null) {
                                            if (accepted.equalsIgnoreCase("Accepted") && !started && !endedRide && !arrivedPickUp && !notifiedRiderA) {

                                                ridedata = new GeneralJobData(jouneyPickup, journeyDestination, serviceProviderGp, id,
                                                        serviceProviderPhone, serviceProviderName, (long) 0, (long) amountTobePaid, "Accepted",
                                                        false, false, serviceType, timeStamp, "Accepted", (long) 0, false, false);

                                                customerjobData.set(ridedata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            final AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                                                            builder.setMessage("Your driver is few minutes to your pick up")
                                                                    .setCancelable(false);
                                                            final AlertDialog alert = builder.create();

                                                            new CountDownTimer(3000, 1000) {
                                                                @Override
                                                                public void onTick(long millisUntilFinished) {
                                                                    alert.show();
                                                                }

                                                                @Override
                                                                public void onFinish() {
                                                                    alert.dismiss();
                                                                    notifiedRiderA = true;
                                                                    startRidePage();
                                                                }
                                                            }.start();
                                                        }
                                                    }
                                                });

                                            } else if (accepted.equalsIgnoreCase("Declined")) {

                                                clientRequest.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        makeLongToast("Your request have been declined, please choose another Vehicle");
                                                        Log.w(TAG, "Ride Request declined, clear request data.");
                                                        customerjobData.update("cancelRide", true);
                                                        customerjobData.update("status", "Declined");
                                                    }
                                                });

                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    private void startRidePage() {
        Intent intent = new Intent(MapActivity.this, RidePage.class);
        intent.putExtra("RideData", ridedata);
        intent.putExtra("servicePLongitude", ridedata.getServiceProviderLocation().getLongitude());
        intent.putExtra("servicePLatitude", ridedata.getServiceProviderLocation().getLatitude());
        intent.putExtra("pickupLongitude", ridedata.getServiceLocation().getLongitude());
        intent.putExtra("pickupLatitude", ridedata.getServiceLocation().getLatitude());
        intent.putExtra("destinationLongitude", ridedata.getDestination().getLongitude());
        intent.putExtra("destinationLatitude", ridedata.getDestination().getLatitude());
        intent.putExtra("amountToBePaid", ridedata.getAmountPaid());
        intent.putExtra("locality", locality);
        startActivityForResult(intent, 1);
        overridePendingTransition(0, 0);

    }


    private void calculateDirectionsForRide(GeoPoint gp) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                pickupLocation.getLatitude(),
                pickupLocation.getLongitude()
        );
        Log.d(TAG, "calculateDirections: finished calculating directions.");
        DirectionsApiRequest directions = new DirectionsApiRequest(mDirectionApi);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        gp.getLatitude(),
                        gp.getLongitude()
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

    private void makeLongToast(String s) {
        Toast.makeText(MapActivity.this, s, Toast.LENGTH_LONG).show();
    }

    private void makeshortToast(String s) {
        Toast.makeText(MapActivity.this, s, Toast.LENGTH_SHORT).show();
    }


    private void getServiceProviderDetails(String id) throws IOException {

        // custom dialog
        final Dialog dialog = new Dialog(MapActivity.this);

        // Create a storage reference from our app
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("paicOqj8BqN76qlKXhv56aIWb9k2.jpg");

        final Uri[] downloadUri = new Uri[1];
        mStorage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri[0] = task.getResult();
                }
            }
        });

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
                    serviceProviderName = "";
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
                    serviceProviderName = name;
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
                    ImageView closeDialog = dialog.findViewById(R.id.close_x);
                    closeDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("serviceproviderpictures");
                    storageReference.child(id + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'profile pic'
                            Picasso.with(dialog.getContext()).load(uri).fit().into(pic);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
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

    }

    private void initMap() {// for initializing the map
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
        new getDeviceLocationAsync().execute();

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

    @Override
    public void onInfoWindowClick(Marker marker) {

        try {
            String servType = marker.getSnippet();
            LatLng ltl = marker.getPosition();
            GeoPoint servicePgp = new GeoPoint(ltl.latitude, ltl.longitude);
            if (servType.equalsIgnoreCase("Taxi") || servType.equalsIgnoreCase("Trycycle(Keke)")) {
                getZeetaDriver(Objects.requireNonNull(marker.getTag()).toString(), servicePgp);
            } else {
                getServiceProviderDetails(Objects.requireNonNull(marker.getTag()).toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setJobDataOnCloud(String id) {
        DocumentReference spJobData = null;
        DocumentReference jobData = null;
        jobData = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection("JobData").document(id);
        spJobData = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("JobData").document(FirebaseAuth.getInstance().getUid());

        jobData.set(new GeneralJobData(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), destination, serviceProviderLocation, id, serviceProviderPhone, serviceProviderName, (long) 0, (long) 0, "Awaiting",
                false, false, serviceRendered, timeStamp, "Awaiting", (long) 0, true, false))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("setJobData", "Job data set: client");
                    }
                });
        spJobData.set(new GeneralJobData(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), destination, serviceProviderLocation, FirebaseAuth.getInstance().getUid(), customerPhoneNumber, customerName, (long) 0, (long) 0, "Awaiting",
                false, false, serviceRendered, timeStamp, "Awaiting", (long) 0, true, false))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("setJobData", "Job data set: SPROvider");
                    }
                });


    }

    public String getLocality() {

        Location location = currentLocation;

        String state = "";
        Log.d("testinglocation", "location latitude" + location.getLatitude());
        Log.d("testinglocation", "location longitude" + location.getLongitude());
        if (location.getLongitude() >= 7 && location.getLongitude() < 8) {// abuja's longitude
            Log.d("testinglocation", "abuja longitude" + location.getLatitude());
            if (location.getLatitude() >= 9 && location.getLatitude() < 9.5) {// abuja's latitude
                Log.d("testinglocation", "abuja latitude" + location.getLatitude());
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
                Log.d("stage2", "passed latitude");
                state = state + "GooglePlex";
            }
        } else {
            state = state + "StateNotFound";
        }

        return state;
    }

    private void getZeetaDriver(String id, GeoPoint servicePLocation) {
        DocumentReference driverDetails = null;

        pickUpP = driverDialog.findViewById(R.id.pickupProgressBar);
        destinationPBar = driverDialog.findViewById(R.id.destinationProgressBar);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            driverDetails = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(id);
        }


        driverDetails.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    serviceProviderPhone = "";
                    serviceProviderName = "";
                    pickupLocation = null;
                    destination = null;
                    serviceProviderLocation = null;
                    serviceRendered = "";
                    destinationET = driverDialog.findViewById(R.id.destination_input);
                    pickET = driverDialog.findViewById(R.id.pickup_input);
                    rideEst = driverDialog.findViewById(R.id.ride_estimate);

                    pickET.setAdapter(new PlaceAutoSuggestionAdapter(MapActivity.this, android.R.layout.simple_list_item_1));
                    destinationET.setAdapter(new PlaceAutoSuggestionAdapter(MapActivity.this, android.R.layout.simple_list_item_1));


                    //fetch driver details
                    DocumentSnapshot doc = task.getResult();
                    String vehicleType = (String) doc.get("vehicleType");
                    String number = (String) doc.get("phoneNumber");
                    String name = (String) doc.get("name");

                    String rating = (String) doc.get("rating");
                    String vehicleNumber = (String) doc.get("vehicleNumber");

                    //assign variable for update
                    serviceRendered = serv;
                    serviceProviderPhone = number;
                    serviceProviderName = name;

                    // hide all the progress bars until when needed
                    pickUpP.setVisibility(View.INVISIBLE);
                    destinationPBar.setVisibility(View.INVISIBLE);

                    TextView textName = (TextView) driverDialog.findViewById(R.id.driver_name);
                    textName.setText("" + serviceProviderName);
                    TextView textVehicleType = (TextView) driverDialog.findViewById(R.id.vehicle_type);
                    textVehicleType.setText("" + vehicleType);
                    TextView textVehicleNumber = (TextView) driverDialog.findViewById(R.id.vehicle_licence);
                    textVehicleNumber.setText("" + vehicleNumber);
                    pickET.setText(customerAddress);

                    pickupLocation = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

                    pickET.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (!pickupFirstClick) {
                                pickET.setText("");
                                pickupFirstClick = true;
                            }

                            pickUpP.setVisibility(View.VISIBLE);
                            geolocate("pickup");
                        }
                    });
                    destinationET.requestFocus();

                    destinationET.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            geolocate("destination");
                            if (destination != null && pickupLocation != null) {
                                hideSoftKeyboard(view);
                                startRideEstimation();
                            }
                            Log.d("parent", "item selected is" + parent.getItemAtPosition(position).toString());
                        }
                    });

                    destinationET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                hideSoftKeyboard(v);
                            }
                        }
                    });

                    destinationET.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            destinationPBar.setVisibility(View.VISIBLE);
                            pickupFirstClick = false;
                            geolocate("destination");
                            if (destination != null && pickupLocation != null) {
                                hideSoftKeyboard(v);
                                startRideEstimation();

                            }
                        }
                    });
                }
                driverDialog.show();

                Button cancel = driverDialog.findViewById(R.id.cancel_ride_request);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearLoaderManager();
                        destinationET.setText("");
                        rideEst.setText("");
                        driverDialog.dismiss();
                    }
                });

                Button sendRideBtn = driverDialog.findViewById(R.id.ride_request_button);
                sendRideBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        destinationText = destinationET.getText().toString();
                        sendRideRequest(id, pickupLocation, destination, (long) rideEstimateAmount, servicePLocation, "Taxi");

                    }
                });

            }

        });

    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new RideEstimationLoader(this, pickupLocation, destination);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        rideEst.setText(data);
        String temp = data;
        rideEstimateAmount = getPriceInteger(data);

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    public void startRideEstimation() {
        priceLoaderManager.initLoader(1, null, this);
    }

    public void clearLoaderManager() {
        priceLoaderManager.destroyLoader(1);
    }

    private void hideSoftKeyboard(View v) {
        //MapActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

    /*public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }*/

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
                                ActivityCompat.requestPermissions(MapActivity.this,
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

    public int getPriceInteger(String s) {
        int val1;
        StringBuilder sb = new StringBuilder("");
        for (int i = 1; i < s.length(); i++) {
            sb.append(s.charAt(i));
        }
        val1 = Integer.parseInt(sb.toString());

        return val1;
    }

    private void updateMarkers() {
        Log.d("update marker", "new Location: started");
        DocumentReference locationRef;
        if (markerList != null && markerList.size() >= 1) {

            for (int i = 0; i < markerList.size() - 1; i++) {
                String key;
                int finalI;
                markerList.size();
                key = (Objects.requireNonNull(markerList.get(i).getTag())).toString();
                finalI = i;

                locationRef = FirebaseFirestore.getInstance()
                        .collection(locality)
                        .document(key);
                final GeoPoint[] gp = new GeoPoint[1];

                locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getGeoPoint("geoPoint") != null) {
                                gp[0] = task.getResult().getGeoPoint("geoPoint");
                                assert gp[0] != null;
                                markerList.get(finalI).setPosition(new LatLng(gp[0].getLatitude(), gp[0].getLongitude()));
                            }
                        }

                    }
                });

            }

        }

    }

    private void updateMarkersRunnable() {
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        if (!callbackPresent) {
            mHandler.postDelayed(mRunnable = new Runnable() {
                @Override
                public void run() {
                    updateMarkers();
                    mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
                }
            }, LOCATION_UPDATE_INTERVAL);
            callbackPresent = true;
        }

    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        AlertDialog internetAlertDialog;
        if (!isConnected) {
            internetAlertDialog = new AlertDialog.Builder(this).create();
            internetAlertDialog.setTitle("Connectivity");
            internetAlertDialog.setMessage("Please check that you are connected to the internet");
            internetAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            internetAlertDialog.show();
        } else {
            internetAlertDialog = new AlertDialog.Builder(this).create();
            internetAlertDialog.setMessage("Internet connection reestablished!");
            internetAlertDialog.setInverseBackgroundForced(true);

            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // internetAlertDialog.show();
                }

                @Override
                public void onFinish() {
                    //internetAlertDialog.dismiss();
                }
            }.start();

        }
    }

    public class getDeviceLocationAsync extends AsyncTask<String, String, String> {

        public LocationManager mLocationManager;

        @Override
        protected void onPreExecute() {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            checkLocationPermission();
        }

        @Override
        protected void onPostExecute(String s) {
            //move camera to current location on map
            if (currentLocation != null) {
                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My location");
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected String doInBackground(String... strings) {
            locationManager = (LocationManager) MapActivity.this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            Log.d(TAG, "getDeviceLocation: getting the device current location");

            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

            try {
                if (mLocationPermissionGranted) {// check first to see if the permission is granted
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                    mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                        @Override
                        public void onComplete(@NonNull Task<android.location.Location> task) {
                            if (task.isComplete()) {
                                Location location = task.getResult();
                                currentLocation = location;
                                try {

                                    locality = getLocalityName(MapActivity.this, location.getLatitude(), location.getLongitude());

                                    Log.d("Locality", "StateOfExcecution " + getLocalityName(MapActivity.this, location.getLatitude(), location.getLongitude()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                customerAddress = getCompleteAddressString(location.getLatitude(), location.getLongitude());

                                Log.d("address", "customer address" + customerAddress);

                                //move camera to current location on map
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My location");

                            }
                        }
                    });
                }
            } catch (SecurityException e) {
                Log.d(TAG, "getDeviceLocation: SecurityException:" + e.getMessage());
            }

            return null;
        }
    }

}
