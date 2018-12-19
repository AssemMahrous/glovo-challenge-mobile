package com.challenge.glovo.glovo.ui;

import com.challenge.glovo.glovo.AppDataManager;
import com.challenge.glovo.glovo.base.BasePresenter;
import com.challenge.glovo.glovo.network.models.City;
import com.challenge.glovo.glovo.network.models.Country;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GlovoPresenter<V extends GlovoMvpView> extends BasePresenter<V> implements GlovoMvpPresenter<V> {

    public GlovoPresenter(AppDataManager dataManager) {
        super(dataManager);
    }

    // getting cities
    @Override
    public void getCities() {
        getDataManager()
                .getApi()
                .getCities()
                .enqueue(new Callback<List<City>>() {
                    @Override
                    public void onResponse(Call<List<City>> call, Response<List<City>> response) {
                        try {
                            if (response.isSuccessful()) {
                                getCountries();
                                setCities(response.body());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<City>> call, Throwable t) {

                    }
                });
    }

    // getting countries
    @Override
    public void getCountries() {
        getDataManager()
                .getApi()
                .getCountries()
                .enqueue(new Callback<List<Country>>() {
                    @Override
                    public void onResponse(Call<List<Country>> call, Response<List<Country>> response) {
                        try {
                            if (response.isSuccessful()) {
                                setCountries(response.body());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Country>> call, Throwable t) {

                    }
                });
    }

    @Override
    public void setCountries(List<Country> countries) {
        getMvpView().setCountries(countries);
    }

    @Override
    public void setCities(List<City> cities) {
        getMvpView().setCities(cities);
    }

    @Override
    public void getData() {
        getCities();
    }
}
