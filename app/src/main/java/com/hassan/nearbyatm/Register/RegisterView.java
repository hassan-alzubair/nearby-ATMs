package com.hassan.nearbyatm.Register;

public interface RegisterView {
    void showLoading();
    void hideLoading();
    void registerCompleted();
    void showError(String err);
}