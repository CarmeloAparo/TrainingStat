package it.unipi.dii.trainingstat.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import it.unipi.dii.trainingstat.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve all relevant references of the initial activity
        Button ConfirmButton = findViewById(R.id.ConfirmButton);

        ConfirmButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        EditText UsernameInput = findViewById(R.id.editTextUsernameName);
        String Username = UsernameInput.getText().toString();

        // the username "aggregatedResults" is not usable
        if (Username.equals("aggregatedResults")){

            Toast.makeText(this, "Username not valid", Toast.LENGTH_SHORT).show();

        }else {

            Intent i = new Intent(this, MenuActivity.class);
            i.putExtra("username", Username);
            startActivity(i);
        }
    }
}