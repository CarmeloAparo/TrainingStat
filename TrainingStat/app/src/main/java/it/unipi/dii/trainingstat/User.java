package it.unipi.dii.trainingstat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String username;
    private int lastIncremental;
    private List<String> pastSessions;

    public User(){
        this.lastIncremental = 0;
        this.pastSessions = new ArrayList<>();
        //this.username = null;
    }

    public User(String username){
        this.username = username;
        this.lastIncremental = 0;
        this.pastSessions = new ArrayList<>();
    }

    public User(User u){
        this.username = u.getUsername();
        this.lastIncremental = u.getLastIncremental();
        this.pastSessions = u.getPastSessions();
    }

    public String getUsername() {
        return username;
    }

    public int getLastIncremental() {
        return lastIncremental;
    }

    public List<String> getPastSessions() {
        return pastSessions;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLastIncremental(int lastIncremental) {
        this.lastIncremental = lastIncremental;
    }

    public void setPastSessions(List<String> pastSessions) {
        this.pastSessions = pastSessions;
    }

    public void addPastSession(String session){
        this.pastSessions.add(session);
    }
}
