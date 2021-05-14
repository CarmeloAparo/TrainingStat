package it.unipi.dii.trainingstat.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import it.unipi.dii.trainingstat.service.interfaces.callback.ICallBackForActivityRecognition;

public class TestIntentService extends BroadcastReceiver {

    final int PERIOD = 1000; //in ms
    List<ActivityTransition> transitions = new ArrayList<>();
    private Context _context;
    private ICallBackForActivityRecognition activity;



    public TestIntentService(ICallBackForActivityRecognition activity) {

        this.activity = activity;
        final int PERIOD = 1000; //in ms

        Intent intent = new Intent(activity.getContext(), this.getClass());
        PendingIntent pi = PendingIntent.getService(activity.getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Task<Void> task = ActivityRecognition.getClient(activity.getContext()).requestActivityUpdates(PERIOD, pi);
        task.addOnSuccessListener(
                (OnSuccessListener) result -> {
                    activity.notifyStill();
                });
        task.addOnFailureListener(
                e -> Log.e("ERROR", e.getMessage()));
    }

    // Handle the callback intent in your service...
    @Override
    public void onReceive(Context context, Intent intent) {

        /*
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                // Do something useful here...
            }
        }

         */

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        activity.notifyStill();
        
        ArrayList < DetectedActivity > detectedActivities = (ArrayList<DetectedActivity>) result.getProbableActivities();
        for (DetectedActivity activity: detectedActivities) {
            String act = convertToString(activity.getType());
            Log.i("AR", "Detected activity: <" + act + ">, " + activity.getConfidence());
        }
    }


    String convertToString(int a) {
        switch (a) {
            case DetectedActivity.ON_BICYCLE:
                return "on bycicle";
            case DetectedActivity.IN_VEHICLE:
                return "in vehicle";
            case DetectedActivity.ON_FOOT:
                return "on foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.UNKNOWN:
                return "unknown";

            default:
                throw new IllegalStateException("Unexpected value: " + a);
        }
    }
}
