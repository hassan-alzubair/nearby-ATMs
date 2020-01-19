package com.hassan.nearbyatm.FindATMs;

import android.location.Location;

import java.util.List;

public interface SearchInteractor {
    interface Callback{
        void onSuccess(List<ATM> atms);
        void onError(String err);
    }

    void search(Location location,int radius,Callback callback);
}
