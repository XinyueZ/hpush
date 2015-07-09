/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hpush.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.iid.InstanceID;
import com.hpush.utils.Prefs;


public class UnregistrationIntentService extends IntentService {
    public static final String UNREGISTRATION_COMPLETE = "unregistrationComplete";
    private static final String TAG = "UnregIntentService";

    public UnregistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
		Prefs prefs =  Prefs.getInstance(getApplicationContext());
        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                instanceID.deleteInstanceID();
				prefs.setPushRegId(null);
            }
        } catch (Exception e) {
        }
        Topics.clear();
        Intent unregistrationComplete = new Intent(UNREGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(unregistrationComplete);
    }
}
