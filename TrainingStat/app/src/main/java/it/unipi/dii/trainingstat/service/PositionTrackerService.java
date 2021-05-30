package it.unipi.dii.trainingstat.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.utils.Constant;

import static it.unipi.dii.trainingstat.App.CHANNEL_ID;

public class PositionTrackerService extends Service {
    private ProximityManager _proximityManager;
    private final int _scanningInterval = 500;   // ms
    private Context _activity;
    private Map<String, Map<String, Long>> _beaconPositions;
    private final int _firstBeaconTH = 15;
    private final int _secondBeaconTH = 10;

    public final static String POSITION_TRACKER_INTENT_FILTER = "Position Tracker data";


    @Override
    public void onCreate() {
        super.onCreate();

        _activity = getApplicationContext();
        configureProximityManager();
        setupBeaconListener();
        _beaconPositions = null;
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.getBeaconPositions(this::setBeaconPosition);

    }

    public Void setBeaconPosition(Map<String, Map<String, Long>> positions) {
        if (positions == null) {
            Toast.makeText(_activity, "Beacon positions not found", Toast.LENGTH_SHORT).show();
            return null;
        }
        _beaconPositions = positions;
        Long rowMax = 0l;
        Long colMax = 0l;
        for (Map.Entry<String, Map<String, Long>> beaconPosition : _beaconPositions.entrySet()) {
            rowMax = Long.max(rowMax, beaconPosition.getValue().get("row"));
            colMax = Long.max(colMax, beaconPosition.getValue().get("col"));
        }


        return null;
    }


