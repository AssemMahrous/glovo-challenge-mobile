package com.challenge.glovo.glovo.ui;

import com.challenge.glovo.glovo.base.MvpPresenter;
import com.challenge.glovo.glovo.network.models.City;
import com.challenge.glovo.glovo.network.models.Country;

import java.util.List;

public interface GlovoMvpPresenter<V extends GlovoMvpView> extends MvpPresenter<V> {

    void getCities();

    void getCountries();

    void setCountries(List<Country> countries);

    void setCities(List<City> cities);

    void getData();
}
