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

package com.hmsecosystem.calculator.iap;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.util.Log;

import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.entity.OrderStatusCode;

public class ExceptionHandle {
    /**
     * The exception is solved.
     */
    public static final int SOLVED = 0;

    /**
     * Handles the exception returned from the IAP API.
     *
     * @param activity The Activity to call the IAP API.
     * @param e The exception returned from the IAP API.
     * @return int
     */
    public static int handle(Activity activity, Exception e) {

        if (e instanceof IapApiException) {
            IapApiException iapApiException = (IapApiException) e;
            Log.i(TAG, "returnCode: " + iapApiException.getStatusCode());
            if (iapApiException.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                IapRequestHelper.startResolutionForResult(activity, iapApiException.getStatus(), Constants.REQ_CODE_LOGIN);
            }
        } else {
            Log.e(TAG, e.getMessage());
        }
        return SOLVED;
    }
}