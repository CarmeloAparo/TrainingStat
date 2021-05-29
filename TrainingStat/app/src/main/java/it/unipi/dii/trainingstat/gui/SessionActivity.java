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
import it.unipi.dii.trainingstat.service.PositionTrackerService;
import it.unipi.dii.trainingstat.service.TrainingStatIntentService;
import it.unipi.dii.trainingstat.service.StepSensorService;
import it.unipi.dii.trainingstat.service.exception.NoStepCounterSensorAvailableException;
import it.unipi.dii.trainingstat.service.interfaces.IPositionService;
import it.unipi.dii.trainingstat.service.interfaces.IStepSensorService;
import it.unipi.dii.trainingstat.service.interfaces.callback.ICallBackForCountingSteps;
import it.unipi.dii.trainingstat.utils.Constant;
import it.unipi.dii.trainingstat.utils.TSDateUtils;


public class SessionActivity extends AppCompatActivity implements ICallBackForCountingSteps {

    private final String TAG = "[SessionActivity]";
    private static final int ACTIVITY_PERMISSION_CODE = 0;
    private long _totalActivityTime; // serve per tenere traccia del tempo contato prima di cliccare pausa
    private boolean chronoRunning;
    private int _totalSteps;

    private ActivityTrackerService _activityTrackerService;
    private IPositionService _positionTrackerService;
    private IStepSensorService _StepSensorService;

    private ActivityRecognitionClient _activityRecognitionClient;
    private PendingIntent _pendingIntent;

    private UserSession _userSession;
    private String _trainingSessionId;

    private Chronometer _chronometer;
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

    BroadcastReceiver _stepCounterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int steps = intent.getIntExtra("Steps", 0);

            Log.d("[SessionActivity] steps reported", String.valueOf(steps));

            passStepCounter(steps);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        if(!areAllPermissionGranted())
            requestPermissions();
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
        getOrGenerateUserSession(username,trainingSession);

        initializeGuiComponents(username);

