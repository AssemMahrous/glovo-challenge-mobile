package com.challenge.glovo.glovo.network;

import com.challenge.glovo.glovo.network.models.City;
import com.challenge.glovo.glovo.network.models.Country;

import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

/**
 * Created by assem on 2/8/2018.
 */

public interface GlovoApi {
    @GET("api/cities/")
    Call<List<City>> getCities();

    @GET("api/countries/")
    Call<List<Country>> getCountries();
}
