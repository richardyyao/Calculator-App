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

import androidx.appcompat.app.AppCompatActivity;

import com.hmsecosystem.calculator.converter.UnitConverter;
import com.hmsecosystem.calculator.iap.CipherUtil;
import com.hmsecosystem.calculator.iap.IapApiCallback;
import com.hmsecosystem.calculator.iap.IapRequestHelper;
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

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean adsFlag = false;
    private static final String REMOVE_ADS_PRODUCTID = "RemoveAds1";
    private boolean isRemoveAdsPurchased = false;

    private BannerView defaultBannerView;
    private static final int REFRESH_TIME = 60;

    private InterstitialAd interstitialAd;

    private RewardAd rewardedAd;

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

    public void onClick(View view)
    {
        Intent i;
        if(view.getId()==R.id.calculatorButton)
        {
            i=new Intent(this,CalculatorActivity.class);
            startActivity(i);
        }
        else if(view.getId()==R.id.converterButton)
        {
            i=new Intent(this, UnitConverter.class);
            startActivity(i);
        }
    }

    private void loadDefaultBannerAd() {
        defaultBannerView = findViewById(R.id.hw_banner_view);
        defaultBannerView.setBannerRefresh(REFRESH_TIME);
        defaultBannerView.setAdId(getString(R.string.banner_ad_id));
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
