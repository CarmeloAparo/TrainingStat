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

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "[MainActivity]";

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent i = getIntent();
        user = (User) i.getSerializableExtra("User");
        TextView UsernameTextView = findViewById(R.id.menuUsernameTV);
        UsernameTextView.setText(user.getUsername());
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

    public TrainingSession startNewTrainingSession() {
        user.setLastIncrementalID(user.getLastIncrementalID() + 1);
        String id = user.getUsername() + "_" + user.getLastIncrementalID();
        TrainingSession trainingSession = new TrainingSession(id, user.getUsername(), "started", TSDateUtils.DateToJsonString(TSDateUtils.getCurrentUTCDate()), null);
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.writeTrainingSession(trainingSession);
        databaseManager.updateUserIncrementalID(user.getUsername(), user.getLastIncrementalID());
        return trainingSession;
    }

    public void newCollectiveSessionButtonClicked(View v) {
        TrainingSession trainingSession = startNewTrainingSession();
        saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
        Intent i = new Intent(this, TrainerActivity.class);
        i.putExtra("User", user);
        i.putExtra("TrainingSession", trainingSession);
        startActivity(i);
    }

    public void newIndividualSessionButtonClicked(View v) {
        TrainingSession trainingSession = startNewTrainingSession();
        saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
        startSessionActivity(trainingSession.getId());
    }

    public void joinCollectiveSessionButtonClicked(View v) {
        EditText sessionIdToJoinET = findViewById(R.id.menuInsertSessionIdET);
        String sessionIdToJoin = sessionIdToJoinET.getText().toString();

        TrainingSession ts;
        try {
            ts = SessionResolver.getTrainingSession(sessionIdToJoin);
        } catch (TrainingSessionNotFound trainingSessionNotFound) {
            Toast.makeText(this, "A session id must be provided", Toast.LENGTH_SHORT).show();
            Log.e(TAG, trainingSessionNotFound.getMessage());
            return;
        }

        if(SessionResolver.isIndividualSession(ts)){
            Toast.makeText(this, "<"+ts.getId()+"> is an individual session", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "tried to join an individual session <"+ts.getId()+"> with username <"+user.getUsername()+">");
            return;
        }
        joinSession(ts);
    }

    private void joinSession(TrainingSession trainingSession) {
        if (trainingSession == null || !trainingSession.getStatus().equals(TrainingSession.STATUS_STARTED)) {
            Toast.makeText(this,
                    "The session is ended. Please check the session id and try again!",
                    Toast.LENGTH_SHORT).show();
        } else {
            saveUserPastSession(trainingSession.getId(), trainingSession.getStartDate());
            startSessionActivity(trainingSession.getId());
        }
    }

    private void startSessionActivity(String trainingSessionId){
        Intent i = new Intent(this, SessionActivity.class);
        i.putExtra("username", user.getUsername());
        i.putExtra("trainingSessionId", trainingSessionId);
        startActivity(i);
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
            i.putExtra("username", user.getUsername());
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