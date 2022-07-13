package com.hmsecosystem.calculator;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.FirebaseMessagingServiceListener;
//import com.appsflyer.attribution.AppsFlyerRequestListener;

public class MyAppsFlyerMsgService extends FirebaseMessagingServiceListener {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

            // Sending new token to AppsFlyer
            AppsFlyerLib.getInstance().updateServerUninstallToken(getApplicationContext(), s);
            // the rest of the code that makes use of the token goes in this method as well
    }
}
