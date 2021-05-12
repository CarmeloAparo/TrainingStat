package it.unipi.dii.trainingstat.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.SessionActivity;
import it.unipi.dii.trainingstat.User;

public class MenuActivity extends AppCompatActivity {

    private String Username;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent i = getIntent();
        //Username = i.getStringExtra("username");
        user = (User) i.getSerializableExtra("User");
        TextView UsernameTextView = findViewById(R.id.menuUsernameTV);
        UsernameTextView.setText(user.getUsername());
        Username = user.getUsername();
        // TODO recuperare lista last sessions se presente

    }

    public void newCollectiveSessionButtonClicked(View v) {
        Toast.makeText(this, "New collective session", Toast.LENGTH_SHORT).show();

        /* TODO: generate una nuova session ID e aggiornare il DB
         *    lanciare la attività dell'allenatore */

    }

    public void newIndividualSessionButtonClicked(View v) {

        Intent i = new Intent(this, SessionActivity.class);
        i.putExtra("username", Username);
        /* TODO: generate una nuova session ID e aggiornare il DB
         */
        i.putExtra("sessionId", Username + "_2"); // CAMBIARE QUESTO VALORE HARD CODED
        startActivity(i);

    }

    public void joinCollectiveSessionButtonClicked(View v) {

        EditText sessionIdToJoinET = findViewById(R.id.menuInsertSessionIdET);
        String sessionIdToJoin = sessionIdToJoinET.getText().toString();

        // Don't even bother the DB with an empty session id
        if (sessionIdToJoin.equals("")) {

            Toast.makeText(this, "A session id must be provided", Toast.LENGTH_SHORT).show();

        } else {

            /* TODO:
             *   controllare che la sessione esista e che abbia status "started"
             *   in caso di errore mostrare un toast informativo
             *   altrimenti avviare la prossima attività
             */

            Toast.makeText(this, "A session id has been provided", Toast.LENGTH_SHORT).show();
        }
    }


}