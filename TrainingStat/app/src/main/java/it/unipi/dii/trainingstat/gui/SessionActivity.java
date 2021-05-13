package it.unipi.dii.trainingstat.gui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

<<<<<<< Updated upstream:TrainingStat/app/src/main/java/it/unipi/dii/trainingstat/gui/SessionActivity.java
import it.unipi.dii.trainingstat.R;

public class SessionActivity extends AppCompatActivity {
=======
import java.util.ArrayList;
import java.util.List;
>>>>>>> Stashed changes:TrainingStat/app/src/main/java/it/unipi/dii/trainingstat/SessionActivity.java

public class SessionActivity extends AppCompatActivity implements SensorEventListener {

    private int ACTIVITY_PERMISSION_CODE = 0;
    private final String TAG = "SessionActivity";
    private Chronometer chronometer;
    private long pauseOffset; // serve per tenere traccia del tempo contato prima di cliccare pausa
    private boolean chronoRunning;

    private TextView StatusTV;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        StatusTV = findViewById(R.id.sessionStatusTV);

        // recupero username e session id
        Intent i = getIntent();
        String Username = i.getStringExtra("username");
        String sessionId = i.getStringExtra("sessionId");

        // inizializzo le textView
        TextView UsernameTextView = findViewById(R.id.sessionUsernameTV);
        TextView SessionIdTextView = findViewById(R.id.sessionSessionIdTV);

        UsernameTextView.setText(Username);
        SessionIdTextView.setText(sessionId);

        chronometer = findViewById(R.id.sessionChronometer);

        requestPermissions();

        sensorSetup();
    }


    private void sensorSetup() {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepSensor == null) {
            Toast.makeText(this, "Step sensor is not present", Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    // calls the right handler method depending on the state of the button
    public void startPauseButtonClicked(View view) {
        Button b = (Button) view;

        if (!chronoRunning) startButtonClicked(b);
        else pauseButtonClicked(b);
        chronoRunning = !chronoRunning;
    }


    private void startButtonClicked(Button startPauseButton) {

        StatusTV.setText("Monitoring");
        startPauseButton.setText(R.string.pause_button_text);

        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();

        //TODO fare partire il timer, inviare i dati al db


    }


    private void pauseButtonClicked(Button startPauseButton) {

        StatusTV.setText("Paused");
        startPauseButton.setText(R.string.start_button_text);

        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

        // TODO fermare il timer e il recupero dei vari dati

    }


    public void stopButtonClicked(View view) {

        StatusTV.setText("Ready");

        // ATTENZIONE QUESTA SAREBBE LA RESET QUINDI VA CAMBIATA
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;

        // reset del counter
        stepCount = 0;
        TextView stepCounterTV = (TextView) findViewById(R.id.sessionStepCounterTV);
        stepCounterTV.setText(String.valueOf(stepCount));

        /*
        TODO bloccare il timer,
          chiudere la sessione,
          chiudere l'attività corrente,
           passare all'attività di visualizzazione delle statistiche
        */

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == stepSensor && chronoRunning) {
            stepCount = (int) event.values[0];
            TextView stepCounterTV = (TextView) findViewById(R.id.sessionStepCounterTV);
            stepCounterTV.setText(String.valueOf(stepCount));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "Accuracy changed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, stepSensor);
    }


    // GESTIONE PERMESSI

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (!hasActivityRecognitionPermission()) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    ACTIVITY_PERMISSION_CODE);
        }
    }


    private boolean hasActivityRecognitionPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACTIVITY_PERMISSION_CODE && grantResults.length != 0){
            for (int i = 0; i< grantResults.length; i++){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Log.i("Permission request", permissions[i] + " granted");
                }
            }
        }

    }


}


