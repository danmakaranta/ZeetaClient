package com.example.zeeta;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.zeeta.adapters.TransactionsAdapter;
import com.example.zeeta.data.Card;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class DashBoard extends AppCompatActivity {

    CollectionReference myTransactions = FirebaseFirestore.getInstance()
            .collection("Customers")
            .document(FirebaseAuth.getInstance().getUid()).collection("Transactions");
    private double walletBalance;
    private TextView dashBoardWallet;
    private RatingBar dashBoardRating;
    private TextView dashBoardCustomerName;
    private TextView dashBoardPhoneNumber;
    private ArrayList<TransactionData> transactionsList = new ArrayList<TransactionData>();
    private double waletBalance;
    private String userRating;
    private String customerName;
    private String phoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        dashboardUpdate();

        dashBoardWallet = findViewById(R.id.dashboardWallet);
        dashBoardRating = findViewById(R.id.ratingDashBoard);
        dashBoardCustomerName = findViewById(R.id.dashboard_name);
        dashBoardPhoneNumber = findViewById(R.id.dashboard_phone_number);

        Button logout = findViewById(R.id.logout_btn);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Signin.class));
                overridePendingTransition(0, 0);
            }
        });


        populateTransactionsList();


        //initialize and assign variables for the bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        //set home icon selected
        bottomNavigationView.setSelectedItemId(R.id.dashboard_button);
        //perform itemselectedlistener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.dashboard_button:
                        return true;
                    case R.id.jobs_button:
                        startActivity(new Intent(getApplicationContext(), Jobs.class));
                        return true;
                    case R.id.home_button:
                        startActivity(new Intent(getApplicationContext(), MapActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

    }


    private void populateTransactionsList() {
        myTransactions.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> docList = task.getResult().getDocuments();

                    if (docList.size() >= 1) {

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String details = document.getData().get("detail").toString();
                            Timestamp date = (Timestamp) document.getData().get("date");
                            Long amount = (Long) document.getData().get("amountPaid");
                            String typeOfTransaction = document.getData().get("type").toString();
                            Card card = (Card) document.getData().get("card");
                            assert amount != null;
                            Double amountPaid = amount.doubleValue();
                            String customerID = document.getData().get("customerID").toString();
                            boolean paidArtisan = document.getBoolean("paidArtisan");
                            transactionsList.add(new TransactionData(details, customerID, paidArtisan, amountPaid.longValue(), date, card, typeOfTransaction));
                            ListAdapter transactionsAdapter = new TransactionsAdapter(DashBoard.this, transactionsList, 1);
                            ListView myListView = (ListView) findViewById(R.id.transactions_list);

                            myListView.setAdapter(transactionsAdapter);

                        }

                    } else {

                    }
                }
            }
        });

    }


    private void dashboardUpdate() {

        DocumentReference waletref = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            waletref = FirebaseFirestore.getInstance()
                    .collection("Customers")
                    .document(Objects.requireNonNull(getInstance().getUid())).collection("Wallet").document("ZeetaAccount");
        }

        if (waletref != null) {
            waletref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            Long tempBalance = (Long) doc.getLong("balance");
                            if (tempBalance != null) {
                                waletBalance = tempBalance.doubleValue();
                                dashBoardWallet.setText("Wallet :N" + waletBalance);
                            }
                        }
                    }
                }
            });
        }

        DocumentReference rating = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            rating = FirebaseFirestore.getInstance()
                    .collection("Customers")
                    .document(Objects.requireNonNull(getInstance().getUid()));
        }

        rating.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String tempRating = doc.getString("rating");
                        userRating = tempRating;
                        dashBoardRating.setRating(Float.parseFloat(userRating));
                        phoneNumber = doc.getString("phoneNumber");
                        customerName = doc.getString("name");
                        dashBoardCustomerName.setText(customerName);
                        dashBoardPhoneNumber.setText(phoneNumber);
                    }
                }
            }
        });

    }

}
