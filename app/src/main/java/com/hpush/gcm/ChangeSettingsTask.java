package com.hpush.gcm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.chopping.net.GsonRequestTask;
import com.hpush.data.Status;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;

/**
 * Request changing user settings on server.
 *
 * @author Xinyue Zhao
 */
public final class ChangeSettingsTask extends GsonRequestTask {
	private final Prefs mPrefs;

	public ChangeSettingsTask(Context cxt, String url) {
		super(  cxt, Method.POST, url, Status.class);
		mPrefs = Prefs.getInstance(cxt.getApplicationContext());
		setRetryPolicy(new DefaultRetryPolicy(mPrefs.getSyncRetry() * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = super.getHeaders();
		if (headers == null || headers.equals(Collections.emptyMap())) {
			headers = new HashMap<>();
		}
		Utils.makeHttpHeaders(headers);
		headers.put("Cookie", "Account=" + mPrefs.getGoogleAccount()+
				";pushID=" + mPrefs.getPushRegId() + ";isFullText=" + mPrefs.isOnlyFullText() + ";msgCount=" + mPrefs.getMsgCount() + ";allowEmptyLink=" +
						mPrefs.allowEmptyUrl());
		return headers;
	}
}
