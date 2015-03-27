package jp.tdu.ics.indoorpositioningsystem.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class MainActivity extends ActionBarActivity {
    IntentFilter intentFilter;
    WifiManager wifiManager;
    WiFiReceiver wifiReceiver;
    final private String icsGlobalIpAddrss = "133.20.243.197";
    final private String icsPrivateIpAddress = "192.168.11.9";
    final private int PORT = 6666;
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
            Map<String,List<Integer>> wifidata = new TreeMap<>();
            for (ScanResult scanResult : scanResultList) {
                String bssid = scanResult.BSSID;
                int rssi = scanResult.level;
                if(!wifidata.containsKey(bssid)){
                    List<Integer> rssiList = new ArrayList<>();
                    rssiList.add(rssi);
                    wifidata.put(bssid, rssiList);
                }else{
                    wifidata.get(bssid).add(rssi);
                }
                //System.out.println(bssid + " " + rssi);
            }
            postWiFiData(wifidata);
            unregisterReceiver(this);
        }
    }

    public void  postWiFiData(final Map<String, List<Integer>> wifiData){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Socket socket = null;
                String message = "";
                try {
                    socket = new Socket(icsGlobalIpAddrss, PORT);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(wifiData);


                    InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receivedMessage;
                    while ((receivedMessage = bufferedReader.readLine()) != null) {
                        message += receivedMessage;
                    }
                    System.out.println(message);

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return message;
            }

            @Override
            protected void onPostExecute(String message){
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }
}

