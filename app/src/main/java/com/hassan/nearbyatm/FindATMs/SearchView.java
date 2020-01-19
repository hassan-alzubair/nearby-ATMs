package com.hassan.nearbyatm.FindATMs;

import java.util.List;

public interface SearchView {
    void showLoading();
    void hideLoading();
    void showList(List<ATM> atms);
    void showError(String err);
}
