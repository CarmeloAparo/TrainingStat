package it.unipi.dii.trainingstat.gui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.service.TestIntentService;
import it.unipi.dii.trainingstat.service.TrainingStatService;
import it.unipi.dii.trainingstat.service.exception.NoStepCounterSensorAvailableException;
import it.unipi.dii.trainingstat.service.interfaces.IActivityCallBackForTrainingService;
import it.unipi.dii.trainingstat.service.interfaces.ITrainingService;


public class SessionActivity extends AppCompatActivity implements IActivityCallBackForTrainingService {

    private ITrainingService _trainingService;
    private final String TAG = "SessionActivity";
    private Chronometer chronometer;
    private long pauseOffset; // serve per tenere traccia del tempo contato prima di cliccare pausa
    private boolean chronoRunning;

    private TextView StatusTV;


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

        // codice GoogleApi for Activity Recognition
        // OnCreate() in MainActivity
        final int PERIOD = 1000; //in ms
        ActivityRecognitionClient mActivityRecognitionClient = new ActivityRecognitionClient(this);
        Intent intent = new Intent(this, TestIntentService.class);
        PendingIntent pi = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(PERIOD, pi);
        // end

        try {
            _trainingService = new TrainingStatService(this);
        } catch (NoStepCounterSensorAvailableException e) {
            Toast.makeText(this, R.string.step_sensor_unavailable_toast, Toast.LENGTH_SHORT).show();
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

        StatusTV.setText(R.string.monitoring);
        startPauseButton.setText(R.string.pause_button_text);

        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();

        _trainingService.registerSensors();

        //TODO fare partire il timer, inviare i dati al db


    }


    private void pauseButtonClicked(Button startPauseButton) {

        StatusTV.setText(R.string.paused);
        startPauseButton.setText(R.string.start_button_text);

        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

        _trainingService.unregisterSensors();

        // TODO fermare il timer e il recupero dei vari dati

    }


    public void stopButtonClicked(View view) {


        /*
        TODO bloccare il timer,
          chiudere la sessione,
          chiudere l'attività corrente,
           passare all'attività di visualizzazione delle statistiche
        */

    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void passStepCounter(int steps) {
        TextView stepCounterTV = findViewById(R.id.sessionStepCounterTV);
        stepCounterTV.setText(String.valueOf(steps));
    }

    @Override
    public void askPermissions(String[] permissions, int permissionCode) {
        ActivityCompat.requestPermissions(this,
                permissions,
                permissionCode);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length != 0 /*&& requestCode == 0*/){
            for (int i = 0; i< grantResults.length; i++){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Log.i("Permission request", permissions[i] + " granted");
                }
            }
        }

    }
}


