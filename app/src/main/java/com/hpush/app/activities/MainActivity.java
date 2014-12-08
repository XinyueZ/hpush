package com.hpush.app.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.astuetz.PagerSlidingTabStrip;
import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.chopping.bus.CloseDrawerEvent;
import com.crashlytics.android.Crashlytics;
import com.hpush.R;
import com.hpush.app.adapters.MainViewPagerAdapter;
import com.hpush.app.fragments.AppListImpFragment;
import com.hpush.bus.ClickMessageCommentsEvent;
import com.hpush.bus.ClickMessageLinkEvent;
import com.hpush.utils.Prefs;
import com.hpush.views.OnViewAnimatedClickedListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Main activity of the app.
 *
 * @author Xinyue Zhao
 */
public final class MainActivity extends BaseActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;

	/**
	 * Navigation drawer.
	 */
	private DrawerLayout mDrawerLayout;

	/**
	 * Use navigation-drawer for this fork.
	 */
	private ActionBarDrawerToggle mDrawerToggle;
	/**
	 * The new actionbar.
	 */
	private Toolbar mToolbar;
	/**
	 * Click to remove all selected items.
	 */
	private ImageButton mRemoveAllBtn;
	/**
	 * Click to bookmark all selected items.
	 */
	private ImageButton mBookmarkAllBtn;
	/**
	 * Open/Close main float buttons.
	 */
	private ImageButton mOpenBtn;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------


	/**
	 * Handler for {@link }.
	 *
	 * @param e
	 * 		Event {@link}.
	 */
	public void onEvent(CloseDrawerEvent e) {
		mDrawerLayout.closeDrawers();
	}

	/**
	 * Handler for {@link ClickMessageLinkEvent}.
	 *
	 * @param e
	 * 		Event {@link ClickMessageLinkEvent}.
	 */
	public void onEvent(ClickMessageLinkEvent e) {
		WebViewActivity.showInstance(this, e.getMessage().getUrl(), e.getSenderV());
	}

	/**
	 * Handler for {@link ClickMessageCommentsEvent}.
	 *
	 * @param e
	 * 		Event {@link ClickMessageCommentsEvent}.
	 */
	public void onEvent(ClickMessageCommentsEvent e) {
		long cId = e.getMessage().getId();
		String url = Prefs.getInstance(getApplication()).getHackerNewsCommentsUrl();
		String target = url + cId;
		WebViewActivity.showInstance(this, target, e.getSenderV());
	}

	//------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(LAYOUT);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		initDrawer();
		final ViewPager viewPager = (ViewPager) findViewById(R.id.vp);
		viewPager.setAdapter(new MainViewPagerAdapter(this, getSupportFragmentManager()));
		// Bind the tabs to the ViewPager
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setViewPager(viewPager);
		tabs.setIndicatorColorResource(R.color.common_white);
		tabs.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		 		int vi = mRemoveAllBtn.getVisibility();
				if(vi == View.VISIBLE) {
					mOpenBtn.performClick();
				}
			}

			@Override
			public void onPageSelected(int position) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mRemoveAllBtn = (ImageButton) findViewById(R.id.remove_all_btn);
		mRemoveAllBtn.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {

			}
		});
		mBookmarkAllBtn = (ImageButton) findViewById(R.id.bookmark_all_btn);
		mBookmarkAllBtn.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {

			}
		});
		mOpenBtn = (ImageButton) findViewById(R.id.float_main_btn);
		mOpenBtn.setOnClickListener(mOpenListener);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
//		checkPlayService();
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		int id = item.getItemId();
		switch (id) {
		case R.id.action_about:
//			showDialogFragment(AboutDialogFragment.newInstance(this), null);
			break;
		case R.id.action_setting:
			SettingActivity.showInstance(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		showAppList();
	}

	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		showAppList();
	}

	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		getSupportFragmentManager().beginTransaction().replace(R.id.app_list_fl, AppListImpFragment.newInstance(this))
				.commit();
	}


	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getApplication());
	}

	/**
	 * Initialize the navigation drawer.
	 */
	private void initDrawer() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.application_name,
					R.string.app_name) {
				@Override
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					//					animShowMainUI();
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);
		}
	}

	/**
	 * Listener for opening all float buttons.
	 */
	private OnClickListener mOpenListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mBookmarkAllBtn.setVisibility(View.VISIBLE);
			AnimatorSet animatorSet = new AnimatorSet();
			ObjectAnimator iiBtnAnim = ObjectAnimator.ofFloat(mBookmarkAllBtn, "translationY", 150f, 0).setDuration(100);
			iiBtnAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mRemoveAllBtn.setVisibility(View.VISIBLE);
				}
			});
			ObjectAnimator iBtnAnim = ObjectAnimator.ofFloat(mRemoveAllBtn, "translationY", 200f, 0).setDuration(200);
			iBtnAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mOpenBtn.setOnClickListener(mCloseListener);
				}
			});
			animatorSet.playSequentially(iiBtnAnim, iBtnAnim);
			animatorSet.start();
		}
	};

	/**
	 * Listener for closing all float buttons.
	 */
	private OnClickListener mCloseListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			AnimatorSet animatorSet = new AnimatorSet();
			ObjectAnimator iiBtnAnim = ObjectAnimator.ofFloat(mBookmarkAllBtn, "translationY", 0, 150f).setDuration(100);
			iiBtnAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mBookmarkAllBtn.setVisibility(View.GONE);
				}
			});
			ObjectAnimator iBtnAnim = ObjectAnimator.ofFloat(mRemoveAllBtn, "translationY", 0, 200f).setDuration(200);
			iBtnAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mRemoveAllBtn.setVisibility(View.GONE);
					mOpenBtn.setOnClickListener(mOpenListener);

				}
			});
			animatorSet.playSequentially(iiBtnAnim, iBtnAnim);
			animatorSet.start();
		}
	};

}
