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

package com.hmsecosystem.calculator.common;

import android.app.Activity;
import android.content.IntentSender;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.StartIapActivityReq;
import com.huawei.hms.iap.entity.StartIapActivityResult;
import com.huawei.hms.support.api.client.Status;

import java.util.List;

public class IapRequestHelper {
    private final static String TAG = "IapRequestHelper";

    /**
     * Create a PurchaseIntentReq object.
     *
     * @param type In-app product type.
     *             The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param productId ID of the in-app product to be paid.
     *              The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @return PurchaseIntentReq
     */
    private static PurchaseIntentReq createPurchaseIntentReq(int type, String productId) {
        PurchaseIntentReq req = new PurchaseIntentReq();
        req.setPriceType(type);
        req.setProductId(productId);
        req.setDeveloperPayload("testPurchase");
        return req;
    }

    /**
     * Create a OwnedPurchasesReq object.
     *
     * @param type type In-app product type.
     *             The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param continuationToken A data location flag which returns from obtainOwnedPurchases api or obtainOwnedPurchaseRecord api.
     * @return OwnedPurchasesReq
     */
    private static OwnedPurchasesReq createOwnedPurchasesReq(int type, String continuationToken) {
        OwnedPurchasesReq req = new OwnedPurchasesReq();
        req.setPriceType(type);
        req.setContinuationToken(continuationToken);
        return req;
    }

    /**
     * Create a ProductInfoReq object.
     *
     * @param type In-app product type.
     *             The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param productIds ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @return ProductInfoReq
     */
    private static ProductInfoReq createProductInfoReq(int type, List<String> productIds) {
        ProductInfoReq req = new ProductInfoReq();
        req.setPriceType(type);
        req.setProductIds(productIds);
        return req;
    }

    /**
     * Obtain in-app product details configured in AppGallery Connect.
     *
     * @param iapClient IapClient instance to call the obtainProductInfo API.
     * @param productIds ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @param type In-app product type.
     *             The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param iapApiCallback IapApiCallback
     */
    public static void obtainProductInfo(IapClient iapClient, final List<String> productIds, int type, final IapApiCallback iapApiCallback) {
        Log.i(TAG, "call obtainProductInfo");

        Task<ProductInfoResult> task = iapClient.obtainProductInfo(createProductInfoReq(type, productIds));
        task.addOnSuccessListener(new OnSuccessListener<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                Log.i(TAG, "obtainProductInfo, success");
                iapApiCallback.onSuccess(result);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "obtainProductInfo, fail");
                iapApiCallback.onFail(e);
            }
        });
    }

    /**
     * Create orders for in-app products in the PMS.
     *
     * @param iapClient IapClient instance to call the createPurchaseIntent API.
     * @param productId ID of the in-app product to be paid.
     *                  The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @param type  In-app product type.
     *              The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param iapApiCallback IapApiCallback
     */
    public static void createPurchaseIntent(final IapClient iapClient, String productId, int type, final IapApiCallback iapApiCallback) {
        Log.i(TAG, "call createPurchaseIntent");
        Task<PurchaseIntentResult> task = iapClient.createPurchaseIntent(createPurchaseIntentReq(type, productId));
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                Log.i(TAG, "createPurchaseIntent, success");
                iapApiCallback.onSuccess(result);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "createPurchaseIntent, fail");
                iapApiCallback.onFail(e);

            }
        });
    }

    /**
     * To start an activity.
     *
     * @param activity The activity to launch a new page.
     * @param status This parameter contains the pendingIntent object of the payment page.
     * @param reqCode Result code.
     */
    public static void startResolutionForResult(Activity activity, Status status, int reqCode) {
        if (status == null) {
            Log.e(TAG, "status is null");
            return;
        }
        if (status.hasResolution()) {
            try {
                status.startResolutionForResult(activity, reqCode);
            } catch (IntentSender.SendIntentException exp) {
                Log.e(TAG, exp.getMessage());
            }
        } else {
            Log.e(TAG, "intent is null");
        }
    }

    /**
     * Query information about all subscribed in-app products, including consumables, non-consumables, and auto-renewable subscriptions.</br>
     * If consumables are returned, the system needs to deliver them and calls the consumeOwnedPurchase API to consume the products.
     * If non-consumables are returned, the in-app products do not need to be consumed.
     * If subscriptions are returned, all existing subscription relationships of the user under the app are returned.
     *
     * @param mClient IapClient instance to call the obtainOwnedPurchases API.
     * @param type In-app product type.
     *             The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param continuationToken A data location flag for a query in pagination mode.
     * @param iapApiCallback IapApiCallback
     */
    public static void obtainOwnedPurchases(IapClient mClient, final int type, String continuationToken, final IapApiCallback iapApiCallback) {
        Log.i(TAG, "call obtainOwnedPurchases");
        Task<OwnedPurchasesResult> task = mClient.obtainOwnedPurchases(IapRequestHelper.createOwnedPurchasesReq(type, continuationToken));
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                Log.i(TAG, "obtainOwnedPurchases, success");
                iapApiCallback.onSuccess(result);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "obtainOwnedPurchases, fail");
                iapApiCallback.onFail(e);
            }
        });

    }
}