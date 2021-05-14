package it.unipi.dii.trainingstat.service;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import it.unipi.dii.trainingstat.service.exception.NoStepCounterSensorAvailableException;
import it.unipi.dii.trainingstat.service.interfaces.callback.ICallBackForTrainingService;
import it.unipi.dii.trainingstat.service.interfaces.ITrainingService;


public class TrainingStatService implements SensorEventListener, ITrainingService {

    private final ICallBackForTrainingService activity;
    private int ACTIVITY_PERMISSION_CODE = 0;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount;

    public TrainingStatService(ICallBackForTrainingService activity) throws NoStepCounterSensorAvailableException {
        this.activity = activity;
        //requestPermissions();
        sensorSetup();
    }

    private void sensorSetup() throws NoStepCounterSensorAvailableException {

        // Get the default sensor for the sensor type from the SenorManager
        sensorManager = (SensorManager) activity.getContext().getSystemService(Activity.SENSOR_SERVICE);
        // sensorType is either Sensor.TYPE_STEP_COUNTER or Sensor.TYPE_STEP_DETECTOR
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (stepSensor == null) {
            throw new NoStepCounterSensorAvailableException();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == stepSensor) {
            stepCount += (int) event.values[0];
            activity.passStepCounter(stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("DEBUG", String.format("Accuracy changed to <%d>", accuracy));
    }

    // GESTIONE PERMESSI
    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (!hasActivityRecognitionPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION);
            }else{
                permissionsToRequest.add("com.google.android.gms.permission.ACTIVITY_RECOGNITION");
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            activity.askPermissions(permissionsToRequest.toArray(new String[0]), ACTIVITY_PERMISSION_CODE);
        }
    }

    private boolean hasActivityRecognitionPermission() {
        return ContextCompat.checkSelfPermission(activity.getContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void unregisterSensors() {
        sensorManager.unregisterListener(this, stepSensor);
    }

    @Override
    public void registerSensors() {
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
