package com.athasamid.sample.pdf.viewer;

import com.androidnetworking.AndroidNetworking;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AndroidNetworking.initialize(this);
    }
}
