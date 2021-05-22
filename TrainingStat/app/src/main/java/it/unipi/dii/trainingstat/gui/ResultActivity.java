package it.unipi.dii.trainingstat.gui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Duration;

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
    private TextView _totalActivityTV;
    private TextView _totalStepsTV;
    private TextView _stillPercentageTV;
    private TextView _walkingPercentageTV;
    private TextView _runningPercentageTV;
    private TextView _unknownPercentageTV;

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
        _totalActivityTV = findViewById(R.id.tv_total_activity_time);
        _totalStepsTV = findViewById(R.id.tv_total_steps);
        _stillPercentageTV = findViewById(R.id.tv_percentage_still);
        _walkingPercentageTV = findViewById(R.id.tv_percentage_walking);
        _runningPercentageTV = findViewById(R.id.tv_percentage_running);
        _unknownPercentageTV = findViewById(R.id.tv_percentage_unknown);

        loadStatisticsFromUserSession();
    }

    private void loadStatisticsFromUserSession() {
        _usernameTV.setText(_userSession.getUsername());
        _trainingSessionIdTV.setText(_trainingSessionId);
        _startTimeTV.setText(TSDateUtils.DateInLocalTimezoneHumanReadable(_userSession.getStartDate()));
        _endTimeTV.setText(TSDateUtils.DateInLocalTimezoneHumanReadable(_userSession.getEndDate()));
        Duration totalTime = Duration.ofMillis(_userSession.getTotalActivityTime());
        String formattedTotalTime = totalTime.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
        _totalActivityTV.setText(formattedTotalTime);
        _totalStepsTV.setText(String.valueOf(_userSession.getTotSteps()));
        _stillPercentageTV.setText(String.format("%.2f",_userSession.getStillPerc()) + " %");
        _walkingPercentageTV.setText(String.format("%.2f",_userSession.getWalkPerc()) + " %");
        _runningPercentageTV.setText(String.format("%.2f",_userSession.getRunPerc()) + " %");
        _unknownPercentageTV.setText(String.format("%.2f",_userSession.getUnknownPerc()) + " %");
    }
}