package it.unipi.dii.trainingstat.gui;

import androidx.appcompat.app.AppCompatActivity;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.entities.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;
import it.unipi.dii.trainingstat.utils.SessionResolver;
import it.unipi.dii.trainingstat.utils.TSDateUtils;
import it.unipi.dii.trainingstat.utils.exeptions.TrainingSessionNotFound;

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

            button.setText(R.string.stop_button_text);
        }
        else if(button.getText().toString().equals(this.getResources().getString(R.string.stop_button_text))) {
            button.setText(R.string.trainer_stopped_button);
            // nessuno può più joinare
            databaseManager.removeUserSessionsListener(trainingSession.getId());

            // recupero tutti gli utenti sessions e vedere se hanno già tutti finito
            databaseManager.getTrainingSession(trainingSession.getId(), this::finishStopActivities);
        }
    }

    public Void finishStopActivities(TrainingSession trainingSession){
        String endDate = TSDateUtils.DateToJsonString(TSDateUtils.getCurrentUTCDate());
        trainingSession.setEndDate(endDate);
        databaseManager.updateTrainingEndDate(trainingSession.getId(), endDate);

        databaseManager.listenUserSessionsChanged(trainingSession.getId(), this::addUserSession);
        databaseManager.updateTrainingStatus(trainingSession.getId(), TrainingSession.STATUS_TERMINATED);
        trainingSession.setStatus(TrainingSession.STATUS_TERMINATED);


        if(checkIfUserFinished()){
            databaseManager.removeUserSessionsListener(trainingSession.getId());
            computeAggregateresults();
        }
        return null;
    }

    private boolean checkIfUserFinished() {

        for( UserSession us: trainingSession.getUserSessions().values()){

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

        trainingSession.addOrUpdateUserSession(userSession);
        if (checkIfUserFinished()) {
            databaseManager.removeUserSessionsListener(trainingSession.getId());
            computeAggregateresults();
        }
        return null;
    }

    private void computeAggregateresults() {
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
        databaseManager.writeUserSession(trainingSession.getId(), aggregateResults);
        addUserButton(aggregateResults);
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