package com.hassan.nearbyatm.FindATMs;

import android.location.Location;

public interface SearchPresenter {
    void search(Location location,int radius);
}
