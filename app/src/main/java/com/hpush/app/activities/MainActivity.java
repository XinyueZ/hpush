package com.hpush.app.activities;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
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
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.chopping.bus.CloseDrawerEvent;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hpush.R;
import com.hpush.app.App;
import com.hpush.app.adapters.MainViewPagerAdapter;
import com.hpush.app.fragments.AboutDialogFragment;
import com.hpush.app.fragments.AboutDialogFragment.EulaConfirmationDialog;
import com.hpush.app.fragments.AppListImpFragment;
import com.hpush.app.fragments.GPlusFragment;
import com.hpush.bus.BookmarkAllEvent;
import com.hpush.bus.EULAConfirmedEvent;
import com.hpush.bus.EULARejectEvent;
import com.hpush.bus.RemoveAllEvent;
import com.hpush.bus.RemoveAllEvent.WhichPage;
import com.hpush.bus.SelectMessageEvent;
import com.hpush.bus.UpdateCurrentTotalMessagesEvent;
import com.hpush.db.DB;
import com.hpush.db.DB.Sort;
import com.hpush.gcm.RegistrationIntentService;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.software.shell.fab.ActionButton;

import de.greenrobot.event.EventBus;

/**
 * Main activity of the app.
 *
 * @author Xinyue Zhao
 */
public final class MainActivity extends BasicActivity implements ObservableScrollViewCallbacks {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;
	/**
	 * The pagers
	 */
	private ViewPager mViewPager;
	/**
	 * Adapter for {@link #mViewPager}.
	 */
	private MainViewPagerAdapter mPagerAdapter;
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
	 * Tabs.
	 */
	private PagerSlidingTabStrip mTabs;
	/**
	 * Click to remove all selected items.
	 */
	private ActionButton mRemoveAllBtn;
	/**
	 * Click to bookmark all selected items.
	 */
	private ActionButton mBookmarkAllBtn;
	/**
	 * Open/Close main float buttons.
	 */
	private ActionButton mOpenBtn;
	/**
	 * Search   buttons.
	 */
	private ActionButton mSearchBtn;
	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;
	/**
	 * Count of total messages saved.
	 */
	private TextView mTotalTv;

