package it.unipi.dii.trainingstat.gui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;
import java.util.function.Function;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.entities.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;
import it.unipi.dii.trainingstat.utils.SessionResolver;
import it.unipi.dii.trainingstat.utils.TSDateUtils;
import it.unipi.dii.trainingstat.utils.exeptions.TrainingSessionNotFound;
import it.unipi.dii.trainingstat.utils.exeptions.UserSessionNotFound;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "[MenuActivity]";

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent i = getIntent();
        user = (User) i.getSerializableExtra("User");
        TextView UsernameTextView = findViewById(R.id.menuUsernameTV);
        UsernameTextView.setText(user.getUsername());

        refreshPastSessionsLayout();

    }

    private void refreshPastSessionsLayout(){
        int id = 0;
        LinearLayout linearLayout = findViewById(R.id.pastSessionsLayout);
        linearLayout.removeAllViews();

        for (Map<String, String> session : user.getPastSessions()) {
            Button button = new Button(this);
            String date = TSDateUtils.DateInLocalTimezoneHumanReadable(
                    TSDateUtils.StringIsoDateToDate(session.get("startDate"))
            );

            String text = session.get("id") + " " + date;
            button.setText(text);
            button.setId(id);
            button.setOnClickListener(this);

            linearLayout.addView(button);
            id++;
        }
    }

    public TrainingSession startNewTrainingSession() {
        user.setLastIncrementalID(user.getLastIncrementalID() + 1);
        String id = user.getUsername() + "_" + user.getLastIncrementalID();
        TrainingSession trainingSession = new TrainingSession(id, user.getUsername(), "started", TSDateUtils.DateToStringIsoDate(TSDateUtils.getCurrentUTCDate()), null);
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.writeTrainingSession(trainingSession);
        databaseManager.updateUserIncrementalID(user.getUsername(), user.getLastIncrementalID());
        return trainingSession;
    }

    public void newCollectiveSessionButtonClicked(View v) {
        TrainingSession trainingSession = startNewTrainingSession();
        saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
        startTrainingActivity(trainingSession);
    }

    private void startTrainingActivity(TrainingSession trainingSession) {
        Intent i = new Intent(this, TrainerActivity.class);
        i.putExtra("User", user);
        i.putExtra("TrainingSession", trainingSession);
        startActivity(i);
    }

    public void newIndividualSessionButtonClicked(View v) {
        TrainingSession trainingSession = startNewTrainingSession();
        saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
        startSessionActivity(trainingSession);
    }

    public void joinCollectiveSessionButtonClicked(View v) {
        EditText sessionIdToJoinET = findViewById(R.id.menuInsertSessionIdET);
        String sessionIdToJoin = sessionIdToJoinET.getText().toString();

        DatabaseManager db = new DatabaseManager();
        db.getTrainingSession(sessionIdToJoin, this::joinSession);
    }

    private Void joinSession(TrainingSession trainingSession) {
        if(trainingSession == null){
            Toast.makeText(this, "<"+trainingSession.getId()+"> not found", Toast.LENGTH_SHORT).show();
        }
        if(SessionResolver.isIndividualSession(trainingSession)){
            Toast.makeText(this, "<"+trainingSession.getId()+"> is an individual session", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "tried to join an individual session <"+trainingSession.getId()+"> with username <"+user.getUsername()+">");
            return null;
        }
        if (!trainingSession.getStatus().equals(TrainingSession.STATUS_STARTED)) {
            Toast.makeText(this,
                    "The session is ended. Please check the session id and try again!",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
        startSessionActivity(trainingSession);
        return null;
    }

    private void startSessionActivity(TrainingSession trainingSession){
        Intent i = new Intent(this, SessionActivity.class);
        i.putExtra("username", user.getUsername());
        i.putExtra("trainingSession", trainingSession);
        startActivity(i);
    }

    @Override
    // called if a past session is clicked
    public void onClick(View v) {
        int buttonId = v.getId();
        Map<String, String> pastSession = user.getPastSessions().get(buttonId);
        String sessionId = pastSession.get("id");
        DatabaseManager db = new DatabaseManager();
        Function<TrainingSession, Void> function = this::showPastSession;
        db.getTrainingSession(sessionId, function);
    }

    private Void showPastSession(TrainingSession trainingSession) {
        if (trainingSession == null) {
            Toast.makeText(this, "An error occurred during past session selection",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        int status;
        try {
            status = SessionResolver.classifyUserSessionFromTrainingSessionId(user.getUsername(), trainingSession);
        } catch (TrainingSessionNotFound | UserSessionNotFound trainingSessionNotFound) {
            Log.e(TAG, trainingSessionNotFound.getMessage());
            return null;
        }

        switch(status){
            case SessionResolver.RUNNING_TRAINING_SESSION:
            case SessionResolver.TERMINATED_TRAINING_SESSION:
                startTrainingActivity(trainingSession);
                return null;

            case SessionResolver.RUNNING_INDIVIDUAL_SESSION:
            case SessionResolver.RUNNING_COLLECTIVE_SESSION:
                startSessionActivity(trainingSession);
                return null;

            case SessionResolver.TERMINATED_INDIVIDUAL_SESSION:
            case SessionResolver.TERMINATED_COLLECTIVE_SESSION:
                UserSession us = trainingSession.getSessionOfUser(user.getUsername());
                startResultActivity(us,trainingSession.getId());
                return null;

            default:
                Log.e(TAG, "Unexpected user session status: " + String.valueOf(status));
                Toast.makeText(this, "Unexpected user session status: " + String.valueOf(status), Toast.LENGTH_SHORT).show();
                return null;
        }
    }

    private void startResultActivity(UserSession userSession, String trainingSessionId) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("userSession", userSession);
        i.putExtra("trainingSessionId", trainingSessionId);
        startActivity(i);
    }

    // aggiorno l'elenco delle past session dell'utente
    private void saveUserPastSession(String sessionId, String date){

        user.addPastSession(sessionId, date);
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.addUserPastSessions(user.getUsername(), user.getPastSessions());
        refreshPastSessionsLayout();
    }

}