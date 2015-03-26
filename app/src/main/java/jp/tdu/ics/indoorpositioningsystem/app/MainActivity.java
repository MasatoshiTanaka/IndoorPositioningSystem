package jp.tdu.ics.indoorpositioningsystem.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    IntentFilter intentFilter;
    WifiManager wifiManager;
    WiFiReceiver wifiReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button estimateButton = (Button)findViewById(R.id.button);

        estimateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                estimateButtonAction();
            }
        });
    }

    private void estimateButtonAction(){
        intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiReceiver = new WiFiReceiver();
        registerReceiver(wifiReceiver, intentFilter);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiManager.startScan();
    }

    class WiFiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> scanResultList = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResultList) {
                System.out.println(scanResult.BSSID + " " + scanResult.level);
            }
            unregisterReceiver(this);
        }
    }
}

