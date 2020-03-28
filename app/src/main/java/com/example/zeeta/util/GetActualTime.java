package com.example.zeeta.util;

import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class GetActualTime extends AsyncTask<String, Void, String> {
    @Override
    protected void onPostExecute(String time) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mdformat = new SimpleDateFormat("h:mm");
        }
        String times = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            times = mdformat.format(calendar.getTime());
        }
        try {
            String areatime = time.substring(time.indexOf(String.valueOf(times)), time.indexOf(String.valueOf(times)) + 5).trim();

        } catch (IndexOutOfBoundsException e) {

        }
    }


    @Override
    protected String doInBackground(String... urls) {
        try {
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int code = urlConnection.getResponseCode();
                if (code == 200) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null)
                        result.append(line);
                    in.close();
                } else {
                    return "error on fetching";
                }
                return result.toString();
            } catch (MalformedURLException e) {
                return "malformed URL";
            } catch (IOException e) {
                return "io exception";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
            return "null";
        }
    }
}
