package it.unipi.dii.trainingstat.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.DetectedActivity;

import java.util.HashMap;
import java.util.Map;

public class ActivityTrackerService extends Service {
    // tiene traccia del numero di millisecondi passati in ogni attività
    Map< String, Long> _msPerActivity = new HashMap< String,Long>();
    // serve per calcolare la durata di ogni tipo di attività
    private long _lastActivityTimeStamp;
    private long _totalDuration;

    private boolean _isMonitoring;

    public ActivityTrackerService(){
        _msPerActivity.put(TrainingStatIntentService.ACTIVITY_STILL, 0L);
        _msPerActivity.put(TrainingStatIntentService.ACTIVITY_WALKING, 0L);
        _msPerActivity.put(TrainingStatIntentService.ACTIVITY_RUNNING, 0L);
        _msPerActivity.put(TrainingStatIntentService.ACTIVITY_UNKNOWN, 0L);

        _isMonitoring = false;
        _totalDuration = 0L;
        _lastActivityTimeStamp = 0L;

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(_activityRecognitionReceiver, new IntentFilter(TrainingStatIntentService.ACTIVITY_RECOGNITION_ID));

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver _activityRecognitionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("Status");
            int type = intent.getIntExtra("ActivityType", DetectedActivity.UNKNOWN);

            Log.d("SessionActivity", message);


            if (_isMonitoring) { // mi interessano solo le attività registrate in monitori
                updateActivity(message);
            }

        }
    };

    private boolean updateActivity(String activity) {
        // calcolo finestra attività e aggiorno
        long activityDuration = SystemClock.elapsedRealtime() - _lastActivityTimeStamp;
        _totalDuration += activityDuration;
        Long oldValue = _msPerActivity.get(activity);
        if(oldValue == null){
            Log.e("[ActivityTrackerService]", "ERROR: activity <"+ activity +"> is not present in the current dictionary");
            return false;
        }
        Long newValue = oldValue + activityDuration;
        _msPerActivity.put(activity, newValue);
        _lastActivityTimeStamp = SystemClock.elapsedRealtime();
        return true;
    }

    public void startTacking(){
        _lastActivityTimeStamp = SystemClock.elapsedRealtime();
        _isMonitoring = true;
    }

    public void stopTacking(){
        _lastActivityTimeStamp = SystemClock.elapsedRealtime();
        _isMonitoring = false;

        updateActivity(TrainingStatIntentService.ACTIVITY_UNKNOWN);
    }

    public Map<String, Double> getPercentages(){
        Map<String, Double> tmp = new HashMap< String,Double>();
        for ( String msPerActivityKey : _msPerActivity.keySet()) {
            Double actualValue = Double.valueOf(_msPerActivity.get(msPerActivityKey));
            Double tmpValue = actualValue/_totalDuration*100;
            tmp.put(msPerActivityKey, tmpValue);
        }
        return tmp;
    }
}
