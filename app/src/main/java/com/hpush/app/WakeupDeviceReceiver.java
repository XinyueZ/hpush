package com.hpush.app;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Wakeup device.
 *
 * @author Xinyue Zhao
 */
public final class WakeupDeviceReceiver extends WakefulBroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = null;
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int day = calendar.get(Calendar.DAY_OF_WEEK);

		if (hour == 0 && min == 15) {
			service = initService(context, false);
		}

		if (day == Calendar.SUNDAY && hour == 23 && min == 45) {
			service = initService(context, true);
		}

		if (service != null) {
			startWakefulService(context, service);
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