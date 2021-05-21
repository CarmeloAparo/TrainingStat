package it.unipi.dii.trainingstat;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import it.unipi.dii.trainingstat.entities.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;

public class DatabaseManager {
    private DatabaseReference mDatabase;
    private ChildEventListener userSessionsListener;


    public DatabaseManager() {
        mDatabase = FirebaseDatabase
                .getInstance("https://trainingstat-565d5-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference();
        userSessionsListener = null;
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

    /*
    non si può eseguire la .awat sul Main thread

    public TrainingSession getTrainingSessionSync(String id) throws Exception {
        Task<DataSnapshot> task = mDatabase.child("trainingSessions").child(id).get();
        DataSnapshot ds;
        try {
            return extractTrainingSessionFromDataSnapshot(Tasks.await(task), id);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new Exception("[getTrainingSessionSync] failed");
        }
    }*/

    public void getTrainingSession(String id, Function<TrainingSession, Void> function){
        mDatabase.child("trainingSessions").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("Test", "Error on task", task.getException());
                    return;
                }
                DataSnapshot d = task.getResult();
                TrainingSession trainingSession = extractTrainingSessionFromDataSnapshot(d, id);
                function.apply(trainingSession);
            }
        });
    }

    private TrainingSession extractTrainingSessionFromDataSnapshot(DataSnapshot dataSnapshot, String id) {
        TrainingSession trainingSession = dataSnapshot.getValue(TrainingSession.class);
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
            else{
                trainingSession.setUserSessions(new HashMap<>());
            }
        }
        return trainingSession;
    }

    public void writeUserSession(String trainingSessionId, UserSession session) {
        String username = session.getUsername();
        session.setUsername(null);
        mDatabase.child("trainingSessions").child(trainingSessionId).child("userSessions")
                .child(username).setValue(session);
        session.setUsername(username);
    }

    public void listenUserSessionsAdded(String id, Function<UserSession, Void> function) {
        if (userSessionsListener != null)
            return;
        userSessionsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String username = snapshot.getKey();
                UserSession userSession = snapshot.getValue(UserSession.class);
                if (userSession != null) {
                    userSession.setUsername(username);
                    function.apply(userSession);    // Add user button in trainer activity
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Log.d("DatabaseManager", "onChildChanged: " + snapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("DatabaseManager", "onChildDeleted: " + snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                Log.d("DatabaseManager", "onChildMoved: " + snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DatabaseManager", "postComments:onCancelled", error.toException());
            }
        };
        mDatabase.child("trainingSessions").child(id).child("userSessions")
                .addChildEventListener(userSessionsListener);
    }

    public void listenUserSessionsChanged(String id, Function<UserSession, Void> function) {
        if (userSessionsListener != null)
            return;
        userSessionsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Log.d("DatabaseManager", "onChildAdded: " + snapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                String username = snapshot.getKey();
                UserSession userSession = snapshot.getValue(UserSession.class);
                if (userSession != null) {
                    userSession.setUsername(username);
                    function.apply(userSession);    // Add user button in trainer activity
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("DatabaseManager", "onChildDeleted: " + snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                Log.d("DatabaseManager", "onChildMoved: " + snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DatabaseManager", "postComments:onCancelled", error.toException());
            }
        };
        mDatabase.child("trainingSessions").child(id).child("userSessions")
                .addChildEventListener(userSessionsListener);
    }

    public void removeUserSessionsListener(String id) {
        if (userSessionsListener != null) {
            mDatabase.child("trainingSessions").child(id).child("userSessions")
                    .removeEventListener(userSessionsListener);
            userSessionsListener = null;
        }
    }


    public void updateTrainingStatus(String trainingId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        mDatabase.child("trainingSessions").child(trainingId).updateChildren(updates);
    }

    public void updateTrainingStartDate(String trainingId, String date) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("startDate", date);
        mDatabase.child("trainingSessions").child(trainingId).updateChildren(updates);
    }

    public void updateTrainingEndDate(String trainingId, String date) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("endDate", date);
        mDatabase.child("trainingSessions").child(trainingId).updateChildren(updates);
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
 * */