package com.challenge.glovo.glovo;

import android.content.Context;

import com.challenge.glovo.glovo.network.GlovoApi;

/**
 * Created by assem on 2/7/2018.
 */

public class AppDataManager {

    private final GlovoApi glovoApi;

    public AppDataManager(Context context,
                          GlovoApi glovoApi) {
        this.glovoApi = glovoApi;
    }


    public GlovoApi getApi() {
        return glovoApi;
    }


}
