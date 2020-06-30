package com.example.zeeta;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zeeta.adapters.CompletedJobsAdapter;
import com.example.zeeta.models.CompletedJobs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class Jobs extends AppCompatActivity implements InternetConnectivityListener {

    private ArrayList<String> selectedServices;
    final ArrayList<CompletedJobs> completedjobsList = new ArrayList<CompletedJobs>();
    CollectionReference jobsOnCloud = FirebaseFirestore.getInstance()
            .collection("Customers")
            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection("JobData");
    private DocumentReference serviceProviderJobs;
    private int hourlyrate;
    private ProgressDialog loadingProgressDialog;
    private InternetAvailabilityChecker mInternetAvailabilityChecker;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        selectedServices = (ArrayList<String>) getIntent().getSerializableExtra("RequestedServices");
        loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setMessage("Loading....");

        InternetAvailabilityChecker.init(this);
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);

        //initialize and assign variables for the bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        //set home icon selected
        bottomNavigationView.setSelectedItemId(R.id.jobs_button);
        //perform itemselectedlistener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.jobs_button:
                        return true;
                    case R.id.home_button:
                        startActivityForResult(new Intent(getApplicationContext(), MapActivity.class), 1);
                        return true;
                    case R.id.services_list:
                        startActivity(new Intent(getApplicationContext(), MapActivity.class));
                        // overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        populateJobList();

    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    private void populateJobList() {

        jobsOnCloud.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> docList = task.getResult().getDocuments();
                    if (!docList.isEmpty()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String name = (document.getData().get("name")).toString();
                            String jobRendered = (document.getData().get("serviceRendered")).toString();
                            Timestamp date = (Timestamp) document.getData().get("timeStamp");
                            String jobStatus = (document.getData().get("status")).toString();
                            String employeeID = (document.getData().get("serviceID")).toString();
                            String phonenumber = (document.getData().get("phoneNumber")).toString();
                            long tempPaid = (long) (document.getData().get("amountPaid"));
                            int tempInt = safeLongToInt(tempPaid);
                            double amountPaid = (double) tempInt;

                            completedjobsList.add(new CompletedJobs(name, date, jobRendered, jobStatus, employeeID, phonenumber, amountPaid));

                            ListAdapter myAdapter = new CompletedJobsAdapter(Jobs.this, completedjobsList, 1);
                            ListView myListView = (ListView) findViewById(R.id.jobs_completed2);

                            myListView.setAdapter(myAdapter);

                            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    DocumentReference invoice;
                                    CompletedJobs jobData = (CompletedJobs) myListView.getItemAtPosition(position);

                                    String jobStatus = jobData.getStatus();
                                    // custom dialog
                                    final Dialog dialog = new Dialog(Jobs.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setContentView(R.layout.invoice);
                                    dialog.setTitle("Invoice");
                                    String employeeID = jobData.getEmployeeID();
                                    ImageView closeDialog = dialog.findViewById(R.id.close_x_invoice);
                                    Button callBtn = dialog.findViewById(R.id.call);
                                    invoice = FirebaseFirestore.getInstance().collection("Customers")
                                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                            .collection("Invoice").document(employeeID);
                                    invoice.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @RequiresApi(api = Build.VERSION_CODES.N)
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            DocumentSnapshot doc = task.getResult();
                                            TextView hours = dialog.findViewById(R.id.invoice_hours);
                                            Button paymentBtn = dialog.findViewById(R.id.make_payment);

                                            if (doc.exists()) {
                                                Long hrs = (Long) doc.getData().get("hoursWorked");
                                                hours.setText("" + hrs);
                                                TextView total = dialog.findViewById(R.id.total_earned);
                                                //long tot = (long) doc.get("amount");
                                                double totDouble = (double) doc.get("amount");
                                                total.setText("N" + totDouble);
                                                TextView hoursRate = dialog.findViewById(R.id.hours_rate);
                                                hourlyrate = (int) (totDouble / hrs);
                                                if (hourlyrate <= 0) {
                                                    hourlyrate = getServiceProviderRate(employeeID);
                                                }

                                                try {// nothing more but to slow down execution a bit to get results before proceeding
                                                    Thread.sleep(2000);
                                                } catch (InterruptedException excp) {
                                                    excp.printStackTrace();
                                                }
                                                hoursRate.setText("N" + hourlyrate);
                                                loadingProgressDialog.dismiss();
                                                paymentBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (totDouble <= 0) {
                                                            loadingProgressDialog.dismiss();
                                                            Toast.makeText(Jobs.this, "Request for an invoice from the " + jobData.getJob(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            } else {


                                                DocumentReference job = FirebaseFirestore.getInstance()
                                                        .collection("Customers")
                                                        .document(Objects.requireNonNull(getInstance().getUid())).collection("JobData").document(jobData.getEmployeeID());
                                                job.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot doc = task.getResult();
                                                            if (doc.exists()) {
                                                                Long hrs = (Long) doc.getData().get("hoursWorked");
                                                                hours.setText("" + hrs);
                                                                TextView total = dialog.findViewById(R.id.total_earned);
                                                                long amountPaidLong = (long) doc.get("amountPaid");
                                                                int tempInt = safeLongToInt(amountPaidLong);
                                                                total.setText("N" + amountPaid);
                                                                TextView hoursRate = dialog.findViewById(R.id.hours_rate);
                                                                if (hrs > 0) {
                                                                    hourlyrate = (int) (tempInt / hrs);
                                                                }
                                                                if (hourlyrate <= 0) {
                                                                    hourlyrate = getServiceProviderRate(employeeID);
                                                                }
                                                                loadingProgressDialog.dismiss();
                                                            }
                                                        }
                                                    }
                                                });

                                            }
                                            callBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", jobData.getPhoneNumber(), null));
                                                    if (ActivityCompat.checkSelfPermission(Jobs.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                                        return;
                                                    }
                                                    startActivity(intent);
                                                    overridePendingTransition(0, 0);
                                                }
                                            });

                                        }
                                    });
                                    DocumentReference rating = null;
                                    final float[] currentRating = new float[1];
                                    final float[] newRating = new float[1];
                                    final float[] userRating = new float[1];
                                    RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        rating = FirebaseFirestore.getInstance()
                                                .collection("Users")
                                                .document(jobData.getEmployeeID());
                                        rating.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    currentRating[0] = Float.parseFloat(Objects.requireNonNull(documentSnapshot.getString("rating")));
                                                    ratingBar.setRating(currentRating[0]);

                                                }
                                            }
                                        });
                                    }
                                    ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                        @Override
                                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                                            userRating[0] = rating;
                                        }
                                    });

                                    TextView textName = dialog.findViewById(R.id.invoiceName);
                                    textName.setText(jobData.getName());

                                    TextView textProf = dialog.findViewById(R.id.job_done);
                                    textProf.setText("Service: " + jobData.getJob());
                                    loadingProgressDialog.dismiss();

                                    Button clsJob = dialog.findViewById(R.id.close_job);
                                    Button makePayment = dialog.findViewById(R.id.make_payment);
                                    Button reportE = dialog.findViewById(R.id.report_service_provider);

                                    closeDialog.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });


                                    DocumentReference finalRating = rating;
                                    clsJob.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            newRating[0] = (currentRating[0] + userRating[0]) / 2;
                                            serviceProviderJobs = FirebaseFirestore.getInstance()
                                                    .collection("Users")
                                                    .document(employeeID)
                                                    .collection("JobData").document(FirebaseAuth.getInstance().getUid());
                                            DocumentReference customerJobs = FirebaseFirestore.getInstance()
                                                    .collection("Customers")
                                                    .document(FirebaseAuth.getInstance().getUid())
                                                    .collection("JobData").document(employeeID);
                                            serviceProviderJobs.update("status", "Closed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        customerJobs.update("status", "Closed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    DecimalFormat df = new DecimalFormat("#.#");
                                                                    String temp = "" + newRating[0];
                                                                    double tempD = Double.valueOf(temp);
                                                                    String newArtisanRating = "" + df.format(tempD);
                                                                    finalRating.update("rating", newArtisanRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    });

                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            });

                                        }
                                    });
                                    if (jobStatus.equalsIgnoreCase("Completed")) {
                                        Toast.makeText(Jobs.this, "This Job has been completed!", Toast.LENGTH_SHORT).show();
                                        clsJob.setEnabled(false);
                                        makePayment.setEnabled(false);
                                        callBtn.setEnabled(false);
                                    } else if (jobStatus.equalsIgnoreCase("Canceled by You")) {
                                        Toast.makeText(Jobs.this, "This Job has been canceled by You!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        dialog.show();
                                        loadingProgressDialog.show();
                                    }

                                }
                            });

                        }
                    }

                    if (docList.size() < 1) {
                        Toast.makeText(Jobs.this, "You do not have previously executed jobs", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

    }


    private int getServiceProviderRate(String employeeID) {
        DocumentReference hourlyrate = FirebaseFirestore.getInstance()
                .collection("Users").document(employeeID);
        final int[] employeeHourlyRate = new int[1];
        hourlyrate.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    employeeHourlyRate[0] = safeLongToInt((long) doc.get("hourlyRate"));
                }
            }
        });
        return employeeHourlyRate[0];
    }


    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        AlertDialog alertDialog;
        if (!isConnected) {
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Connectivity");
            alertDialog.setMessage("Please check that you are connected to the internet");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }
}
