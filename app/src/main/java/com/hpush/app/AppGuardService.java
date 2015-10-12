package com.hpush.app;

import android.app.IntentService;
import android.content.Intent;

import com.hpush.db.DB;

/**
 * A server that protect application  by deleting unused data.
 *
 * @author Xinyue Zhao
 */
public class AppGuardService extends IntentService {
	public static final String EXTRAS_RMV_ALL = AppGuardService.class.getName() + ".EXTRAS.RMV_ALL";

	public AppGuardService() {
		super("AppGuardService");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		boolean allToRemove = intent.getBooleanExtra(EXTRAS_RMV_ALL, false);
		DB db = DB.getInstance(getApplicationContext());
		if (!allToRemove) {
			db.clearDailies();
		} else {
			db.removeMessage(null);
		}
		WakeupDeviceReceiver.completeWakefulIntent(intent);
	}
}
