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
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
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

import com.chopping.bus.CloseDrawerEvent;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
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
import com.hpush.app.noactivities.SyncBookmarkIntentService;
import com.hpush.bus.BookmarkAllEvent;
import com.hpush.bus.EULAConfirmedEvent;
import com.hpush.bus.EULARejectEvent;
import com.hpush.bus.FloatActionButtonEvent;
import com.hpush.bus.RemoveAllEvent;
import com.hpush.bus.RemoveAllEvent.WhichPage;
import com.hpush.bus.SelectMessageEvent;
import com.hpush.gcm.RegistrationIntentService;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;
import com.software.shell.fab.ActionButton;

import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;

/**
 * Main activity of the app.
 *
 * @author Xinyue Zhao
 */
public final class MainActivity extends BasicActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;
	/**
	 * The pagers
	 */
	private ViewPager                               mViewPager;
	/**
	 * Adapter for {@link #mViewPager}.
	 */
	private MainViewPagerAdapter                    mPagerAdapter;
	/**
	 * Navigation drawer.
	 */
	private DrawerLayout                            mDrawerLayout;
	/**
	 * Use navigation-drawer for this fork.
	 */
	private ActionBarDrawerToggle                   mDrawerToggle;
	/**
	 * The new actionbar.
	 */
	private Toolbar                                 mToolbar;
	/**
	 * Tabs.
	 */
	private android.support.design.widget.TabLayout mTabs;
	/**
	 * Click to remove all selected items.
	 */
	private ActionButton                            mRemoveAllBtn;
	/**
	 * Click to bookmark all selected items.
	 */
	private ActionButton                            mBookmarkAllBtn;
	/**
	 * Open/Close main float buttons.
	 */
	private ActionButton                            mOpenBtn;
	/**
	 * Search   buttons.
	 */
	private ActionButton                            mSearchBtn;
	/**
	 * The interstitial ad.
	 */
	private InterstitialAd                          mInterstitialAd;

	/**
	 * Container for toolbar and viewpager.
	 */
	private View           mHeaderView;
	/**
	 * Action progress indicator.
	 */
	private ProgressDialog mProgressDialog;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.hpush.bus.FloatActionButtonEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.FloatActionButtonEvent}.
	 */
	public void onEvent( FloatActionButtonEvent e ) {
		if( e.isHide() ) {
			mOpenBtn.hide();
			hideFABs();
		} else {
			mOpenBtn.show();
		}
	}


	/**
	 * Handler for {@link  EULARejectEvent}.
	 *
	 * @param e
	 * 		Event {@link  EULARejectEvent}.
	 */
	public void onEvent( EULARejectEvent e ) {
		ActivityCompat.finishAfterTransition( this );
	}

	/**
	 * Handler for {@link EULAConfirmedEvent}
	 *
	 * @param e
	 * 		Event {@link  EULAConfirmedEvent}.
	 */
	public void onEvent( EULAConfirmedEvent e ) {
		Prefs.getInstance( App.Instance )
			 .setUpdatedV2( true );
		ConnectGoogleActivity.showInstance( this );
	}

	/**
	 * Handler for {@link CloseDrawerEvent}.
	 *
	 * @param e
	 * 		Event {@link CloseDrawerEvent}.
	 */
	public void onEvent( CloseDrawerEvent e ) {
		mDrawerLayout.closeDrawers();
	}


	/**
	 * Handler for {@link SelectMessageEvent}.
	 *
	 * @param e
	 * 		Event {@link SelectMessageEvent}.
	 */
	public void onEvent( SelectMessageEvent e ) {
		openFloatButtons();
	}


	//------------------------------------------------

	/**
	 * Show single instance of {@link com.hpush.app.activities.MainActivity}
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	public static void showInstance( Context cxt ) {
		Intent intent = new Intent(
				cxt,
				MainActivity.class
		);
		intent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP );
		cxt.startActivity( intent );
	}


	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		switch( requestCode ) {
			case ConnectGoogleActivity.REQ:
				if( resultCode == RESULT_OK ) {
					Prefs prefs = Prefs.getInstance( App.Instance );
					if( prefs.isEULAOnceConfirmed() && !TextUtils.isEmpty( prefs.getGoogleAccount() ) ) {
						checkPushSetting();
					}
				} else {
					ActivityCompat.finishAffinity( this );
				}
		}
		super.onActivityResult(
				requestCode,
				resultCode,
				data
		);
	}


	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		Fabric.with(
				this,
				new Crashlytics()
		);
		setContentView( LAYOUT );

		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive( Context context, Intent intent ) {
				if( !TextUtils.isEmpty( Prefs.getInstance( getApplicationContext() )
											 .getPushRegId() ) ) {
					dismissProgressDialog();
					SubscribeTopicsActivity.showInstance( MainActivity.this );
				} else {
					View view = findViewById( R.id.coordinator_layout );
					dismissProgressDialog();
					Snackbar.make(
							view,
							R.string.meta_load_error,
							Snackbar.LENGTH_LONG
					)
							.setAction(
									R.string.btn_retry,
									new OnClickListener() {
										@Override
										public void onClick( View v ) {
											mProgressDialog = ProgressDialog.show(
													MainActivity.this,
													null,
													getString( R.string.msg_push_registering )
											);
											Intent intent = new Intent(
													MainActivity.this,
													RegistrationIntentService.class
											);
											startService( intent );
										}
									}
							)
							.show();
				}
			}
		};


		if( getResources().getBoolean( R.bool.landscape ) ) {
			setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
		}


		mHeaderView = findViewById( R.id.error_content );
		ViewCompat.setElevation(
				mHeaderView,
				getResources().getDimension( R.dimen.toolbar_elevation )
		);
		mToolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( mToolbar );
		initDrawer();
		mViewPager = (ViewPager) findViewById( R.id.vp );
		mPagerAdapter = new MainViewPagerAdapter(
				this,
				getSupportFragmentManager()
		);
		mViewPager.setAdapter( mPagerAdapter );
		calcActionBarHeight();
		// Bind the tabs to the ViewPager
		mTabs = (android.support.design.widget.TabLayout) findViewById( R.id.tabs );
		mTabs.setupWithViewPager( mViewPager );
		mViewPager.addOnPageChangeListener( new OnPageChangeListener() {
			@Override
			public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) {
				closeFloatButtons();
			}

			@Override
			public void onPageSelected( int position ) {
			}

			@Override
			public void onPageScrollStateChanged( int state ) {

			}
		} );

		mRemoveAllBtn = (ActionButton) findViewById( R.id.remove_all_btn );
		mRemoveAllBtn.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick( View v ) {
				hideFABs();
				EventBus.getDefault()
						.post( new RemoveAllEvent( mViewPager.getCurrentItem() == 0 ? WhichPage.Messages : WhichPage.Bookmarks ) );
			}
		} );
		mBookmarkAllBtn = (ActionButton) findViewById( R.id.bookmark_all_btn );
		mBookmarkAllBtn.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick( View v ) {
				hideFABs();
				EventBus.getDefault()
						.post( new BookmarkAllEvent() );
			}
		} );
		mOpenBtn = (ActionButton) findViewById( R.id.float_main_btn );
		mOpenBtn.setOnClickListener( mOpenListener );
		mSearchBtn = (ActionButton) findViewById( R.id.float_search_btn );
		ViewCompat.setTranslationX(
				mSearchBtn,
				ViewCompat.getTranslationX( mOpenBtn )
		);
		mSearchBtn.setOnClickListener( mSearchListener );

		//User that have used this application, should go back to login-page for upgrade version 2.0
		Prefs prefs = Prefs.getInstance( App.Instance );
		if( prefs.isEULAOnceConfirmed() && !prefs.isUpdatedV2() ) {
			prefs.setUpdatedV2( true );
			Utils.logout();
			com.chopping.utils.Utils.showLongToast(
					App.Instance,
					R.string.msg_welcome
			);
			ConnectGoogleActivity.showInstance( this );
		} else {
			//User that have used this application and done clear(logout), should go back to login-page.
			if( prefs.isEULAOnceConfirmed() && TextUtils.isEmpty( prefs.getGoogleAccount() ) ) {
				com.chopping.utils.Utils.showLongToast(
						App.Instance,
						R.string.msg_welcome_return
				);
				ConnectGoogleActivity.showInstance( this );
			} else if( prefs.isEULAOnceConfirmed() && !TextUtils.isEmpty( prefs.getGoogleAccount() ) ) {
				//Should do something.....
			}
		}
	}

	/**
	 * Dismiss all Float-Action-buttons.
	 */
	private void hideFABs() {
		mRemoveAllBtn.hide();
		mBookmarkAllBtn.hide();
		mSearchBtn.hide();
	}


	@Override
	protected void onNewIntent( Intent intent ) {
		super.onNewIntent( intent );
		setIntent( intent );
	}


	/**
	 * Dismiss all float-buttons.
	 */
	private void closeFloatButtons() {
		int vi = mRemoveAllBtn.getVisibility();
		if( vi == View.VISIBLE ) {
			mOpenBtn.performClick();
		}
	}


	/**
	 * Open all float-buttons.
	 */
	private void openFloatButtons() {
		int vi = mRemoveAllBtn.getVisibility();
		if( vi != View.VISIBLE ) {
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
		if( mDrawerToggle != null ) {
			mDrawerToggle.syncState();
		}
		checkPlayService();
		LocalBroadcastManager.getInstance( this )
							 .registerReceiver(
									 mRegistrationBroadcastReceiver,
									 new IntentFilter( RegistrationIntentService.REGISTRATION_COMPLETE )
							 );
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance( this )
							 .unregisterReceiver( mRegistrationBroadcastReceiver );
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate(
				R.menu.main,
				menu
		);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu( Menu menu ) {

		MenuItem menuShare = menu.findItem( R.id.action_share_app );
		//Getting the actionprovider associated with the menu item whose id is share.
		android.support.v7.widget.ShareActionProvider provider
				= (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider( menuShare );
		//Setting a share intent.
		String subject = getString(
				R.string.lbl_share_app_title,
				getString( R.string.application_name )
		);
		String text    = getString( R.string.lbl_share_app_content );
		provider.setShareIntent( Utils.getDefaultShareIntent(
				provider,
				subject,
				text
		) );

		return super.onPrepareOptionsMenu( menu );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		closeFloatButtons();
		if( mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected( item ) ) {
			return true;
		}
		int id = item.getItemId();
		switch( id ) {
			case R.id.action_search:
				onSearchRequested();
				break;
			case R.id.action_about:
				showDialogFragment(
						AboutDialogFragment.newInstance( this ),
						null
				);
				break;
			case R.id.action_setting:
				SettingActivity.showInstance(
						this,
						null
				);
				break;
			case R.id.action_facebook:
				Bundle postParams = new Bundle();
				final WebDialog fbDlg = new WebDialog.FeedDialogBuilder(
						this,
						getString( R.string.applicationId ),
						postParams
				).setCaption( String.format(
						getString( R.string.lbl_share_app_title ),
						getString( R.string.lbl_share_item_title )
				) )
				 .setName( getString( R.string.lbl_share_item_title ) )
				 .setDescription( getString( R.string.lbl_share_app_content ) )
				 .setLink( getString( R.string.lbl_app_link ) )
				 .build();
				fbDlg.setOnCompleteListener( new OnCompleteListener() {
					@Override
					public void onComplete( Bundle bundle, FacebookException e ) {
						fbDlg.dismiss();
					}
				} );
				fbDlg.show();
				break;
			case R.id.action_tweet:
				break;
		}
		return super.onOptionsItemSelected( item );
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
		if( TextUtils.isEmpty( Prefs.getInstance( getApplication() )
									.getPushRegId() ) ) {
			new android.support.v7.app.AlertDialog.Builder( this ).setTitle( R.string.application_name )
																  .setMessage( R.string.lbl_turn_on_push_info )
																  .setCancelable( false )
																  .setPositiveButton(
																		  R.string.lbl_yes,
																		  new DialogInterface.OnClickListener() {
																			  @Override
																			  public void onClick( DialogInterface dialog, int which ) {
																				  mProgressDialog = ProgressDialog.show(
																						  MainActivity.this,
																						  null,
																						  getString( R.string.msg_push_registering )
																				  );
																				  Intent intent = new Intent(
																						  MainActivity.this,
																						  RegistrationIntentService.class
																				  );
																				  startService( intent );
																			  }
																		  }
																  )
																  .setNegativeButton(
																		  R.string.lbl_no,
																		  new DialogInterface.OnClickListener() {
																			  @Override
																			  public void onClick( DialogInterface dialog, int which ) {

																			  }
																		  }
																  )
																  .create()
																  .show();
		}
	}

	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		getSupportFragmentManager().beginTransaction()
								   .replace(
										   R.id.app_list_fl,
										   AppListImpFragment.newInstance( this )
								   )
								   .commit();
	}


	/**
	 * Initialize the navigation drawer.
	 */
	private void initDrawer() {
		ActionBar actionBar = getSupportActionBar();
		if( actionBar != null ) {
			actionBar.setHomeButtonEnabled( true );
			actionBar.setDisplayHomeAsUpEnabled( true );
			mDrawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
			mDrawerToggle = new ActionBarDrawerToggle(
					this,
					mDrawerLayout,
					R.string.application_name,
					R.string.app_name
			);


			mDrawerLayout.setDrawerListener( mDrawerToggle );
			setupDrawerContent( (NavigationView) findViewById( R.id.nav_view ) );
		}
	}

	/**
	 * Set-up of navi-bar left.
	 */
	private void setupDrawerContent( NavigationView navigationView ) {
		navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected( MenuItem menuItem ) {
				menuItem.setChecked( true );
				mDrawerLayout.closeDrawer( Gravity.LEFT );

				switch( menuItem.getItemId() ) {
					case R.id.action_home:
						WebViewActivity.showInstance(
								MainActivity.this,
								null,
								null,
								null
						);
						break;
					case R.id.action_blog:
						WebViewActivity.showInstance(
								MainActivity.this,
								Prefs.getInstance( App.Instance )
									 .getHackerNewsBlogUrl(),
								null,
								null
						);
						break;
					case R.id.action_recent:
						DailiesActivity.showInstance( MainActivity.this );
						break;
					case R.id.action_more_apps:
						mDrawerLayout.openDrawer( Gravity.RIGHT );
						break;
					case R.id.action_settings:
						SettingActivity.showInstance(
								MainActivity.this,
								null
						);
						break;
				}
				return true;
			}
		} );
	}

	/**
	 * Listener for opening all float buttons.
	 */
	private OnClickListener mSearchListener = new OnClickListener() {
		@Override
		public void onClick( View v ) {
			hideFABs();
			onSearchRequested();
		}
	};
	/**
	 * Listener for opening all float buttons.
	 */
	private OnClickListener mOpenListener   = new OnClickListener() {
		@Override
		public void onClick( View v ) {
			if( mViewPager.getCurrentItem() == 0 ) {
				mBookmarkAllBtn.show();
			}
			mRemoveAllBtn.show();
			mSearchBtn.show();
			mOpenBtn.setOnClickListener( mCloseListener );
		}
	};
	/**
	 * Listener for closing all float buttons.
	 */
	private OnClickListener mCloseListener  = new OnClickListener() {
		@Override
		public void onClick( View v ) {
			mBookmarkAllBtn.hide();
			mRemoveAllBtn.hide();
			mSearchBtn.hide();
			mOpenBtn.setOnClickListener( mOpenListener );
		}
	};

	/**
	 * Make an Admob.
	 */
	private void makeAds() {
		int curTime  = App.Instance.getAdsShownTimes();
		int adsTimes = Prefs.getInstance( App.Instance )
							.getShownDetailsAdsTimes();
		if( curTime % adsTimes == 0 ) {
			// Create an ad.
			mInterstitialAd = new InterstitialAd( this );
			mInterstitialAd.setAdUnitId( getString( R.string.ad_inters_unit_id ) );
			// Create ad request.
			AdRequest adRequest = new AdRequest.Builder().build();
			// Begin loading your interstitial.
			mInterstitialAd.setAdListener( new AdListener() {
				@Override
				public void onAdLoaded() {
					super.onAdLoaded();
					displayInterstitial();
				}
			} );
			mInterstitialAd.loadAd( adRequest );
		}
		curTime++;
		App.Instance.setAdsShownTimes( curTime );
	}


	/**
	 * Invoke displayInterstitial() when you are ready to display an interstitial.
	 */
	public void displayInterstitial() {
		if( mInterstitialAd.isLoaded() ) {
			mInterstitialAd.show();
		}
	}

	/**
	 * To confirm whether the validation of the Play-service of Google Inc.
	 */
	private void checkPlayService() {
		final int isFound = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this );
		if( isFound == ConnectionResult.SUCCESS ) {//Ignore update.
			//The "End User License Agreement" must be confirmed before you use this application.
			if( !Prefs.getInstance( getApplication() )
					  .isEULAOnceConfirmed() ) {
				showDialogFragment(
						new EulaConfirmationDialog(),
						null
				);
			}
		} else {
			new android.support.v7.app.AlertDialog.Builder( this ).setTitle( R.string.application_name )
																  .setMessage( R.string.lbl_play_service )
																  .setCancelable( false )
																  .setPositiveButton(
																		  R.string.btn_ok,
																		  new DialogInterface.OnClickListener() {
																			  public void onClick( DialogInterface dialog,
																								   int whichButton
																			  ) {
																				  dialog.dismiss();
																				  Intent intent = new Intent( Intent.ACTION_VIEW );
																				  intent.setData( Uri.parse( getString( R.string.play_service_url ) ) );
																				  try {
																					  startActivity( intent );
																				  } catch( ActivityNotFoundException e0 ) {
																					  intent.setData( Uri.parse( getString( R.string.play_service_web ) ) );
																					  try {
																						  startActivity( intent );
																					  } catch( Exception e1 ) {
																						  //Ignore now.
																					  }
																				  } finally {
																					  finish();
																				  }
																			  }
																		  }
																  )
																  .create()
																  .show();
		}
	}


	/**
	 * Show  {@link android.support.v4.app.DialogFragment}.
	 *
	 * @param _dlgFrg
	 * 		An instance of {@link android.support.v4.app.DialogFragment}.
	 * @param _tagName
	 * 		Tag name for dialog, default is "dlg". To grantee that only one instance of {@link android.support.v4.app.DialogFragment} can been seen.
	 */
	protected void showDialogFragment( DialogFragment _dlgFrg, String _tagName ) {
		try {
			if( _dlgFrg != null ) {
				DialogFragment      dialogFragment = _dlgFrg;
				FragmentTransaction ft             = getSupportFragmentManager().beginTransaction();
				// Ensure that there's only one dialog to the user.
				Fragment prev = getSupportFragmentManager().findFragmentByTag( "dlg" );
				if( prev != null ) {
					ft.remove( prev );
				}
				try {
					if( TextUtils.isEmpty( _tagName ) ) {
						dialogFragment.show(
								ft,
								"dlg"
						);
					} else {
						dialogFragment.show(
								ft,
								_tagName
						);
					}
				} catch( Exception _e ) {
				}
			}
		} catch( Exception _e ) {
		}
	}


	/**
	 * Close progress indicator.
	 */
	private void dismissProgressDialog() {
		if( mProgressDialog != null && mProgressDialog.isShowing() ) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		if( mDrawerLayout.isDrawerOpen( Gravity.LEFT ) || mDrawerLayout.isDrawerOpen( Gravity.RIGHT ) ) {
			mDrawerLayout.closeDrawers();
		} else {
			Intent intent = new Intent(
					this,
					SyncBookmarkIntentService.class
			);
			startService( intent );
			super.onBackPressed();
		}
	}


}
