package it.unipi.dii.trainingstat.service;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import it.unipi.dii.trainingstat.service.exception.NoStepCounterSensorAvailableException;
import it.unipi.dii.trainingstat.service.interfaces.callback.ICallBackForCountingSteps;
import it.unipi.dii.trainingstat.service.interfaces.ITrainingSensorService;


public class TrainingStatSensorService implements SensorEventListener, ITrainingSensorService {

    private final ICallBackForCountingSteps activity;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount;

    public TrainingStatSensorService(ICallBackForCountingSteps activity) throws NoStepCounterSensorAvailableException {
        this.activity = activity;
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

    @Override
    public void unregisterSensors() {
        sensorManager.unregisterListener(this, stepSensor);
    }

    @Override
    public void registerSensors() {
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
