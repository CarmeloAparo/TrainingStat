package it.unipi.dii.trainingstat;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseManager {
    private DatabaseReference mDatabase;
    private String idTrainingSession;

    public DatabaseManager() {
        // Connettersi al DB creando la sessione ed ottenendo l'ID
    }

    public DatabaseManager(Integer idTrainingSession) {
        mDatabase = FirebaseDatabase
                .getInstance("https://trainingstat-565d5-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference().child(idTrainingSession.toString());
        this.idTrainingSession = idTrainingSession.toString();
    }
}

/*
 * idTrainingSession {
 *   managerName
 *   status = terminated
 *   idUserSession1 {
 *       username
 *       totSteps
 *       heatmap (int[][])
 *       stillPerc
 *       walkPerc
 *       runPerc
 *       maxSpeed
 *       meanSpeed
 *   }
 *   idUserSession2 {
 *       User 2 data
 *   }
 * }
 * */