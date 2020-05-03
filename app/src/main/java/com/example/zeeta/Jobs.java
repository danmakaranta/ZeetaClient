package com.example.zeeta;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Jobs extends AppCompatActivity {

    private ArrayList<String> selectedServices;
    final ArrayList<CompletedJobs> completedjobsList = new ArrayList<CompletedJobs>();
    CollectionReference jobsOnCloud = FirebaseFirestore.getInstance()
            .collection("Customers")
            .document(FirebaseAuth.getInstance().getUid()).collection("JobData");

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        selectedServices = (ArrayList<String>) getIntent().getSerializableExtra("RequestedServices");

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
                        startActivity(new Intent(getApplicationContext(), MapActivity.class).putExtra("RequestedServices", selectedServices));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.dashboard_button:
                        startActivity(new Intent(getApplicationContext(), DashBoard.class).putExtra("RequestedServices", selectedServices));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        if (isInternetConnection()) {
            populateJobList();
        } else {
            Toast.makeText(Jobs.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
        }

    }

    private void populateJobList() {

        jobsOnCloud.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> docList = task.getResult().getDocuments();

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String name = document.getData().get("serviceProviderName").toString();
                        String jobRendered = document.getData().get("serviceRendered").toString();
                        Timestamp date = (Timestamp) document.getData().get("timeStamp");
                        String jobStatus = document.getData().get("status").toString();
                        String employeeID = document.getData().get("serviceProviderID").toString();
                        String phonenumber = document.getData().get("serviceProviderPhoneNumber").toString();

                        completedjobsList.add(new CompletedJobs(name, date, jobRendered, jobStatus, employeeID, phonenumber));

                        ListAdapter myAdapter = new CompletedJobsAdapter(Jobs.this, completedjobsList, 1);
                        ListView myListView = (ListView) findViewById(R.id.jobs_completed2);
                        myListView.setAdapter(myAdapter);


                        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                DocumentReference invoice;
                                CompletedJobs jobData = (CompletedJobs) myListView.getItemAtPosition(position);

                                String jobStatus = jobData.getStatus();

                                if (jobStatus.equalsIgnoreCase("Completed")) {
                                    Toast.makeText(Jobs.this, "This Job has been completed!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // custom dialog
                                    final Dialog dialog = new Dialog(Jobs.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setContentView(R.layout.invoice);
                                    dialog.setTitle("Invoice");
                                    String employeeID = jobData.getEmployeeID();

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
                                                long tot = (long) doc.get("amount");
                                                total.setText("N" + tot);
                                                TextView hoursRate = dialog.findViewById(R.id.hours_rate);
                                                int hrate = (int) (tot / hrs);
                                                hoursRate.setText("N" + hrate);
                                                paymentBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (tot <= 0) {
                                                            Toast.makeText(Jobs.this, "Request for an invoice from the " + jobData.getJob(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }


                                            Button callBtn = dialog.findViewById(R.id.call);
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

                                    TextView textName = dialog.findViewById(R.id.invoiceName);
                                    textName.setText(jobData.getName());

                                    TextView textProf = dialog.findViewById(R.id.job_done);
                                    textProf.setText("Service: " + jobData.getJob());

                                    Button clsJob = dialog.findViewById(R.id.close_job);
                                    Button makePayment = dialog.findViewById(R.id.make_payment);
                                    Button reportE = dialog.findViewById(R.id.report_service_provider);

                                    clsJob.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();


                                }


                            }
                        });

                    }
                    if (docList.size() >= 1) {

                    } else {
                        Toast.makeText(Jobs.this, "You do not have previously executed jobs", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
    
}
