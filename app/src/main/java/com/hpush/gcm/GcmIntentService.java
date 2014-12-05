package com.hpush.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hpush.R;

/**
 * Handle notification.
 *
 * @author Xinyue Zhao
 */
public class GcmIntentService extends IntentService {
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mNotifyBuilder;
	private Bitmap mLargeIcon;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				//ignore.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				//ignore.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				sendNotification(extras);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(final Bundle msg) {
		final String by = msg.getString("by");
		final long id = Long.valueOf(msg.getString("id"));
		final long score = Long.valueOf(msg.getString("score"));
		final String text = msg.getString("text");
		final long time = Long.valueOf(msg.getString("time"));
		final String title = msg.getString("title");
		final String url = msg.getString("url");


		if(TextUtils.isEmpty(text) ) {
			return;
		}
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
				PendingIntent.FLAG_ONE_SHOT);


		mNotifyBuilder = new NotificationCompat.Builder(GcmIntentService.this).setWhen(System.currentTimeMillis())
				.setSmallIcon(R.drawable.ic_stat_yp).setTicker(title).setContentTitle(title).setContentText(text)
				.setStyle(new BigTextStyle().bigText(text).setBigContentTitle(title).setSummaryText("#" + by)).setAutoCancel(true).setLargeIcon(
						mLargeIcon);
		mNotifyBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify((int) id, mNotifyBuilder.build());

	}
}
