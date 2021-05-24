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
    private User _user;
    private TrainingSession _trainingSession;
    private DatabaseManager _databaseManager;
    private int _numPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        Intent intent = getIntent();
        _user = (User) intent.getSerializableExtra("User");
        _trainingSession = (TrainingSession) intent.getSerializableExtra("TrainingSession");
        TextView usernameTV = findViewById(R.id.trainerUsernameTV);
        TextView sessionIdTV = findViewById(R.id.trainerSessionIdTV);
        usernameTV.setText(_user.getUsername());
        sessionIdTV.setText(_trainingSession.getId());
        Button StopButton = findViewById(R.id.trainerStartStopButton);


        if (_trainingSession.getSessionOfUser(_user.getUsername()) != null){
            // i risultati aggregari sono già stati calcolati -> sessione già terminata
            disableButton(StopButton);

            // devo comunque mostrare i Button degli utenti
            for(UserSession us: _trainingSession.getUserSessions().values()){
                addUserButton(us);
            }
        }
        else{
            // da fare se la sessione è nuova o non ancora conclusa
            StopButton.setText(R.string.stop_button_text);
            _databaseManager = new DatabaseManager();
            _databaseManager.listenUserSessionsAdded(_trainingSession.getId(), this::addUserButton);
            _numPlayers = 0;
        }
    }

    public void stopButtonClicked(View view) {
        // stop button cannot be pressed again
        Button button = (Button) view;
        disableButton(button);

        // nessuno può più joinare
        _databaseManager.removeUserSessionsListener(_trainingSession.getId());

        // recupero tutti gli utenti sessions e vedere se hanno già tutti finito
        _databaseManager.getTrainingSession(_trainingSession.getId(), this::finishStopActivities);

    }

    private void disableButton(Button stopButton){
        stopButton.setText(R.string.trainer_stopped_button);
        stopButton.setEnabled(false);
    }


    public Void finishStopActivities(TrainingSession trainingSession){
        _trainingSession = trainingSession;
        String endDate = TSDateUtils.DateToStringIsoDate(TSDateUtils.getCurrentUTCDate());
        _trainingSession.setEndDate(endDate);
        _databaseManager.updateTrainingEndDate(_trainingSession.getId(), endDate);

        _databaseManager.listenUserSessionsChanged(_trainingSession.getId(), this::addUserSession);
        _databaseManager.updateTrainingStatus(_trainingSession.getId(), TrainingSession.STATUS_TERMINATED);
        trainingSession.setStatus(TrainingSession.STATUS_TERMINATED);

        if(checkIfUserFinished()){
            _databaseManager.removeUserSessionsListener(trainingSession.getId());
            computeAggregateResults();
        }
        return null;
    }

    private boolean checkIfUserFinished() {
        if(_trainingSession.getUserSessions().isEmpty()) return true;

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
        _numPlayers++;
        return null;
    }

    public Void addUserSession(UserSession userSession) {
        // controllare che tutte le sessioni figlie abbiano status terminated

        _trainingSession.addOrUpdateUserSession(userSession);
        if (checkIfUserFinished()) {
            _databaseManager.removeUserSessionsListener(_trainingSession.getId());
            computeAggregateResults();
        }
        return null;
    }

    private void computeAggregateResults() {
        UserSession aggregateResults = new UserSession();
        aggregateResults.setUsername(_user.getUsername());
        aggregateResults.setStatus(UserSession.STATUS_TERMINATED);
        aggregateResults.setStartDate(_trainingSession.getStartDate());
        aggregateResults.setEndDate(_trainingSession.getEndDate());
        aggregateResults.setTotalActivityTime(TSDateUtils.DurationBetweeenStringISODates(_trainingSession.getStartDate(), _trainingSession.getEndDate()));

        int totSteps = 0;
        double stillPerc = 0;
        double walkPerc = 0;
        double runPerc = 0;
        double unkPerc = 0;
        for (Map.Entry<String, UserSession> entry : _trainingSession.getUserSessions().entrySet()) {

            if (entry == null) break; // la sessione non aveva utenti
            UserSession userSession = entry.getValue();
            totSteps += userSession.getTotSteps();
            stillPerc += userSession.getStillPerc();
            walkPerc += userSession.getWalkPerc();
            runPerc += userSession.getRunPerc();
            unkPerc += userSession.getUnknownPerc();
        }
        _numPlayers = (_numPlayers == 0) ? 1: _numPlayers; // evito divisione per 0
        aggregateResults.setTotSteps(totSteps);
        aggregateResults.setStillPerc(stillPerc / _numPlayers);
        aggregateResults.setWalkPerc(walkPerc / _numPlayers);
        aggregateResults.setRunPerc(runPerc / _numPlayers);
        aggregateResults.setUnknownPerc(unkPerc / _numPlayers);
        _databaseManager.writeUserSession(_trainingSession.getId(), aggregateResults);
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

        startResultActivity(userSession, _trainingSession.getId());

    }

    private void startResultActivity(UserSession userSession, String trainingSessionId) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("userSession", userSession);
        i.putExtra("trainingSessionId", trainingSessionId);
        startActivity(i);
    }


}