package com.hassan.nearbyatm.FindATMs;

import android.location.Location;
import android.location.LocationManager;

import com.hassan.nearbyatm.Base.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchInteractorImpl implements SearchInteractor, Callback<ResponseBody> {

    private Location myLocation;
    private Callback callback;

    @Override
    public void search(Location location, int radius, Callback callback) {
        this.myLocation = location;
        this.callback = callback;
        RetrofitClient.getGoogleApisInstance().create(ATMSearchService.class).searchATMs(location.getLatitude() + "," + location.getLongitude(), radius, "atm", true, "AIzaSyDwNBXmHBDQ29JWsRH8gwNVkf7mM0-flaI")
                .enqueue(this);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        try {
            List<ATM> atms = parseATMJson(response.body().string());
            Collections.sort(atms, new Comparator<ATM>() {
                @Override
                public int compare(ATM atm, ATM t1) {
                    return myLocation.distanceTo(atm.getLocation()) > myLocation.distanceTo(t1.getLocation()) ? 0 : -1;
                }
            });
            if (atms.size() > 5){
                List<ATM> nearestFiveAtms = new ArrayList<>();
                for (int i=0; i<5; i++){
                    nearestFiveAtms.add(atms.get(i));
                }
                callback.onSuccess(nearestFiveAtms);
            }else{
                callback.onSuccess(atms);
            }
        } catch (Exception ex) {
            callback.onError(ex.getMessage());
        }
    }

    private List<ATM> parseATMJson(String string) throws JSONException {
        JSONObject jsonObject = new JSONObject(string);
        List<ATM> atms = new ArrayList<>();
        JSONArray array = jsonObject.getJSONArray("results");
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(object.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
            location.setLongitude(object.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
            float distanceInKm = myLocation.distanceTo(location) / 1000;
            ATM atm = new ATM();
            atm.setLocation(location);
            atm.setName(object.getString("name"));
            atm.setId(object.getString("place_id"));
            atm.setDistance(distanceInKm);
            atm.setVicinity(object.getString("vicinity"));
            atms.add(atm);
        }
        return atms;
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        callback.onError(t.getMessage());
    }
}
