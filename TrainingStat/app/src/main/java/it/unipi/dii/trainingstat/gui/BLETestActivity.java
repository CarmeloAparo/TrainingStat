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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.getLastMeasurementId(this::setLastMeasurementId);
        data = new HashMap<>();
        data.put("measurements", new ArrayList<Map<String, Object>>());
    }

    public Void setLastMeasurementId(Integer lastMeasurement) {
        this.lastMeasurement = lastMeasurement;
        Log.d("Test", "Last measurement id: " + lastMeasurement);
        return null;
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
                messageTV.setText("Scanning stopped");
                lastMeasurement++;
                String id = "Measure_" + lastMeasurement;
                DatabaseManager databaseManager = new DatabaseManager();
                databaseManager.collectData(id, data, lastMeasurement);
                data.clear();
                data.put("measurements", new ArrayList<Map<String, Object>>());
            }
        }
    }

    private void configureProximityManager() {
        KontaktSDK.initialize("wLvvNlIMqZdHvvTVrhsgmAySANYdDplM");
        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.configuration()
                .scanMode(ScanMode.LOW_LATENCY)     // Scan performance (Balanced, Low Latency or Low Power)
                .scanPeriod(ScanPeriod.RANGING)     // Scan duration and intervals
                .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                .deviceUpdateCallbackInterval(500);
    }

    private void setupBeaconListener() {
        proximityManager.setIBeaconListener(new IBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {}

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                //Beacons updated
                List<Map<String, Object>> measures = (List<Map<String, Object>>) data.get("measurements");
                Map<String, Object> measure = new HashMap<>();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                String timestamp = df.format(calendar.getTime());
                measure.put("Timestamp", timestamp);
                for (IBeaconDevice iBeacon : iBeacons) {
                    Log.d("Test", "UUID: " + iBeacon.getUniqueId());
                    Log.d("Test", "UUID: " + iBeacon.getRssi());
                    Log.d("Test", "UUID: " + iBeacon.getDistance());

                    Map<String, Object> beacon = new HashMap<>();
                    beacon.put("RSSI", iBeacon.getRssi());
                    beacon.put("Distance", iBeacon.getDistance());

                    measure.put(iBeacon.getUniqueId(), beacon);
                }
                measures.add(measure);
            }

            @Override
            public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {
                Log.d("Test_LOSS", "BEACON PERSO ");
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