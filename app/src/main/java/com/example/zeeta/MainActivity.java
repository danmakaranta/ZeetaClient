package com.example.zeeta;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public double latitude;
    public double longitude;
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;
    Intent intentThatCalled;
    String voice2text; //added

    public static boolean isLocationEnabled(Context context) {
        //...............
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init();
        secondInit();
        intentThatCalled = getIntent();
        voice2text = intentThatCalled.getStringExtra("v2txt");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //getLocation();
        }

    }


    private void secondInit() {
        FirebaseApp.initializeApp(this);
        Intent intent = new Intent(MainActivity.this, StartPage.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }


    @Override
    public void onLocationChanged(Location location) {
        //Hey, a non null location! Sweet!

        //remove location callback:
        locationManager.removeUpdates(this);

        //open the map:
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
        // searchNearestPlace(voice2text);
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

    public void searchNearestPlace(String v2txt) {
        Log.e("TAG", "Started");
        v2txt = v2txt.toLowerCase();
        String[] placesS = {"accounting", "airport", "aquarium", "atm", "attraction", "bakery", "bakeries", "bank", "bar", "cafe", "campground", "casino", "cemetery", "cemeteries", "church", "courthouse", "dentist", "doctor", "electrician", "embassy", "embassies", "establishment", "finance", "florist", "food", "grocery", "groceries", "supermarket", "gym", "health", "hospital", "laundry", "laundries", "lawyer", "library", "libraries", "locksmith", "lodging", "mosque", "museum", "painter", "park", "parking", "pharmacy", "pharmacies", "physiotherapist", "plumber", "police", "restaurant", "school", "spa", "stadium", "storage", "store", "synagog", "synagogue", "university", "universities", "zoo"};
        String[] placesM = {"amusement park", "animal care", "animal care", "animal hospital", "art gallery", "art galleries", "beauty salon", "bicycle store", "book store", "bowling alley", "bus station", "car dealer", "car rental", "car repair", "car wash", "city hall", "clothing store", "convenience store", "department store", "electronics store", "electronic store", "fire station", "funeral home", "furniture store", "gas station", "general contractor", "hair care", "hardware store", "hindu temple", "home good store", "homes good store", "home goods store", "homes goods store", "insurance agency", "insurance agencies", "jewelry store", "liquor store", "local government office", "meal delivery", "meal deliveries", "meal takeaway", "movie rental", "movie theater", "moving company", "moving companies", "night club", "pet store", "place of worship", "places of worship", "post office", "real estate agency", "real estate agencies", "roofing contractor", "rv park", "shoe store", "shopping mall", "subway station", "taxi stand", "train station", "travel agency", "travel agencies", "veterinary care"};
        int index;
        for (int i = 0; i <= placesM.length - 1; i++) {
            Log.e("TAG", "forM");
            if (v2txt.contains(placesM[i])) {
                Log.e("TAG", "sensedM?!");
                index = i;
                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + placesM[index]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                finish();
            }
        }
        for (int i = 0; i <= placesS.length - 1; i++) {
            Log.e("TAG", "forS");
            if (v2txt.contains(placesS[i])) {
                Log.e("TAG", "sensedS?!");
                index = i;
                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + placesS[index]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                finish();
            }
        }
    }

}
