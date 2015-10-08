package com.hpush.bus;


import android.content.Intent;

/**
 * Common and native sharing event .
 *
 * @author Xinyue Zhao
 */
public final class ShareIntentEvent {
	private Intent mIntent;

	public ShareIntentEvent(Intent intent) {
		mIntent = intent;
	}

	public Intent getIntent() {
		return mIntent;
	}
}
