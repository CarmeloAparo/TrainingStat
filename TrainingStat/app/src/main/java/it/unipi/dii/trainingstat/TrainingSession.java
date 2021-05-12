package it.unipi.dii.trainingstat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unipi.dii.trainingstat.gui.UserSession;

public class TrainingSession {
    private String trainer;
    private String status;
    private String startDate;
    private String endDate;
    private List<Map<String, UserSession>> userSessions;

    public TrainingSession(){}

    public TrainingSession(String trainer, String status, String startDate, String endDate,
                           List<Map<String, UserSession>> userSessions){
        this.trainer = trainer;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userSessions = userSessions;
    }

    public String getTrainer() {
        return trainer;
    }

    public String getStatus() {
        return status;
    }

    public String getStartDate() { return startDate; }

    public String getEndDate() { return endDate; }

    public List<Map<String, UserSession>> getSessions() {
        return userSessions;
    }

    public void addSession(UserSession u) {
        String username = u.getUsername();
        u.setUsername(null);
        Map<String, UserSession> session = new HashMap<>();
        session.put(username, u);
        this.userSessions.add(session);
    }

    public void setTrainer(String trainer) { this.trainer = trainer; }

    public void setStatus(String status) { this.status = status; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

}
