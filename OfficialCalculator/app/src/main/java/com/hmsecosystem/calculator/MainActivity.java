/*
      Copyright 2021. Futurewei Technologies Inc. All rights reserved.
      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at
        http:  www.apache.org/licenses/LICENSE-2.0
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
*/

package com.hmsecosystem.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.hmsecosystem.calculator.common.CipherUtil;
import com.hmsecosystem.calculator.common.IapApiCallback;
import com.hmsecosystem.calculator.common.IapRequestHelper;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.ads.reward.RewardAd;
import com.huawei.hms.ads.reward.RewardAdLoadListener;
import com.huawei.hms.ads.reward.RewardAdStatusListener;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean adsFlag = false;
    private static final String REMOVE_ADS_PRODUCTID = "RemoveAds1";
    private boolean isRemoveAdsPurchased = false;

    private BannerView defaultBannerView;
    private static final int REFRESH_TIME = 60;

    private InterstitialAd interstitialAd;

    private RewardAd rewardedAd;

    private EditText screen;
    private  boolean operator, hasdot;

    private static final String TAG = "MainActivity";
    private String pushtoken = "";

    private IapClient mClient;
    // Define a variable for the Analytics Kit instance.
    HiAnalyticsInstance instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable Analytics Kit logging.
        HiAnalyticsTools.enableLog();

        // Generate an Analytics Kit instance.
        instance = HiAnalytics.getInstance(this);
        mClient = Iap.getIapClient(this);

        screen = findViewById(R.id.screen);
        screen.setFocusable(false);

        HwAds.init(this);
        queryPurchases(null);
        getToken();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
        queryPurchases(null);
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d(TAG, "onRestart");
        queryPurchases(null);
    }

    public void add0(View v) {
        operator = false;
        if(screen.getText().toString().compareToIgnoreCase("Infinity") == 0 ||
                (screen.getText().length() == 1 && screen.getText().charAt(0) == '0'))
            screen.setText("");
        screen.setText(screen.getText() + "0");
        moveCarot();
    }

    public void add1(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0') ||
                screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "1");
        moveCarot();
    }

    public void add2(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0')
                || screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "2");
        moveCarot();
    }

    public void add3(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0')
                || screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "3");
        moveCarot();
    }

    public void add4(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0')
                || screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "4");
        moveCarot();
    }

    public void add5(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0')
                || screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "5");
        moveCarot();
    }

    public void add6(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0')
                || screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "6");
        moveCarot();
    }

    public void add7(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0')
                || screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "7");
        moveCarot();
    }

    public void add8(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0')
                || screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "8");
        moveCarot();
    }

    public void add9(View v) {
        operator = false;
        if((screen.getText().length() == 1 && screen.getText().charAt(0) == '0')
                || screen.getText().toString().compareToIgnoreCase("Infinity") == 0)
            screen.setText("");
        screen.setText(screen.getText() + "9");
        moveCarot();
    }

    public void sum(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString()) && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            if(operator) {
                screen.setText(screen.getText().subSequence(0, screen.getText().length() - 3));
            }
            screen.setText(screen.getText().toString() + " + ");
            operator = true;
            hasdot = false;
            moveCarot();
        }
    }

    public void sub(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString()) && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            if(operator) {
                screen.setText(screen.getText().subSequence(0, screen.getText().length() - 3));
            }

            screen.setText(screen.getText() + " - ");
            operator = true;
            hasdot = false;
            moveCarot();
        }
    }

    public void div(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString()) && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            if(operator) {
                screen.setText(screen.getText().subSequence(0, screen.getText().length() - 3));
            }

            screen.setText(screen.getText() + " / ");
            operator = true;
            hasdot = false;
            moveCarot();
        }
    }

    public void mult(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString()) && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            if(operator) {
                screen.setText(screen.getText().subSequence(0, screen.getText().length() - 3));
            }

            screen.setText(screen.getText().toString() + " * ");
            operator = true;
            hasdot = false;
            moveCarot();
        }
    }

    public void sqrt(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString()) &&
                !operator && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            String expression = getLastDigitedNumber();
            String answer = Double.toString(Math.sqrt(Double.parseDouble(expression)));
            hasdot = doesItHasADot(answer);
            if(answer.charAt(answer.length() - 1) == '0' && answer.charAt(answer.length() - 2) == '.') {
                answer = answer.substring(0, answer.length() - 2);
                hasdot = false;
            }
            screen.setText(screen.getText() + answer);
            moveCarot();
        }
    }

    public void powto2(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString())
                && !operator && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            String expression = getLastDigitedNumber();
            String answer = Double.toString(Math.pow(Double.parseDouble(expression), 2));
            hasdot = doesItHasADot(answer);
            if(answer.charAt(answer.length() - 1) == '0' && answer.charAt(answer.length() - 2) == '.') {
                answer = answer.substring(0, answer.length() - 2);
                hasdot = false;
            }
            screen.setText(screen.getText() + answer);
            moveCarot();
        }
    }

    public void fat(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString())
                && !operator && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            String expression = getLastDigitedNumber();
            long toInt = (long)Double.parseDouble(expression);
            if(toInt <= 20) {
                long answer = 1;
                for (int i = 2; i <= toInt; i++) {
                    answer *= i;
                }
                screen.setText(screen.getText() + Long.toString(answer));
            } else {
                screen.setText(screen.getText() + "0");
            }
            hasdot = false;
            moveCarot();
        }
    }
    public void plusminus(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString())
                && !operator && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            String expression = getLastDigitedNumber();
            String d = Double.toString(Double.parseDouble(expression) * -1);
            hasdot = doesItHasADot(d);
            if(d.charAt(d.length() - 1) == '0' && d.charAt(d.length() - 2) == '.') {
                d = d.substring(0, d.length() - 2);
                hasdot = false;
            }
            screen.setText(screen.getText() + d);
            moveCarot();
        }
    }

    public void dot(View v) {
        if(!hasdot && !operator
                && !TextUtils.isEmpty(screen.getText().toString())
                && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            screen.setText(screen.getText()+".");
            hasdot = true;
            moveCarot();
        }
    }

    public void clear(View v) {
        screen.setText("");
        hasdot = operator = false;
        moveCarot();
    }

    public void ce(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString()) && !operator) {
            getLastDigitedNumber();
            hasdot = false;
            operator = true;
            moveCarot();
        }
    }

    public void erase(View v) {
        char lastChar = ' ';
        if(!TextUtils.isEmpty(screen.getText().toString())) {
            if(screen.getText().toString().compareToIgnoreCase("Infinity") == 0) {
               screen.setText("");
            } else {
                if (operator) {
                    screen.setText(screen.getText().subSequence(0, screen.getText().length() - 3));
                    operator = false;
                } else {
                    lastChar = screen.getText().charAt(screen.getText().length() - 1);
                    screen.setText(screen.getText().subSequence(0, screen.getText().length() - 1));
                }
                if (screen.getText().length() > 0) {
                    char currentLastOne = screen.getText().charAt(screen.getText().length() - 1);
                    if (currentLastOne == '.') {
                        screen.setText(screen.getText().subSequence(0, screen.getText().length() - 1));
                        hasdot = false;
                    } else if (currentLastOne == ' ') {
                        operator = true;
                    } else if (currentLastOne == '-') {

                        screen.setText(screen.getText().subSequence(0, screen.getText().length() - 1));
                    }
                }
            }

            if(lastChar == '.')
                hasdot = false;
            moveCarot();
        }
    }


    public void result(View v) {
        if(!TextUtils.isEmpty(screen.getText().toString())
                && !operator && screen.getText().toString().compareToIgnoreCase("Infinity") != 0) {
            List<Double> number = new ArrayList<Double>();
            List<Character> operators = new ArrayList<Character>();

            String expression = screen.getText().toString();
            String value = "";
            expression+=" ";
            for(int i = 0; i < expression.length(); i++) {
                if((expression.charAt(i) != ' ' && !isAnOperatorSign(expression.charAt(i))) ||
                        (isAnOperatorSign(expression.charAt(i)) && expression.charAt(i + 1) != ' ')) {
                    value += expression.charAt(i);
                } else {
                    if(value != "") {
                        number.add(Double.parseDouble(value));
                        value = "";
                    } else {
                        if(isAnOperatorSign(expression.charAt(i))) {
                            operators.add((expression.charAt(i)));
                        }
                    }
                }
            }
            if(operators.size() > 0) {
                String resp = calculation(number, operators).toString();
                hasdot = doesItHasADot(resp);
                if(resp.charAt(resp.length() - 1) == '0' && resp.charAt(resp.length() - 2) == '.') {
                    hasdot = false;
                }
                // update screen
                screen.setText(resp);
                operator = false;
                moveCarot();
            }
        }

    }

    public Double calculation(List<Double>number, List<Character>op) {
        Double resp = 0.0;
        while(number.size() > 1) {
            int i;
            boolean found = false;
            for (i = 0; i < op.size(); i++) {
                if(op.get(i) == '/' || op.get(i) == '*') {
                    found = true;
                    break;
                }
            }

            if(!found) {
                i = 0;
            }
            resp = doMath(number.get(i), number.get(i + 1), op.get(i));
            number.set(i + 1, resp);
            number.remove(i);
            op.remove(i);
        }

        return resp;
    }

    public String getLastDigitedNumber() {
        String expression = "";
        int i = 0;
        for(i = screen.getText().length() - 1; i >= 0; i--) {
            if(screen.getText().charAt(i) != ' ') {
                expression+=screen.getText().charAt(i);
            } else {
                break;
            }
        }
        screen.setText(screen.getText().subSequence(0, i + 1));
        String finalres = "";
        for(i = expression.length() - 1; i >= 0; i--)
            finalres+=expression.charAt(i);

        return finalres;
    }

    public boolean doesItHasADot(String s) {
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '.') {
                return true;
            }
        }
        return false;
    }

    public double doMath(Double n1, double n2, char op) {
        double r = 0;
        switch(op) {
            case '+':
                r = n1 + n2;
                break;
            case '-':
                r = n1 - n2;
                break;
            case '/':
                r = n1 / n2;
                break;
            case '*':
                r = n1 * n2;
                break;
        }
        return r;
    }

    public boolean isAnOperatorSign(char c) {
        return (c == '+' || c == '-' || c == '/' || c == '*');
    }

    public void moveCarot() {
        screen.setSelection(screen.getText().length());
    }

    private void loadDefaultBannerAd() {
        defaultBannerView = findViewById(R.id.hw_banner_view);
        defaultBannerView.setBannerRefresh(REFRESH_TIME);

        AdParam adParam = new AdParam.Builder().build();
        defaultBannerView.loadAd(adParam);
    }

    /**
     * get token
     */
    private void getToken() {
        Log.i(TAG, "get token: begin");
        // get token
        new Thread() {
            @Override
            public void run() {
                try {
                    // read from agconnect-services.json
                    String appId = AGConnectServicesConfig.fromContext(MainActivity.this).getString("client/app_id");
                    pushtoken = HmsInstanceId.getInstance(MainActivity.this).getToken(appId, "HCM");
                    if(!TextUtils.isEmpty(pushtoken)) {
                        Log.i(TAG, "get token:" + pushtoken);
                    }
                } catch (Exception e) {
                    Log.i(TAG,"getToken failed, " + e);

                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        if(!adsFlag){
            menu.removeItem(R.id.reward_ad);
            menu.removeItem(R.id.interstitial_ad);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.reward_ad:
                rewardAdShow();
                return true;
            case R.id.interstitial_ad:
                loadInterstitialAd();
                return true;
            case R.id.remove_ads:
                intent = new Intent(MainActivity.this, NonConsumptionActivity.class);
                startActivity(intent);
                return true;
            case R.id.about:
                intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.privacy_policy:
                intent = new Intent(MainActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
                return true;
            case R.id.terms_of_service:
                intent = new Intent(MainActivity.this, TermsOfServiceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private AdListener adListenerInterstitial = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            showInterstitial();
        }

        @Override
        public void onAdClicked() {
            Log.d(TAG, "onAdClicked");
            super.onAdClicked();
        }

        @Override
        public void onAdOpened() {
            Log.d(TAG, "onAdOpened");
            super.onAdOpened();
        }
    };

    private void loadInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdId(getString(R.string.image_ad_id));
        interstitialAd.setAdListener(adListenerInterstitial);

        AdParam adParam = new AdParam.Builder().build();
        interstitialAd.loadAd(adParam);
    }

    private void showInterstitial() {
        // Display an interstitial ad.
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show(this);
        }
    }

    /**
     * Load a rewarded ad.
     */
    private void loadRewardAd() {
        if (rewardedAd == null) {
            rewardedAd = new RewardAd(this, getString(R.string.ad_id_reward));
        }

        RewardAdLoadListener rewardAdLoadListener = new RewardAdLoadListener();
        rewardedAd.loadAd(new AdParam.Builder().build(), rewardAdLoadListener);
    }

    /**
     * Display a rewarded ad.
     */
    private void rewardAdShow() {
        if (rewardedAd.isLoaded()) {
            rewardedAd.show(this, new RewardAdStatusListener() {
                @Override
                public void onRewardAdClosed() {
                    loadRewardAd();
                }

            });
        }
    }

    private void queryPurchases(final String continuationToken) {
        // Query users' purchased non-consumable products.
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_NONCONSUMABLE, continuationToken, new IapApiCallback<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                Log.i(TAG, "@@@: obtainOwnedPurchases, success, result: " + result);
                checkRemoveAdsPurchaseState(result);
                if (result != null && !TextUtils.isEmpty(result.getContinuationToken())) {
                    queryPurchases(result.getContinuationToken());
                }
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "@@@: obtainOwnedPurchases, type=" + IapClient.PriceType.IN_APP_NONCONSUMABLE + ", " + e.getMessage());
            }
        });
    }

    private void checkRemoveAdsPurchaseState(OwnedPurchasesResult result) {
        if (result == null || result.getInAppPurchaseDataList() == null) {
            Log.i(TAG, "@@@: result is null");
            return;
        }
        List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
        List<String> inAppSignature= result.getInAppSignature();
        for (int i = 0; i < inAppPurchaseDataList.size(); i++) {
            // Check whether the signature of the purchase data is valid.
            if (CipherUtil.doCheck(inAppPurchaseDataList.get(i), inAppSignature.get(i), CipherUtil.getPublicKey())) {
                try {
                    InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataList.get(i));
                    if (inAppPurchaseDataBean.getPurchaseState() == InAppPurchaseData.PurchaseState.PURCHASED) {
                        // Check whether the purchased product is Remove Ads.
                        if (REMOVE_ADS_PRODUCTID.equals(inAppPurchaseDataBean.getProductId())) {
                            isRemoveAdsPurchased = true;
                        }
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "@@@: delivery:" + e.getMessage());
                }
            } else {
                Log.e(TAG, "@@@: delivery:" +  ", verify signature error");
            }
        }
        if (!isRemoveAdsPurchased) {
            showAds();
        }
    }

    private void showAds(){
        adsFlag=true;
        loadDefaultBannerAd();
        loadRewardAd();
    }
}
