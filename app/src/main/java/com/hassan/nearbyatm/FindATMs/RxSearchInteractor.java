package com.hassan.nearbyatm.FindATMs;

import android.location.Location;
import android.location.LocationManager;

import com.google.gson.JsonObject;
import com.hassan.nearbyatm.Base.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class RxSearchInteractor implements SearchInteractor {

    private Location myLocation;
    private List<ATM> myAtms = new ArrayList<>();

    @Override
    public void search(Location location, int radius, final Callback callback) {
        this.myLocation = location;
        myAtms.clear();
        RetrofitClient.getGoogleApisInstance().create(ATMSearchService.class).searchATMsWithRx(location.getLatitude() + "," + location.getLongitude(), radius, "atm", true, "AIzaSyDwNBXmHBDQ29JWsRH8gwNVkf7mM0-flaI")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        try {
                            List<ATM> atms = parseATMJson(responseBody.string());
                            final List<ATM> atmsToEmit;
                            if (atms.size() > 5) {
                                List<ATM> nearestFiveAtms = new ArrayList<>();
                                for (int i = 0; i < 5; i++) {
                                    nearestFiveAtms.add(atms.get(i));
                                }
                                atmsToEmit = nearestFiveAtms;
                            } else {
                                atmsToEmit = atms;
                            }

                            Observable.create(new ObservableOnSubscribe<ATM>() {
                                @Override
                                public void subscribe(ObservableEmitter<ATM> emitter) throws Exception {
                                    for (ATM atm : atmsToEmit)
                                        if (!emitter.isDisposed())
                                            emitter.onNext(atm);
                                    if (!emitter.isDisposed())
                                        emitter.onComplete();
                                }
                            })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .flatMap(new Function<ATM, ObservableSource<?>>() {
                                        @Override
                                        public ObservableSource<?> apply(final ATM atm) throws Exception {
                                            return Observable.create(new ObservableOnSubscribe<ATM>() {
                                                @Override
                                                public void subscribe(final ObservableEmitter<ATM> emitter) throws Exception {
                                                    RetrofitClient.getRetrofitInstance().create(ATMSearchService.class)
                                                            .getAtmStatus(atm.getId())
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(new SingleObserver<ResponseBody>() {
                                                                @Override
                                                                public void onSubscribe(Disposable d) {

                                                                }

                                                                @Override
                                                                public void onSuccess(ResponseBody responseBody) {
                                                                    try {
                                                                        JSONObject object = new JSONObject(responseBody.string());
                                                                        atm.setOnline(object.getBoolean("is_online"));
                                                                        emitter.onNext(atm);
                                                                        emitter.onComplete();
                                                                    } catch (Exception e) {
                                                                        emitter.onError(e);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onError(Throwable e) {
                                                                    emitter.onError(e);
                                                                }
                                                            });
                                                }
                                            });
                                        }
                                    })
                                    .subscribe(new Observer<Object>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onNext(Object o) {
                                            myAtms.add((ATM) o);
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            Collections.sort(myAtms, new Comparator<ATM>() {
                                                @Override
                                                public int compare(ATM atm, ATM t1) {
                                                    return myLocation.distanceTo(atm.getLocation()) > myLocation.distanceTo(t1.getLocation()) ? 0 : -1;
                                                }
                                            });

                                            callback.onSuccess(myAtms);
                                        }
                                    });


                        } catch (Exception ex) {
                            callback.onError(ex.getMessage());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(e.getMessage());
                    }
                });
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
}