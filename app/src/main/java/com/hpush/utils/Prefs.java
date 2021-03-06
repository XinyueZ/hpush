package com.hpush.utils;

import android.content.Context;

import com.chopping.application.BasicPrefs;

/**
 * Application's preferences.
 *
 * @author Xinyue Zhao
 */
public final class Prefs extends BasicPrefs {
	/**
	 * Impl singleton pattern.
	 */
	private static Prefs sInstance;


	public static final  String KEY_PUSH_REG_ID         = "key.push.regid";
	public static final  String KEY_PUSH_SETTING        = "key.push.setting";
	private static final String KEY_LAST_PUSHED_TIME    = "key.last.pushed.time";
	private static final String KEY_G_ACCOUNT           = "key.g.account";
	/**
	 * The display-name of Google's user.
	 */
	private static final String KEY_GOOGLE_DISPLAY_NAME = "key.google.display.name";
	/**
	 * Url to user's profile-image.
	 */
	private static final String KEY_GOOGLE_THUMB_URL    = "key.google.thumb.url";

	public static final String KEY_SORT_TYPE  = "key.sort.type";
	public static final String KEY_SOUND_TYPE = "key.sound.type";

	/**
	 * Storage. Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 * {@code true} if EULA has been shown and agreed.
	 */
	private static final String KEY_EULA_SHOWN = "key_eula_shown";

	private static final String PUSH_HOST                   = "push_host";
	private static final String PUSH_SENDER_ID              = "push_sender_id";
	private static final String PUSH_URL_INFO_BACKEND_SYNC  = "push_url_info_backend_sync";
	private static final String HACKER_NEWS_HOME_URL        = "hacker_news_home_url";
	private static final String HACKER_NEWS_COMMENTS_URL    = "hacker_news_comments_url";
	private static final String HACKER_NEWS_BLOG_URL        = "hacker_news_blog_url";
	private static final String KEY_SHOWN_DETAILS_ADS_TIMES = "ads";
	private static final String SYNC_RETRY                  = "sync_retry";
	private static final String DEFAULT_SORT_VALUE          = "default_sort_value";


	//--------------
	//Different push-newsletters
	public static final String KEY_PUSH_TOPSTORIES  = "key.push.topstories";
	public static final String KEY_PUSH_NEWSTORIES  = "key.push.newstories";
	public static final String KEY_PUSH_ASKSTORIES  = "key.push.askstories";
	public static final String KEY_PUSH_SHOWSTORIES = "key.push.showstories";
	public static final String KEY_PUSH_JOBSTORIES  = "key.push.jobstories";
	public static final String KEY_PUSH_SUMMARY     = "key.push.summary";
	//--------------

	/**
	 * Flag for updated version 2.0 {@true} if upgrade was done.
	 */
	private static final String KEY_UPDATED_V2 = "key.updated.v2";


	/**
	 * Created a DeviceData storage.
	 *
	 * @param context
	 * 		A context object.
	 */
	private Prefs( Context context ) {
		super( context );
	}

	/**
	 * Get instance of  {@link  Prefs} singleton.
	 *
	 * @param _context
	 * 		{@link android.app.Application}.
	 *
	 * @return The {@link  Prefs} singleton.
	 */
	public static Prefs getInstance( Context _context ) {
		if( sInstance == null ) {
			sInstance = new Prefs( _context );
		}
		return sInstance;
	}


	/**
	 * Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @return {@code true} if EULA has been shown and agreed.
	 */
	public boolean isEULAOnceConfirmed() {
		return getBoolean(
				KEY_EULA_SHOWN,
				false
		);
	}

	/**
	 * Set whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @param isConfirmed
	 * 		{@code true} if EULA has been shown and agreed.
	 */
	public void setEULAOnceConfirmed( boolean isConfirmed ) {
		setBoolean(
				KEY_EULA_SHOWN,
				isConfirmed
		);
	}

	public String getPushRegId() {
		return getString(
				KEY_PUSH_REG_ID,
				null
		);
	}

	public void setPushRegId( String regId ) {
		setString(
				KEY_PUSH_REG_ID,
				regId
		);
	}

	private String getPushHost() {
		return getString(
				PUSH_HOST,
				null
		);
	}

	public long getPushSenderId() {
		return getLong(
				PUSH_SENDER_ID,
				-1
		);
	}


	public String getPushBackendSyncUrl() {
		return getPushHost() + getString(
				PUSH_URL_INFO_BACKEND_SYNC,
				null
		);
	}

	public String getHackerNewsHomeUrl() {
		return getString(
				HACKER_NEWS_HOME_URL,
				null
		);
	}

	public String getHackerNewsCommentsUrl() {
		return getString(
				HACKER_NEWS_COMMENTS_URL,
				null
		);
	}

	public String getHackerNewsBlogUrl() {
		return getString(
				HACKER_NEWS_BLOG_URL,
				null
		);
	}


