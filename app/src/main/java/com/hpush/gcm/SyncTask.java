package com.hpush.gcm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.chopping.net.GsonRequestTask;
import com.hpush.data.SyncList;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;

/**
 * Only wrapper for a volley request.
 *
 * @author Xinyue Zhao
 */
public final class SyncTask {
	/**
	 * Only wrapper for a volley request.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	public static void sync( Context cxt ) {
		final Prefs prefs = Prefs.getInstance( cxt );
		GsonRequestTask<SyncList> task = new GsonRequestTask<SyncList>( cxt, Method.POST, prefs.getPushBackendSyncUrl(), SyncList.class ) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String, String> headers = super.getHeaders();
				if( headers == null || headers.equals( Collections.emptyMap() ) ) {
					headers = new HashMap<>();
				}
				Utils.makeHttpHeaders( headers );
				return headers;
			}
		};
		task.setRetryPolicy( new DefaultRetryPolicy( prefs.getSyncRetry() * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
													 DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
		) );
		task.execute();
	}
}
