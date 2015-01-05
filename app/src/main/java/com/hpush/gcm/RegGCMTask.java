package com.hpush.gcm;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hpush.bus.GCMRegistedEvent;
import com.hpush.bus.InsertAccountEvent;
import com.hpush.utils.Prefs;

import de.greenrobot.event.EventBus;

/**
 * Register GCM.
 *
 * @author Xinyue Zhao
 */
public   class RegGCMTask extends AsyncTask<Void, Void, String> {
	private GoogleCloudMessaging mGCM;
	private Prefs mPrefs;

	public RegGCMTask(Context context) {
		mGCM = GoogleCloudMessaging.getInstance(context);
		mPrefs = Prefs.getInstance(context.getApplicationContext());
	}

	@Override
	protected String doInBackground(Void... params) {
		String regId;
		try {
			if(TextUtils.isEmpty(mPrefs.getPushRegId())) {
				regId = mGCM.register(mPrefs.getPushSenderId() + "");
			} else {
				regId = mPrefs.getPushRegId();
			}
			mPrefs.turnOnPush();
		} catch (IOException ex) {
			regId = null;
		}
		return regId;
	}

	@Override
	protected void onPostExecute(final String regId) {
		if (!TextUtils.isEmpty(regId)) {
			mPrefs.setPushRegId(regId);
			EventBus.getDefault().post(new GCMRegistedEvent());
			EventBus.getDefault().postSticky(new InsertAccountEvent());
		}
	}
}

