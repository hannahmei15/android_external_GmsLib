/*
 * Copyright 2013-2015 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microg.gms.wearable;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.AccountInfo;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.microg.gms.common.api.ApiBuilder;
import org.microg.gms.common.api.ApiConnection;

public class WearableApiBuilder implements ApiBuilder<Wearable.WearableOptions> {
    private static final String TAG = "GmsWearableApi";

    @Override
    public ApiConnection build(Context context, Looper looper, Wearable.WearableOptions options,
            AccountInfo accountInfo, GoogleApiClient.ConnectionCallbacks callbacks,
            GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        Log.d(TAG, "Wearables not supported");
        return null;
    }
}
