package it.unipi.dii.trainingstat.gui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ca.hss.heatmaplib.HeatMap;
import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.services.BeaconService;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BLETestActivity extends AppCompatActivity {
    private ProximityManager proximityManager;
    int lastMeasurement;
    private Map<String, Object> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bletest);
        checkPermissions();
        lastMeasurement = 0;
        data = new HashMap<>();
    }

    private void checkPermissions() {
        int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //we should show some explanation for user here
                Toast.makeText(this, "Application need your permission to access location", Toast.LENGTH_SHORT).show();
            } else {
                //request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (100 == requestCode) {
                //same request code as was in request permission
                Log.d("Test", "Permission to access location granted");
            }

        } else {
            //not granted permission
            Log.d("Test", "Permission to access location denied");
        }
    }

    public void manageButton(View v) {
        int buttonId = v.getId();
        TextView messageTV = findViewById(R.id.messagesTV);
        if (buttonId == R.id.startScanning) {
            String beaconDistance = ((EditText) findViewById(R.id.beaconDistancePT)).getText().toString();
            String gait = ((EditText) findViewById(R.id.gaitPT)).getText().toString();
            String path = ((EditText) findViewById(R.id.pathPT)).getText().toString();
            configureProximityManager();
            setupBeaconListener();
            startScanning();
            messageTV.setText("Scanning started");
            String[] cells = path.split(",");
            List<String> cellData = new ArrayList<>();
            cellData.addAll(Arrays.asList(cells));
            data.put("BeaconDistance", beaconDistance);
            data.put("Gait", gait);
            data.put("Path", cellData);
        }
        else {
            if (buttonId == R.id.stopScanning) {
                stopScanning();
                lastMeasurement++;
                String id = "Measure_" + lastMeasurement;
                DatabaseManager databaseManager = new DatabaseManager();
                databaseManager.collectData(id, data);
            }
        }
    }

    private void configureProximityManager() {
        KontaktSDK.initialize("wLvvNlIMqZdHvvTVrhsgmAySANYdDplM");
        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.configuration()
                .scanMode(ScanMode.LOW_LATENCY)     // Scan performance (Balanced, Low Latency or Low Power)
                .scanPeriod(ScanPeriod.create(3000, 2000))    // Scan duration and intervals
                .activityCheckConfiguration(ActivityCheckConfiguration.DISABLED)
                .deviceUpdateCallbackInterval(500);
    }

    private void setupBeaconListener() {
        proximityManager.setIBeaconListener(new IBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
                //Beacon discovered
                Log.d("Test", "Beacon discovered!");
                Log.d("Test", "Beacon details:!");
                Log.d("Test", "UUID: " + iBeacon.getProximityUUID());
                Log.d("Test", "UniqueId: " + iBeacon.getUniqueId());
                Log.d("Test", "Major: " + iBeacon.getMajor());
                Log.d("Test", "Minor: " + iBeacon.getMinor());
                Log.d("Test", "TxPower: " + iBeacon.getTxPower());
                Log.d("Test", "Distance: " + iBeacon.getDistance());
                Log.d("Test", "Proximity: " + iBeacon.getProximity());
                Log.d("Test", "Rssi: " + iBeacon.getRssi());
                Log.d("Test", "TxPower: " + iBeacon.getTxPower());
                Log.d("Test", "Address: " + iBeacon.getAddress());
                Log.d("Test", "Beacon region details!");
                Log.d("Test", "ProximityUUID: " + region.getIdentifier());
                Log.d("Test", "Major: " + region.getMajor());
                Log.d("Test", "Minor: " + region.getMinor());
                Log.d("Test", "Proximity: " + region.getProximity());
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                //Beacons updated
                Log.d("Test", "Beacon updated!");
                for (IBeaconDevice iBeacon : iBeacons) {
                    Log.d("Test", "UUID: " + iBeacon.getProximityUUID());
                    Log.d("Test", "UniqueId: " + iBeacon.getUniqueId());
                    Log.d("Test", "Major: " + iBeacon.getMajor());
                    Log.d("Test", "Minor: " + iBeacon.getMinor());
                }
                Log.d("Test", "Beacon region details!");
                Log.d("Test", "ProximityUUID: " + region.getIdentifier());
                Log.d("Test", "Major: " + region.getMajor());
                Log.d("Test", "Minor: " + region.getMinor());
                Log.d("Test", "Proximity: " + region.getProximity());
            }

            @Override
            public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {
                //Beacon lost
                Log.d("Test", "Beacon lost!");
                Log.d("Test", "UUID: " + iBeacon.getProximityUUID());
                Log.d("Test", "UniqueId: " + iBeacon.getUniqueId());
                Log.d("Test", "Major: " + iBeacon.getMajor());
                Log.d("Test", "Minor: " + iBeacon.getMinor());
                Log.d("Test", "Beacon region details!");
                Log.d("Test", "ProximityUUID: " + region.getIdentifier());
                Log.d("Test", "Major: " + region.getMajor());
                Log.d("Test", "Minor: " + region.getMinor());
                Log.d("Test", "Proximity: " + region.getProximity());
            }
        });
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                Log.d("Test", "Start scanning");
                proximityManager.startScanning();
            }
        });
    }

    private void stopScanning() {
        Log.d("Test", "Stop scanning");
        proximityManager.stopScanning();
        proximityManager.disconnect();
        proximityManager = null;
    }
}