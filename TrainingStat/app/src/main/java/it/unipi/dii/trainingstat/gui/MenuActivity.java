package it.unipi.dii.trainingstat.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.entities.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;
import it.unipi.dii.trainingstat.utils.TSDateUtils;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private String Username;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent i = getIntent();
        user = (User) i.getSerializableExtra("User");
        TextView UsernameTextView = findViewById(R.id.menuUsernameTV);
        UsernameTextView.setText(user.getUsername());
        Username = user.getUsername();
        int id = 0;
        for (Map<String, String> session : user.getPastSessions()) {
            Button button = new Button(this);
            button.setText(session.get("startDate"));
            button.setId(id);
            button.setOnClickListener(this);
            LinearLayout linearLayout = findViewById(R.id.pastSessionsLayout);
            linearLayout.addView(button);
            id++;
        }
    }

    public TrainingSession startTrainingSession() {
        user.setLastIncrementalID(user.getLastIncrementalID() + 1);
        String id = Username + "_" + user.getLastIncrementalID();
        TrainingSession trainingSession = new TrainingSession(id, Username, "started", TSDateUtils.DateToJsonString(TSDateUtils.getCurrentUTCDate()), null);
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.writeTrainingSession(trainingSession);
        databaseManager.updateUserIncrementalID(Username, user.getLastIncrementalID());
        return trainingSession;
    }

    public void newCollectiveSessionButtonClicked(View v) {
        TrainingSession trainingSession = startTrainingSession();
        saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
        Intent i = new Intent(this, TrainerActivity.class);
        i.putExtra("User", user);
        i.putExtra("TrainingSession", trainingSession);
        startActivity(i);
    }

    public void newIndividualSessionButtonClicked(View v) {
        TrainingSession trainingSession = startTrainingSession();
        saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
        Intent i = new Intent(this, SessionActivity.class);
        i.putExtra("username", Username);
        i.putExtra("trainingSessionId", trainingSession.getId());
        startActivity(i);
    }

    public void joinCollectiveSessionButtonClicked(View v) {
        EditText sessionIdToJoinET = findViewById(R.id.menuInsertSessionIdET);
        String sessionIdToJoin = sessionIdToJoinET.getText().toString();

        // Don't even bother the DB with an empty session id
        if (sessionIdToJoin.equals("")) {
            Toast.makeText(this, "A session id must be provided", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseManager db = new DatabaseManager();
            Function<TrainingSession, Void> function = this::joinSession;
            db.getTrainingSession(sessionIdToJoin, function);
        }
    }

    public Void joinSession(TrainingSession trainingSession) {
        if (trainingSession == null || !trainingSession.getStatus().equals("started")) {
            Toast.makeText(this,
                    "The session doesn't exists or it is ended. Please check the session id and try again!",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            UserSession session = new UserSession();
            session.setUsername(Username);
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.writeUserSession(trainingSession.getId(), session);
            saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
            Intent i = new Intent(this, SessionActivity.class);
            i.putExtra("username", Username);
            i.putExtra("trainingSessionId", trainingSession.getId());
            startActivity(i);
        }
        return null;
    }

    @Override
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
        }
        else {
            /*TODO:
            *  Avviare l'activity dei risultati invece che la session activity*/
            Intent i = new Intent(this, SessionActivity.class);
            i.putExtra("username", Username);
            i.putExtra("trainingSession", trainingSession);
            startActivity(i);
        }
        return null;
    }

    // aggiorno l'elenco delle past session dell'utente
    private void saveUserPastSession(String sessionId, String date){

        user.addPastSession(sessionId, date);
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.addUserPastSessions(user.getUsername(), user.getPastSessions());

    }

}