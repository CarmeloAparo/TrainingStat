package it.unipi.dii.trainingstat.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class TrainingStatIntentService extends IntentService {
    public static final String ACTIVITY_RECOGNITION_ID = "ActivityRecognition";
    public static final String ACTIVITY_STILL = "still";
    public static final String ACTIVITY_WALKING = "walking";
    public static final String ACTIVITY_RUNNING = "running";
    public static final String ACTIVITY_UNKNOWN = "unknown";

    public TrainingStatIntentService() {
        super("TrainingStatIntentService");
        Log.d("TrainingStatIntentService", "Constructor called");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent)) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            if (!detectedActivities.isEmpty()){

                int type = DetectedActivity.UNKNOWN;
                int confidence = 0;

                for (DetectedActivity activity : detectedActivities) {

                    int confidenceAnalyzed = activity.getConfidence();
                    int typeAnalized = activity.getType();

                    if(typeAnalized == DetectedActivity.STILL ||
                            typeAnalized == DetectedActivity.WALKING ||
                            typeAnalized == DetectedActivity.RUNNING){

                        if(confidenceAnalyzed > confidence) {
                            type = typeAnalized;
                            confidence = confidenceAnalyzed;
                        }
                    }
                    // loggo comunque l'attività vista nel for
                    String act = convertToString(activity.getType());
                    Log.d("TrainingStatIntentService", "Detected activity: <" + act + ">, " + activity.getConfidence());
                }

                sendMessageToActivity(convertToString(type), type);
            }
        }
    }

    private void sendMessageToActivity(String activityStatus, int activityType) {
        Intent intent = new Intent(ACTIVITY_RECOGNITION_ID);
        intent.setAction(ACTIVITY_RECOGNITION_ID);
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
                return ACTIVITY_STILL;
            case DetectedActivity.RUNNING:
                return ACTIVITY_RUNNING;
            case DetectedActivity.WALKING:
                return ACTIVITY_WALKING;
            case DetectedActivity.TILTING:
                return "tilting";
            default: //unknown
                return ACTIVITY_UNKNOWN;
        }
    }

}
