package com.hpush.app;


import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

public final class AppGuardService extends GcmTaskService {
	private static final String TAG = "AppGuardService";
	private static int sLastHour = -1;
	private static int sLastMin = -1;

	@Override
	public int onRunTask(TaskParams taskParams) {
		synchronized (AppGuardService.TAG) {
			Intent service = null;
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int min = calendar.get(Calendar.MINUTE);
			if (hour == sLastHour && min == sLastMin) {
				return GcmNetworkManager.RESULT_SUCCESS;
			}
			sLastHour = hour;
			sLastMin = min;
			int day = calendar.get(Calendar.DAY_OF_WEEK);


			service = initService(this, false);


			if (day == Calendar.SUNDAY) {
				service = initService(this, true);
			}

			if (service != null) {
				startService(service);
			}
			return GcmNetworkManager.RESULT_SUCCESS;
		}
	}


	@NonNull
	private Intent initService(Context context, boolean allToRemove) {
		Intent service;
		service = new Intent(context, DeleteDataService.class);
		service.putExtra(DeleteDataService.EXTRAS_RMV_ALL, allToRemove);
		return service;
	}
}
