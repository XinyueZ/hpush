/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hpush.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.hpush.utils.Prefs;

public class RegistrationIntentService extends IntentService {
	public static final String REGISTRATION_COMPLETE = "registrationComplete";
	private static final String TAG = "RegIntentService";

	public RegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Prefs prefs = Prefs.getInstance(getApplicationContext());
		try {
			synchronized (TAG) {
				InstanceID instanceID = InstanceID.getInstance(this);
				String token = instanceID.getToken(prefs.getPushSenderId() + "", GoogleCloudMessaging.INSTANCE_ID_SCOPE,
						null);
				prefs.setPushRegId(token);
			}
		} catch (Exception e) {
			prefs.setPushRegId(null);
		}
		Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
		LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
	}

}
