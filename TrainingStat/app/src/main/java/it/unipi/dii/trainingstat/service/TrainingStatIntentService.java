package it.unipi.dii.trainingstat.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class TrainingStatIntentService extends IntentService {
    public static final String ACTIVITY_RECOGNITION = "ActivityRecognition";

    public TrainingStatIntentService() {
        super("TrainingStatIntentService");
        Log.d("TrainingStatIntentService", "Constructor called");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent)) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            for (DetectedActivity activity : detectedActivities) {
                String act = convertToString(activity.getType());
                Log.d("TrainingStatIntentService", "Detected activity: <" + act + ">, " + activity.getConfidence());
            }

            int type = detectedActivities.get(0).getType();
            sendMessageToActivity(convertToString(type), type);

        }
    }

    private void sendMessageToActivity(String activityStatus, int activityType) {
        Intent intent = new Intent(ACTIVITY_RECOGNITION);
        intent.setAction(ACTIVITY_RECOGNITION);
        // You can also include some extra data.
        intent.putExtra("Status", activityStatus);
        intent.putExtra("ActivityType", activityType);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    String convertToString(int a) {
        switch (a) {
            case DetectedActivity.ON_BICYCLE:
                return "bycicle";
            case DetectedActivity.IN_VEHICLE:
                return "vehicle";
            case DetectedActivity.ON_FOOT:
                return "foot";
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
