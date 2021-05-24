package it.unipi.dii.trainingstat.entities;

import java.io.Serializable;
import java.util.List;

public class UserSession implements Serializable {
    public static String STATUS_READY = "ready";
    public static String STATUS_MONITORING = "monitoring";
    public static String STATUS_PAUSED = "paused";
    public static String STATUS_TERMINATED = "terminated";

    private String username;
    private Integer totSteps;
    private int[][] heatmap;
    private Double stillPerc;
    private Double walkPerc;
    private Double runPerc;
    private Double unknownPerc;
    private String startDate;
    private String endDate;
    private Long totalActivityTime;
    private String status;

    // Empty constructor is needed by Firebase
    public UserSession() {}

    public UserSession(String username, Integer totSteps, int[][] heatmap, Double stillPerc, Double walkPerc, Double runPerc, Double unknownPerc, String startDate, String endDate, Long totalActivityTime, String status) {
        this.username = username;
        this.totSteps = totSteps;
        this.heatmap = heatmap;
        this.stillPerc = stillPerc;
        this.walkPerc = walkPerc;
        this.runPerc = runPerc;
        this.unknownPerc = unknownPerc;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalActivityTime = totalActivityTime;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public Integer getTotSteps() {
        return totSteps;
    }

    public int[][] getHeatmap() {
        return heatmap;
    }

    public Double getStillPerc() {
        return stillPerc;
    }

    public Double getWalkPerc() {
        return walkPerc;
    }

    public Double getRunPerc() {
        return runPerc;
    }

    public void setUsername(String username) { this.username = username; }

    public void setTotSteps(Integer totSteps) {
        this.totSteps = totSteps;
    }

    public void setHeatmap(int[][] heatmap) {
        this.heatmap = heatmap;
    }

    public void setStillPerc(Double stillPerc) {
        this.stillPerc = stillPerc;
    }

    public void setWalkPerc(Double walkPerc) {
        this.walkPerc = walkPerc;
    }

    public void setRunPerc(Double runPerc) {
        this.runPerc = runPerc;
    }

    public Double getUnknownPerc() {
        return unknownPerc;
    }

    public void setUnknownPerc(Double unknownPerc) {
        this.unknownPerc = unknownPerc;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Long getTotalActivityTime() {
        return totalActivityTime;
    }

    public void setTotalActivityTime(Long totalActivityTime) {
        this.totalActivityTime = totalActivityTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
