package com.hpush.utils;

import android.content.Context;
import android.content.Intent;

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
}