package com.hpush.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.chopping.utils.Utils;
import com.hpush.R;
import com.hpush.bus.LoadedAllDailiesEvent;
import com.hpush.bus.ShowActionBar;
import com.software.shell.fab.ActionButton;

/**
 * Show all {@link com.hpush.data.Recent}s.
 *
 * @author Xinyue Zhao
 */
public class DailiesActivity extends BasicActivity {
	/**
	 * For action-bar.
	 */
	private Toolbar mToolbar;
	/**
	 * Progress indicator.
	 */
	private View mPbV;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.hpush.bus.ShowActionBar}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.ShowActionBar}.
	 */
	public void onEvent(ShowActionBar e) {
		ActionBar ab = getSupportActionBar();
		if (!e.isShow()) {
			if (ab.isShowing()) {
				ab.hide();
			}
		} else {
			if (!ab.isShowing()) {
				ab.show();
			}
		}
	}

	/**
	 * Handler for {@link LoadedAllDailiesEvent}.
	 *
	 * @param e
	 * 		Event {@link LoadedAllDailiesEvent}.
	 */
	public void onEvent(LoadedAllDailiesEvent e) {
		if (e.getCount() > 0) {
			toggleUI();
		} else {
			Utils.showLongToast(getApplication(), R.string.msg_no_data);
		}
		mPbV.setVisibility(View.GONE);
	}


	//------------------------------------------------

	/**
	 * Show single instance of {@link com.hpush.app.activities.DailiesActivity}
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	public static void showInstance(Context cxt) {
		Intent intent = new Intent(cxt, DailiesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		mPbV = findViewById(R.id.progressBar);

		if (getResources().getBoolean(R.bool.landscape)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}


		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		calcActionBarHeight();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setErrorHandlerAvailable(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			ActivityCompat.finishAfterTransition(this);
			MainActivity.showInstance(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * @return Layout id.
	 */
	protected int getLayoutResId() {
		return R.layout.activity_dailies;
	}

	/**
	 * Show some UIs after loading all data.
	 */
	protected void toggleUI() {
		final ActionButton rmAllV = (ActionButton) findViewById(R.id.remove_all_btn);
		rmAllV.show();
	}
}
