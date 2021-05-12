package it.unipi.dii.trainingstat;

import java.util.List;

public class TrainingSession {
    private String trainer;
    private boolean terminate;
    private List<UserSession> sessions;

    public TrainingSession(){}

    public TrainingSession(String trainer, boolean terminate, List<UserSession> list){
        this.trainer = trainer;
        this.terminate = terminate;
        this.sessions = list;
    }

    public String getTrainer() {
        return trainer;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public List<UserSession> getSessions() {
        return sessions;
    }

    public void addSession(UserSession u){
        this.sessions.add(u);
    }

}
