package com.hassan.nearbyatm.Register;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.hassan.nearbyatm.FindATMs.MapsActivity;
import com.hassan.nearbyatm.R;

public class RegisterActivity extends AppCompatActivity implements RegisterView {

    private static final String REGISTERED_KEY = "registered";
    RegisterPresenter presenter;
    RegisterInteractor interactor = new TestRegisterInteractor();
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Register");
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(REGISTERED_KEY, false) == true) {
            startActivity(new Intent(this, MapsActivity.class));
            finish();
        }
        presenter = new RegisterPresenterImpl(this, interactor);

        final AppCompatEditText txtName = findViewById(R.id.txtName);
        final AppCompatEditText txtPhone = findViewById(R.id.txtPhone);
        Button btnRegister = findViewById(R.id.btnRegister);

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait ...");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(txtName.getText())) {
                    txtName.setError("Name is empty");
                    return;
                }
                if (TextUtils.isEmpty(txtPhone.getText())) {
                    txtPhone.setError("Phone is empty");
                    return;
                }
                presenter.register(txtName.getText().toString(), txtPhone.getText().toString());
            }
        });
    }

    @Override
    public void showLoading() {
        dialog.show();
    }

    @Override
    public void hideLoading() {
        dialog.hide();
    }

    @Override
    public void registerCompleted() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(REGISTERED_KEY, true).apply();
        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }

    @Override
    public void showError(String err) {
        new AlertDialog.Builder(this)
                .setMessage(err)
                .setTitle("Error")
                .setCancelable(true)
                .show();
    }
}
