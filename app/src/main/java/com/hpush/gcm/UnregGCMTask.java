package com.hpush.gcm;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hpush.bus.DeleteAccountEvent;
import com.hpush.utils.Prefs;

import de.greenrobot.event.EventBus;

/**
 * Register GCM.
 *
 * @author Xinyue Zhao
 */
public   class UnregGCMTask extends AsyncTask<Void, Void, String> {
	private GoogleCloudMessaging mGCM;
	private Prefs mPrefs;

	public UnregGCMTask(Context context  ) {
		mGCM = GoogleCloudMessaging.getInstance(context);
		mPrefs = Prefs.getInstance(context.getApplicationContext());
	}

	@Override
	protected String doInBackground(Void... params) {
		String regId;
		try {
			mGCM.unregister();
			regId = mPrefs.getPushRegId();
			mPrefs.turnOffPush();
		} catch (IOException ex) {
			regId = null;
		}
		return regId;
	}

	@Override
	protected void onPostExecute(final String regId) {
//		if (!TextUtils.isEmpty(regId)) {
		mPrefs.setPushRegId(null);
		EventBus.getDefault().postSticky(new DeleteAccountEvent());
//		}
	}
}

