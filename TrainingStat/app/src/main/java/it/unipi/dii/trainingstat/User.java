package it.unipi.dii.trainingstat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private String username;
    private int lastIncrementalID;
    private List<Map<String, String>> pastSessions;

    public User(){
        this.lastIncrementalID = 0;
        this.pastSessions = new ArrayList<>();
        this.username = null;
    }

    public User(String username){
        this.username = username;
        this.lastIncrementalID = 0;
        this.pastSessions = new ArrayList<>();
    }

    public User(User u){
        this.username = u.getUsername();
        this.lastIncrementalID = u.getLastIncrementalID();
        this.pastSessions = u.getPastSessions();
    }

    public String getUsername() {
        return username;
    }

    public int getLastIncrementalID() {
        return lastIncrementalID;
    }

    public List<Map<String, String>> getPastSessions() {
        return pastSessions;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLastIncrementalID(int lastIncrementalID) {
        this.lastIncrementalID = lastIncrementalID;
    }

    public void setPastSessions(List<Map<String, String>> pastSessions) {
        this.pastSessions = pastSessions;
    }

    public void addPastSession(String sessionID, String date){
        Map<String, String> session = new HashMap<>();
        session.put("id", sessionID);
        session.put("startDate", date);
        pastSessions.add(0, session);
    }
}
