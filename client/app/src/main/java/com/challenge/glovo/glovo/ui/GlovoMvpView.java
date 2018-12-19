package com.challenge.glovo.glovo.ui;

import com.challenge.glovo.glovo.base.MvpView;
import com.challenge.glovo.glovo.network.models.City;
import com.challenge.glovo.glovo.network.models.Country;

import java.util.List;

public interface GlovoMvpView extends MvpView {

    void setViews();

    void setCities(List<City> cities);

    void setCountries(List<Country> countries);
}
