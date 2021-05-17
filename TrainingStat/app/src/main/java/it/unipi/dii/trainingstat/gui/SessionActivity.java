package it.unipi.dii.trainingstat.gui;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.service.ActivityTrackerService;
import it.unipi.dii.trainingstat.service.DummyService;
import it.unipi.dii.trainingstat.service.TrainingStatIntentService;
import it.unipi.dii.trainingstat.service.TrainingStatSensorService;
import it.unipi.dii.trainingstat.service.exception.NoStepCounterSensorAvailableException;
import it.unipi.dii.trainingstat.service.interfaces.callback.ICallBackForTrainingService;
import it.unipi.dii.trainingstat.service.interfaces.ITrainingSensorService;


public class SessionActivity extends AppCompatActivity implements ICallBackForTrainingService {

    private static final int ACTIVITY_PERMISSION_CODE = 0;
    private ActivityTrackerService _activityTrackerService;
    private ITrainingSensorService _trainingService;
    private final String TAG = "SessionActivity";
    private Chronometer chronometer;
    private long pauseOffset; // serve per tenere traccia del tempo contato prima di cliccare pausa
    private boolean chronoRunning;



    private TextView StatusTV;

    private void initializeActivityRecognition(){

        requestActivityRecognitionPermissions();

        if(!hasActivityRecognitionPermission()){
            Toast.makeText(this, "Activity recognition permission are necessary to use the app", Toast.LENGTH_SHORT).show();
            finish();
        }

        final  int  PERIOD  =  1000;  //in  ms
        ActivityRecognitionClient mActivityRecognitionClient  =  new  ActivityRecognitionClient(this);
        Intent  i  =  new  Intent(this,  TrainingStatIntentService.class);
        PendingIntent pi  =  PendingIntent.getService(this,  1,  i,  PendingIntent.FLAG_UPDATE_CURRENT);
        Task<Void> task  =  mActivityRecognitionClient.requestActivityUpdates(PERIOD,  pi);
    }

    private void requestActivityRecognitionPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (!hasActivityRecognitionPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION);
            }else{
                permissionsToRequest.add("com.google.android.gms.permission.ACTIVITY_RECOGNITION");
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            this.askPermissions(permissionsToRequest.toArray(new String[0]), ACTIVITY_PERMISSION_CODE);
        }
    }

    private boolean hasActivityRecognitionPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }


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


        initializeActivityRecognition();
        _activityTrackerService = new ActivityTrackerService();

        try {
            _trainingService = new TrainingStatSensorService(this);
        } catch (NoStepCounterSensorAvailableException e) {
            Toast.makeText(this, R.string.step_sensor_unavailable_toast, Toast.LENGTH_SHORT).show();
            _trainingService = new DummyService();
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

        // aggiorno TV
        StatusTV.setText(R.string.monitoring);
        startPauseButton.setText(R.string.pause_button_text);

        // faccio partire o ripartire il cronometro
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();

        // registro il sensore degli step
        _trainingService.registerSensors();

        _activityTrackerService.startTacking();
    }


    private void pauseButtonClicked(Button startPauseButton) {

        // aggiorno TV
        StatusTV.setText(R.string.paused);
        startPauseButton.setText(R.string.start_button_text);

        chronometer.stop();

        // contiene il tempo passato fino adesso
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

        // scollego il sensore degli step
        _trainingService.unregisterSensors();

        _activityTrackerService.stopTacking();

    }


    public void stopButtonClicked(View view) {

        if(chronoRunning){
            chronometer.stop();
            // adesso conterrà tutti i ms passati nella sessione
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            _trainingService.unregisterSensors();
            // tengo conto della mancata classificazione dell'attività quando clicco pause
            _activityTrackerService.stopTacking();
        }

        Map<String, Double> percentages = _activityTrackerService.getPercentages();

        // DEBUG stampo i risultati a mano
        /*Log.d("SessionActivity", "Still precentage: " + String.valueOf(fractionActivity[0]));
        Log.d("SessionActivity", "Walking precentage: " + String.valueOf(fractionActivity[1]));
        Log.d("SessionActivity", "Running precentage: " + String.valueOf(fractionActivity[2]));
        Log.d("SessionActivity", "Unknown precentage: " + String.valueOf(fractionActivity[3]));*/

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

    @Override
    public void notifyRunning() {
        Toast.makeText(this, "RUNNING", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyStill() {
        Toast.makeText(this, "STILL", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyWalking() {
        Toast.makeText(this, "WALKING", Toast.LENGTH_SHORT).show();
    }

}


