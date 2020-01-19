package com.hassan.nearbyatm.Register;

public interface RegisterInteractor {

    interface Callback{
        void onSuccess();
        void onError(String err);
    }

    void register(String username,String phone,Callback callback);
}
