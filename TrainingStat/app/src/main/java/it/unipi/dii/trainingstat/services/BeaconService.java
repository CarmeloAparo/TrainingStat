package it.unipi.dii.trainingstat.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.List;

public class BeaconService extends Service {
    private static final String ACTION_START_SCANNING = "ACTION_START_SCANNING";
    private ProximityManager proximityManager;

    public BeaconService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_SCANNING.equals(action)) {
                configureProximityManager();
                setupBeaconListener();
                startScanning();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopScanning();
        super.onDestroy();
    }

    private void configureProximityManager() {
        KontaktSDK.initialize("wLvvNlIMqZdHvvTVrhsgmAySANYdDplM");
        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.configuration()
                .scanMode(ScanMode.LOW_LATENCY)     // Scan performance (Balanced, Low Latency or Low Power)
                .scanPeriod(ScanPeriod.RANGING);    // Scan duration and intervals
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