package it.unipi.dii.trainingstat.gui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ca.hss.heatmaplib.HeatMap;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.services.BeaconService;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

public class BLETestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bletest);
        checkPermissions();

        HeatMap heatMap = (HeatMap) findViewById(R.id.heatmap);
        heatMap.setMinimum(0.0);
        heatMap.setMaximum(100.0);
        //add random data to the map
        for (float x = (float) 0.125; x < 1; x += 0.25) {
            for (float y = (float) 0.125; y < 1; y += 0.25) {
                HeatMap.DataPoint point = new HeatMap.DataPoint(x, y, x * 100.0);
                heatMap.addData(point);
            }
        }
        //set the radius to 300 pixels.
        heatMap.setRadius(300);
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
        Intent intent = new Intent(this, BeaconService.class);
        if (buttonId == R.id.startScanning) {
            intent.setAction("ACTION_START_SCANNING");
            startService(intent);
        }
        else {
            if (buttonId == R.id.stopScanning) {
                stopService(intent);
            }
        }
    }
}