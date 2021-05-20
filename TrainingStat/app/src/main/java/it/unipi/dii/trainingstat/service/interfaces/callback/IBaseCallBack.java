package it.unipi.dii.trainingstat.service.interfaces.callback;

import android.content.Context;

public interface IBaseCallBack {

    Context getContext();
    void askPermissions(String[] permissions, int permissionCode);
}
