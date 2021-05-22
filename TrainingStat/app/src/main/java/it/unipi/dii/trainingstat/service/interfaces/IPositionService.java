package it.unipi.dii.trainingstat.service.interfaces;

public interface IPositionService {
    int[][] getHeatmap();
    void startScanning();
    void stopScanning();
}
