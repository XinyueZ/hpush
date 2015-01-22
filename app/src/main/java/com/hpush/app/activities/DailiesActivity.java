package com.hpush.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.chopping.utils.DeviceUtils;
import com.hpush.R;
import com.hpush.bus.DeleteAllDailiesEvent;
import com.hpush.bus.LoadedAllDailiesEvent;
import com.hpush.bus.ShowActionBar;
import com.hpush.views.OnViewAnimatedClickedListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import de.greenrobot.event.EventBus;

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
	 * @return Layout id.
	 */
	protected int getLayoutResId() {
		return R.layout.activity_dailies;
	}

	/**
	 * Show some UIs after loading all data.
	 */
	protected void toggleUI() {
		final View rmAllV = findViewById(R.id.remove_all_btn);

		int screenWidth = DeviceUtils.getScreenSize(getApplication()).Width;
		ViewHelper.setTranslationX(rmAllV, -screenWidth);
		ViewHelper.setRotation(rmAllV, 360f * 4);
		ViewPropertyAnimator animator = ViewPropertyAnimator.animate(rmAllV);
		animator.x(screenWidth - getResources().getDimensionPixelSize(R.dimen.float_button_rmv_all_dailies)).rotation(0).setDuration(MainActivity.ANIM_SPEED * 2).setListener(
				new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						super.onAnimationStart(animation);
						rmAllV.setVisibility(View.VISIBLE);
					}
				}).start();

		rmAllV.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new DeleteAllDailiesEvent());
			}
		});
	}
}
