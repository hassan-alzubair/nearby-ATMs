package com.hassan.nearbyatm.FindATMs;

import android.location.Location;

import java.util.List;

public class SearchPresenterImpl implements SearchPresenter, SearchInteractor.Callback {

    SearchView view;
    SearchInteractor interactor;

    public SearchPresenterImpl(SearchView view, SearchInteractor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    @Override
    public void search(Location location, int radius) {
        view.showLoading();
        interactor.search(location, radius, this);
    }

    @Override
    public void onSuccess(List<ATM> atms) {
        view.hideLoading();
        view.showList(atms);
    }

    @Override
    public void onError(String err) {
        view.hideLoading();
        view.showError(err);
    }
}
