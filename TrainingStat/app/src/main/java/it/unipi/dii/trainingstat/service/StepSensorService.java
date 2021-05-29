package it.unipi.dii.trainingstat.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.gui.SessionActivity;
import it.unipi.dii.trainingstat.service.exception.NoStepCounterSensorAvailableException;
import it.unipi.dii.trainingstat.service.interfaces.IStepSensorService;
import it.unipi.dii.trainingstat.utils.Constant;

import static it.unipi.dii.trainingstat.App.CHANNEL_ID;


public class StepSensorService extends Service implements SensorEventListener, IStepSensorService {

    private Context _context;
    private SensorManager _sensorManager;
    private Sensor _stepSensor;

    public StepSensorService() {
    }


    private void sensorSetup() throws NoStepCounterSensorAvailableException {

        // Get the default sensor for the sensor type from the SenorManager
        _sensorManager = (SensorManager) getApplicationContext().getSystemService(Activity.SENSOR_SERVICE);
        // sensorType is either Sensor.TYPE_STEP_COUNTER or Sensor.TYPE_STEP_DETECTOR
        _stepSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (_stepSensor == null) {
            throw new NoStepCounterSensorAvailableException();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == _stepSensor) {

            int stepCount = (int) event.values[0];
            sendMessageToActivity(stepCount);
        }
    }

    private void sendMessageToActivity(int steps) {
        Intent intent = new Intent(Constant.STEP_COUNTER_INTENT_FILTER);

        intent.putExtra("Steps", steps);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("DEBUG", String.format("Accuracy changed to <%d>", accuracy));
    }

    @Override
    public void unregisterSensors() {
        _sensorManager.unregisterListener(this, _stepSensor);
    }

    @Override
    public void registerSensors() {
        _sensorManager.registerListener(this, _stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, SessionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Service")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        try {
            sensorSetup();
        } catch (NoStepCounterSensorAvailableException e) {
            e.printStackTrace();
        }
        registerSensors();
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSensors();
        stopSelf();
    }



}
