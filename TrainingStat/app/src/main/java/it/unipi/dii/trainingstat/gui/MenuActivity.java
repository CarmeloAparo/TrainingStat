package it.unipi.dii.trainingstat.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.SessionActivity;

public class MenuActivity extends AppCompatActivity {

    String Username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent i = getIntent();
        Username = i.getStringExtra("username");
        TextView UsernameTextView = findViewById(R.id.menuUsernameTV);
        UsernameTextView.setText(Username);

        // TODO recuperare lista last sessions se presente

    }

    public void newCollectiveSessionButtonClicked(View v){
        Toast.makeText(this, "New collective session", Toast.LENGTH_SHORT).show();

        /* TODO: generate una nuova session ID e aggiornare il DB
        *    lanciare la attività dell'allenatore */

    }

    public void newIndividualSessionButtonClicked(View v){

        Intent i = new Intent(this, SessionActivity.class);
        i.putExtra("username", Username);
        /* TODO: generate una nuova session ID e aggiornare il DB
        */
        i.putExtra("sessionId", Username + "_2"); // CAMBIARE QUESTO VALORE HARD CODED
        startActivity(i);

    }

    public void joinCollectiveSessionButtonClicked(View v){
        Toast.makeText(this, "Join collective session", Toast.LENGTH_SHORT).show();


        /* TODO: controllare che sia stato inserito qualcosa nell'edit text
        *   controllare che la sessione esista e che abbia status "started"
        *   in caso di errore mostrare un toast informativo
        *   altrimenti avviare la prossima attività
        */

    }


}