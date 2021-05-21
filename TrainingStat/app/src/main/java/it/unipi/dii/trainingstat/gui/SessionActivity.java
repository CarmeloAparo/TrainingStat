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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.entities.TrainingSession;
import it.unipi.dii.trainingstat.entities.UserSession;
import it.unipi.dii.trainingstat.service.ActivityTrackerService;
import it.unipi.dii.trainingstat.service.DummyService;
import it.unipi.dii.trainingstat.service.TrainingStatIntentService;
import it.unipi.dii.trainingstat.service.TrainingStatSensorService;
import it.unipi.dii.trainingstat.service.exception.NoStepCounterSensorAvailableException;
import it.unipi.dii.trainingstat.service.interfaces.ITrainingSensorService;
import it.unipi.dii.trainingstat.service.interfaces.callback.ICallBackForCountingSteps;
import it.unipi.dii.trainingstat.utils.SessionResolver;
import it.unipi.dii.trainingstat.utils.TSDateUtils;
import it.unipi.dii.trainingstat.utils.exeptions.TrainingSessionNotFound;


public class SessionActivity extends AppCompatActivity implements ICallBackForCountingSteps{

    private static final int ACTIVITY_PERMISSION_CODE = 0;
    private ActivityTrackerService _activityTrackerService;
    private ITrainingSensorService _trainingService;
    private final String TAG = "[SessionActivity]";
    private Chronometer _chronometer;
    private long _totalActivityTime; // serve per tenere traccia del tempo contato prima di cliccare pausa
    private boolean chronoRunning;
    private int _totalSteps;


    private ActivityRecognitionClient _activityRecognitionClient;
    private PendingIntent _pendingIntent;

    private UserSession _userSession;
    private String _trainingSessionId;

    private TextView _activityStatusTV;
    private TextView _statusTV;

    BroadcastReceiver _activityUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("Status");

            Log.d("[SessionActivity]", message);

