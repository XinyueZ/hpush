package com.hpush.utils;

import android.content.Context;

import com.chopping.application.BasicPrefs;
import com.hpush.R;

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


	public static final String KEY_PUSH_REG_ID = "key.push.regid";
	public static final String KEY_PUSH_SETTING = "key.push.setting";
	public static final String KEY_FULL_TEXT = "key.only.full.text";
	public static final String KEY_MSG_COUNT = "key.msg.count";
	public static final String KEY_SAVE_LATEST_ONLY = "key.save.latest.only";
	public static final String KEY_ALLOW_EMPTY_URL = "key.allow.empty.url";
	private static final String KEY_LAST_PUSHED_TIME = "key.last.pushed.time";
	private static final String KEY_G_ACCOUNT = "key.g.account";

	/**
	 * Storage. Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 * {@code true} if EULA has been shown and agreed.
	 */
	private static final String KEY_EULA_SHOWN = "key_eula_shown";
	private static final String PUSH_HOST = "push_host";
	private static final String PUSH_SENDER_ID = "push_sender_id";
	private static final String PUSH_URL_INFO_BACKEND_REG = "push_url_info_backend_reg";
	private static final String PUSH_URL_INFO_BACKEND_UNREG = "push_url_info_backend_unreg";
	private static final String PUSH_URL_INFO_BACKEND_EDIT = "push_url_info_backend_edit";
	private static final String HACKER_NEWS_HOME_URL = "hacker_news_home_url";
	private static final String HACKER_NEWS_COMMENTS_URL = "hacker_news_comments_url";
	private static final String KEY_SHOWN_DETAILS_ADS_TIMES = "ads";

	/**
	 * Created a DeviceData storage.
	 *
	 * @param context
	 * 		A context object.
	 */
	private Prefs(Context context) {
		super(context);
	}

	/**
	 * Get instance of  {@link  Prefs} singleton.
	 *
	 * @param _context
	 * 		{@link android.app.Application}.
	 *
	 * @return The {@link  Prefs} singleton.
	 */
	public static Prefs getInstance(Context _context) {
		if (sInstance == null) {
			sInstance = new Prefs(_context);
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
		return getBoolean(KEY_EULA_SHOWN, false);
	}

	/**
	 * Set whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @param isConfirmed
	 * 		{@code true} if EULA has been shown and agreed.
	 */
	public void setEULAOnceConfirmed(boolean isConfirmed) {
		setBoolean(KEY_EULA_SHOWN, isConfirmed);
	}


	public void setPushRegId(String regId) {
		setString(KEY_PUSH_REG_ID, regId);
	}

	public String getPushRegId() {
		return getString(KEY_PUSH_REG_ID, null);
	}


	private String getPushHost() {
		return getString(PUSH_HOST, null);
	}

	public long getPushSenderId() {
		return getLong(PUSH_SENDER_ID, -1);
	}

	public String getPushBackendRegUrl() {
		return getPushHost() + getString(PUSH_URL_INFO_BACKEND_REG, null);
	}

	public String getPushBackendUnregUrl() {
		return getPushHost() + getString(PUSH_URL_INFO_BACKEND_UNREG, null);
	}

	public String getPushBackendEditUrl() {
		return getPushHost() + getString(PUSH_URL_INFO_BACKEND_EDIT, null);
	}

	public String getHackerNewsHomeUrl() {
		return getString(HACKER_NEWS_HOME_URL, null);
	}

	public String getHackerNewsCommentsUrl() {
		return getString(HACKER_NEWS_COMMENTS_URL, null);
	}

	public void turnOnPush() {
		setBoolean(KEY_PUSH_SETTING, true);
	}

	public void turnOffPush() {
		setBoolean(KEY_PUSH_SETTING, false);
	}


	public boolean isPushTurnedOn() {
		return getBoolean(KEY_PUSH_SETTING, false);
	}


	public int getShownDetailsAdsTimes() {
		return getInt(KEY_SHOWN_DETAILS_ADS_TIMES, 5);
	}

	public boolean isOnlyFullText() {
		return getBoolean(KEY_FULL_TEXT, mContext.getResources().getBoolean(R.bool.default_accept_full_text));
	}

	public String getMsgCount() {
		return getString(KEY_MSG_COUNT, "" + mContext.getResources().getInteger(R.integer.default_msg_count));
	}

	public boolean isOnlySaveLatest() {
		return getBoolean(KEY_SAVE_LATEST_ONLY, mContext.getResources().getBoolean(R.bool.default_only_save_latest));
	}

	public boolean allowEmptyUrl() {
		return getBoolean(KEY_ALLOW_EMPTY_URL, mContext.getResources().getBoolean(R.bool.default_allow_empty_url));
	}

	public void setLastPushedTime(long pushedTime) {
		setLong(KEY_LAST_PUSHED_TIME, pushedTime);
	}

	public long getLastPushedTime() {
		return getLong(KEY_LAST_PUSHED_TIME, -1);
	}

	/**
	 * Set logined account.
	 * @param account  logined account.
	 */
	public void setGoogleAccount(String account) {
		setString(KEY_G_ACCOUNT, account);
	}

	/**
	 *
	 * @return Get logined account.
	 */
	public String getGoogleAccount() {
		return getString(KEY_G_ACCOUNT, null);
	}
}