        initializeServices();

    }

    private void initializeServices() {
        _activityTrackerService = new ActivityTrackerService(this);
        _positionTrackerService = new PositionTrackerService(this);

        initializeActivityRecognition();
        startMonitoringActivityRecognition(1000);

        LocalBroadcastManager.getInstance(
                getApplicationContext()).registerReceiver(
                _activityUpdateReceiver,
                new IntentFilter(ActivityTrackerService.ACTIVITY_STATUS_UPDATE)
        );

        LocalBroadcastManager.getInstance(
                getApplicationContext()).registerReceiver(
                _stepCounterReceiver,
                new IntentFilter(Constant.STEP_COUNTER_INTENT_FILTER)
        );


    }

    private void initializeGuiComponents(String username) {
        // aggiorno il cronometro e total steps nel caso sia rientrato nella sessione
        _chronometer = findViewById(R.id.sessionChronometer);
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
    }

    private void getOrGenerateUserSession(String username, TrainingSession trainingSession) {

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
        _userSession.setStillPerc(percentages.get(TrainingStatIntentService.ACTIVITY_STILL));
        _userSession.setWalkPerc(percentages.get(TrainingStatIntentService.ACTIVITY_WALKING));
        _userSession.setRunPerc(percentages.get(TrainingStatIntentService.ACTIVITY_RUNNING));
        _userSession.setUnknownPerc(percentages.get(TrainingStatIntentService.ACTIVITY_UNKNOWN));
        int[][] matrixHeatmap = _positionTrackerService.getHeatmap();
        List<List<Integer>> heatmap = UserSession.matrixIntToHeatmap(matrixHeatmap);
        _userSession.setHeatmap(heatmap);
        dm.writeUserSession(_trainingSessionId, _userSession);
    }

    private void initializeActivityRecognition(){

        requestPermissions();
        if(!areAllPermissionGranted()){
            Toast.makeText(this, "In order to use the app must been provided al permission needed", Toast.LENGTH_SHORT).show();
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

    private void requestPermissions() {
        String[] permissionsToRequest = whatOfThosePermissionsAreNotGranted(getNeededPermission());

        if (permissionsToRequest.length > 0) {
            this.askPermissions(permissionsToRequest, ACTIVITY_PERMISSION_CODE);
        }
    }

    private String[] getNeededPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE};
        }else{
            return new String[]{
                    "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
                    Manifest.permission.ACCESS_FINE_LOCATION};
        }
    }

    private String[] whatOfThosePermissionsAreNotGranted(String[] permissionNeeded) {
        List<String> notGranted = new ArrayList<>();
        for (String permission : permissionNeeded){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                notGranted.add(permission);
            }
        }
        return notGranted.toArray(new String[notGranted.size()]);
    }

    private boolean areAllPermissionGranted(){
        return whatOfThosePermissionsAreNotGranted(getNeededPermission()).length == 0;
    }

    // calls the right handler method depending on the state of the button
    public void startPauseButtonClicked(View view) {
        Button b = (Button) view;

        if (!chronoRunning) startButtonClicked(b);
        else pauseButtonClicked(b);
        chronoRunning = !chronoRunning;
    }

    private void startButtonClicked(Button startPauseButton) {

        Button stopButton = findViewById(R.id.sessionStopButton);
        stopButton.setEnabled(true);

        // aggiorno TV
        _statusTV.setText(R.string.monitoring);
        startPauseButton.setText(R.string.pause_button_text);

        // faccio partire o ripartire il cronometro
        _chronometer.setBase(SystemClock.elapsedRealtime() - _totalActivityTime);
        _chronometer.start();

        // registro il sensore degli step
        startStepSensorService();
        _positionTrackerService.startScanning();
        _activityTrackerService.startTacking();

        _userSession.setStatus(UserSession.STATUS_MONITORING);
        if(_userSession.getStartDate() == null){
            Date now = TSDateUtils.getCurrentUTCDate();
            String nowString = TSDateUtils.DateToStringIsoDate(now);
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
        stopStepSensorService();
        _positionTrackerService.stopScanning();
        _activityTrackerService.stopTacking();

        _userSession.setStatus(UserSession.STATUS_PAUSED);
        updateDbUserSession();
    }
    
    public void stopButtonClicked(View view) {
        Button startPause = findViewById(R.id.sessionStartButton);
        startPause.setEnabled(false);
        _statusTV.setText(R.string.terminated);

        // potresti cliccare stop anche dopo aver messo in pausa -> i service sono già stoppati
        if(chronoRunning){
            _chronometer.stop();
            // adesso conterrà tutti i ms passati nella sessione
            _totalActivityTime = SystemClock.elapsedRealtime() - _chronometer.getBase();
            stopStepSensorService();
            // tengo conto della mancata classificazione dell'attività quando clicco pause
            _activityTrackerService.stopTacking();
            _positionTrackerService.stopScanning();
        }
        _userSession.setStatus(UserSession.STATUS_TERMINATED);
        _userSession.setEndDate(TSDateUtils.DateToStringIsoDate(TSDateUtils.getCurrentUTCDate()));
        updateDbUserSession();
        stopMonitoringActivityRecognition();

        startResultActivity();
        finish();
    }

    private void startResultActivity() {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("userSession", _userSession);
        i.putExtra("trainingSessionId", _trainingSessionId);
        startActivity(i);
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void passStepCounter(int steps) {
        _totalSteps += steps;
        TextView stepCounterTV = findViewById(R.id.sessionStepCounterTV);
        stepCounterTV.setText(String.valueOf(_totalSteps));
    }

    public void startStepSensorService() {
        Intent serviceIntent = new Intent(this, StepSensorService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopStepSensorService() {
        Intent serviceIntent = new Intent(this, StepSensorService.class);
        stopService(serviceIntent);
    }

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


