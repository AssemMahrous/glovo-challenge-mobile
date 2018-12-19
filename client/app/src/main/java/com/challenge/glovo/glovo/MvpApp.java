package com.challenge.glovo.glovo;

import android.app.Application;

import com.challenge.glovo.glovo.network.ApiModule;


/**
 * Created by assem on 2/8/2018.
 */

public class MvpApp extends Application {
    AppDataManager dataManager;

    @Override
    public void onCreate() {
        super.onCreate();

        ApiModule apiModule = new ApiModule();
        dataManager = new AppDataManager(getApplicationContext(),
                apiModule.provideApiService());


    }


    public AppDataManager getDataManager() {
        return dataManager;
    }


}
