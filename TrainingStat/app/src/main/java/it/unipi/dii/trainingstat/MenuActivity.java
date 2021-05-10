package it.unipi.dii.trainingstat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent i = getIntent();
        String Username = i.getStringExtra("username");
        TextView UsernameTextView = (TextView) findViewById(R.id.textViewUsername);
        UsernameTextView.setText(Username);
    }
}