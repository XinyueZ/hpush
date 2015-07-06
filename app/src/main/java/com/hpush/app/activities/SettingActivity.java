package com.hpush.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.chopping.application.LL;
import com.chopping.bus.ApplicationConfigurationDownloadedEvent;
import com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent;
import com.chopping.exceptions.CanNotOpenOrFindAppPropertiesException;
import com.chopping.exceptions.InvalidAppPropertiesException;
import com.hpush.R;
import com.hpush.gcm.RegistrationIntentService;
import com.hpush.gcm.SubscribeIntentService;
import com.hpush.gcm.Topics;
import com.hpush.gcm.UnregistrationIntentService;
import com.hpush.gcm.UnsubscribeIntentService;
import com.hpush.utils.Prefs;

import de.greenrobot.event.EventBus;


/**
 * Setting .
 */
public final class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	/**
	 * The "ActionBar".
	 */
	private Toolbar mToolbar;

	/**
	 * Progress indicator.
	 */
	private ProgressDialog mPb;
	/**
	 * Listener while registering push-feature.
	 */
	private BroadcastReceiver mRegistrationBroadcastReceiver;
	/**
	 * Listener while unregistering push-feature.
	 */
	private BroadcastReceiver mUnregistrationBroadcastReceiver;

	private CheckBoxPreference mTopPref;
	private CheckBoxPreference mNewPref;
	private CheckBoxPreference mAskPref;
	private CheckBoxPreference mShowPref;
	private CheckBoxPreference mJobPref;


	private Intent mSubJobIntent;
	private Intent mSubShowIntent;
	private Intent mSubAskIntent;
	private Intent mSubTopIntent;
	private Intent mSubNewIntent;
	private BroadcastReceiver mSubTopsRegRecv;
	private BroadcastReceiver mSubNewRegRecv;
	private BroadcastReceiver mSubAskRegRecv;
	private BroadcastReceiver mSubShowRegRecv;
	private BroadcastReceiver mSubJobsRegRecv;


	private Intent mUnsubJobIntent;
	private Intent mUnsubShowIntent;
	private Intent mUnsubAskIntent;
	private Intent mUnsubTopIntent;
	private Intent mUnsubNewIntent;
	private BroadcastReceiver mUnsubTopsRegRecv;
	private BroadcastReceiver mUnsubNewRegRecv;
	private BroadcastReceiver mUnsubAskRegRecv;
	private BroadcastReceiver mUnsubShowRegRecv;
	private BroadcastReceiver mUnsubJobsRegRecv;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.chopping.bus.ApplicationConfigurationDownloadedEvent}
	 *
	 * @param e
	 * 		Event {@link  com.chopping.bus.ApplicationConfigurationDownloadedEvent}.
	 */
	public void onEvent(ApplicationConfigurationDownloadedEvent e) {
		onAppConfigLoaded();
	}

	/**
	 * Handler for {@link com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent}.
	 *
	 * @param e
	 * 		Event {@link com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent}.
	 */
	public void onEvent(ApplicationConfigurationLoadingIgnoredEvent e) {
		LL.i("Ignored a change to load application's configuration.");
		onAppConfigIgnored();
	}

	//------------------------------------------------

	/**
	 * Show an instance of SettingsActivity.
	 *
	 * @param context
	 * 		A context object.
	 * @param openSettingV
	 * 		The view that open the {@link com.hpush.app.activities.WebViewActivity}.
	 */
	public static void showInstance(Activity context, View openSettingV) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//		if (openSettingV != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) ){
		//			ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(context,
		//					Pair.create(openSettingV, "openSettingV"));
		//			context.startActivity(intent, transitionActivityOptions.toBundle());
		//		} else {
		context.startActivity(intent);
		//		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Prefs prefs = Prefs.getInstance(getApplication());
		if (getResources().getBoolean(R.bool.landscape)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		addPreferencesFromResource(R.xml.settings);
		mPb = ProgressDialog.show(this, null, getString(R.string.msg_app_init));
		mPb.setCancelable(true);
		mToolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar, null, false);
		addContentView(mToolbar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mToolbar.setTitle(R.string.application_name);
		mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backPressed();
			}
		});

		CheckBoxPreference push = (CheckBoxPreference) findPreference(Prefs.KEY_PUSH_SETTING);
		push.setSummary(getString(!TextUtils.isEmpty(prefs.getPushRegId()) ? R.string.setting_push_on :
				R.string.setting_push_off));
		push.setChecked(!TextUtils.isEmpty(prefs.getPushRegId()));
		push.setOnPreferenceChangeListener(this);

		subscribeTopStories();
		unsubscribeTopStories();
		mTopPref = (CheckBoxPreference) findPreference(Prefs.KEY_PUSH_TOPSTORIES);
		mTopPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (TextUtils.isEmpty(Prefs.getInstance(getApplicationContext()).getPushRegId())) {
					Snackbar.make(findViewById(android.R.id.list), R.string.lbl_no_push_for_subscribe,
							Snackbar.LENGTH_SHORT).show();
					mTopPref.setChecked(false);
				} else {
					if (Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_TOPSTORIES)) {
						subscribeTopStories();
					} else {
						unsubscribeTopStories();
					}
				}
				return true;
			}
		});

		subscribeNewStories();
		unsubscribeNewStories();
		mNewPref = (CheckBoxPreference) findPreference(Prefs.KEY_PUSH_NEWSTORIES);
		mNewPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (TextUtils.isEmpty(Prefs.getInstance(getApplicationContext()).getPushRegId())) {
					Snackbar.make(findViewById(android.R.id.list), R.string.lbl_no_push_for_subscribe,
							Snackbar.LENGTH_SHORT).show();
					mNewPref.setChecked(false);
				} else {
					if (Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_NEWSTORIES)) {
						subscribeNewStories();
					} else {
						unsubscribeNewStories();
					}
				}
				return true;
			}
		});

		subscribeAskStories();
		unsubscribeAskStories();
		mAskPref = (CheckBoxPreference) findPreference(Prefs.KEY_PUSH_ASKSTORIES);
		mAskPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (TextUtils.isEmpty(Prefs.getInstance(getApplicationContext()).getPushRegId())) {
					Snackbar.make(findViewById(android.R.id.list), R.string.lbl_no_push_for_subscribe,
							Snackbar.LENGTH_SHORT).show();
					mAskPref.setChecked(false);
				} else {
					if (Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_ASKSTORIES)) {
						subscribeAskStories();
					} else {
						unsubscribeAskStories();
					}
				}
				return true;
			}
		});

		subscribeShowStories();
		unsubscribeShowStories();
		mShowPref = (CheckBoxPreference) findPreference(Prefs.KEY_PUSH_SHOWSTORIES);
		mShowPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (TextUtils.isEmpty(Prefs.getInstance(getApplicationContext()).getPushRegId())) {
					Snackbar.make(findViewById(android.R.id.list), R.string.lbl_no_push_for_subscribe,
							Snackbar.LENGTH_SHORT).show();
					mShowPref.setChecked(false);
				} else {
					if (Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_SHOWSTORIES)) {
						subscribeShowStories();
					} else {
						unsubscribeShowStories();
					}
				}
				return true;
			}
		});

		subscribeJobStories();
		unsubscribeJobStories();
		mJobPref = (CheckBoxPreference) findPreference(Prefs.KEY_PUSH_JOBSTORIES);
		mJobPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (TextUtils.isEmpty(Prefs.getInstance(getApplicationContext()).getPushRegId())) {
					Snackbar.make(findViewById(android.R.id.list), R.string.lbl_no_push_for_subscribe,
							Snackbar.LENGTH_SHORT).show();
					mJobPref.setChecked(false);
				} else {
					if (Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_JOBSTORIES)) {
						subscribeJobStories();
					} else {
						unsubscribeJobStories();
					}
				}
				return true;
			}
		});
		mTopPref.setChecked(prefs.getPush(Prefs.KEY_PUSH_TOPSTORIES));
		mNewPref.setChecked(prefs.getPush(Prefs.KEY_PUSH_NEWSTORIES));
		mAskPref.setChecked(prefs.getPush(Prefs.KEY_PUSH_ASKSTORIES));
		mShowPref.setChecked(prefs.getPush(Prefs.KEY_PUSH_SHOWSTORIES));
		mJobPref.setChecked(prefs.getPush(Prefs.KEY_PUSH_JOBSTORIES));

		CheckBoxPreference allowEmptyUrl = (CheckBoxPreference) findPreference(Prefs.KEY_ALLOW_EMPTY_URL);
		allowEmptyUrl.setOnPreferenceChangeListener(this);

		ListPreference sort = (ListPreference) findPreference(Prefs.KEY_SORT_TYPE);
		String value = prefs.getSortTypeValue();
		sort.setValue(value);
		int pos = Integer.valueOf(value);
		String[] arr = getResources().getStringArray(R.array.setting_sort_types);
		sort.setSummary(arr[pos]);
		sort.setOnPreferenceChangeListener(this);


		ListPreference sound = (ListPreference) findPreference(Prefs.KEY_SOUND_TYPE);
		String soundValue = prefs.getSoundTypeValue();
		sound.setValue(soundValue);
		int posSound = Integer.valueOf(soundValue);
		String[] arrSound = getResources().getStringArray(R.array.setting_sound_types);
		sound.setSummary(arrSound[posSound]);
		sound.setOnPreferenceChangeListener(this);

		((MarginLayoutParams) findViewById(android.R.id.list).getLayoutParams()).topMargin = getActionBarHeight(this);


		//Listeners for register and unregister PUSH.
		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (!TextUtils.isEmpty(Prefs.getInstance(getApplicationContext()).getPushRegId())) {
					dismissPb();
					SubscribeTopicsActivity.showInstance(SettingActivity.this);
				} else {
					dismissPb();
					Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
							.setAction(R.string.btn_retry, new OnClickListener() {
								@Override
								public void onClick(View v) {
									mPb = ProgressDialog.show(SettingActivity.this, null, getString(
											R.string.msg_push_registering));
									mPb.setCancelable(true);
									Intent intent = new Intent(SettingActivity.this, RegistrationIntentService.class);
									startService(intent);
								}
							}).show();
				}
			}
		};

		mUnregistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (TextUtils.isEmpty(Prefs.getInstance(getApplicationContext()).getPushRegId())) {
					dismissPb();
					mTopPref.setChecked(false);
					mNewPref.setChecked(false);
					mAskPref.setChecked(false);
					mShowPref.setChecked(false);
					mJobPref.setChecked(false);
				} else {
					dismissPb();
					Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
							.setAction(R.string.btn_retry, new OnClickListener() {
								@Override
								public void onClick(View v) {
									mPb = ProgressDialog.show(SettingActivity.this, null, getString(
											R.string.msg_push_unregistering));
									mPb.setCancelable(true);
									Intent intent = new Intent(SettingActivity.this, UnregistrationIntentService.class);
									startService(intent);
								}
							}).show();
				}
			}
		};


		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(
				RegistrationIntentService.REGISTRATION_COMPLETE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mUnregistrationBroadcastReceiver, new IntentFilter(
				UnregistrationIntentService.UNREGISTRATION_COMPLETE));
	}


	/**
	 * Get height of {@link android.support.v7.app.ActionBar}.
	 *
	 * @param activity
	 * 		{@link android.app.Activity} that hosts an  {@link android.support.v7.app.ActionBar}.
	 *
	 * @return Height of bar.
	 */
	public static int getActionBarHeight(Activity activity) {
		int[] abSzAttr;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			abSzAttr = new int[] { android.R.attr.actionBarSize };
		} else {
			abSzAttr = new int[] { R.attr.actionBarSize };
		}
		TypedArray a = activity.obtainStyledAttributes(abSzAttr);
		return a.getDimensionPixelSize(0, -1);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			backPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(Prefs.KEY_PUSH_SETTING)) {
			if (!Boolean.valueOf(newValue.toString())) {
				mPb = ProgressDialog.show(SettingActivity.this, null, getString(R.string.msg_push_unregistering));
				Intent intent = new Intent(this, UnregistrationIntentService.class);
				startService(intent);
			} else {
				mPb = ProgressDialog.show(this, null, getString(R.string.msg_push_registering));
				Intent intent = new Intent(this, RegistrationIntentService.class);
				startService(intent);
			}
		}


		if (preference.getKey().equals(Prefs.KEY_SORT_TYPE)) {
			int pos = Integer.valueOf(newValue.toString());
			String[] arr = getResources().getStringArray(R.array.setting_sort_types);
			preference.setSummary(arr[pos]);
		}


		if (preference.getKey().equals(Prefs.KEY_SOUND_TYPE)) {
			int pos = Integer.valueOf(newValue.toString());
			String[] arr = getResources().getStringArray(R.array.setting_sound_types);
			preference.setSummary(arr[pos]);
		}
		return true;
	}


	@Override
	protected void onResume() {
		EventBus.getDefault().registerSticky(this);
		super.onResume();

		String mightError = null;
		try {
			Prefs.getInstance(getApplication()).downloadApplicationConfiguration();
		} catch (InvalidAppPropertiesException _e) {
			mightError = _e.getMessage();
		} catch (CanNotOpenOrFindAppPropertiesException _e) {
			mightError = _e.getMessage();
		}
		if (mightError != null) {
			new AlertDialog.Builder(this).setTitle(com.chopping.R.string.app_name).setMessage(mightError).setCancelable(
					false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).create().show();
		}
	}

	@Override
	protected void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnregistrationBroadcastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mSubTopsRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mSubNewRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mSubAskRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mSubShowRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mSubJobsRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnsubTopsRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnsubNewRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnsubAskRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnsubShowRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnsubJobsRegRecv);
		super.onDestroy();
	}

	/**
	 * Remove the progress indicator.
	 */
	private void dismissPb() {
		if (mPb != null && mPb.isShowing()) {
			mPb.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		backPressed();
	}

	private void onAppConfigLoaded() {
		dismissPb();
	}

	private void onAppConfigIgnored() {
		dismissPb();
	}

	private void backPressed() {
		dismissPb();
		ActivityCompat.finishAfterTransition(this);
	}


	private void subscribeJobStories() {
		if (mSubJobIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mSubJobsRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mJobPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_JOBSTORIES));
					boolean success = intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										subscribeJobStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mSubJobIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mSubJobIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_JOB_STORIES);
			mSubJobIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_JOBSTORIES);
			mSubJobIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_JOB_STORIES);
		} else {
			startService(mSubJobIntent);
		}
	}

	private void unsubscribeJobStories() {
		if (mUnsubJobIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mUnsubJobsRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mJobPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_JOBSTORIES));
					boolean success = intent.getBooleanExtra(UnsubscribeIntentService.UNSUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										unsubscribeJobStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(UnsubscribeIntentService.UNSUBSCRIBE_NAME));
			mUnsubJobIntent = new Intent(getApplicationContext(), UnsubscribeIntentService.class);
			mUnsubJobIntent.putExtra(UnsubscribeIntentService.TOPIC, Topics.GET_TOP_STORIES);
			mUnsubJobIntent.putExtra(UnsubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_TOPSTORIES);
			mUnsubJobIntent.putExtra(UnsubscribeIntentService.UNSUBSCRIBE_NAME, Topics.GET_TOP_STORIES);
		} else {
			startService(mUnsubJobIntent);
		}
	}

	private void subscribeShowStories() {
		if (mSubShowIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mSubShowRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mShowPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(
							Prefs.KEY_PUSH_SHOWSTORIES));
					boolean success = intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										subscribeShowStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mSubShowIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mSubShowIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_SHOW_STORIES);
			mSubShowIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_SHOWSTORIES);
			mSubShowIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_SHOW_STORIES);
		} else {
			startService(mSubShowIntent);
		}
	}

	private void unsubscribeShowStories() {
		if (mUnsubShowIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mUnsubShowRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mShowPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(
							Prefs.KEY_PUSH_SHOWSTORIES));
					boolean success = intent.getBooleanExtra(UnsubscribeIntentService.UNSUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										unsubscribeShowStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(UnsubscribeIntentService.UNSUBSCRIBE_NAME));
			mUnsubShowIntent = new Intent(getApplicationContext(), UnsubscribeIntentService.class);
			mUnsubShowIntent.putExtra(UnsubscribeIntentService.TOPIC, Topics.GET_TOP_STORIES);
			mUnsubShowIntent.putExtra(UnsubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_TOPSTORIES);
			mUnsubShowIntent.putExtra(UnsubscribeIntentService.UNSUBSCRIBE_NAME, Topics.GET_TOP_STORIES);
		} else {
			startService(mUnsubShowIntent);
		}
	}

	private void subscribeAskStories() {
		if (mSubAskIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mSubAskRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mAskPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_ASKSTORIES));
					boolean success = intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										subscribeAskStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mSubAskIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mSubAskIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_ASK_STORIES);
			mSubAskIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_ASKSTORIES);
			mSubAskIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_ASK_STORIES);
		} else {
			startService(mSubAskIntent);
		}
	}

	private void unsubscribeAskStories() {
		if (mUnsubAskIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mUnsubAskRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mAskPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_ASKSTORIES));
					boolean success = intent.getBooleanExtra(UnsubscribeIntentService.UNSUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										unsubscribeAskStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(UnsubscribeIntentService.UNSUBSCRIBE_NAME));
			mUnsubAskIntent = new Intent(getApplicationContext(), UnsubscribeIntentService.class);
			mUnsubAskIntent.putExtra(UnsubscribeIntentService.TOPIC, Topics.GET_TOP_STORIES);
			mUnsubAskIntent.putExtra(UnsubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_TOPSTORIES);
			mUnsubAskIntent.putExtra(UnsubscribeIntentService.UNSUBSCRIBE_NAME, Topics.GET_TOP_STORIES);
		} else {
			startService(mUnsubAskIntent);
		}
	}

	private void subscribeNewStories() {
		if (mSubNewIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mSubNewRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mNewPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_NEWSTORIES));
					boolean success = intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										subscribeNewStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mSubNewIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mSubNewIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_NEW_STORIES);
			mSubNewIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_NEWSTORIES);
			mSubNewIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_NEW_STORIES);
		} else {
			startService(mSubNewIntent);
		}
	}

	private void unsubscribeNewStories() {
		if (mUnsubNewIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mUnsubNewRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mNewPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_NEWSTORIES));
					boolean success = intent.getBooleanExtra(UnsubscribeIntentService.UNSUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										unsubscribeNewStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(UnsubscribeIntentService.UNSUBSCRIBE_COMPLETE));
			mUnsubNewIntent = new Intent(getApplicationContext(), UnsubscribeIntentService.class);
			mUnsubNewIntent.putExtra(UnsubscribeIntentService.TOPIC, Topics.GET_NEW_STORIES);
			mUnsubNewIntent.putExtra(UnsubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_NEWSTORIES);
			mUnsubNewIntent.putExtra(UnsubscribeIntentService.UNSUBSCRIBE_NAME, Topics.GET_NEW_STORIES);
		} else {
			startService(mUnsubNewIntent);
		}
	}

	private void subscribeTopStories() {
		if (mSubTopIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mSubTopsRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mTopPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_TOPSTORIES));
					boolean success = intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										subscribeTopStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mSubTopIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mSubTopIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_TOP_STORIES);
			mSubTopIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_TOPSTORIES);
			mSubTopIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_TOP_STORIES);
		} else {
			startService(mSubTopIntent);
		}
	}


	private void unsubscribeTopStories() {
		if (mUnsubTopIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mUnsubTopsRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mTopPref.setChecked(Prefs.getInstance(getApplicationContext()).getPush(Prefs.KEY_PUSH_TOPSTORIES));
					boolean success = intent.getBooleanExtra(UnsubscribeIntentService.UNSUBSCRIBE_RESULT, false);
					if(!success) {
						Snackbar.make(findViewById(android.R.id.list), R.string.meta_load_error, Snackbar.LENGTH_LONG)
								.setAction(R.string.btn_retry, new OnClickListener() {
									@Override
									public void onClick(View v) {
										unsubscribeTopStories();
									}
								}).show();
					}
				}
			}, new IntentFilter(UnsubscribeIntentService.UNSUBSCRIBE_NAME));
			mUnsubTopIntent = new Intent(getApplicationContext(), UnsubscribeIntentService.class);
			mUnsubTopIntent.putExtra(UnsubscribeIntentService.TOPIC, Topics.GET_TOP_STORIES);
			mUnsubTopIntent.putExtra(UnsubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_TOPSTORIES);
			mUnsubTopIntent.putExtra(UnsubscribeIntentService.UNSUBSCRIBE_NAME, Topics.GET_TOP_STORIES);
		} else {
			startService(mUnsubTopIntent);
		}
	}


}
