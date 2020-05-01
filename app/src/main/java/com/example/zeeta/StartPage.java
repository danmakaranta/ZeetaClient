package com.example.zeeta;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

public class StartPage extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        if (isInternetConnection()) {
            new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // do nothing 1s
                }

                @Override
                public void onFinish() {
                    // do something end times 5s
                    Intent intent = new Intent(StartPage.this, Signin.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }

            }.start();

        } else {
            Toast.makeText(this, "Please check that you are connected to the internet!", Toast.LENGTH_SHORT).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getActiveNetworkInfo()).isConnectedOrConnecting();
    }

}
