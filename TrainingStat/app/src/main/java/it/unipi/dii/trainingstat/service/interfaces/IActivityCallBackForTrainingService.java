package it.unipi.dii.trainingstat.service.interfaces;

import android.content.Context;

import java.util.List;

public interface IActivityCallBackForTrainingService {
    Context getContext();
    void passStepCounter(int steps);
    void askPermissions(String[] permissions, int permissionCode);
}
