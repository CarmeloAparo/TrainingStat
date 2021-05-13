package it.unipi.dii.trainingstat.gui;

import android.annotation.SuppressLint;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.function.Function;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;

public class MenuActivity extends AppCompatActivity {

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
            button.setOnClickListener(pastSessionsButtonListener);
            LinearLayout linearLayout = findViewById(R.id.pastSessionsLayout);
            linearLayout.addView(button);
            id++;
        }
    }

    public TrainingSession startTrainingSession() {
        user.setLastIncrementalID(user.getLastIncrementalID() + 1);
        String id = Username + "_" + user.getLastIncrementalID();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        String startDate = df.format(calendar.getTime());
        TrainingSession trainingSession = new TrainingSession(id, Username, "started", startDate, null);
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.writeTrainingSession(trainingSession);
        databaseManager.updateUserIncrementalID(Username, user.getLastIncrementalID());
        user.addPastSession(id, startDate);
        databaseManager.addUserPastSessions(Username, user.getPastSessions());
        return trainingSession;
    }

    public void newCollectiveSessionButtonClicked(View v) {
        TrainingSession trainingSession = startTrainingSession();
        /* TODO: lanciare la attività dell'allenatore */
    }

    public void newIndividualSessionButtonClicked(View v) {
        TrainingSession trainingSession = startTrainingSession();
        Intent i = new Intent(this, SessionActivity.class);
        i.putExtra("username", Username);
        i.putExtra("trainingSession", trainingSession);
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
            Intent i = new Intent(this, SessionActivity.class);
            i.putExtra("username", Username);
            i.putExtra("trainingSession", trainingSession);
            startActivity(i);
        }
        return null;
    }

    private View.OnClickListener pastSessionsButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*TODO:
            *   Sulla base di quale bottone è stato cliccato prelevare i dati della sessione dal DB
            *   ed avviare l'activity che mostra i risultati
            * */
        }
    };

}