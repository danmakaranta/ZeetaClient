package com.example.zeeta;

import android.animation.ArgbEvaluator;
import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.zeeta.adapters.ViewPagerAdapter;
import com.example.zeeta.data.GeneralJobData;
import com.example.zeeta.models.RequestInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class Fashion_samples extends AppCompatActivity {

    private static final String TAG = "Fashion_Samples";
    final String[] serviceProviderPhone = new String[1];
    final String[] serviceProviderName = new String[1];
    ViewPager viewPager;
    Integer[] colors = null;
    ArgbEvaluator evaluator = new ArgbEvaluator();
    ViewPagerAdapter adapter2;
    private ValueEventListener mDBListner;
    private ArrayList<String> imageUrlList;
    private int pageIndex;
    private DatabaseReference mDatabaseRef;
    private List<Upload> mUpload;
    private Button backToPrevidusPage, requestFashionDesigner, call_fashionD;
    private ProgressDialog requestProgressDialog;
    private CountDownTimer waitCountDownTimer;
    private boolean timerRunning = false;
    private long timeInMillis = 15000;
    private String id;
    private Location currentLocation;
    private String customerPhoneNumber = "";
    private String customerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fashion_samples);
        updateCustomerDetails();

        id = getIntent().getStringExtra("ID");
        double longitude = getIntent().getDoubleExtra("Longitude", 0.0);
        double latitude = getIntent().getDoubleExtra("Latitude", 0.0);
        currentLocation = new Location(LocationManager.GPS_PROVIDER);
        currentLocation.setLongitude(longitude);
        currentLocation.setLatitude(latitude);
        DocumentReference serviceProviderData = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id);
        serviceProviderData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    serviceProviderName[0] = (String) doc.get("name");
                    serviceProviderPhone[0] = (String) doc.get("phoneNumber");
                }

            }
        });

        Log.d("testing", "testing, testing" + id);
        backToPrevidusPage = findViewById(R.id.backToMapActivity);
        requestFashionDesigner = findViewById(R.id.request_fashionDesigner);
        call_fashionD = findViewById(R.id.call_fashionDesigner);
        call_fashionD.setVisibility(View.GONE);
        requestProgressDialog = new ProgressDialog(this);
        requestProgressDialog.setMessage("Sending Request....");

        requestFashionDesigner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendClientRequest(id);
            }
        });

        backToPrevidusPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageUrlList = new ArrayList();
        mUpload = new ArrayList<>();
        adapter2 = new ViewPagerAdapter(Fashion_samples.this, imageUrlList);

        mDatabaseRef = FirebaseDatabase.getInstance("https://zeeta-6b4c0.firebaseio.com").getReference("fashionPics").child(id);

        mDBListner = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUpload.clear();
                imageUrlList.clear();
                int i = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    assert upload != null;
                    upload.setmKey(postSnapshot.getKey());
                    mUpload.add(upload);
                    imageUrlList.add(upload.getmImageUrl());
                    i++;
                }

                viewPager = findViewById(R.id.ViewPager);
                viewPager.setAdapter(adapter2);
                viewPager.setPadding(50, 50, 50, 30);
                adapter2.notifyDataSetChanged();

                viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position < (adapter2.getCount() - 1) && position < (colors.length - 1)) {
                            viewPager.setBackgroundColor((Integer) evaluator
                                    .evaluate(positionOffset, colors[position], colors[position + 1]));
                        } else {
                            viewPager.setBackgroundColor(colors[colors.length - 1]);
                        }
                    }

                    @Override
                    public void onPageSelected(int position) {
                        pageIndex = position;
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Integer[] colors_temp = {getResources().getColor(R.color.blue3),
                getResources().getColor(R.color.blue5),
                getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.darkGrey)
        };
        colors = colors_temp;

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
                    /*Intent mapActivityIntent = new Intent(Fashion_samples.this, MapActivity.class);
                    mapActivityIntent.putExtra("listen", true);
                    mapActivityIntent.putExtra("serviceProviderID", id);
                    startActivity(mapActivityIntent);*/

                } else {
                    Log.e(TAG, "sendClientRequest: error sending customer request!");
                }
            }
        });

    }

    private void listenForUpdate(String id) {
        DocumentReference acceptanceUpdate;

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
                        Toast.makeText(Fashion_samples.this, "Your request have been accepted! Hold on for Service Provider!", Toast.LENGTH_LONG).show();
                        notifyAcceptance[0] = true;
                        setJobDataOnCloud(id);

                    } else if (documentSnapshot.getString("accepted").equals("Declined")) {
                        //clear your request since it has been declined
                        acceptanceUpdate.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.w(TAG, "Request not accepted, clear any outstanding request data.");
                            }
                        });

                        Toast.makeText(Fashion_samples.this, "Your request have been declined, please choose another service provider", Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG, "A change has been effected on this doc");
                }

            }
        });
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


    private void setJobDataOnCloud(String id) {

        try {// nothing more but to slow down execution a bit to get results before proceeding
            Thread.sleep(2000);
        } catch (InterruptedException excp) {
            excp.printStackTrace();
        }

        Timestamp timeStamp = Timestamp.now();
        DocumentReference spJobData = null;
        DocumentReference jobData = null;
        jobData = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection("JobData").document(id);
        spJobData = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(id).collection("JobData").document(FirebaseAuth.getInstance().getUid());

        jobData.set(new GeneralJobData(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()), null, null, id, serviceProviderPhone[0], serviceProviderName[0], (long) 0, (long) 0, "Accepted",
                false, false, "Fashion Designer", timeStamp, "Awaiting", (long) 0, true, false))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("setJobData", "Job data set: client");
                    }
                });
        spJobData.set(new GeneralJobData(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()),
                null, null, FirebaseAuth.getInstance().getUid(), customerPhoneNumber, customerName, (long) 0, (long) 0, "Accepted",
                false, false, "Fashion Designer", timeStamp, "Awaiting", (long) 0, false, false))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("setJobData", "Job data set: SPROvider");
                    }
                });

    }

    private void startTimer() {

        waitCountDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeInMillis = millisUntilFinished;
                //updateTime();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                timeInMillis = 10000;
                requestProgressDialog.dismiss();
            }
        }.start();
        timerRunning = true;
    }

    private void updateTime() {
        int minutes = (int) (timeInMillis / 1000) / 60;
        int seconds = (int) (timeInMillis / 1000) % 60;

        String timeformated = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        // wait_timer.setText(timeformated);

    }

}