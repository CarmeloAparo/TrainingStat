package it.unipi.dii.trainingstat.service;

import android.util.Log;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.service.interfaces.IPositionService;
import it.unipi.dii.trainingstat.service.interfaces.callback.IBaseCallBack;

public class PositionTrackerService implements IPositionService {
    private ProximityManager proximityManager;
    private final int scanningInterval = 500;   // ms
    private IBaseCallBack _activity;
    private int[][] positionMatrix;
    private Map<String, Map<String, Long>> beaconPositions;
    private final int firstBeaconTH = 10;
    private final int secondBeaconTH = 7;
    private final int thirdBeaconTH = 7;

    public PositionTrackerService(IBaseCallBack activity){
        _activity = activity;
        configureProximityManager();
        setupBeaconListener();
        positionMatrix = null;
        beaconPositions = null;
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.getBeaconPositions(this::setBeaconPosition);
    }

    public Void setBeaconPosition(Map<String, Map<String, Long>> positions) {
        if (positions == null) {
            Toast.makeText(_activity.getContext(), "Beacon positions not found", Toast.LENGTH_SHORT).show();
            return null;
        }
        beaconPositions = positions;
        Long rowMax = 0l;
        Long colMax = 0l;
        for (Map.Entry<String, Map<String, Long>> beaconPosition : beaconPositions.entrySet()) {
            rowMax = Long.max(rowMax, beaconPosition.getValue().get("row"));
            colMax = Long.max(colMax, beaconPosition.getValue().get("col"));
        }
        positionMatrix = new int[rowMax.intValue() + 1][colMax.intValue() + 1];
        for (int[] row : positionMatrix) {
            Arrays.fill(row, 0);
        }
        return null;
    }

    @Override
    public int[][] getHeatmap() {
        return positionMatrix;
    }

    @Override
    public void startScanning() {
        startProximityService();
    }

    @Override
    public void stopScanning() {
        stopProximityService();
    }

    private void configureProximityManager() {
        KontaktSDK.initialize("wLvvNlIMqZdHvvTVrhsgmAySANYdDplM");
        proximityManager = ProximityManagerFactory.create(_activity.getContext());
        proximityManager.configuration()
                .scanMode(ScanMode.LOW_LATENCY)     // Scan performance (Balanced, Low Latency or Low Power)
                .scanPeriod(ScanPeriod.RANGING)     // Scan duration and intervals
                .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                .deviceUpdateCallbackInterval(scanningInterval);
    }

    private void startProximityService() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                Log.d("PositionService", "Start scanning");
                proximityManager.startScanning();
            }
        });
    }

    private void stopProximityService() {
        Log.d("PositionService", "Stop scanning");
        proximityManager.stopScanning();
        proximityManager.disconnect();
    }

    private void setupBeaconListener() {
        proximityManager.setIBeaconListener(new IBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {}

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                List<IBeaconDevice> filteredBeacons = filterBeacons(iBeacons);
                int[] position = estimatePosition(filteredBeacons);
                if (position == null) {
                    return;
                }
                positionMatrix[position[0]][position[1]]++;
            }

            @Override
            public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {}
        });
    }

    // Filters the beacons returning a list that contains only relevant beacons ordered per decreasing RSSI
    private List<IBeaconDevice> filterBeacons(List<IBeaconDevice> iBeacons) {
        iBeacons.sort(Comparator.comparing(IBeaconDevice::getRssi).reversed());
        List<IBeaconDevice> filteredBeacons = new ArrayList<>();
        if ((iBeacons.size() == 1) ||
                ((iBeacons.get(0).getRssi() - iBeacons.get(1).getRssi()) >= firstBeaconTH)) {
            filteredBeacons.add(iBeacons.get(0));
        }
        else if ((iBeacons.size() == 2) ||
                ((iBeacons.get(1).getRssi() - iBeacons.get(2).getRssi()) >= secondBeaconTH) ||
                (iBeacons.size() == 3) ||
                ((iBeacons.get(2).getRssi() - iBeacons.get(3).getRssi()) >= thirdBeaconTH)) {
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
        if (beaconPositions == null)
            return null;
        int[] position = new int[2];
        Long rowRssiMax = beaconPositions.get(iBeacons.get(0).getUniqueId()).get("row");
        Long colRssiMax = beaconPositions.get(iBeacons.get(0).getUniqueId()).get("col");
        if (iBeacons.size() == 1) {
            position[0] = rowRssiMax.intValue();
            position[1] = colRssiMax.intValue();
        }
        else if (iBeacons.size() == 2) {
            Long rowRssiMin = beaconPositions.get(iBeacons.get(1).getUniqueId()).get("row");
            Long colRssiMin = beaconPositions.get(iBeacons.get(1).getUniqueId()).get("col");
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
        else {
            Long rowMax = 0l;
            Long colMax = 0l;
            Long positionRow = rowRssiMax;
            Long positionCol = colRssiMax;
            for (IBeaconDevice beacon : iBeacons) {
                rowMax = Long.max(rowMax, beaconPositions.get(beacon.getUniqueId()).get("row"));
                colMax = Long.max(colMax, beaconPositions.get(beacon.getUniqueId()).get("col"));
            }
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
}
