package com.hpush.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.chopping.application.LL;
import com.chopping.bus.ApplicationConfigurationDownloadedEvent;
import com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent;
import com.chopping.exceptions.CanNotOpenOrFindAppPropertiesException;
import com.chopping.exceptions.InvalidAppPropertiesException;
import com.hpush.R;
import com.hpush.gcm.EditTask;
import com.hpush.gcm.RegGCMTask;
import com.hpush.gcm.UnregGCMTask;
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
	 *  @param openSettingV
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
				finish();
			}
		});

		CheckBoxPreference push = (CheckBoxPreference) findPreference(Prefs.KEY_PUSH_SETTING);
		push.setSummary( getString(prefs.isPushTurnedOn() ? R.string.setting_push_on :R.string.setting_push_off ));
		push.setOnPreferenceChangeListener(this);

		CheckBoxPreference fullText = (CheckBoxPreference) findPreference(Prefs.KEY_FULL_TEXT);
		fullText.setOnPreferenceChangeListener(this);

		CheckBoxPreference allowEmptyUrl = (CheckBoxPreference) findPreference(Prefs.KEY_ALLOW_EMPTY_URL);
		allowEmptyUrl.setOnPreferenceChangeListener(this);

		EditTextPreference count = (EditTextPreference) findPreference(Prefs.KEY_MSG_COUNT);
		count.setSummary(getString(R.string.setting_messages_count, prefs.getMsgCount()));
		count.setOnPreferenceChangeListener(this);


		((MarginLayoutParams) findViewById(android.R.id.list).getLayoutParams()).topMargin = getActionBarHeight(this);
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
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(Prefs.KEY_PUSH_SETTING)) {
			if (!Boolean.valueOf(newValue.toString())) {
				AsyncTaskCompat.executeParallel(new UnregGCMTask(getApplication(), Prefs.getInstance(getApplication()).getGoogleAccount()) {
					ProgressDialog dlg;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dlg = ProgressDialog.show(SettingActivity.this, null, getString(
								R.string.msg_push_unregistering));
						dlg.setCancelable(false);
					}

					@Override
					protected void onPostExecute(String regId) {
						super.onPostExecute(regId);
						dlg.dismiss();
					}
				});
			} else {
				AsyncTaskCompat.executeParallel(new RegGCMTask(getApplication()) {
					ProgressDialog dlg;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dlg = ProgressDialog.show(SettingActivity.this, null, getString(R.string.msg_push_registering));
						dlg.setCancelable(false);
					}

					@Override
					protected void onPostExecute(String regId) {
						super.onPostExecute(regId);
						dlg.dismiss();
					}
				});
			}
		}

		if (preference.getKey().equals(Prefs.KEY_MSG_COUNT)) {
			int count = Integer.valueOf(newValue.toString());
			int max = getResources().getInteger(R.integer.max_msg_count);
			if (count > max) {
				count = max;
			}
			preference.setSummary(getString(R.string.setting_messages_count, count + ""));
		}
		return true;
	}


	@Override
	protected void onResume() {
		EventBus.getDefault().register(this);
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
		dismissPb();
		mPb = ProgressDialog.show(this, null, getString(R.string.msg_save_data));
		mPb.setCancelable(true);
		Prefs prefs = Prefs.getInstance(getApplication());
		final String regId = prefs.getPushRegId();
		if (!TextUtils.isEmpty(regId)) {
			 new EditTask(getApplication(), Method.POST, prefs
					.getPushBackendEditUrl(), new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					backPressed();
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					backPressed();
				}
			} ).execute();
		} else {
			backPressed();
		}
	}

	private void onAppConfigLoaded() {
		dismissPb();
	}

	private void onAppConfigIgnored() {
		dismissPb();
	}

	private void backPressed() {
		dismissPb();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			finishAfterTransition();
		} else {
			finish();
		}
	}
}
