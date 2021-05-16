package it.unipi.dii.trainingstat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import it.unipi.dii.trainingstat.entities.UserSession;

public class TrainingSession implements Serializable {
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

    public void addUserSession(UserSession u) {
        String username = u.getUsername();
        u.setUsername(null);
        this.userSessions.put(username, u);
        u.setUsername(username);
    }

    public void setId(String id) { this.id = id; }

    public void setTrainer(String trainer) { this.trainer = trainer; }

    public void setStartedStatus() { this.status = "started"; }

    public void setEndedStatus() { this.status = "ended"; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

}
