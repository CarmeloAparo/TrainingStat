package it.unipi.dii.trainingstat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve all relevant references of the initial activity
        Button ConfirmButton = findViewById(R.id.ConfirmButton);


        ConfirmButton.setOnClickListener(this);

    }

    // triggered only if confirmation button is clicked
    @Override
    public void onClick(View v) {

        EditText UsernameInput = findViewById(R.id.editTextUsernameName);

        String Username = UsernameInput.getText().toString();

        Intent i = new Intent(this, MenuActivity.class);
        i.putExtra("username", Username);
        startActivity(i);
    }
}