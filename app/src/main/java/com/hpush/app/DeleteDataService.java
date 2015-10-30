package com.hpush.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.hpush.R;
import com.hpush.app.activities.MainActivity;
import com.hpush.db.DB;


public class DeleteDataService extends IntentService {
	public static final String EXTRAS_RMV_ALL = DeleteDataService.class.getName() + ".EXTRAS.RMV_ALL";

	public DeleteDataService() {
		super("AppGuardService");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		String appName = getString(R.string.application_name);
		String packageAdr = getPackageName();
		boolean allToRemove = intent.getBooleanExtra(EXTRAS_RMV_ALL, false);
		DB db = DB.getInstance(getApplicationContext());
		if (!allToRemove) {
			db.clearDailies();
			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			sendNotification(0x90, this, appName, getString(R.string.msg_clear_daily), i);
		} else {
			db.removeMessage(null);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setData(Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", packageAdr)));
			sendNotification(0x91, this, appName, getString(R.string.msg_clear_all), i);
		}
	}

	private static final void sendNotification(int id, Context cxt, String title, String content, Intent intent) {
		PendingIntent pendingIntent = PendingIntent.getActivity(cxt, (int) System.currentTimeMillis(), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Bitmap bitmap = BitmapFactory.decodeResource(cxt.getResources(), R.drawable.ic_launcher);
		NotificationManager mgr = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.notify(id, new NotificationCompat.Builder(cxt).setWhen(System.currentTimeMillis()).setAutoCancel(true)
				.setSmallIcon(R.drawable.ic_stat_yp).setLargeIcon(bitmap).setTicker(title).setContentTitle(title)
				.setContentText(content).setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title)
						.setSummaryText(content)).setContentIntent(pendingIntent).setLights(cxt.getResources().getColor(
						R.color.primary_color), 1000, 1000).build());
	}
}
