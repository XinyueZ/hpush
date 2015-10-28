package com.hpush.app;

import android.app.IntentService;
import android.content.Intent;

import com.hpush.db.DB;


public class DeleteDataService extends IntentService {
	public static final String EXTRAS_RMV_ALL = DeleteDataService.class.getName() + ".EXTRAS.RMV_ALL";

	public DeleteDataService() {
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
	}
}
