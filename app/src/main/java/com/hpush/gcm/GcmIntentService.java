package com.hpush.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.InboxStyle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hpush.R;
import com.hpush.app.activities.MainActivity;
import com.hpush.bus.LoadAllEvent;
import com.hpush.bus.UpdateCurrentTotalMessagesEvent;
import com.hpush.data.Message;
import com.hpush.db.DB;
import com.hpush.utils.Prefs;

import de.greenrobot.event.EventBus;

import static android.media.AudioManager.RINGER_MODE_SILENT;

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


	/**
	 * Put the message into a notification and post it. This is just one simple example of what you might choose to do
	 * with a GCM message.
	 *
	 * @param msg  Data of messages.
	 */
	private void sendNotification(final Bundle msg) {
		final boolean isSummary = Boolean.parseBoolean(msg.getString("isSummary"));

		//Notify only for the "summary"s.
		if(isSummary) {
			final String summary =  msg.getString("summary");
			final int count = Integer.valueOf(msg.getString("count"));
			if(count > 0) {
				String [] lines = summary.split("<tr>");
				if(lines.length>0) {
					InboxStyle style = new InboxStyle();
					for (String line : lines) {
						style.addLine(line);
					}
					final String summaryTitle = getString(R.string.lbl_update_from_hacker_news, count);
					mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
					Intent intent = new Intent(this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					final PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),
							intent, PendingIntent.FLAG_ONE_SHOT);
					mNotifyBuilder = new NotificationCompat.Builder(GcmIntentService.this).setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_stat_yp).setTicker(summaryTitle)
							.setContentTitle(summaryTitle).setContentText(lines[0]).setStyle(style.setBigContentTitle(
									summaryTitle).setSummaryText("+" + count + "...")).setAutoCancel(true).setLargeIcon(
									mLargeIcon);
					mNotifyBuilder.setContentIntent(contentIntent);


					AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					if (audioManager.getRingerMode() != RINGER_MODE_SILENT) {
						mNotifyBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000 });
						mNotifyBuilder.setSound(Uri.parse(String.format("android.resource://%s/%s", getPackageName(), R.raw.signal)));
					}
					mNotifyBuilder.setLights(getResources().getColor(R.color.primary_color), 1000, 1000);


					mNotificationManager.notify(0x98, mNotifyBuilder.build());
					EventBus.getDefault().post(new UpdateCurrentTotalMessagesEvent());
					//Load all data on UI if possible, but I don't this is correct, because the "summary" might be earlier than others.
					EventBus.getDefault().post(new LoadAllEvent());
				}
			}
		} else {
			final String by = msg.getString("by");
			final long id = Long.valueOf(msg.getString("c_id"));
			final long score = Long.valueOf(msg.getString("score"));
			final long commentsCount = Long.valueOf(msg.getString("comments_count"));
			final String text = msg.getString("text");
			final long time = Long.valueOf(msg.getString("time"));
			final String title = msg.getString("title");
			final String url = msg.getString("url");
			final String pushedTime =  msg.getString("pushed_time") ;
			final long pushedtime = Long.valueOf(pushedTime);
			Prefs.getInstance(getApplication()).setLastPushedTime(pushedtime);

			DB db = DB.getInstance(getApplication());
			Message message = new Message(by, id, score, commentsCount, text, time, title, url, pushedtime);
			boolean foundMsg = db.findMessage(message);
			boolean foundBookmark = db.findBookmark(message);
			if(!foundMsg && !foundBookmark) {//To test whether in our local database or not.
				//Save in database.
				db.addMessage(message);
			} else {
				if(foundMsg) {
					db.updateMessage(message);
				} else if(foundBookmark) {
					db.updateBookmark(message);
				}
			}
		}
	}
}
