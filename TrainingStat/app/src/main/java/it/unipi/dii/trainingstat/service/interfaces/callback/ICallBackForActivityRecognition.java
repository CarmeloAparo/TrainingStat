package it.unipi.dii.trainingstat.service.interfaces.callback;

import com.google.android.gms.location.DetectedActivity;

import java.util.EnumSet;
import java.util.Enumeration;

public interface ICallBackForActivityRecognition extends IBaseCallBack {

    void notifyRunning();
    void notifyStill();
    void notifyWalking();
}