            _activityStatusTV.setText(message.toUpperCase());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        if(!hasActivityRecognitionPermission())
            requestActivityRecognitionPermissions();
        else
            finalizeOnCreate();
    }

    private void finalizeOnCreate(){

        _statusTV = findViewById(R.id.sessionStatusTV);
        _activityStatusTV = findViewById(R.id.sessionStatusActivityTV);
        _activityStatusTV.setText(getString(R.string.activity_status_unknown).toUpperCase());

        // recupero username e session id
        Intent i = getIntent();
        String username = i.getStringExtra("username");
        TrainingSession trainingSession = (TrainingSession) i.getSerializableExtra("trainingSession");
        _trainingSessionId = trainingSession.getId();

        // genero la user session da inserire nella mia stessa training session
        initializeUserSession(username,trainingSession);

        // aggiorno il cronometro e total steps nel caso sia rientrato nella sessione
        Long auxLong =  _userSession.getTotalActivityTime();
        _totalActivityTime = (auxLong == null) ? 0L : auxLong;
        _chronometer.setBase(SystemClock.elapsedRealtime() - _totalActivityTime);

        Integer auxInt = _userSession.getTotSteps();
        _totalSteps = (auxInt == null)? 0 : auxInt;



        // inizializzo le textView
        TextView UsernameTextView = findViewById(R.id.sessionUsernameTV);
        TextView SessionIdTextView = findViewById(R.id.sessionSessionIdTV);

        UsernameTextView.setText(username);
        SessionIdTextView.setText(_trainingSessionId);

        _chronometer = findViewById(R.id.sessionChronometer);

        _activityTrackerService = new ActivityTrackerService(this);

        initializeActivityRecognition();
        startMonitoringActivityRecognition(1000);

        LocalBroadcastManager.getInstance(
                getApplicationContext()).registerReceiver(
                _activityUpdateReceiver,
                new IntentFilter(ActivityTrackerService.ACTIVITY_STATUS_UPDATE)
        );
        try {
            _trainingService = new TrainingStatSensorService(this);
        } catch (NoStepCounterSensorAvailableException e) {
            Toast.makeText(this, R.string.step_sensor_unavailable_toast, Toast.LENGTH_SHORT).show();
            _trainingService = new DummyService();
        }

    }


    private void initializeUserSession(String username, TrainingSession trainingSession) {

        if(trainingSession != null){
            _userSession = trainingSession.getSessionOfUser(username);
        }

        if(_userSession == null) {
            DatabaseManager dm = new DatabaseManager();
            _userSession = new UserSession();
            _userSession.setUsername(username);
            _userSession.setStatus(UserSession.STATUS_READY);
            dm.writeUserSession(_trainingSessionId, _userSession);
        }
    }

    private void updateDbUserSession(){
        DatabaseManager dm = new DatabaseManager();
        _userSession.setTotalActivityTime(_totalActivityTime);
        _userSession.setTotSteps(_totalSteps);
        Map<String, Double> percentages = _activityTrackerService.getPercentages();
        // DEBUG stampo i risultati a mano
        Log.d("SessionActivity", "Still precentage: " + percentages.get(TrainingStatIntentService.ACTIVITY_STILL));
        Log.d("SessionActivity", "Walking precentage: " + percentages.get(TrainingStatIntentService.ACTIVITY_WALKING));
        Log.d("SessionActivity", "Running precentage: " + percentages.get(TrainingStatIntentService.ACTIVITY_RUNNING));
        Log.d("SessionActivity", "Unknown precentage: " + percentages.get(TrainingStatIntentService.ACTIVITY_UNKNOWN));
        _userSession.setStillPerc(percentages.get(TrainingStatIntentService.ACTIVITY_STILL));
        _userSession.setWalkPerc(percentages.get(TrainingStatIntentService.ACTIVITY_WALKING));
        _userSession.setRunPerc(percentages.get(TrainingStatIntentService.ACTIVITY_RUNNING));
        _userSession.setUnknownPerc(percentages.get(TrainingStatIntentService.ACTIVITY_UNKNOWN));
        dm.writeUserSession(_trainingSessionId, _userSession);
    }


    private void initializeActivityRecognition(){

        requestActivityRecognitionPermissions();

        if(!hasActivityRecognitionPermission()){
            Toast.makeText(this, "Activity recognition permission are necessary to use the app", Toast.LENGTH_SHORT).show();
            finish();
        }


        _activityRecognitionClient  =  new  ActivityRecognitionClient(this);
        Intent  i  =  new  Intent(this,  TrainingStatIntentService.class);
        _pendingIntent  =  PendingIntent.getService(this,  1,  i,  PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void startMonitoringActivityRecognition(int periodInMs){
        Task<Void> task  =  _activityRecognitionClient.requestActivityUpdates(periodInMs,  _pendingIntent);

        task.addOnSuccessListener(result -> Log.d(TAG, "Successfully requested activity updates"));
        // Adds a listener that is called if the Task fails.
        Task<Void> voidTask = task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Requesting activity updates failed to start");
            }
        });
    }

    private void stopMonitoringActivityRecognition(){
        if(_activityRecognitionClient != null){
            Task<Void> task = _activityRecognitionClient.removeActivityUpdates(_pendingIntent);
            // Adds a listener that is called if the Task completes successfully.
            task.addOnSuccessListener(result -> Log.d(TAG, "Removed activity updates successfully!"));
            // Adds a listener that is called if the Task fails.
            task.addOnFailureListener(e -> Log.e(TAG, "Failed to remove activity updates!"));
        }
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


    // calls the right handler method depending on the state of the button
    public void startPauseButtonClicked(View view) {
        Button b = (Button) view;

        if (!chronoRunning) startButtonClicked(b);
        else pauseButtonClicked(b);
        chronoRunning = !chronoRunning;
    }


    private void startButtonClicked(Button startPauseButton) {

        // aggiorno TV
        _statusTV.setText(R.string.monitoring);
        startPauseButton.setText(R.string.pause_button_text);

        // faccio partire o ripartire il cronometro
        _chronometer.setBase(SystemClock.elapsedRealtime() - _totalActivityTime);
        _chronometer.start();

        // registro il sensore degli step
        _trainingService.registerSensors();

        _activityTrackerService.startTacking();

        _userSession.setStatus(UserSession.STATUS_MONITORING);
        if(_userSession.getStartDate() == null){
            Date now = TSDateUtils.getCurrentUTCDate();
            String nowString = TSDateUtils.DateToJsonString(now);
            _userSession.setStartDate(nowString);
        }

        updateDbUserSession();
    }


    private void pauseButtonClicked(Button startPauseButton) {

        // aggiorno TV
        _statusTV.setText(R.string.paused);
        startPauseButton.setText(R.string.start_button_text);

        _chronometer.stop();

        // contiene il tempo passato fino adesso
        _totalActivityTime = SystemClock.elapsedRealtime() - _chronometer.getBase();

        // scollego il sensore degli step
        _trainingService.unregisterSensors();

        _activityTrackerService.stopTacking();
        _userSession.setStatus(UserSession.STATUS_PAUSED);
        updateDbUserSession();
    }


    public void stopButtonClicked(View view) {

        if(chronoRunning){
            _chronometer.stop();
            // adesso conterrà tutti i ms passati nella sessione
            _totalActivityTime = SystemClock.elapsedRealtime() - _chronometer.getBase();
            _trainingService.unregisterSensors();
            // tengo conto della mancata classificazione dell'attività quando clicco pause
            _activityTrackerService.stopTacking();
        }
        _userSession.setStatus(UserSession.STATUS_TERMINATED);
        _userSession.setEndDate(TSDateUtils.DateToJsonString(TSDateUtils.getCurrentUTCDate()));
        updateDbUserSession();
        stopMonitoringActivityRecognition();
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void passStepCounter(int steps) {
        _totalSteps += steps;
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
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission request", permissions[i] + "not granted");
                    finish();
                }
                Log.i("Permission request", permissions[i] + " granted");
            }
            finalizeOnCreate();
        }

    }


}


