package com.hpush.utils;

import android.content.Context;

import com.chopping.application.BasicPrefs;

/**
 * Application's preferences.
 *
 * @author Xinyue Zhao
 */
public final class Prefs extends BasicPrefs {
	public static final String NA = "N/A";
	public static final String API_LIMIT = "API limit exceeded!";
	/**
	 * Impl singleton pattern.
	 */
	private static Prefs sInstance;


	public static final String KEY_PUSH_REG_ID = "key.push.regid";
	public static final String KEY_PUSH_SETTING = "key.push.setting";
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

	public void turnOnPush() {
		setBoolean(KEY_PUSH_SETTING, true);
	}


	public int getShownDetailsAdsTimes() {
		return getInt(KEY_SHOWN_DETAILS_ADS_TIMES, 5);
	}
}
