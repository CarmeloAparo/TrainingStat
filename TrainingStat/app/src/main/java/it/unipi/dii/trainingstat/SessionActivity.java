package it.unipi.dii.trainingstat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SessionActivity extends AppCompatActivity {

    Date startTime;
    TextView TimerTV;
    boolean paused;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        Intent i = getIntent();
        String Username = i.getStringExtra("sessionId");
        TextView UsernameTextView = (TextView) findViewById(R.id.SessionId);
        UsernameTextView.setText(Username);


        TimerTV = findViewById(R.id.textViewTimer);
        TimerTV.setText("00:00:00");

        startTime = null;
        paused = true;

    }

    // calls the right handler method depending on the state of the button
    public void startPauseButtonClicked(View view){
        Button b = (Button)view;
        if(paused) startButtonClicked(b);
        else pauseButtonClicked(b);
        paused = !paused;
    }


    private void startButtonClicked(Button b) {
        if(startTime == null){
            startTime = Calendar.getInstance().getTime();
        }

        Log.i("START", startTime.toString());

        b.setText(R.string.pause_button_text);

        //TODO fare partire il timer, inviare i dati al db



    }


    private void pauseButtonClicked(Button b){

        Log.i("PAUSE", startTime.toString());
        b.setText(R.string.start_button_text);
        Date now = Calendar.getInstance().getTime();

        long diff_millis = now.getTime() - startTime.getTime();
        Date diff = new Date(diff_millis);

        Log.i("DEBUG", diff.toString());

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

        TimerTV.setText(df.format(diff));

        // TODO fermare il timer

    }


    public void stopButtonClicked(View view) {

        /*
        TODO bloccare il timer,
          chiudere la sessione,
          chiudere l'attività corrente,
           passare all'attività di visualizzazione delle statistiche
        */



    }


}