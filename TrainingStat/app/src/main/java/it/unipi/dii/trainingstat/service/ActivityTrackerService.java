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

public class ActivityTrackerService{
    public static final String ACTIVITY_STATUS_UPDATE = "ActivityStatusUpdate";
    // tiene traccia del numero di millisecondi passati in ogni attività
    Map< String, Long> _msPerActivity = new HashMap< String,Long>();
    // serve per calcolare la durata di ogni tipo di attività
    private long _lastActivityTimeStamp;
    private long _totalDuration;
    private String _lastActivityStatus;

    private boolean _isMonitoring;
    private Context _context;

    public ActivityTrackerService(Context context){
        _context = context;
        _msPerActivity.put(TrainingStatIntentService.ACTIVITY_STILL, 0L);
        _msPerActivity.put(TrainingStatIntentService.ACTIVITY_WALKING, 0L);
        _msPerActivity.put(TrainingStatIntentService.ACTIVITY_RUNNING, 0L);
        _msPerActivity.put(TrainingStatIntentService.ACTIVITY_UNKNOWN, 0L);

        _isMonitoring = false;
        _totalDuration = 0L;
        _lastActivityTimeStamp = 0L;
        _lastActivityStatus = null;

        // mi interessano solo le attività registrate in monitori
        BroadcastReceiver _activityRecognitionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("Status");
                int type = intent.getIntExtra("ActivityType", DetectedActivity.UNKNOWN);

                Log.d("[ActivityTrackerService]", message);

                if (_isMonitoring) { // mi interessano solo le attività registrate in monitori
                    updateActivity(message);
                }

            }
        };

        LocalBroadcastManager.getInstance(
            _context).registerReceiver(
                _activityRecognitionReceiver,
                new IntentFilter(TrainingStatIntentService.ACTIVITY_RECOGNITION_ID)
        );

    }

    private boolean updateActivity(String activity) {
        // calcolo finestra attività e aggiorno
        long activityDuration = SystemClock.elapsedRealtime() - _lastActivityTimeStamp;
        Long oldValue = _msPerActivity.getOrDefault(activity, null);
        if(oldValue == null){
            _lastActivityStatus = null;
            Log.d("[ActivityTrackerService]", "[INVALID ACTIVITY] activity <"+ activity +"> is not present in the current dictionary");
            return false;
        }
        if(activity != _lastActivityStatus){
            String lastStatus = (_lastActivityStatus == null) ? "null" : _lastActivityStatus;
            Log.d("[ActivityTrackerService]", "[STATUS CHANGED] from <"+ lastStatus +"> to <"+activity+">");
            sendMessageToActivity(activity);
        }
        Long newValue = oldValue + activityDuration;
        _msPerActivity.put(activity, newValue);
        _totalDuration += activityDuration;
        _lastActivityTimeStamp = SystemClock.elapsedRealtime();
        _lastActivityStatus = activity;
        return true;
    }

    private void sendMessageToActivity(String activityStatus) {
        Intent intent = new Intent(ACTIVITY_STATUS_UPDATE);
        intent.setAction(ACTIVITY_STATUS_UPDATE);
        // You can also include some extra data.
        intent.putExtra("Status", activityStatus);

        LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
    }

    public void startTacking(){
        Log.d("[ActivityTrackerService]", "[START TRACKING]");
        _lastActivityTimeStamp = SystemClock.elapsedRealtime();
        _isMonitoring = true;
    }

    public void stopTacking(){
        Log.d("[ActivityTrackerService]", "[STOP TRACKING]");
        _lastActivityTimeStamp = SystemClock.elapsedRealtime();
        _isMonitoring = false;

        if(_lastActivityStatus != null){
            updateActivity(_lastActivityStatus);
        }else{
            updateActivity(TrainingStatIntentService.ACTIVITY_UNKNOWN);
        }

        _lastActivityStatus = null;

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
