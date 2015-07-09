package com.hpush.gcm;


import com.hpush.app.App;
import com.hpush.utils.Prefs;

public final class Topics {
	public static final String GET_SUMMARY  = "summary";
	public static final String GET_TOP_STORIES  = "topstories";
	public static final String GET_NEW_STORIES = "newstories";
	public static final String GET_ASK_STORIES  = "askstories";
	public static final String GET_SHOW_STORIES = "showstories";
	public static final String GET_JOB_STORIES  = "jobstories";

	/**
	 * Remove all topics that have been subscribed.
	 */
	public  static void clear() {
		Prefs prefs = Prefs.getInstance(App.Instance);
		prefs.setPush(Prefs.KEY_PUSH_SUMMARY, false);
		prefs.setPush(Prefs.KEY_PUSH_TOPSTORIES, false);
		prefs.setPush(Prefs.KEY_PUSH_NEWSTORIES, false);
		prefs.setPush(Prefs.KEY_PUSH_ASKSTORIES, false);
		prefs.setPush(Prefs.KEY_PUSH_SHOWSTORIES, false);
		prefs.setPush(Prefs.KEY_PUSH_JOBSTORIES, false);
	}
}
