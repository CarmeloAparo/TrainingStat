package it.unipi.dii.trainingstat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.unipi.dii.trainingstat.gui.UserSession;

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

    public void writeUserSession(UserSession session) {
        String userKey = mDatabase.child(idTrainingSession).push().getKey();
        mDatabase.child(userKey).setValue(session);
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