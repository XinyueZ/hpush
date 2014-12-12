package com.hpush.gcm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.chopping.net.TaskHelper;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;

/**
 * Edit user setting on server.
 *
 * @author Xinyue Zhao
 */
public final class EditTask extends StringRequest {
	private final Prefs mPrefs;

	public EditTask(Context cxt, int method, String url, Listener<String> listener, ErrorListener errorListener ) {
		super(method, url, listener, errorListener);
		mPrefs = Prefs.getInstance(cxt.getApplicationContext());
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

	public void execute() {
		TaskHelper.getRequestQueue().add(this);
	}
}
