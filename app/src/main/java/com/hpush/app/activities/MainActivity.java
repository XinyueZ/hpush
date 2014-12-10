package com.hpush.app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ImageButton;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.chopping.bus.CloseDrawerEvent;
import com.crashlytics.android.Crashlytics;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
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
import com.hpush.bus.LoginedGPlusEvent;
import com.hpush.bus.LogoutGPlusEvent;
import com.hpush.bus.RemoveAllEvent;
import com.hpush.bus.RemoveAllEvent.WhichPage;
import com.hpush.bus.SelectMessageEvent;
import com.hpush.bus.UpdateCurrentTotalMessagesEvent;
import com.hpush.db.DB;
import com.hpush.db.DB.Sort;
import com.hpush.gcm.RegGCMTask;
import com.hpush.gcm.UnregGCMTask;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;
import com.hpush.views.OnViewAnimatedClickedListener;
import com.hpush.views.OnViewAnimatedClickedListener3;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import de.greenrobot.event.EventBus;

/**
 * Main activity of the app.
 *
 * @author Xinyue Zhao
 */
public final class MainActivity extends BaseActivity implements ConnectionCallbacks, OnConnectionFailedListener,
		ObservableScrollViewCallbacks {
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
	/**
	 * Count of total messages saved.
	 */
	private TextView mTotalTv;

	/**
	 * Container for toolbar and viewpager.
	 */
	private View mHeaderView;

	private SnackBar mSnackBar;
	private SignInButton mGPlusBtn;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	private ProgressDialog mProgressDialog;
	private static int REQUEST_CODE_RESOLVE_ERR = 0x98;
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
		WebViewActivity.showInstance(this, e.getMessage().getUrl(), e.getSenderV(), e.getMessage());
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
		WebViewActivity.showInstance(this, target, e.getSenderV(), e.getMessage());
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
	 * Handler for {@link com.hpush.bus.LogoutGPlusEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.LogoutGPlusEvent}.
	 */
	public void onEvent(LogoutGPlusEvent e) {
		logoutGPlus();
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(LAYOUT);
		mHeaderView = findViewById(R.id.error_content);
		ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		initDrawer();
		mViewPager = (ViewPager) findViewById(R.id.vp);
		mPagerAdapter = new MainViewPagerAdapter(this, getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setPadding(0, 2 * calcActionBarHeight(), 0, 0);
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

		mRemoveAllBtn = (ImageButton) findViewById(R.id.remove_all_btn);
		mRemoveAllBtn.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new RemoveAllEvent(mViewPager.getCurrentItem() == 0? WhichPage.Messages : WhichPage.Bookmarks));
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

		mSnackBar = new SnackBar(this);
		mPlusClient = new PlusClient.Builder(this, this, this).build();
		mGPlusBtn = (SignInButton) findViewById(R.id.sign_in_btn);
		mGPlusBtn.setSize(SignInButton.SIZE_WIDE);
		mGPlusBtn.setOnClickListener(new OnViewAnimatedClickedListener3() {
			@Override
			public void onClick() {
				loginGPlus();
			}
		});
		if (!TextUtils.isEmpty(Prefs.getInstance(getApplication()).getGoogleAccount())) {
			mPlusClient.connect();
		}
		mTotalTv = (TextView) findViewById(R.id.total_tv);
		this.refreshCurrentTotalMessages();
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


	@Override
	public void onResume() {
		super.onResume();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		checkPlayService();
		handleGPlusLinkedUI();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		MenuItem menuShare = menu.findItem(R.id.action_share_app);
		//Getting the actionprovider associated with the menu item whose id is share.
		android.support.v7.widget.ShareActionProvider provider =
				(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);
		//Setting a share intent.
		String subject = getString(R.string.lbl_share_app_title, getString(R.string.application_name));
		String text = getString(R.string.lbl_share_app_content);
		provider.setShareIntent(Utils.getDefaultShareIntent(provider, subject, text));

		menu.findItem(R.id.action_setting).setVisible(mPlusClient != null && mPlusClient.isConnected());
		if (!TextUtils.isEmpty(Prefs.getInstance(getApplication()).getGoogleAccount())) {
			menu.findItem(R.id.action_setting).setVisible(true);
		}
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
			SettingActivity.showInstance(this, null);
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
	 * Show main float button.
	 */
	private void showOpenFloatButton() {
		mOpenBtn.setVisibility(View.VISIBLE);
	}

	/**
	 * Dismiss main float button.
	 */
	private void hideOpenFloatButton() {
		mOpenBtn.setVisibility(View.INVISIBLE);
	}

	/**
	 * Show pagers.
	 */
	private void showViewPager() {
		mViewPager.setVisibility(View.VISIBLE);
	}

	/**
	 * Dismiss pagers.
	 */
	private void hideViewPager() {
		mViewPager.setVisibility(View.INVISIBLE);
	}

	/**
	 * Show tabs.
	 */
	private void showTabs() {
		mTabs.setVisibility(View.VISIBLE);
	}

	/**
	 * Dismiss tabs.
	 */
	private void hideTabs() {
		mTabs.setVisibility(View.INVISIBLE);
	}

	/**
	 * Show button for gplus.
	 */
	private void showGPlusButton() {
		mGPlusBtn.setVisibility(View.VISIBLE);
	}

	/**
	 * Dismiss button for gplus.
	 */
	private void hideGPlusButton() {
		mGPlusBtn.setVisibility(View.GONE);
	}

	private void handleGPlusLinkedUI() {
		if (mPlusClient != null && mPlusClient.isConnected()) {
			showTabs();
			showViewPager();
			showOpenFloatButton();
			hideGPlusButton();
			findViewById(R.id.open_hack_news_home_ll).setVisibility(View.VISIBLE);
			findViewById(R.id.open_setting_ll).setVisibility(View.VISIBLE);
		} else {
			if (!TextUtils.isEmpty(Prefs.getInstance(getApplication()).getGoogleAccount())) {
				showTabs();
				showViewPager();
				showOpenFloatButton();
				hideGPlusButton();
				findViewById(R.id.open_hack_news_home_ll).setVisibility(View.VISIBLE);
				findViewById(R.id.open_setting_ll).setVisibility(View.VISIBLE);
			} else {
				hideTabs();
				hideViewPager();
				hideOpenFloatButton();
				showGPlusButton();
				findViewById(R.id.open_hack_news_home_ll).setVisibility(View.GONE);
				findViewById(R.id.open_setting_ll).setVisibility(View.GONE);
			}
		}
		supportInvalidateOptionsMenu();
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
			findViewById(R.id.open_hack_news_home_ll).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.showInstance(MainActivity.this, null, v, null);
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				}
			});
			findViewById(R.id.open_setting_ll).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SettingActivity.showInstance(MainActivity.this, v);
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

	@Override
	public void onConnected(Bundle bundle) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mGPlusBtn.setVisibility(View.GONE);
		}
		if (TextUtils.isEmpty(Prefs.getInstance(getApplication()).getPushRegId())) {
			new AlertDialog.Builder(this).setTitle(R.string.application_name).setMessage(R.string.lbl_turn_on_push_info)
					.setCancelable(false).setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AsyncTaskCompat.executeParallel(new RegGCMTask(getApplication()));
					mSnackBar.show(getString(R.string.msg_wait_new_messages));
					makeAds();
				}
			}).setNeutralButton(R.string.lbl_no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					makeAds();
				}
			}).create().show();
		}
		EventBus.getDefault().postSticky(new LoginedGPlusEvent(mPlusClient));
		Prefs.getInstance(getApplication()).setGoogleAccount(mPlusClient.getAccountName());
		handleGPlusLinkedUI();
	}

	@Override
	public void onDisconnected() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mGPlusBtn.setVisibility(View.VISIBLE);

		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
			} catch (SendIntentException e) {
				mPlusClient.connect();
			}
		} else {
			mSnackBar.show(getString(R.string.lbl_login_fail));
		}
	}

	/**
	 * Login Google+
	 */
	private void loginGPlus() {
		if (mConnectionResult == null) {
			mProgressDialog = ProgressDialog.show(this, null, getString(R.string.lbl_login_gplus));
			mProgressDialog.setCancelable(true);
			mPlusClient.connect();
		} else {
			try {
				mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
			} catch (SendIntentException e) {
				mConnectionResult = null;
				mPlusClient.connect();
			}
		}
	}


	/**
	 * Logout Google+
	 */
	private void logoutGPlus() {
		if (mPlusClient.isConnected()) {
			mGPlusBtn.setVisibility(View.VISIBLE);
			mPlusClient.clearDefaultAccount();
			mPlusClient.disconnect();
		}
		Prefs prefs = Prefs.getInstance(getApplication());
		if (!TextUtils.isEmpty(prefs.getGoogleAccount())) {
			AsyncTaskCompat.executeParallel(new UnregGCMTask(getApplication(), prefs.getGoogleAccount()));
			prefs.setGoogleAccount(null);
			handleGPlusLinkedUI();
		}
		mSnackBar.show(getString(R.string.lbl_logout_gplus_cause));
	}


	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
			mConnectionResult = null;
			mPlusClient.connect();
		}
	}


	/**
	 * @return The height of actionbar.
	 */
	private int calcActionBarHeight() {
		int[] abSzAttr;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			abSzAttr = new int[] { android.R.attr.actionBarSize };
		} else {
			abSzAttr = new int[] { R.attr.actionBarSize };
		}
		TypedArray a = obtainStyledAttributes(abSzAttr);
		return a.getDimensionPixelSize(0, -1);
	}

	private void refreshCurrentTotalMessages() {
		AsyncTask<Void, int[], int[]> task = new AsyncTask<Void, int[], int[]>() {
			@Override
			protected int[] doInBackground(Void... params) {
				DB db = DB.getInstance(getApplication());
				return new  int[]{ db.getBookmarks(Sort.DESC).size() , db.getMessages(Sort.DESC).size()};
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