	public int getShownDetailsAdsTimes() {
		return getInt(
				KEY_SHOWN_DETAILS_ADS_TIMES,
				5
		);
	}

	public long getLastPushedTime() {
		return getLong(
				KEY_LAST_PUSHED_TIME,
				-1
		);
	}

	public void setLastPushedTime( long pushedTime ) {
		setLong(
				KEY_LAST_PUSHED_TIME,
				pushedTime
		);
	}

	/**
	 * @return Get logined account.
	 */
	public String getGoogleAccount() {
		return getString(
				KEY_G_ACCOUNT,
				null
		);
	}

	/**
	 * Set logined account.
	 *
	 * @param account
	 * 		logined account.
	 */
	public void setGoogleAccount( String account ) {
		setString(
				KEY_G_ACCOUNT,
				account
		);
	}

	/**
	 * @return Timeout for retry to sync data on backend.
	 */
	public int getSyncRetry() {
		return getInt(
				SYNC_RETRY,
				60
		);
	}

	/**
	 * @return Sort type, by score or time of push, "0-4".
	 */
	public String getSortTypeValue() {
		return getString(
				KEY_SORT_TYPE,
				getDefaultSortValue() + ""
		);
	}

	/**
	 * Set sort type.
	 *
	 * @param sortTypeValue
	 * 		"0-4".
	 */
	public void setSortTypeValue( String sortTypeValue ) {
		setString(
				KEY_SORT_TYPE,
				sortTypeValue
		);
	}


	/**
	 * @return sound type, "0-2".
	 */
	public String getSoundTypeValue() {
		return getString(
				KEY_SOUND_TYPE,
				"0"
		);
	}

	private int getDefaultSortValue() {
		return getInt(
				DEFAULT_SORT_VALUE,
				2
		);
	}

	/**
	 * To check whether the push which is named by {@code keyName} has been subscribed or not.
	 *
	 * @param keyName
	 * 		See.
	 * 		<pre>
	 * 																<code>
	 * 						public static final String KEY_PUSH_TOPSTORIES = "key.push.topstories";
	 * 						public static final String KEY_PUSH_NEWSTORIES = "key.push.newstories";
	 * 						public static final String KEY_PUSH_ASKSTORIES = "key.push.askstories";
	 * 						public static final String KEY_PUSH_SHOWSTORIES = "key.push.showstories";
	 * 						public static final String KEY_PUSH_JOBSTORIES = "key.push.jobstories";
	 * 																</code>
	 * 														</pre>
	 *
	 * @return {@code true} if the push named by {@code keyName}  is subscribed.
	 */
	public boolean getPush( String keyName ) {
		return getBoolean(
				keyName,
				false
		);
	}


	/**
	 * To set whether the push which is named by {@code keyName} has been subscribed or not.
	 *
	 * @param keyName
	 * 		See.
	 * 		<pre>
	 * 																<code>
	 * 						public static final String KEY_PUSH_TOPSTORIES = "key.push.topstories";
	 * 						public static final String KEY_PUSH_NEWSTORIES = "key.push.newstories";
	 * 						public static final String KEY_PUSH_ASKSTORIES = "key.push.askstories";
	 * 						public static final String KEY_PUSH_SHOWSTORIES = "key.push.showstories";
	 * 						public static final String KEY_PUSH_JOBSTORIES = "key.push.jobstories";
	 * 																</code>
	 * 														</pre>
	 */
	public void setPush( String keyName, boolean value ) {
		setBoolean(
				keyName,
				value
		);
	}

	/**
	 * The display-name of Google's user.
	 */
	public String getGoogleDisplyName() {
		return getString(
				KEY_GOOGLE_DISPLAY_NAME,
				null
		);
	}

	/**
	 * The display-name of Google's user.
	 */
	public void setGoogleDisplyName( String displayName ) {
		setString(
				KEY_GOOGLE_DISPLAY_NAME,
				displayName
		);
	}

	/**
	 * Url to user's profile-image.
	 */
	public String getGoogleThumbUrl() {
		return getString(
				KEY_GOOGLE_THUMB_URL,
				null
		);
	}

	/**
	 * The display-name of Google's user.
	 */
	public void setGoogleThumbUrl( String thumbUrl ) {
		setString(
				KEY_GOOGLE_THUMB_URL,
				thumbUrl
		);
	}

	/**
	 * Flag for updated version 2.0
	 *
	 * @return {@true} if upgrade was done.
	 */
	public boolean isUpdatedV2() {
		return getBoolean(
				KEY_UPDATED_V2,
				false
		);
	}

	/**
	 * Flag for updated version 2.0
	 *
	 * @param updatedV2
	 * 		{@true} if upgrade was done.
	 */
	public void setUpdatedV2( boolean updatedV2 ) {
		setBoolean(
				KEY_UPDATED_V2,
				updatedV2
		);
	}
}
