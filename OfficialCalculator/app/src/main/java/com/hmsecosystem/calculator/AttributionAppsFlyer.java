package com.hmsecosystem.calculator;

import static com.hmsecosystem.calculator.App.TAG;

import android.content.Context;
import android.util.Log;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.oaid.OaidClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AttributionAppsFlyer {

    private final Context context;

    private AttributionAppsFlyer(Context ctx){
       context = ctx;
    }

    public static AttributionAppsFlyer createAttributionAppsFlyer(Context ctx) {
        return new AttributionAppsFlyer(ctx);
    }

    public void TrackEvent(){

        /* Track Events in real time */
        Map<String, Object> eventValue = new HashMap<>();
        eventValue.put(AFInAppEventParameterName.REVENUE, 200);
        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "category_a");
        eventValue.put(AFInAppEventParameterName.CONTENT_ID, "1234567");
        eventValue.put(AFInAppEventParameterName.CURRENCY, "USD");

        logInAppEvent();

    }
    public void logInAppEvent(){

        Add2Wishlist();
        fetchOAID();
    }
    public void Add2Wishlist(){
        Map<String, Object> eventValues = new HashMap<>();
        eventValues.put(AFInAppEventParameterName.PRICE, 1234.56);
        eventValues.put(AFInAppEventParameterName.CONTENT_ID,"1234567");

        AppsFlyerLib.getInstance().logEvent(context,
                AFInAppEventType.ADD_TO_WISH_LIST , eventValues);
    }


    public void fetchOAID(){
        OaidClient.Info oaid = new OaidClient(context,1, TimeUnit.SECONDS).fetch();

        if(oaid == null){
            Log.d(TAG,"error can't fetch OAID");
        }
        else{
             String id = oaid.getId();
             String lat = oaid.getLat().toString();
             Log.d(TAG,"oaid: "+ id + "value: "+ lat);
        }
    }
}
