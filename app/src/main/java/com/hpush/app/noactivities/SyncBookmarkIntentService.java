package com.hpush.app.noactivities;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.hpush.app.App;
import com.hpush.data.Bookmark;
import com.hpush.data.MessageListItem;
import com.hpush.db.DB;
import com.hpush.db.DB.Sort;
import com.hpush.utils.Prefs;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Sync local bookmarks to backend.
 *
 * @author Xinyue Zhao
 */
public final class SyncBookmarkIntentService extends IntentService {
	public static final String  SYNC_COMPLETE = "syncComplete";
	public static final String  SYNC_RESULT   = "result";
	private static final String TAG           = "SyncBookmarkIntentService";

	public SyncBookmarkIntentService() {
		super( TAG );
	}

	@Override
	protected void onHandleIntent( Intent intent ) {
		Prefs prefs = Prefs.getInstance( getApplicationContext() );
		try {
			synchronized( TAG ) {
				BmobQuery<Bookmark> query = new BmobQuery<>();
				query.addWhereEqualTo(
						"mUID",
						prefs.getGoogleAccount()
				);
				query.findObjects(
						SyncBookmarkIntentService.this,
						new FindListener<Bookmark>() {
							@Override
							public void onSuccess( List<Bookmark> list ) {
								syncPull( list );
								syncPush();
								Intent syncComplete = new Intent( SYNC_COMPLETE );
								syncComplete.putExtra(
										SYNC_RESULT,
										true
								);
								LocalBroadcastManager.getInstance( SyncBookmarkIntentService.this )
													 .sendBroadcast( syncComplete );
							}

							@Override
							public void onError( int i, String s ) {
								Intent syncComplete = new Intent( SYNC_COMPLETE );
								if( i == 101 ) {//object not found
									syncComplete.putExtra(
											SYNC_RESULT,
											true
									);
									syncPush();
								} else {
									syncComplete.putExtra(
											SYNC_RESULT,
											false
									);
								}
								LocalBroadcastManager.getInstance( SyncBookmarkIntentService.this )
													 .sendBroadcast( syncComplete );
							}
						}
				);
			}
		} catch( Exception e ) {
			Intent syncComplete = new Intent( SYNC_COMPLETE );
			syncComplete.putExtra(
					SYNC_RESULT,
					false
			);
			LocalBroadcastManager.getInstance( SyncBookmarkIntentService.this )
								 .sendBroadcast( syncComplete );
		}
	}

	/**
	 * Push local bookmarks to backend.
	 */
	private void syncPush() {
		final List<MessageListItem> messagesInBookmarkLocal = DB.getInstance( App.Instance )
																.getBookmarks( Sort.DESC );
		Prefs                       prefs                   = Prefs.getInstance( getApplicationContext() );
		for( MessageListItem item : messagesInBookmarkLocal ) {
			Bookmark newBookmark = new Bookmark(
					prefs.getGoogleAccount(),
					item.getMessage()
			);
			newBookmark.save( SyncBookmarkIntentService.this );
		}
	}

	/**
	 * Pull backend-bookmarks to local.
	 */
	private void syncPull( List<Bookmark> list ) {
		DB db = DB.getInstance( App.Instance );
		for( Bookmark b : list ) {
			if( !db.findBookmark( b ) ) {
				db.addBookmark( b );
			}
		}
	}

}