    @Override
    public void onDestroy() {
        Log.d("PositionService", "Stop scanning");
        if (_proximityManager != null) {
            _proximityManager.stopScanning();
            _proximityManager.disconnect();
            _proximityManager = null;
        }
        stopSelf();
        Toast.makeText(this, "Scanning service stopped.", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Position Tracker service")
                .setSmallIcon(R.drawable.ic_android)
                .build();
        startForeground(2, notification);

        _proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                Log.d("PositionService", "Start scanning");
                _proximityManager.startScanning();
            }
        });

        return START_NOT_STICKY;
    }



    private void configureProximityManager() {
        KontaktSDK.initialize("wLvvNlIMqZdHvvTVrhsgmAySANYdDplM");
        _proximityManager = ProximityManagerFactory.create(_activity);
        _proximityManager.configuration()
                .scanMode(ScanMode.LOW_LATENCY)     // Scan performance (Balanced, Low Latency or Low Power)
                .scanPeriod(ScanPeriod.RANGING)     // Scan duration and intervals
                .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                .deviceUpdateCallbackInterval(_scanningInterval);
    }

    private void setupBeaconListener() {
        _proximityManager.setIBeaconListener(new IBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {}

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                List<IBeaconDevice> filteredBeacons = filterBeacons(iBeacons);
                int[] position = estimatePosition(filteredBeacons);
                if (position == null) {
                    return;
                }
                sendMessageToActivity(position[0], position[1], 1 );
            }

            @Override
            public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {}
        });
    }

    // Filters the beacons returning a list that contains only relevant beacons ordered per decreasing RSSI
    private List<IBeaconDevice> filterBeacons(List<IBeaconDevice> scanningIBeacons) {
        if (scanningIBeacons == null)
            return null;
        // Get from the scanning list only the beacon contained in the map
        List<IBeaconDevice> iBeacons = new ArrayList<>();
        for (IBeaconDevice iBeacon : scanningIBeacons) {
            if (_beaconPositions.containsKey(iBeacon.getUniqueId())) {
                iBeacons.add(iBeacon);
            }
        }
        if (iBeacons.size() == 0)
            return null;
        // order the beacons in the update with RSSI in descending order
        iBeacons.sort(Comparator.comparing(IBeaconDevice::getRssi).reversed());
        List<IBeaconDevice> filteredBeacons = new ArrayList<>();

        if ((iBeacons.size() == 1) ||
            ((iBeacons.get(0).getRssi() - iBeacons.get(1).getRssi()) >= _firstBeaconTH)) {
            filteredBeacons.add(iBeacons.get(0));
        }
        else if ((iBeacons.size() == 2) ||
                ((iBeacons.get(1).getRssi() - iBeacons.get(2).getRssi()) >= _secondBeaconTH)) {
            filteredBeacons.add(iBeacons.get(0));
            filteredBeacons.add(iBeacons.get(1));
        }
        else {
            filteredBeacons.add(iBeacons.get(0));
            filteredBeacons.add(iBeacons.get(1));
            filteredBeacons.add(iBeacons.get(2));
        }
        return filteredBeacons;
    }

    // Finds the coordinates of the matrix cell giving the list of filtered beacons
    private int[] estimatePosition(List<IBeaconDevice> iBeacons) {
        if ((_beaconPositions == null) || (iBeacons == null))
            return null;
        int[] position = new int[2];
        // get indexes of beacon with max RSSI
        Long rowRssiMax = _beaconPositions.get(iBeacons.get(0).getUniqueId()).get("row");
        Long colRssiMax = _beaconPositions.get(iBeacons.get(0).getUniqueId()).get("col");
        if (iBeacons.size() == 1) {
            position[0] = rowRssiMax.intValue();
            position[1] = colRssiMax.intValue();
        }
        else if (iBeacons.size() == 2) {
            Long rowRssiMin = _beaconPositions.get(iBeacons.get(1).getUniqueId()).get("row");
            Long colRssiMin = _beaconPositions.get(iBeacons.get(1).getUniqueId()).get("col");
            // estimate the row
            if (rowRssiMax.equals(rowRssiMin))
                position[0] = rowRssiMax.intValue();
            else {
                Double rowAvg = (rowRssiMax.doubleValue() + rowRssiMin.doubleValue()) / 2;
                if (rowRssiMax > rowRssiMin) {
                    position[0] = ((Double) Math.ceil(rowAvg)).intValue();
                }
                else {
                    position[0] = ((Double) Math.floor(rowAvg)).intValue();
                }
            }
            // estimate the column
            if (colRssiMax.equals(colRssiMin))
                position[1] = colRssiMax.intValue();
            else {
                Double colAvg = (colRssiMax.doubleValue() + colRssiMin.doubleValue()) / 2;
                if (colRssiMax > colRssiMin) {
                    position[1] = ((Double) Math.ceil(colAvg)).intValue();
                }
                else {
                    position[1] = ((Double) Math.floor(colAvg)).intValue();
                }
            }
        }
        else { // I'm in a square on the center of the field
            Long rowMax = 0l;
            Long colMax = 0l;
            Long positionRow = rowRssiMax;
            Long positionCol = colRssiMax;
            for (IBeaconDevice beacon : iBeacons) { // get the limits of the field
                rowMax = Long.max(rowMax, _beaconPositions.get(beacon.getUniqueId()).get("row"));
                colMax = Long.max(colMax, _beaconPositions.get(beacon.getUniqueId()).get("col"));
            }
            // moving the estimate in the center
            if ((rowRssiMax.equals(rowMax)) && ((rowRssiMax - 1) >= 0)) {
                positionRow--;
            }
            if (!(rowRssiMax.equals(rowMax)) && (rowRssiMax + 1) <= rowMax) {
                positionRow++;
            }
            if ((colRssiMax.equals(colMax)) && ((colRssiMax - 1) >= 0)) {
                positionCol--;
            }
            if (!(colRssiMax.equals(colMax)) && (colRssiMax + 1) <= colMax) {
                positionCol++;
            }
            position[0] = positionRow.intValue();
            position[1] = positionCol.intValue();
        }
        return position;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void sendMessageToActivity(int row, int column, int value) {
        Intent intent = new Intent(POSITION_TRACKER_INTENT_FILTER);

        intent.putExtra("row", row);
        intent.putExtra("column", column);
        intent.putExtra("value", value);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


}
