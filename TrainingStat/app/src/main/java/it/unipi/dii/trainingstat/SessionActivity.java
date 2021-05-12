package it.unipi.dii.trainingstat;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SessionActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private long pauseOffset; // serve per tenere traccia del tempo contato prima di cliccare pausa
    private boolean running;
    private TextView StatusTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        StatusTV = findViewById(R.id.sessionStatusTV);

        // recupero username e session id
        Intent i = getIntent();
        String Username = i.getStringExtra("username");
        String sessionId = i.getStringExtra("sessionId");

        // inizializzo le textView
        TextView UsernameTextView = findViewById(R.id.sessionUsernameTV);
        TextView SessionIdTextView = findViewById(R.id.sessionSessionIdTV);

        UsernameTextView.setText(Username);
        SessionIdTextView.setText(sessionId);

        chronometer = findViewById(R.id.sessionChronometer);

    }

    // calls the right handler method depending on the state of the button
    public void startPauseButtonClicked(View view){
        Button b = (Button)view;

        if(!running) startButtonClicked(b);
        else pauseButtonClicked(b);
        running = !running;
    }


    private void startButtonClicked(Button startPauseButton) {

        StatusTV.setText("Monitoring");
        startPauseButton.setText(R.string.pause_button_text);

        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();

        //TODO fare partire il timer, inviare i dati al db


    }


    private void pauseButtonClicked(Button startPauseButton){

        StatusTV.setText("Paused");
        startPauseButton.setText(R.string.start_button_text);

        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

        // TODO fermare il timer e il recupero dei vari dati

    }


    public void stopButtonClicked(View view) {

        StatusTV.setText("Ready");

        // ATTENZIONE QUESTA SAREBBE LA RESET QUINDI VA CAMBIATA
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;

        /*
        TODO bloccare il timer,
          chiudere la sessione,
          chiudere l'attività corrente,
           passare all'attività di visualizzazione delle statistiche
        */

    }


}


