package it.unipi.dii.trainingstat.gui;

import androidx.appcompat.app.AppCompatActivity;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.entities.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;
import it.unipi.dii.trainingstat.utils.TSDateUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public class TrainerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "[TrainerActivity]";
    private User user;
    private TrainingSession _trainingSession;
    private DatabaseManager databaseManager;
    private int numPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("User");
        _trainingSession = (TrainingSession) intent.getSerializableExtra("TrainingSession");
        TextView usernameTV = findViewById(R.id.trainerUsernameTV);
        TextView sessionIdTV = findViewById(R.id.trainerSessionIdTV);
        usernameTV.setText(user.getUsername());
        sessionIdTV.setText(_trainingSession.getId());
        Button startStopButton = findViewById(R.id.trainerStartStopButton);
        startStopButton.setText(_trainingSession.getStatus());
        databaseManager = new DatabaseManager();
        databaseManager.listenUserSessionsAdded(_trainingSession.getId(), this::addUserButton);
        numPlayers = 0;
    }

    public void startStopButtonClicked(View view) {
        Button button = (Button) view;
        if (button.getText().toString().equals(this.getResources().getString(R.string.start_button_text))) {
            button.setText(R.string.stop_button_text);
        }
        else if(button.getText().toString().equals(this.getResources().getString(R.string.stop_button_text))) {
            button.setText(R.string.trainer_stopped_button);
            // nessuno può più joinare
            databaseManager.removeUserSessionsListener(_trainingSession.getId());

            // recupero tutti gli utenti sessions e vedere se hanno già tutti finito
            databaseManager.getTrainingSession(_trainingSession.getId(), this::finishStopActivities);
        }
    }

    public Void finishStopActivities(TrainingSession trainingSession){
        _trainingSession = trainingSession;
        String endDate = TSDateUtils.DateToStringIsoDate(TSDateUtils.getCurrentUTCDate());
        _trainingSession.setEndDate(endDate);
        databaseManager.updateTrainingEndDate(_trainingSession.getId(), endDate);

        databaseManager.listenUserSessionsChanged(_trainingSession.getId(), this::addUserSession);
        databaseManager.updateTrainingStatus(_trainingSession.getId(), TrainingSession.STATUS_TERMINATED);
        trainingSession.setStatus(TrainingSession.STATUS_TERMINATED);


        if(checkIfUserFinished()){
            databaseManager.removeUserSessionsListener(trainingSession.getId());
            computeAggregateresults();
        }
        return null;
    }

    private boolean checkIfUserFinished() {
        for( UserSession us: _trainingSession.getUserSessions().values()){
            if(!us.getStatus().equals(UserSession.STATUS_TERMINATED)){
                return false;
            }
        }
        return true;
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
        // controllare che tutte le sessioni figlie abbiano status terminated

        _trainingSession.addOrUpdateUserSession(userSession);
        if (checkIfUserFinished()) {
            databaseManager.removeUserSessionsListener(_trainingSession.getId());
            computeAggregateresults();
        }
        return null;
    }

    private void computeAggregateresults() {
        UserSession aggregateResults = new UserSession();
        aggregateResults.setUsername(user.getUsername());
        aggregateResults.setStatus(UserSession.STATUS_TERMINATED);
        aggregateResults.setStartDate(aggregateResults.getStartDate());
        aggregateResults.setEndDate(aggregateResults.getEndDate());
        int totSteps = 0;
        double stillPerc = 0;
        double walkPerc = 0;
        double runPerc = 0;
        for (Map.Entry<String, UserSession> entry : _trainingSession.getUserSessions().entrySet()) {
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
        databaseManager.writeUserSession(_trainingSession.getId(), aggregateResults);
        _trainingSession.addOrUpdateUserSession(aggregateResults);
        addUserButton(aggregateResults);
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        Map<String, UserSession> userSessions = _trainingSession.getUserSessions();
        UserSession userSession = userSessions.get(button.getText().toString());
        if (userSession == null) {
            Log.d("Test", "User session not arrived");
            return;
        }
        if(!userSession.getStatus().equals(UserSession.STATUS_TERMINATED)){
            Toast.makeText(this, "This session is not terminated yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Visualizzazione risultati da implementare", Toast.LENGTH_SHORT).show();
        /*
        * TODO: Avviare l'activity dei risultati passando o la user session o tutta la training session
        * */
        Log.d("Test", "User session arrived");
    }
}