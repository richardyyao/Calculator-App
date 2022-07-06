package com.hmsecosystem.calculator;

import android.app.Application;
import android.content.res.Resources;

import androidx.multidex.MultiDexApplication;

import com.appsflyer.AppsFlyerLib;

public class App extends MultiDexApplication {
    private static Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        resources = getResources();
       final String AF_DEV_KEY = resources.getString(R.string.AF_DEV_KEY);

        AppsFlyerLib.getInstance().init(AF_DEV_KEY,null,this);
        AppsFlyerLib.getInstance().start(this);

//        set single Dashboard third party store name here:
//        The value set here appears in AppsFlyer raw data install_app_store field and in
//        Overview dashboard
//        Cohort dashboard
//        Raw data reports (Raw data reports are an AppsFlyer premium feature)
        AppsFlyerLib.getInstance().setOutOfStore("AG_Connect");
    }

    public static Resources getAppResources() {
        return resources;
    }

}