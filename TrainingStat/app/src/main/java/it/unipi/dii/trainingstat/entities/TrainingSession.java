package it.unipi.dii.trainingstat.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TrainingSession implements Serializable {
    public static final String STATUS_STARTED = "started";
    public static final String STATUS_TERMINATED = "terminated";

    private String id;
    private String trainer;
    private String status;
    private String startDate;
    private String endDate;
    private Map<String, UserSession> userSessions;

    public TrainingSession(){}

    public TrainingSession(String id, String trainer, String status, String startDate, String endDate){
        this.id = id;
        this.trainer = trainer;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userSessions = new HashMap<>();
    }

    public Void copyFrom (TrainingSession ts){
        if(ts == null){
            return null;
        }
        this.id = ts.id;
        this.trainer = ts.trainer;
        this.status = ts.status;
        this.startDate = ts.startDate;
        this.endDate = ts.endDate;
        this.userSessions = ts.userSessions;
        return null;
    }

    public String getId() { return id; }

    public String getTrainer() {
        return trainer;
    }

    public String getStatus() {
        return status;
    }

    public String getStartDate() { return startDate; }

    public String getEndDate() { return endDate; }

    public Map<String, UserSession> getUserSessions() {
        return userSessions;
    }

    public UserSession getSessionOfUser(String username){
        return userSessions.get(username);
    }

    public void addUserSession(UserSession u) {
        String username = u.getUsername();
        u.setUsername(null);
        this.userSessions.put(username, u);
        u.setUsername(username);
    }

    public void setId(String id) { this.id = id; }

    public void setTrainer(String trainer) { this.trainer = trainer; }

    public void setStatus(String newStatus){ this.status = newStatus; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

}
