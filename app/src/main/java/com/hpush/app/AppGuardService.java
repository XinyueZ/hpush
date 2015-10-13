package com.hpush.app;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

public final class AppGuardService extends Service {
	private IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
	private BroadcastReceiver mReceiver = new WakeupDeviceReceiver();

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerReceiver(mReceiver, mIntentFilter);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		try {
			unregisterReceiver(mReceiver);
		} catch (Exception e) {
			//Ignore...
		}
		super.onDestroy();
	}
}
