package com.hassan.nearbyatm.Register;

public class RegisterPresenterImpl implements RegisterPresenter, RegisterInteractor.Callback {

    private RegisterView view;
    private RegisterInteractor interactor;

    public RegisterPresenterImpl(RegisterView view, RegisterInteractor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    @Override
    public void register(String username, String phone) {
        view.showLoading();
        interactor.register(username, phone, this);
    }

    @Override
    public void onSuccess() {
        view.hideLoading();
        view.registerCompleted();
    }

    @Override
    public void onError(String err) {
        view.hideLoading();
        view.showError(err);
    }
}