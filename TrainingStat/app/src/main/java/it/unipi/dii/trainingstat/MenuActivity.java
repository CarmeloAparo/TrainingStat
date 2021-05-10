package it.unipi.dii.trainingstat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    String Username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent i = getIntent();
        Username = i.getStringExtra("username");
        TextView UsernameTextView = (TextView) findViewById(R.id.textViewUsername);
        UsernameTextView.setText(Username);

    }

    public void StartSessionButtonClicked(View v){

        Intent i = new Intent(this, SessionActivity.class);
        i.putExtra("sessionId", Username);
        startActivity(i);

    }


}