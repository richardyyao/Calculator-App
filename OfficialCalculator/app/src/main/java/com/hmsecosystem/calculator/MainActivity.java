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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.common.GoogleApiAvailability;
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

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean adsFlag = false;
    private static final String REMOVE_ADS_PRODUCTID = "RemoveAds1";
    private boolean isRemoveAdsPurchased = false;

    private BannerView defaultBannerView;
    private static final int REFRESH_TIME = 60;

    private InterstitialAd interstitialAdHms;

    private RewardAd rewardedAdHms;

    private static final String TAG = "MainActivity";
    private String pushtoken = "";

    private IapClient mClient;
    // Define a variable for the Analytics Kit instance.
    HiAnalyticsInstance instance;

    //Google AdMob
    private AdView adViewBanner;
    private boolean gmsMode = false;
    boolean isLoading;
    private RewardedAd rewardedAdGms;
    private com.google.android.gms.ads.interstitial.InterstitialAd interstitialAdGms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isGmsAvailable()){
            // Enable Analytics Kit logging.
            HiAnalyticsTools.enableLog();

            // Generate an Analytics Kit instance.
            instance = HiAnalytics.getInstance(this);
            mClient = Iap.getIapClient(this);

            HwAds.init(this);
            queryPurchases(null);
            getToken();
        }else{
            // Initialize the Mobile Ads SDK.
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {}
            });

            // Set your test devices. Check your logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
            // to get test ads on this device."
            MobileAds.setRequestConfiguration(
                    new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
                            .build());

            loadBannerAdGms();
            loadRewardedAdGms();
            loadInterstitialAdGms();
            gmsMode = true;
        }


    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
        if(!gmsMode){
            queryPurchases(null);
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d(TAG, "onRestart");
        if(!gmsMode){
            queryPurchases(null);
        }
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

    private void loadDefaultBannerAdHms() {
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
        if(gmsMode){
            menu.removeItem(R.id.remove_ads);
        }else if(!adsFlag){
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
                if(gmsMode){
                    showRewardedVideoGms();
                }else{
                    rewardAdShowHms();
                }
                return true;
            case R.id.interstitial_ad:
                if(gmsMode){
                    showInterstitialGms();
                }else{
                    loadInterstitialAdHms();
                }
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

    private AdListener adListenerInterstitialHms = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            showInterstitialHms();
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

    private void loadInterstitialAdHms() {
        interstitialAdHms = new InterstitialAd(this);
        interstitialAdHms.setAdId(getString(R.string.image_ad_id));
        interstitialAdHms.setAdListener(adListenerInterstitialHms);

        AdParam adParam = new AdParam.Builder().build();
        interstitialAdHms.loadAd(adParam);
    }

    private void showInterstitialHms() {
        // Display an interstitial ad.
        if (interstitialAdHms != null && interstitialAdHms.isLoaded()) {
            interstitialAdHms.show(this);
        }
    }

    /**
     * Load a rewarded ad.
     */
    private void loadRewardAdHms() {
        if (rewardedAdHms == null) {
            rewardedAdHms = new RewardAd(this, getString(R.string.ad_id_reward));
        }

        RewardAdLoadListener rewardAdLoadListener = new RewardAdLoadListener();
        rewardedAdHms.loadAd(new AdParam.Builder().build(), rewardAdLoadListener);
    }

    /**
     * Display a rewarded ad.
     */
    private void rewardAdShowHms() {
        if (rewardedAdHms.isLoaded()) {
            rewardedAdHms.show(this, new RewardAdStatusListener() {
                @Override
                public void onRewardAdClosed() {
                    loadRewardAdHms();
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
            showAdsHms();
        }
    }

    private void showAdsHms(){
        adsFlag=true;
        loadDefaultBannerAdHms();
        loadRewardAdHms();
    }

    public boolean isGmsAvailable() {
        boolean isAvailable;
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        isAvailable = (com.google.android.gms.common.ConnectionResult.SUCCESS == result);
        Log.i(TAG, "isGmsAvailable: " + isAvailable);
        return isAvailable;
    }

    private void loadBannerAdGms(){
        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        adViewBanner = findViewById(R.id.ad_view_banner);
        // Create an ad request.
        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adViewBanner.loadAd(adRequest);
    }

    private void loadRewardedAdGms() {
        if (rewardedAdGms == null) {
            Log.d(TAG, "loading GMS reward ad");
            isLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    getString(R.string.reward_ad_id_gms),
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage());
                            MainActivity.this.isLoading = false;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            MainActivity.this.rewardedAdGms = rewardedAd;
                            MainActivity.this.isLoading = false;
                            Log.d(TAG, "loaded GMS reward ad");
                        }
                    });
        }
    }

    private void showRewardedVideoGms() {

        if (rewardedAdGms == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }

        rewardedAdGms.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "onAdShowedFullScreenContent");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAdHms = null;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAdHms = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");
//                        MainActivity.this.loadRewardedAd();
                    }
                });

        Activity activityContext = MainActivity.this;
        rewardedAdGms.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("TAG", "The user earned the reward.");
                    }
                });
    }

    public void loadInterstitialAdGms() {
        AdRequest adRequest = new AdRequest.Builder().build();
        com.google.android.gms.ads.interstitial.InterstitialAd.load(
                this,
                getString(R.string.interstitial_ad_id_gms),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        MainActivity.this.interstitialAdGms = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        MainActivity.this.interstitialAdGms = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        MainActivity.this.interstitialAdGms = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        interstitialAdGms = null;
                    }
                });
    }

    private void showInterstitialGms() {
        if (interstitialAdGms != null) {
            interstitialAdGms.show(this);
        }
    }

}
