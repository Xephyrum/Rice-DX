package com.cs.adamson.ricedx;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import android.view.View;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnActivate;
    private ImageButton btnExit;
    private TextView lblInternet;
    private ProgressDialog pDialog;
    private String myMacAddress;

    @Override
    protected void onStop(){
        unregisterReceiver(mConnReceiver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        btnActivate = (ImageButton)findViewById(R.id.btnActivate);
        btnExit = (ImageButton)findViewById(R.id.btnExit);

        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        myMacAddress = wInfo.getMacAddress();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btnActivate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                proceed();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndRemoveTask();
            }
        });

        this.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            lblInternet = (TextView)findViewById(R.id.lblInternet);
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            if(currentNetworkInfo.isConnected()){
                lblInternet.setTextColor(Color.GREEN);
                lblInternet.setText("Connected to Internet");
            }
            else{
                lblInternet.setTextColor(Color.RED);
                lblInternet.setText("No Internet Connection");
            }
        }
    };

    private void proceed() {
        Intent intent = new Intent(MainActivity.this, ClockInActivity.class);
        startActivity(intent);
        finish();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}

