package com.hpush.gcm;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chopping.net.TaskHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hpush.utils.Prefs;

/**
 * Register GCM.
 *
 * @author Xinyue Zhao
 */
public   class UnregGCMTask extends AsyncTask<Void, Void, String> {
	private GoogleCloudMessaging mGCM;
	private Prefs mPrefs;
	private String mAccount;

	public UnregGCMTask(Context context, String account) {
		mGCM = GoogleCloudMessaging.getInstance(context);
		mPrefs = Prefs.getInstance(context.getApplicationContext());
		mAccount = account;
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
			StringRequest req = new StringRequest(Request.Method.POST, mPrefs.getPushBackendUnregUrl(),
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						mPrefs.setPushRegId(null);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mPrefs.setPushRegId(null);
					}
			}) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> headers = super.getHeaders();
					if (headers == null || headers.equals(Collections.emptyMap())) {
						headers = new HashMap<>();
					}
					headers.put("Cookie", "Account=" + mAccount);
					return headers;
				}
			};
			TaskHelper.getRequestQueue().add(req);
//		}
	}
}

