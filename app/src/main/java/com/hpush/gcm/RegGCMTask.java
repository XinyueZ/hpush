package com.hpush.gcm;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chopping.application.LL;
import com.chopping.net.TaskHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;

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
			StringRequest req = new StringRequest(Request.Method.POST, mPrefs.getPushBackendRegUrl(),
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
//						mPrefs.setPushRegId(regId);
						LL.d(response.toString());
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						LL.d(error.toString());
//						mPrefs.setPushRegId(null);
					}
			}) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> headers = super.getHeaders();
					if (headers == null || headers.equals(Collections.emptyMap())) {
						headers = new HashMap<>();
					}
					Utils.makeHttpHeaders(headers);
					headers.put("Cookie", "Account=" + mPrefs.getGoogleAccount()+";pushID=" + regId + ";isFullText=" + mPrefs.isOnlyFullText() + ";msgCount=" + mPrefs.getMsgCount() + ";allowEmptyLink=" + mPrefs.allowEmptyUrl());
					return headers;
				}
			};
			TaskHelper.getRequestQueue().add(req);
		}
	}
}

