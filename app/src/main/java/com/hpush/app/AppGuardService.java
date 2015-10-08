package com.hpush.app;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.hpush.db.DB;

/**
 * A server that protect application  by deleting unused data.
 *
 * @author Xinyue Zhao
 */
public class AppGuardService extends Service {
	private IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context cxt, Intent intent) {
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int min = calendar.get(Calendar.MINUTE);
			int day = calendar.get(Calendar.DAY_OF_WEEK);

			if (hour == 0 && min == 15) {
				DB db = DB.getInstance(getApplication());
				db.clearDailies();
			}

			if (day == Calendar.SUNDAY && hour == 23 && min == 45) {
				DB db = DB.getInstance(getApplication());
				db.removeMessage(null);
			}

		}
	};


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Utils.showShortToast(this, "AppGuardService");
		registerReceiver(mReceiver, mIntentFilter);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
}
