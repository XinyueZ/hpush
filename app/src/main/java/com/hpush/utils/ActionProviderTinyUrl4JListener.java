package com.hpush.utils;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.hpush.data.Message;
import com.tinyurl4j.TinyUrl4JListener;
import com.tinyurl4j.data.Response;

/**
 * Safety wrapper for {@link TinyUrl4JListener}.
 *
 * @author Xinyue Zhao
 */
public final class ActionProviderTinyUrl4JListener implements TinyUrl4JListener {
	private WeakReference<Context> mContextWeakReference;
	private android.support.v7.widget.ShareActionProvider mProvider;
	private int mSubjectResId;
	private int mContentResId;
	private Message msg;

	public ActionProviderTinyUrl4JListener(Context cxt, android.support.v7.widget.ShareActionProvider provider,
			int titleResId, int contentResId, Message msg) {
		mContextWeakReference = new WeakReference<>(cxt);
		mProvider = provider;
		mSubjectResId = titleResId;
		mContentResId = contentResId;
		this.msg = msg;
	}

	@Override
	public void onResponse(Response response) {
		if (mContextWeakReference.get() != null) {
			Context cxt = mContextWeakReference.get();
			String text;
			String subject = cxt.getString(mSubjectResId);
			if (response != null) {
				text = cxt.getString(mContentResId, msg.getTitle(), response.getResult());
			} else {
				text = cxt.getString(mContentResId, msg.getTitle(), Prefs.getInstance(cxt).getHackerNewsHomeUrl());
			}
			mProvider.setShareIntent(Utils.getDefaultShareIntent(mProvider, subject, text));
		}
	}
}
