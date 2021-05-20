package it.unipi.dii.trainingstat.gui;

import androidx.appcompat.app.AppCompatActivity;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.entities.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;
import it.unipi.dii.trainingstat.utils.TSDateUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class TrainerActivity extends AppCompatActivity implements View.OnClickListener {
    private User user;
    private TrainingSession trainingSession;
    private DatabaseManager databaseManager;
    private int numPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("User");
        trainingSession = (TrainingSession) intent.getSerializableExtra("TrainingSession");
        TextView usernameTV = findViewById(R.id.trainerUsernameTV);
        TextView sessionIdTV = findViewById(R.id.trainerSessionIdTV);
        usernameTV.setText(user.getUsername());
        sessionIdTV.setText(trainingSession.getId());
        Button startStopButton = findViewById(R.id.trainerStartStopButton);
        startStopButton.setText(R.string.start_button_text);
        databaseManager = new DatabaseManager();
        databaseManager.listenUserSessionsAdded(trainingSession.getId(), this::addUserButton);
        numPlayers = 0;
    }

    public void startStopButtonClicked(View view) {
        Button button = (Button) view;
        if (button.getText().toString().equals(this.getResources().getString(R.string.start_button_text))) {
            databaseManager.removeUserSessionsListener(trainingSession.getId());
            button.setText(R.string.stop_button_text);
        }
        else if(button.getText().toString().equals(this.getResources().getString(R.string.stop_button_text))) {
            button.setText(R.string.trainer_stopped_button);

            String endDate = TSDateUtils.DateToJsonString(TSDateUtils.getCurrentUTCDate());
            trainingSession.setEndDate(endDate);
            databaseManager.updateTrainingEndDate(trainingSession.getId(), endDate);
            databaseManager.listenUserSessionsChanged(trainingSession.getId(), this::addUserSession);
            databaseManager.updateTrainingStatus(trainingSession.getId(), "terminated");
            trainingSession.setStatus("terminated");
        }
    }

    public Void addUserButton(UserSession userSession) {
        Button button = new Button(this);
        button.setText(userSession.getUsername());
        button.setOnClickListener(this);
        LinearLayout linearLayout = findViewById(R.id.trainerUsersLinearLayout);
        linearLayout.addView(button);
        numPlayers++;
        return null;
    }

    public Void addUserSession(UserSession userSession) {
        trainingSession.addUserSession(userSession);
        if (trainingSession.getUserSessions().keySet().size() == numPlayers) {
            databaseManager.removeUserSessionsListener(trainingSession.getId());
            user.addPastSession(trainingSession.getId(), trainingSession.getStartDate());
            databaseManager.addUserPastSessions(user.getUsername(), user.getPastSessions());
            UserSession aggregateResults = computeAggregateresults();
            databaseManager.writeUserSession(trainingSession.getId(), aggregateResults);
        }
        return null;
    }

    private UserSession computeAggregateresults() {
        UserSession aggregateResults = new UserSession();
        aggregateResults.setUsername(user.getUsername());
        int totSteps = 0;
        double stillPerc = 0;
        double walkPerc = 0;
        double runPerc = 0;
        for (Map.Entry<String, UserSession> entry : trainingSession.getUserSessions().entrySet()) {
            UserSession userSession = entry.getValue();
            totSteps += userSession.getTotSteps();
            stillPerc += userSession.getStillPerc();
            walkPerc += userSession.getWalkPerc();
            runPerc += userSession.getRunPerc();
        }
        aggregateResults.setTotSteps(totSteps / numPlayers);
        aggregateResults.setStillPerc(stillPerc / numPlayers);
        aggregateResults.setWalkPerc(walkPerc / numPlayers);
        aggregateResults.setRunPerc(runPerc / numPlayers);
        return aggregateResults;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        Map<String, UserSession> userSessions = trainingSession.getUserSessions();
        UserSession userSession = userSessions.get(button.getText().toString());
        if (userSession == null) {
            Log.d("Test", "User session not arrived");
            return;
        }
        /*
        * TODO: Avviare l'activity dei risultati passando o la user session o tutta la training session
        * */
        Log.d("Test", "User session arrived");
    }
}