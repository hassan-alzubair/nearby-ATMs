package com.hassan.nearbyatm.FindATMs;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ATMSearchService {

    @GET("/maps/api/place/nearbysearch/json")
    Call<ResponseBody> searchATMs(
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("types") String types,
            @Query("sensor") boolean sensor,
            @Query("key") String key);


    // to use with RxJava
    @GET("/maps/api/place/nearbysearch/json")
    Single<ResponseBody> searchATMsWithRx(
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("types") String types,
            @Query("sensor") boolean sensor,
            @Query("key") String key);


    @GET("/api.php")
    Single<ResponseBody> getAtmStatus(@Query("id") String id);
}
