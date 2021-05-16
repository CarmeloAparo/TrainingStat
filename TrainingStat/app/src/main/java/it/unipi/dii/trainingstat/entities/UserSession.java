package it.unipi.dii.trainingstat.entities;

import java.io.Serializable;
import java.util.List;

public class UserSession implements Serializable {
    private String username;
    private int totSteps;
    private List<List<Double>> heatmap;
    private double stillPerc;
    private double walkPerc;
    private double runPerc;
    private double maxSpeed;
    private double meanSpeed;

    // Empty constructor is needed by Firebase
    public UserSession() {}

    public UserSession(String username, int totSteps, List<List<Double>> heatmap, double stillPerc,
                       double walkPerc, double runPerc, double maxSpeed, double meanSpeed) {
        this.username = username;
        this.totSteps = totSteps;
        this.heatmap = heatmap;
        this. stillPerc = stillPerc;
        this.walkPerc = walkPerc;
        this.runPerc = runPerc;
        this.maxSpeed = maxSpeed;
        this.meanSpeed = meanSpeed;
    }

    public String getUsername() {
        return username;
    }

    public int getTotSteps() {
        return totSteps;
    }

    public List<List<Double>> getHeatmap() {
        return heatmap;
    }

    public double getStillPerc() {
        return stillPerc;
    }

    public double getWalkPerc() {
        return walkPerc;
    }

    public double getRunPerc() {
        return runPerc;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMeanSpeed() {
        return meanSpeed;
    }

    public void setUsername(String username) { this.username = username; }

    public void setTotSteps(int totSteps) {
        this.totSteps = totSteps;
    }

    public void setHeatmap(List<List<Double>> heatmap) {
        this.heatmap = heatmap;
    }

    public void setStillPerc(double stillPerc) {
        this.stillPerc = stillPerc;
    }

    public void setWalkPerc(double walkPerc) {
        this.walkPerc = walkPerc;
    }

    public void setRunPerc(double runPerc) {
        this.runPerc = runPerc;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setMeanSpeed(double meanSpeed) {
        this.meanSpeed = meanSpeed;
    }
}
