package com.hpush.app.activities;

import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;

import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.facebook.FacebookException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.hpush.R;
import com.hpush.bus.ClickMessageCommentsEvent;
import com.hpush.bus.ClickMessageLinkEvent;
import com.hpush.bus.ShareMessageEvent;
import com.hpush.data.MessageListItem;
import com.hpush.utils.Prefs;

/**
 * Abstract {@link android.app.Activity} of the app.
 *
 * @author Xinyue Zhao
 */
public abstract class BasicActivity extends BaseActivity {

	/**
	 * Height of action-bar general.
	 */
	private int mActionBarHeight;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.hpush.bus.ClickMessageCommentsEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.ClickMessageCommentsEvent}.
	 */
	public void onEvent(ClickMessageCommentsEvent e) {
		long cId = e.getMessage().getId();
		String url = Prefs.getInstance(getApplication()).getHackerNewsCommentsUrl();
		String target = url + cId;
		WebViewActivity.showInstance(this, target, e.getSenderV(), e.getMessage());
	}

	/**
	 * Handler for {@link com.hpush.bus.ShareMessageEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.ShareMessageEvent}.
	 */
	public void onEvent(ShareMessageEvent e) {
		MessageListItem msg = e.getMessage();
		switch (e.getType()) {
		case Facebook:
			Bundle postParams = new Bundle();
			final WebDialog fbDlg = new WebDialog.FeedDialogBuilder(this, getString(R.string.applicationId), postParams)
					.setName(msg.getTitle()).setDescription(msg.getText()).setLink(msg.getUrl()).build();
			fbDlg.setOnCompleteListener(new OnCompleteListener() {
				@Override
				public void onComplete(Bundle bundle, FacebookException e) {
					fbDlg.dismiss();
				}
			});
			fbDlg.show();
			break;
		case Tweet:
			break;
		}
	}

	/**
	 * Handler for {@link com.hpush.bus.ClickMessageLinkEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.ClickMessageLinkEvent}.
	 */
	public void onEvent(ClickMessageLinkEvent e) {
		WebViewActivity.showInstance(this, e.getMessage().getUrl(), e.getSenderV(), e.getMessage());
	}

	//------------------------------------------------

	/**
	 * Calculate height of actionbar.
	 */
	protected void calcActionBarHeight() {
		int[] abSzAttr;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			abSzAttr = new int[] { android.R.attr.actionBarSize };
		} else {
			abSzAttr = new int[] { R.attr.actionBarSize };
		}
		TypedArray a = obtainStyledAttributes(abSzAttr);
		mActionBarHeight = a.getDimensionPixelSize(0, -1);
	}


	/**
	 *
	 * @return  Height of action-bar general.
	 */
	protected int getActionBarHeight() {
		return mActionBarHeight;
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getApplication());
	}

}
