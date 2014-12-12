package com.hpush.utils;

import android.content.Context;
import android.content.Intent;

import com.hpush.db.MessagesTbl;

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

	/**
	 * Get column name to sort data.
	 * <p/>
	 * <code> <p/>
	 * <item>Scores</item> <p/>
	 * <item>Arrival</item> <p/>
	 * <item>Creation</item> <p/>
	 * <item>Comments</item> <p/>
	 * </code>
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 *
	 * @return Name of column.
	 */
	public static String getSortBy(Context cxt) {
		String sortTypeValue = Prefs.getInstance(cxt.getApplicationContext()).getSortTypeValue();
		switch (sortTypeValue) {
		case "0":
			return MessagesTbl.SCORE;
		case "1":
			return MessagesTbl.PUSHED_TIME;
		case "2":
			return MessagesTbl.TIME;
		case "3":
			return MessagesTbl.COMMENTS_COUNT;
		default:
			return MessagesTbl.TIME;
		}
	}
}