	/**
	 * Container for toolbar and viewpager.
	 */
	private View mHeaderView;
	/**
	 * Action progress indicator.
	 */
	private ProgressDialog mProgressDialog;
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
		ActivityCompat.finishAfterTransition(this);
	}

	/**
	 * Handler for {@link EULAConfirmedEvent}
	 *
	 * @param e
	 * 		Event {@link  EULAConfirmedEvent}.
	 */
	public void onEvent(EULAConfirmedEvent e) {
		ConnectGoogleActivity.showInstance(this);
	}

	/**
	 * Handler for {@link CloseDrawerEvent}.
	 *
	 * @param e
	 * 		Event {@link CloseDrawerEvent}.
	 */
	public void onEvent(CloseDrawerEvent e) {
		mDrawerLayout.closeDrawers();
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


	/**
	 * Handler for {@link UpdateCurrentTotalMessagesEvent}.
	 *
	 * @param e
	 * 		Event {@link UpdateCurrentTotalMessagesEvent}.
	 */
	public void onEvent(UpdateCurrentTotalMessagesEvent e) {
		this.refreshCurrentTotalMessages();
	}


	//------------------------------------------------

	/**
	 * Show single instance of {@link com.hpush.app.activities.MainActivity}
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	public static void showInstance(Context cxt) {
		Intent intent = new Intent(cxt, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ConnectGoogleActivity.REQ:
			if (resultCode == RESULT_OK) {
				Prefs prefs = Prefs.getInstance(App.Instance);
				if (prefs.isEULAOnceConfirmed() && !TextUtils.isEmpty(prefs.getGoogleAccount())) {
					checkPushSetting();
				}
			} else {
				ActivityCompat.finishAffinity(this);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(LAYOUT);

		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (!TextUtils.isEmpty(Prefs.getInstance(getApplicationContext()).getPushRegId())) {
					dismissProgressDialog();
					SubscribeTopicsActivity.showInstance(MainActivity.this);
				} else {
					dismissProgressDialog();
					Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
							.setAction(R.string.btn_retry, new OnClickListener() {
								@Override
								public void onClick(View v) {
									mProgressDialog = ProgressDialog.show(MainActivity.this, null, getString(
											R.string.msg_push_registering));
									Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
									startService(intent);
								}
							}).show();
				}
			}
		};


		if (getResources().getBoolean(R.bool.landscape)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		getSupportFragmentManager().beginTransaction().replace(R.id.gplus_container, GPlusFragment.newInstance(
				getApplication())).commit();

		mHeaderView = findViewById(R.id.error_content);
		ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		initDrawer();
		mViewPager = (ViewPager) findViewById(R.id.vp);
		mPagerAdapter = new MainViewPagerAdapter(this, getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		calcActionBarHeight();
		mViewPager.setPadding(0, 2 * getActionBarHeight(), 0, 0);
		// Bind the tabs to the ViewPager
		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mTabs.setViewPager(mViewPager);
		mTabs.setIndicatorColorResource(R.color.common_white);
		mTabs.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				closeFloatButtons();
			}

			@Override
			public void onPageSelected(int position) {
				propagateToolbarState(toolbarIsShown());
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		propagateToolbarState(toolbarIsShown());

		mRemoveAllBtn = (ActionButton) findViewById(R.id.remove_all_btn);
		mRemoveAllBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new RemoveAllEvent(
						mViewPager.getCurrentItem() == 0 ? WhichPage.Messages : WhichPage.Bookmarks));
			}
		});
		mBookmarkAllBtn = (ActionButton) findViewById(R.id.bookmark_all_btn);
		mBookmarkAllBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new BookmarkAllEvent());
			}
		});
		mOpenBtn = (ActionButton) findViewById(R.id.float_main_btn);
		mOpenBtn.setOnClickListener(mOpenListener);
		mSearchBtn = (ActionButton) findViewById(R.id.float_search_btn);
		ViewCompat.setTranslationX(mSearchBtn, ViewCompat.getTranslationX(mOpenBtn));
		mSearchBtn.setOnClickListener(mSearchListener);


		mTotalTv = (TextView) findViewById(R.id.total_tv);
		this.refreshCurrentTotalMessages();

		Prefs prefs = Prefs.getInstance(App.Instance);
		if (prefs.isEULAOnceConfirmed() && TextUtils.isEmpty(prefs.getGoogleAccount())) {
			ConnectGoogleActivity.showInstance(this);
		} else if (prefs.isEULAOnceConfirmed() && !TextUtils.isEmpty(prefs.getGoogleAccount())) {
			//Should do something.....
		}
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
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


	/**
	 * Listener while registering push-feature.
	 */
	private BroadcastReceiver mRegistrationBroadcastReceiver;

	@Override
	public void onResume() {
		super.onResume();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		checkPlayService();
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(
				RegistrationIntentService.REGISTRATION_COMPLETE));
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem menuShare = menu.findItem(R.id.action_share_app);
		//Getting the actionprovider associated with the menu item whose id is share.
		android.support.v7.widget.ShareActionProvider provider =
				(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);
		//Setting a share intent.
		String subject = getString(R.string.lbl_share_app_title, getString(R.string.application_name));
		String text = getString(R.string.lbl_share_app_content);
		provider.setShareIntent(Utils.getDefaultShareIntent(provider, subject, text));

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		closeFloatButtons();
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		int id = item.getItemId();
		switch (id) {
		case R.id.action_search:
			onSearchRequested();
			break;
		case R.id.action_about:
			showDialogFragment(AboutDialogFragment.newInstance(this), null);
			break;
		case R.id.action_setting:
			SettingActivity.showInstance(this, null);
			break;
		case R.id.action_facebook:
			Bundle postParams = new Bundle();
			final WebDialog fbDlg = new WebDialog.FeedDialogBuilder(this, getString(R.string.applicationId), postParams)
					.setCaption(String.format(getString(R.string.lbl_share_app_title), getString(
							R.string.lbl_share_item_title))).setName(getString(R.string.lbl_share_item_title))
					.setDescription(getString(R.string.lbl_share_app_content)).setLink(getString(R.string.lbl_app_link))
					.build();
			fbDlg.setOnCompleteListener(new OnCompleteListener() {
				@Override
				public void onComplete(Bundle bundle, FacebookException e) {
					fbDlg.dismiss();
				}
			});
			fbDlg.show();
			break;
		case R.id.action_tweet:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		showAppList();
		makeAds();
	}


	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		showAppList();
		makeAds();
	}

	/**
	 * To ask whether open push-option.
	 */
	private void checkPushSetting() {
		if (TextUtils.isEmpty(Prefs.getInstance(getApplication()).getPushRegId())) {
			new android.support.v7.app.AlertDialog.Builder(this).setTitle(R.string.application_name).setMessage(R.string.lbl_turn_on_push_info)
					.setCancelable(false).setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mProgressDialog = ProgressDialog.show(MainActivity.this, null, getString(
							R.string.msg_push_registering));
					Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
					startService(intent);
				}
			}).setNegativeButton(R.string.lbl_no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).create().show();
		}
	}

	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		getSupportFragmentManager().beginTransaction().replace(R.id.app_list_fl, AppListImpFragment.newInstance(this))
				.commit();
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
					showToolbar();
				}
			};


			mDrawerLayout.setDrawerListener(mDrawerToggle);
			setupDrawerContent((NavigationView) findViewById(R.id.nav_view));
		}
	}

	/**
	 * Set-up of navi-bar left.
	 */
	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				menuItem.setChecked(true);
				mDrawerLayout.closeDrawer(Gravity.LEFT);

				switch (menuItem.getItemId()) {
				case R.id.action_home:
					WebViewActivity.showInstance(MainActivity.this, null, null, null);
					break;
				case R.id.action_recent:
					DailiesActivity.showInstance(MainActivity.this);
					break;
				case R.id.action_more_apps:
					mDrawerLayout.openDrawer(Gravity.RIGHT);
					break;
				case R.id.action_settings:
					SettingActivity.showInstance(MainActivity.this, null);
					break;
				}
				return true;
			}
		});
	}

	/**
	 * Listener for opening all float buttons.
	 */
	private OnClickListener mSearchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onSearchRequested();
		}
	};

	/**
	 * Listener for opening all float buttons.
	 */
	private OnClickListener mOpenListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mViewPager.getCurrentItem() == 0) {
				mBookmarkAllBtn.show();
			}
			mRemoveAllBtn.show();
			mSearchBtn.show();
			mOpenBtn.setOnClickListener(mCloseListener);
		}
	};

	/**
	 * Listener for closing all float buttons.
	 */
	private OnClickListener mCloseListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mBookmarkAllBtn.hide();
			mRemoveAllBtn.hide();
			mSearchBtn.hide();
			mOpenBtn.setOnClickListener(mOpenListener);
		}
	};

	/**
	 * Make an Admob.
	 */
	private void makeAds() {
		int curTime = App.Instance.getAdsShownTimes();
		int adsTimes = Prefs.getInstance(App.Instance).getShownDetailsAdsTimes();
		if (curTime % adsTimes == 0) {
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
		curTime++;
		App.Instance.setAdsShownTimes(curTime);
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
		if (isFound == ConnectionResult.SUCCESS) {//Ignore update.
			//The "End User License Agreement" must be confirmed before you use this application.
			if (!Prefs.getInstance(getApplication()).isEULAOnceConfirmed()) {
				showDialogFragment(new EulaConfirmationDialog(), null);
			}
		} else {
			new android.support.v7.app.AlertDialog.Builder(this).setTitle(R.string.application_name).setMessage(R.string.lbl_play_service).setCancelable(
					false).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(getString(R.string.play_service_url)));
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e0) {
						intent.setData(Uri.parse(getString(R.string.play_service_web)));
						try {
							startActivity(intent);
						} catch (Exception e1) {
							//Ignore now.
						}
					} finally {
						finish();
					}
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


	private void refreshCurrentTotalMessages() {
		AsyncTask<Void, int[], int[]> task = new AsyncTask<Void, int[], int[]>() {
			@Override
			protected int[] doInBackground(Void... params) {
				DB db = DB.getInstance(getApplication());
				return new int[] { db.getBookmarks(Sort.DESC).size(), db.getMessages(Sort.DESC).size() };
			}

			@Override
			protected void onPostExecute(int[] ints) {
				super.onPostExecute(ints);
				int sum = ints[0] + ints[1];
				mTotalTv.setText(sum + "");
			}
		};
		AsyncTaskCompat.executeParallel(task);
	}


	/**
	 * Close progress indicator.
	 */
	private void dismissProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(Gravity.LEFT) || mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
			mDrawerLayout.closeDrawers();
		} else {
			super.onBackPressed();
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	//UI effect:
	//https://github.com/ksoichiro/Android-ObservableScrollView/blob/master/observablescrollview-samples/src/main/java/com/github/ksoichiro/android/observablescrollview/samples/ViewPagerTabActivity.java
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	private int mBaseTranslationY;

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
		int toolbarHeight = mToolbar.getHeight();
		float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
		if (firstScroll) {
			if (-toolbarHeight < currentHeaderTranslationY) {
				mBaseTranslationY = scrollY;
			}
		}
		int headerTranslationY = Math.min(0, Math.max(-toolbarHeight, -(scrollY - mBaseTranslationY)));
		ViewPropertyAnimator.animate(mHeaderView).cancel();
		ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
	}

	@Override
	public void onDownMotionEvent() {

	}


	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		mBaseTranslationY = 0;

		Fragment fragment = mPagerAdapter.getItemAt(mViewPager.getCurrentItem());
		if (fragment == null) {
			return;
		}
		View view = fragment.getView();
		if (view == null) {
			return;
		}

		// ObservableXxxViews have same API
		// but currently they don't have any common interfaces.
		adjustToolbar(scrollState, view);
	}

	private void adjustToolbar(ScrollState scrollState, View view) {
		int toolbarHeight = mToolbar.getHeight();
		final Scrollable scrollView = (Scrollable) view.findViewById(R.id.msg_rv);
		if (scrollView == null) {
			return;
		}
		if (scrollState == ScrollState.UP) {
			if (toolbarHeight < scrollView.getCurrentScrollY()) {
				hideToolbar();
			} else if (scrollView.getCurrentScrollY() < toolbarHeight) {
				showToolbar();
			}
		} else if (scrollState == ScrollState.DOWN) {
			if (toolbarHeight < scrollView.getCurrentScrollY()) {
				showToolbar();
			}
		}
	}

	private void hideToolbar() {
		float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
		int toolbarHeight = mToolbar.getHeight();
		if (headerTranslationY != -toolbarHeight) {
			ViewPropertyAnimator.animate(mHeaderView).cancel();
			ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
		}
		propagateToolbarState(false);
	}

	private void showToolbar() {
		float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
		if (headerTranslationY != 0) {
			ViewPropertyAnimator.animate(mHeaderView).cancel();
			ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
		}
		propagateToolbarState(true);
	}

	private void propagateToolbarState(boolean isShown) {
		int toolbarHeight = mToolbar.getHeight();

		// Set scrollY for the fragments that are not created yet
		mPagerAdapter.setScrollY(isShown ? 0 : toolbarHeight);

		// Set scrollY for the active fragments
		for (int i = 0; i < mPagerAdapter.getCount(); i++) {
			// Skip current item
			if (i == mViewPager.getCurrentItem()) {
				continue;
			}

			// Skip destroyed or not created item
			Fragment f = mPagerAdapter.getItemAt(i);
			if (f == null) {
				continue;
			}

			View view = f.getView();
			if (view == null) {
				continue;
			}
			propagateToolbarState(isShown, view, toolbarHeight);
		}
	}


	private void propagateToolbarState(boolean isShown, View view, int toolbarHeight) {
		Scrollable scrollView = (Scrollable) view.findViewById(R.id.msg_rv);
		if (scrollView == null) {
			return;
		}
		if (isShown) {
			// Scroll up
			if (0 < scrollView.getCurrentScrollY()) {
				scrollView.scrollVerticallyTo(0);
			}
		} else {
			// Scroll down (to hide padding)
			if (scrollView.getCurrentScrollY() < toolbarHeight * 2) {
				scrollView.scrollVerticallyTo(toolbarHeight * 2);
			}
		}
	}

	private boolean toolbarIsShown() {
		return ViewHelper.getTranslationY(mHeaderView) == 0;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
}
