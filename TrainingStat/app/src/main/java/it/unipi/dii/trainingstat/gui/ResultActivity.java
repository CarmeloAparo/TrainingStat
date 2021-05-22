package it.unipi.dii.trainingstat.gui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;
import java.util.Random;

import ca.hss.heatmaplib.HeatMap;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.entities.UserSession;
import it.unipi.dii.trainingstat.utils.TSDateUtils;

public class ResultActivity extends AppCompatActivity {

    private String _trainingSessionId;
    private UserSession _userSession;
    private TextView _usernameTV;
    private TextView _trainingSessionIdTV;
    private TextView _startTimeTV;
    private TextView _endTimeTV;
    private Chronometer _totalActivityChrono;
    private TextView _totalStepsTV;
    private TextView _stillPercentageTV;
    private TextView _walkingPercentageTV;
    private TextView _runningPercentageTV;
    private TextView _unknownPercentageTV;
    private HeatMap _heatMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent i = getIntent();

        _trainingSessionId = i.getStringExtra("trainingSessionId");
        if(_trainingSessionId == null || _trainingSessionId.equals("")){
            Toast.makeText(this, "Training session id is null or empty", Toast.LENGTH_SHORT).show();
            finish();
        }
        _userSession = (UserSession) i.getSerializableExtra("userSession");
        if(_userSession == null ){
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
        }

        _usernameTV = findViewById(R.id.tv_username);
        _trainingSessionIdTV = findViewById(R.id.tv_session_id);
        _startTimeTV = findViewById(R.id.tv_start_time);
        _endTimeTV = findViewById(R.id.tv_end_time);
        _totalActivityChrono = findViewById(R.id.chrono_totalactivity_time);
        _totalStepsTV = findViewById(R.id.tv_total_steps);
        _stillPercentageTV = findViewById(R.id.tv_percentage_still);
        _walkingPercentageTV = findViewById(R.id.tv_percentage_walking);
        _runningPercentageTV = findViewById(R.id.tv_percentage_running);
        _unknownPercentageTV = findViewById(R.id.tv_percentage_unknown);

        heatMapSetUp();
        loadStatisticsFromUserSession();
    }

    private void loadStatisticsFromUserSession() {
        _usernameTV.setText(_userSession.getUsername());
        _trainingSessionIdTV.setText(_trainingSessionId);
        _startTimeTV.setText(TSDateUtils.DateInLocalTimezoneHumanReadable(_userSession.getStartDate()));
        _endTimeTV.setText(TSDateUtils.DateInLocalTimezoneHumanReadable(_userSession.getEndDate()));
        _totalActivityChrono.setBase(SystemClock.elapsedRealtime() - _userSession.getTotalActivityTime());
        _totalStepsTV.setText(String.valueOf(_userSession.getTotSteps()));
        _stillPercentageTV.setText(String.format("%.2f",_userSession.getStillPerc()) + " %");
        _walkingPercentageTV.setText(String.format("%.2f",_userSession.getWalkPerc()) + " %");
        _runningPercentageTV.setText(String.format("%.2f",_userSession.getRunPerc()) + " %");
        _unknownPercentageTV.setText(String.format("%.2f",_userSession.getUnknownPerc()) + " %");
    }

    private void heatMapSetUp(){
        _heatMap = findViewById(R.id.heatmap);
        int[][] userHeatMap = _userSession.getHeatmap();

        // TODO: mettere messaggio di errore; per ora ne creo una nuova
        if(userHeatMap == null){
            Toast.makeText(this, "La heatmap era null", Toast.LENGTH_SHORT).show();
            userHeatMap = fakeHeatMap(5, 10);
        }

        int rowLen = userHeatMap.length;
        int columnLen = userHeatMap[0].length;
        double max = getMaxValueHeatMap(userHeatMap, rowLen, columnLen);

        _heatMap.setMinimum(0.0);
        _heatMap.setMaximum(100.0);

        Map<Float, Integer> colors = new ArrayMap<>();
        for (int i = 0; i < 21; i++) {
            float stop = ((float)i) / 20.0f;
            int color = doGradient(i * 5, 0, 100, 0xff00ff00, 0xffff0000);
            colors.put(stop, color);
        }
        _heatMap.setColorStops(colors);

        float stepDimentionX = 1.0f / columnLen;
        float stepOffsetX = stepDimentionX/2;
        float stepDimentionY = 1.0f / rowLen;
        float stepOffsetY = stepDimentionY/2;

        for(int i = 0; i < rowLen; i++){
            float y = (i+1)*stepDimentionY - stepOffsetY;

            for(int j = 0; j< columnLen; j++){

                float x = (j+1)*stepDimentionX - stepOffsetX;
                double value = (userHeatMap[i][j]/max) * 100;
                HeatMap.DataPoint point = new HeatMap.DataPoint(x, y, value);
                _heatMap.addData(point);
            }
        }
    }

    private double getMaxValueHeatMap(int[][] heatmap, int rows, int columns){

        double max = 0;
        for(int i = 0; i < rows; i++){
            for(int j = 0; j< columns; j++){
               if (heatmap[i][j] > max)
                   max = heatmap[i][j];
            }
        }
        return max;
    }

    private int [][] fakeHeatMap(int rows, int columns){

        Random random = new Random();
        int result[][] = new int[rows][columns];
        int aux = 0;

        for(int i = 0; i < rows; i++){
            for(int j = 0; j< columns; j++){
                result[i][j] = (int)(random.nextDouble() * 100);
            }
        }

        return result;
    }

    private static int doGradient(double value, double min, double max, int min_color, int max_color) {
        if (value >= max) {
            return max_color;
        }
        if (value <= min) {
            return min_color;
        }
        float[] hsvmin = new float[3];
        float[] hsvmax = new float[3];
        float frac = (float)((value - min) / (max - min));
        Color.RGBToHSV(Color.red(min_color), Color.green(min_color), Color.blue(min_color), hsvmin);
        Color.RGBToHSV(Color.red(max_color), Color.green(max_color), Color.blue(max_color), hsvmax);
        float[] retval = new float[3];
        for (int i = 0; i < 3; i++) {
            retval[i] = interpolate(hsvmin[i], hsvmax[i], frac);
        }
        return Color.HSVToColor(retval);
    }

    private static float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }

}


