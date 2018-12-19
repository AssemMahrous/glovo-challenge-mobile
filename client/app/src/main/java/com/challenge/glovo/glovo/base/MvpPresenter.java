package com.challenge.glovo.glovo.base;

/**
 * Created by assem on 1/28/2018.
 */

public interface MvpPresenter <V extends MvpView> {
    void onAttach(V mvpView);
}
