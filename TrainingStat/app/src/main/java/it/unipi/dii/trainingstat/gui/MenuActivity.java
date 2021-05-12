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