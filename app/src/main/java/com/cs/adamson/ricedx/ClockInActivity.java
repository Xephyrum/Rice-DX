package com.cs.adamson.ricedx;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cs.adamson.ricedx.DBHelper;
//import com.cs.adamson.ricedx.SessionManager;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ClockInActivity extends AppCompatActivity {

    private Button btnViewData;
    //private Button btnClockIn;
    private Button btnForecast;

    private TextView lblDate;
    private TextView lblLocation;
    private TextView lblWeather;
    private TextView lblWind;
    private TextView lblAir;
    private TextView lblSoil;
    private TextView lblPrediction;

    private SwipeRefreshLayout swipeContainer;

    private TextView lblStatus;
    private TextView lblGps;
    private TextView lblInternet;
    private ProgressDialog pDialog;
    private ProgressDialog sendDialog;
    //private SessionManager session;
    private String lat;
    private String lng;

    private double currentWindSpeed;
    private double currentAirTemp;
    private double currentSoilTemp;
    private double currentCavans;

    private double lat1 = 14.993056;
    private double lng1 = 120.776944;

    private DBHelper myDb;

    /*@Override
    protected void onStop(){
        unregisterReceiver(mConnReceiver);
        super.onStop();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_in);

        lblStatus = (TextView) findViewById(R.id.lblStatus);
        lblGps = (TextView) findViewById(R.id.lblGps);

        lblDate = (TextView) findViewById(R.id.lblDate);
        lblLocation = (TextView) findViewById(R.id.lblLocation);
        lblWeather = (TextView) findViewById(R.id.lblWeather);
        lblWind = (TextView) findViewById(R.id.lblWind);
        lblAir = (TextView) findViewById(R.id.lblAir);
        lblSoil = (TextView) findViewById(R.id.lblSoil);
        lblPrediction = (TextView) findViewById(R.id.lblPrediction);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        sendDialog = new ProgressDialog(this);
        sendDialog.setCancelable(false);

        lat = "0";
        lng = "0";

        /*btnViewData = (Button) findViewById(R.id.btnViewData);
        btnViewData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ClockInActivity.this, TableActivity.class);
                startActivity(intent);
            }
        }); FOR TESTING*/

        /*swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getWeather(true);
                myDb.getLineChartData();
                swipeContainer.setRefreshing(false);
            }
        }); FOR TESTING */

        /*swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light); TEST*/

        /*btnForecast = (Button) findViewById(R.id.btnForecast);
        btnForecast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ClockInActivity.this, ForecastActivity.class);
                startActivity(intent);
            }
        }); TESTING*/

        myDb = new DBHelper(this);
        myDb.getReadableDatabase();

        //session = new SessionManager(getApplicationContext());
        this.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        getWeather(true);

    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            lblInternet = (TextView) findViewById(R.id.lblInternet);
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailOver = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            /*if (currentNetworkInfo.isConnected()) {
                lblInternet.setBackgroundColor(Color.GREEN);
                lblInternet.setText("Connected to Internet");
            } else {
                lblInternet.setBackgroundColor(Color.RED);
                lblInternet.setText("No Internet Connection");
            }TEST*/
        }
    };

    private void clockInError(String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ClockInActivity.this);
        dlgAlert.setMessage(message);
        //dlgAlert.setIcon(R.drawable.close);
        dlgAlert.setTitle("Error");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void predictValue() {
        double mlr = myDb.calculateMLR(currentWindSpeed, currentSoilTemp);
        currentCavans = mlr;
        lblPrediction.setText("Prediction: " + Double.toString(currentCavans) + " cavans");
        myDb.insertPrediction(currentWindSpeed, currentSoilTemp, currentCavans);
    }

    private void getWeather(boolean forceCoords) {
        sendDialog.setMessage("Getting Weather Updates ...");
        showSendDialog();
        RequestQueue queue = Volley.newRequestQueue(this);
        //String empID = session.getEmployeeID(ClockInActivity.this);

        String url;


        if (forceCoords) {
            url = "http://api.apixu.com/v1/current.json?key=b77f6395137c4a64a71143843180707&q=14.993056,120.776944";
            checkGPS();

        } else {
            checkGPS();
            Log.v("TAG", "Plugging in coordinates");

            url = "http://api.apixu.com/v1/current.json?key=b77f6395137c4a64a71143843180707&q=" + lat + "," + lng;

        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            hideSendDialog();
                            try {
                                JSONObject jObject = new JSONObject(response);

                                String location = jObject.getString("location");
                                JSONObject jLocation = new JSONObject(location);

                                String name = jLocation.getString("name");
                                String region = jLocation.getString("region");

                                String current = jObject.getString("current");
                                JSONObject jCurrent = new JSONObject(current);

                                String condition = jCurrent.getString("condition");
                                JSONObject jCondition = new JSONObject(condition);

                                String weather = jCondition.getString("text");
                                String wind = jCurrent.getString("wind_kph");
                                String air = jCurrent.getString("temp_c");

                                if (Integer.parseInt(air) > 40)
                                    lblAir.setTextColor(Color.RED);
                                else
                                    lblAir.setTextColor(Color.BLACK);

                                if (Integer.parseInt(air) + 4 > 40)
                                    lblSoil.setTextColor(Color.RED);
                                else
                                    lblSoil.setTextColor(Color.BLACK);

                                double soilTemp = Double.parseDouble(air) + 4;
                                double windSpeed = Double.parseDouble(wind);

                                lblLocation.setText("Location: " + name + ", " + region);
                                lblWeather.setText(weather);
                                lblWind.setText(wind + "KPH");
                                lblAir.setText(air + "'C");

                                String soil = Double.toString(soilTemp);
                                lblSoil.setText(soil + "'C");
                                currentAirTemp = Double.parseDouble(air);
                                currentSoilTemp = soilTemp;
                                currentWindSpeed = windSpeed;

                                predictValue();
                                return;
                            } catch (JSONException e) {
                                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ClockInActivity.this);
                                dlgAlert.setMessage(e.getMessage());
                                //dlgAlert.setIcon(R.drawable.close);
                                dlgAlert.setTitle("ERROR");
                                dlgAlert.setPositiveButton("OK", null);
                                dlgAlert.setCancelable(true);
                                dlgAlert.create().show();
                            }
                            Date curDate = new Date();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                            String DatetoStr = format.format(curDate);
                            lblDate.setText(DatetoStr);
                        } else {
                            Log.v("TAG", "No Response");
                            lblStatus.setText("Request failed!");
                            hideSendDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("TAG", "Response Error");
                lblStatus.setText("Request failed!");
                hideSendDialog();
            }
        });
        queue.add(stringRequest);
    }

    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b){
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    private void checkGPS() {
        Log.v("TAG", "Checking GPS");
        pDialog.setMessage("Checking GPS...");
        showDialog();

        LocationManager locationManager = (LocationManager) ClockInActivity.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.v("TAG", "onLocationChanged Working");
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                lblStatus.setText(latitude + ", " + longitude);
                lat = String.valueOf(latitude);
                lng = String.valueOf(longitude);
                hideDialog();

                //Updates weather data for every 500 meters travelled
                if(meterDistanceBetweenPoints((float)latitude, (float)longitude, (float)lat1, (float)lng1) > 500){
                    getWeather(false);
                    lat1 = latitude;
                    lng1 = longitude;
                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
                Log.v("TAG", "GPS Enabled");
                lblGps.setBackgroundColor(Color.GREEN);
                lblGps.setText("GPS Enabled");
                hideDialog();
                lblStatus.setText("Locating...");
                getWeather(true);

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.v("TAG", "GPS Disabled");
                lblGps.setBackgroundColor(Color.RED);
                lblGps.setText("GPS Disabled");
                hideDialog();
                lat = "";
                lng = "";
                lblStatus.setText("");
            }
        };

        if(Build.VERSION.SDK_INT < 23)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        else{
            if (ActivityCompat.checkSelfPermission(ClockInActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ClockInActivity.this, "Please enable LOCATION ACCESS in the settings", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 500, locationListener);
                Log.v("TAG", "GPS STATUS:" + Boolean.toString(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
                Log.v("TAG", "Requesting GPS " + lat +", " + lng);
                hideDialog();
            }
        }
    }

    private void showDialog(){
        if(!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog(){
        if(pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showSendDialog(){
        if(!sendDialog.isShowing())
            sendDialog.show();
    }

    private void hideSendDialog(){
        if(sendDialog.isShowing())
            sendDialog.dismiss();
    }

}
