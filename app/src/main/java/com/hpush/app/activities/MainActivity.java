package com.hpush.app.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hpush.R;
import com.hpush.app.adapters.MainViewPagerAdapter;
import com.hpush.app.fragments.AboutDialogFragment;
import com.hpush.app.fragments.AboutDialogFragment.EulaConfirmationDialog;
import com.hpush.app.fragments.AppListImpFragment;
import com.hpush.bus.BookmarkAllEvent;
import com.hpush.bus.ClickMessageCommentsEvent;
import com.hpush.bus.ClickMessageLinkEvent;
import com.hpush.bus.EULAConfirmedEvent;
import com.hpush.bus.EULARejectEvent;
import com.hpush.bus.RemoveAllEvent;
import com.hpush.bus.SelectMessageEvent;
import com.hpush.gcm.RegGCMTask;
import com.hpush.utils.Prefs;
import com.hpush.views.OnViewAnimatedClickedListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import de.greenrobot.event.EventBus;

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
	 * Flag that this {@link com.hpush.app.activities.MainActivity} is opened by clicking on notification-center.
	 */
	public static final String EXTRAS_OPEN_FROM_NOTIFICATION = "com.hpush.app.activities.EXTRAS.open_from_notification";
	/**
	 * The pagers
	 */
	private ViewPager mViewPager;
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
	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link  EULARejectEvent}.
	 *
	 * @param e
	 * 		Event {@link  EULARejectEvent}.
	 */
	public void onEvent(EULARejectEvent e) {
		finish();
	}

	/**
	 * Handler for {@link EULAConfirmedEvent}
	 *
	 * @param e
	 * 		Event {@link  EULAConfirmedEvent}.
	 */
	public void onEvent(EULAConfirmedEvent e) {

		new AlertDialog.Builder(this).setTitle(R.string.application_name).setMessage(R.string.lbl_turn_on_push_info)
				.setCancelable(false).setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AsyncTaskCompat.executeParallel(new RegGCMTask(getApplication()));
				makeAds();
		}
		}).setNeutralButton(R.string.lbl_no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				makeAds();
			}
		}).create().show();
	}

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


	/**
	 * Handler for {@link SelectMessageEvent}.
	 *
	 * @param e
	 * 		Event {@link SelectMessageEvent}.
	 */
	public void onEvent(SelectMessageEvent e) {
		openFloatButtons();
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
		mViewPager = (ViewPager) findViewById(R.id.vp);
		mViewPager.setAdapter(new MainViewPagerAdapter(this, getSupportFragmentManager()));
		// Bind the tabs to the ViewPager
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setViewPager(mViewPager);
		tabs.setIndicatorColorResource(R.color.common_white);
		tabs.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				closeFloatButtons();
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
				EventBus.getDefault().post(new RemoveAllEvent());
			}
		});
		mBookmarkAllBtn = (ImageButton) findViewById(R.id.bookmark_all_btn);
		mBookmarkAllBtn.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new BookmarkAllEvent());
			}
		});
		mOpenBtn = (ImageButton) findViewById(R.id.float_main_btn);
		mOpenBtn.setOnClickListener(mOpenListener);
		handleIntent();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent();
	}

	/**
	 * Handle the {@link android.app.Activity}'s {@link android.content.Intent}.
	 */
	private void handleIntent() {
		Intent intent = getIntent();
		if (intent.getBooleanExtra(EXTRAS_OPEN_FROM_NOTIFICATION, false)) {
			NotificationManager nc = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
			nc.cancelAll();
		}
	}

	/**
	 * Dismiss all float-buttons.
	 */
	private void closeFloatButtons() {
		int vi = mRemoveAllBtn.getVisibility();
		if (vi == View.VISIBLE) {
			mOpenBtn.performClick();
		}
	}


	/**
	 * Open all float-buttons.
	 */
	private void openFloatButtons() {
		int vi = mRemoveAllBtn.getVisibility();
		if (vi != View.VISIBLE) {
			mOpenBtn.performClick();
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		checkPlayService();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		closeFloatButtons();
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		int id = item.getItemId();
		switch (id) {
		case R.id.action_about:
			showDialogFragment(AboutDialogFragment.newInstance(this), null);
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
			findViewById(R.id.open_setting_ll).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SettingActivity.showInstance(MainActivity.this);
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				}
			});
		}
	}

	/**
	 * Listener for opening all float buttons.
	 */
	private OnClickListener mOpenListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mViewPager.getCurrentItem() == 0) {
				mBookmarkAllBtn.setVisibility(View.VISIBLE);
			}
			AnimatorSet animatorSet = new AnimatorSet();
			ObjectAnimator iiBtnAnim = ObjectAnimator.ofFloat(mBookmarkAllBtn, "translationY", 150f, 0).setDuration(
					100);
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
			ObjectAnimator iiBtnAnim = ObjectAnimator.ofFloat(mBookmarkAllBtn, "translationY", 0, 150f).setDuration(
					100);
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

	/**
	 * Make an Admob.
	 */
	private void makeAds() {
		// Create an ad.
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId(getString(R.string.ad_inters_unit_id));
		// Create ad request.
		AdRequest adRequest = new AdRequest.Builder().build();
		// Begin loading your interstitial.
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				displayInterstitial();
			}
		});
		mInterstitialAd.loadAd(adRequest);
	}


	/**
	 * Invoke displayInterstitial() when you are ready to display an interstitial.
	 */
	public void displayInterstitial() {
		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
		}
	}

	/**
	 * To confirm whether the validation of the Play-service of Google Inc.
	 */
	private void checkPlayService() {
		final int isFound = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (isFound == ConnectionResult.SUCCESS ||
				isFound == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {//Ignore update.
			//The "End User License Agreement" must be confirmed before you use this application.
			if (!Prefs.getInstance(getApplication()).isEULAOnceConfirmed()) {
				showDialogFragment(new EulaConfirmationDialog(), null);
			}
		} else {
			new AlertDialog.Builder(this).setTitle(R.string.application_name).setMessage(R.string.lbl_play_service)
					.setCancelable(false).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(getString(R.string.play_service_url)));
					startActivity(intent);
					finish();
				}
			}).create().show();
		}
	}


	/**
	 * Show  {@link android.support.v4.app.DialogFragment}.
	 *
	 * @param _dlgFrg
	 * 		An instance of {@link android.support.v4.app.DialogFragment}.
	 * @param _tagName
	 * 		Tag name for dialog, default is "dlg". To grantee that only one instance of {@link
	 * 		android.support.v4.app.DialogFragment} can been seen.
	 */
	protected void showDialogFragment(DialogFragment _dlgFrg, String _tagName) {
		try {
			if (_dlgFrg != null) {
				DialogFragment dialogFragment = _dlgFrg;
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				// Ensure that there's only one dialog to the user.
				Fragment prev = getSupportFragmentManager().findFragmentByTag("dlg");
				if (prev != null) {
					ft.remove(prev);
				}
				try {
					if (TextUtils.isEmpty(_tagName)) {
						dialogFragment.show(ft, "dlg");
					} else {
						dialogFragment.show(ft, _tagName);
					}
				} catch (Exception _e) {
				}
			}
		} catch (Exception _e) {
		}
	}
}
