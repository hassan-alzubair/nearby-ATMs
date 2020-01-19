package com.hassan.nearbyatm.Register;

import android.os.Handler;

public class TestRegisterInteractor implements RegisterInteractor {

    @Override
    public void register(String username, String phone, final Callback callback) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess();
            }
        }, 3000);
    }
}
