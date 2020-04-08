package com.example.zeeta;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.zeeta.adapters.CompletedJobsAdapter;
import com.example.zeeta.models.CompletedJobs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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
            .document(FirebaseAuth.getInstance().getUid()).collection("Jobs");

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

                        String name = document.getData().get("employeeName").toString();
                        String jobRendered = document.getData().get("serviceRendered").toString();
                        Timestamp date = (Timestamp) document.getData().get("dateRendered");
                        String jobStatus = document.getData().get("status").toString();
                        String employeeID = document.getData().get("employeeID").toString();

                        completedjobsList.add(new CompletedJobs(name, date, jobRendered, jobStatus, employeeID));

                        ListAdapter myAdapter = new CompletedJobsAdapter(Jobs.this, completedjobsList, 1);
                        ListView myListView = (ListView) findViewById(R.id.jobs_completed2);
                        myListView.setAdapter(myAdapter);
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
