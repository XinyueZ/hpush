package com.hpush.utils;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.formatDateTime;

/**
 * Util-methods.
 *
 * @author Xinyue Zhao
 */
public final class Utils {
	/**
	 * There is different between android pre 3.0 and 3.x, 4.x on this wording.
	 */
	public static final String ALPHA =
			(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) ? "alpha" : "Alpha";
	/**
	 * Convert a timestamps to a readable date in string.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param timestamps
	 * 		A long value for a timestamps.
	 *
	 * @return A date string format.
	 */
	public static String convertTimestamps2DateString(Context cxt, long timestamps) {
		return formatDateTime(cxt, timestamps, FORMAT_SHOW_YEAR | FORMAT_SHOW_DATE |
				FORMAT_SHOW_TIME | FORMAT_ABBREV_MONTH);
	}

	/**
	 * Standard sharing app for sharing on actionbar.
	 */
	public static Intent getDefaultShareIntent(android.support.v7.widget.ShareActionProvider provider, String subject,
			String body) {
		if (provider != null) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			i.putExtra(android.content.Intent.EXTRA_TEXT, body);
			provider.setShareIntent(i);
			return i;
		}
		return null;
	}

	/**
	 * Do some correct on http-header.
	 * @param headers The available http-header.
	 */
	public static void makeHttpHeaders(Map<String, String> headers) {

		if (headers.get("Accept-Encoding") == null) {
			headers.put("Accept-Encoding", "gzip");
		}
		if (headers.get("Content-Type") == null) {
			headers.put("Content-Type", "application/x-www-form-urlencoded");
		}
		if (headers.get("Content-Length") == null) {
			headers.put("Content-Length", "0");
		}
	}

}
