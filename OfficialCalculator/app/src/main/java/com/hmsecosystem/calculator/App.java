package com.hmsecosystem.calculator;



import android.content.res.Resources;
import androidx.multidex.MultiDexApplication;

public class App extends MultiDexApplication {
    private static Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();
        resources = getResources();
    }

    public static Resources getAppResources() {
        return resources;
    }

}