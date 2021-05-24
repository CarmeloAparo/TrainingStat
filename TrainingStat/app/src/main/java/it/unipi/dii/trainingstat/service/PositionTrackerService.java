package it.unipi.dii.trainingstat.service;

import it.unipi.dii.trainingstat.service.interfaces.IPositionService;
import it.unipi.dii.trainingstat.service.interfaces.callback.IBaseCallBack;

public class PositionTrackerService implements IPositionService {

    private IBaseCallBack _activity;

    public PositionTrackerService(IBaseCallBack activity){
        _activity = activity;
    }
    @Override
    public int[][] getHeatmap() {
        return new int[0][];
    }

    @Override
    public void startScanning() {

    }

    @Override
    public void stopScanning() {

    }
}
