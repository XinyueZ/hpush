package com.hpush.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hpush.R;
import com.hpush.bus.ShowActionBar;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Show all {@link com.hpush.data.Recent}s.
 *
 * @author Xinyue Zhao
 */
public final class DailiesActivity extends BasicActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_dailies;
	/**
	 * For action-bar.
	 */
	private Toolbar mToolbar;

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
		if (!e.isShow()) {
//			animHideMainUI();
			getSupportActionBar().hide();
		} else {
//			animShowMainUI();
			getSupportActionBar().show();
		}
	}

	//------------------------------------------------

	/**
	 * Show single instance of {@link com.hpush.app.activities.DailiesActivity}
	 *
	 * @param cxt {@link android.content.Context}.
	 */
	public static void showInstance(Context cxt) {
		Intent intent = new Intent(cxt, DailiesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);

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
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				finishAfterTransition();
			} else {
				finish();
			}
			MainActivity.showInstance(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * Dismiss actionbar, and add-new-btn.
	 */
	private void animHideMainUI() {
		animToolActionBar(-getActionBarHeight() * 4);
	}

	/**
	 * Show actionbar, and add-new-btn.
	 */
	private void animShowMainUI() {
		animToolActionBar(0);
	}

	/**
	 * Animation and moving actionbar(toolbar).
	 *
	 * @param value
	 * 		The property value of animation.
	 */
	private void animToolActionBar(final float value) {
		ViewPropertyAnimator animator = ViewPropertyAnimator.animate(mToolbar);
		animator.translationY(value).setDuration(400).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if (value == 0) {
					getSupportActionBar().show();
				} else {
					getSupportActionBar().hide();
				}
			}
		});
	}

}
