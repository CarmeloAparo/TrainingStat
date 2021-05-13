package it.unipi.dii.trainingstat;


import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.gui.MainActivity;
import it.unipi.dii.trainingstat.gui.MenuActivity;
import it.unipi.dii.trainingstat.entities.UserSession;

public class DatabaseManager {
    private DatabaseReference mDatabase;


    public DatabaseManager() {
        mDatabase = FirebaseDatabase
                .getInstance("https://trainingstat-565d5-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference();

    }

    public void getUser(String username, Function<User, Void> function){
        mDatabase.child("users").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("Test", "Error on task", task.getException());
                }
                else {
                    DataSnapshot d = task.getResult();
                    User u;
                    if(d.getValue() == null) {
                        u = new User();
                        mDatabase.child("users").child(username).setValue(u);
                    } else {
                        u = d.getValue(User.class);
                    }
                    u.setUsername(username);
                    function.apply(u);
                }
            }
        });
    }

    public void updateUserIncrementalID(String username, int lastIncrementalID) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastIncrementalID", lastIncrementalID);
        mDatabase.child("users").child(username).updateChildren(updates);
    }

    public void addUserPastSessions(String username, List<Map<String, String>> pastSessions) {
        Map<String, Object> sessions = new HashMap<>();
        sessions.put("pastSessions", pastSessions);
        mDatabase.child("users").child(username).updateChildren(sessions);
    }

    public void writeTrainingSession(TrainingSession trainingSession) {
        String trainer = trainingSession.getTrainer();
        String id = trainingSession.getId();
        trainingSession.setTrainer(null);
        trainingSession.setId(null);
        mDatabase.child("trainingSessions").child(id).setValue(trainingSession);
        trainingSession.setTrainer(trainer);
        trainingSession.setId(id);
    }

    public void getTrainingSession(String id, Function<TrainingSession, Void> function){
        mDatabase.child("trainingSessions").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("Test", "Error on task", task.getException());
                }
                else {
                    DataSnapshot d = task.getResult();
                    TrainingSession trainingSession = d.getValue(TrainingSession.class);
                    if(trainingSession != null) {
                        trainingSession.setId(id);
                        String trainer = id.substring(0, id.lastIndexOf("_"));
                        trainingSession.setTrainer(trainer);
                        // Set the username of the user sessions
                        Map<String, UserSession> userSessions = trainingSession.getUserSessions();
                        if (userSessions != null) {
                            for (Map.Entry<String, UserSession> entry : userSessions.entrySet()) {
                                entry.getValue().setUsername(entry.getKey());
                            }
                        }
                    }
                    function.apply(trainingSession);
                }
            }
        });
    }

    // Da qui in poi vecchie funzioni di test da sistemare

    public void writeUserSession(UserSession session) {
        /*String userKey = mDatabase.child("sessions").push().getKey();
        mDatabase.child(userKey).setValue(session);*/
        DatabaseReference dbr = mDatabase;
        String userKey = dbr.child("sessions").push().getKey();
        dbr.child("Training_Data_"+session.getUsername()).setValue(session);
        //dbr.child(userKey).setValue(session);
    }

    public void readData(TrainingSession t, MainActivity m) {

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("Test", "onChildAdded:" + dataSnapshot.getKey());
                // A new comment has been added, add it to the displayed list
                UserSession comment = dataSnapshot.getValue(UserSession.class);

                DataSnapshot u = dataSnapshot;
                //for ( DataSnapshot u : dataSnapshot.getChildren()) {
                    if (u == null) {
                        Log.d("Test", "onChildAdded-Session: null");
                    } else{
                        Log.d("Test", "onChildAdded-Session:" + u.child("username"));
                        String r = (String) u.child("username").getValue();


                        t.addUserSession(u.getValue(UserSession.class));
                        Log.d("Test", "check_Size_list:" + t.getUserSessions().size());

                        // this is to check if when receiving X users data, you can start new activity in the app, and yes, you need to pass the main activity status
                        if(t.getUserSessions().size() > 1) {

                            Intent i = new Intent(m, MenuActivity.class);
                            i.putExtra("username", "username");
                            m.startActivity(i);
                        }
                    }
                    // ...
                //}
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("Test", "onChildChanged:" + dataSnapshot.getKey());
                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("Test", "onChildDeleted:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("Test", "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Test", "onChildCancelled:");
            }
        };
        DatabaseReference dbr = mDatabase.child("sessions");
        dbr.addChildEventListener(childEventListener);

    }
}

/*
DatabaseManager {
	private String trainingSessionId;	// La classe mantiene come argomento l'id della sessione di allenamento
	private sessionListener;

	public User readUser(String username) { Preleva l'utente con username "username" nell'oggetto "users" }

	public void writeUser(User user) { Inserisce utente }

	public void updateUser(String username) { Aggiorna l'utente inserendo una nuova sessione nella lista delle sessioni precedenti ed eventualmente incrementa il contatore degli id delle sessioni precedenti }

	public String writeTrainingSession(TrainingSession trainingSession) { Scrive la sessione di allenamento sul DB. Restituisce l'ID della sessione e lo inserisce nel parametro della classe }

	public void writeUserSessionId(String trainigSessionId) { Inserisce la sessione dell'utente vuota nella training session con chiave trainigSessionId e salva la chiave nel parametro della classe }

	public void listenSessionArrivals(List<String> usernames) { Crea un listener sulla lista "sessions" all'interno della training session. Riceve in input una lista e inserisce gli username delle user session man mano che vengono inserite }

	public void removeListener() { Elimina il listener su "sessions" dopo che la sessione è stata avviata dall'allenatore perchè bisogna farne partire un altro a cui possiamo passare il numero di utenti che dobbiamo attendere prima di prelevare i risultati }

	public void updateStatus(String status) { Aggiorna lo status della sessione di allenamento }

	public void writeUserSession(UserSession userSession) { Scrive i dati dell'utente all'interno della sessione di allenamento }

	public listenSessionResults(TrainingSession trainingSession, int numUsers) { Crea un listener sulla lista "sessions" all'interno della training session. inserisce le user session nell'oggetto training session passato in input. Quando sono stati inserite "numUsers" sessioni, vengono calcolati i risultati aggregati e inseriti sul DB }

	public writeAggregateResults(UserSession results) { Inserisce i risultati aggregati nella sessione di allenamento }

	public listenStatus() { Crea listener sul campo "status" della training session. Quando il campo diventa "ended" calcola le statistiche e le inserisce sul DB }

}

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