package it.unipi.dii.trainingstat.gui;

import androidx.appcompat.app.AppCompatActivity;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.function.Function;

public class TrainerActivity extends AppCompatActivity implements View.OnClickListener {
    private User user;
    private TrainingSession trainingSession;
    private DatabaseManager databaseManager;

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
        startStopButton.setText(R.string.trainerStartButton);
        databaseManager = new DatabaseManager();
        Function<UserSession, Void> addUserButton = this::addUserButton;
        databaseManager.listenUserSessions(trainingSession.getId(), addUserButton);
    }

    public void startStopButtonClicked(View view) {
        Log.d("Test", "StartStopButtonClicked");
        UserSession userSession = new UserSession("topolino", 10, null, 1, 1, 1, 1, 1);
        databaseManager.writeUserSession(trainingSession.getId(), userSession);
    }

    public Void addUserButton(UserSession userSession) {
        Button button = new Button(this);
        button.setText(userSession.getUsername());
        button.setOnClickListener(this);
        LinearLayout linearLayout = findViewById(R.id.trainerUsersLinearLayout);
        linearLayout.addView(button);
        return null;
    }

    public Void addUserSession(UserSession userSession) {
        Log.d("Test", "AddUserSession");
        return null;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        Log.d("Test", button.getText().toString());
    }
}