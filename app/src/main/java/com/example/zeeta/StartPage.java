package com.example.zeeta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.firebase.auth.FirebaseAuth;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class StartPage extends AppCompatActivity implements InternetConnectivityListener {

    private static final String TAG = "STARTPAGE";
    //Firebase
    FirebaseAuth.AuthStateListener mAuthListener;
    private InternetAvailabilityChecker mInternetAvailabilityChecker;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        InternetAvailabilityChecker.init(this);
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // do nothing 1s
            }

            @Override
            public void onFinish() {
                // do something end times 5s
                /*Intent intent = new Intent(StartPage.this, Signin.class);
                startActivity(intent);
                overridePendingTransition(0, 0);*/
                Intent intent = new Intent(StartPage.this, MapActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }

        }.start();

    }

    @Override
    public void onBackPressed() {

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
