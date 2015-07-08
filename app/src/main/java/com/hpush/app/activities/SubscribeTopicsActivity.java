package com.hpush.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.hpush.R;
import com.hpush.databinding.ActivitySubscribeTopicsBinding;
import com.hpush.gcm.SubscribeIntentService;
import com.hpush.gcm.Topics;
import com.hpush.utils.Prefs;

/**
 * Status of subscribing topics.
 *
 * @author Xinyue Zhao
 */
public final class SubscribeTopicsActivity extends AppCompatActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_subscribe_topics;

	/**
	 * Data-binding.
	 */
	private ActivitySubscribeTopicsBinding mBinding;

	/**
	 * Show single instance of {@link SubscribeTopicsActivity}
	 *
	 * @param cxt
	 * 		{@link Context}.
	 */
	public static void showInstance(Context cxt) {
		Intent intent = new Intent(cxt, SubscribeTopicsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}

	private volatile int mTotal = 6;
	private Intent mSumIntent;
	private Intent mJobIntent;
	private Intent mShowIntent;
	private Intent mAskIntent;
	private Intent mTopIntent;
	private Intent mNewIntent;
	private BroadcastReceiver mTopsRegRecv;
	private BroadcastReceiver mNewRegRecv;
	private BroadcastReceiver mAskRegRecv;
	private BroadcastReceiver mShowRegRecv;
	private BroadcastReceiver mJobsRegRecv;
	private BroadcastReceiver mSumRegRecv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);

		mBinding.closeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityCompat.finishAfterTransition(SubscribeTopicsActivity.this);
			}
		});

		if (getResources().getBoolean(R.bool.landscape)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		mBinding.topstoriesRetryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				mBinding.topstoriesStatusTv.setVisibility(View.GONE);
				subscribeTopStories();
			}
		});
		subscribeTopStories();

		mBinding.newstoriesRetryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				mBinding.newstoriesStatusTv.setVisibility(View.GONE);
				subscribeNewStories();
			}
		});
		subscribeNewStories();

		mBinding.askstoriesRetryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				mBinding.askstoriesStatusTv.setVisibility(View.GONE);
				subscribeAskStories();
			}
		});
		subscribeAskStories();

		mBinding.showstoriesRetryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				mBinding.showstoriesStatusTv.setVisibility(View.GONE);
				subscribeShowStories();
			}
		});
		subscribeShowStories();

		mBinding.jobstoriesRetryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				mBinding.jobstoriesStatusTv.setVisibility(View.GONE);
				subscribeJobStories();
			}
		});
		subscribeJobStories();

		mBinding.summaryRetryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				mBinding.summaryStatusTv.setVisibility(View.GONE);
				subscribeSummary();
			}
		});
		subscribeSummary();
	}

	private void subscribeSummary() {
		if (mSumIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mSumRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mBinding.summaryStatusTv.setVisibility(View.VISIBLE);
					if (intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false)) {
						mBinding.summaryStatusTv.setText(R.string.lbl_finished);
						updateSubscribing();
					} else {
						mBinding.summaryStatusTv.setText(R.string.lbl_failed);
						mBinding.summaryRetryBtn.setVisibility(View.VISIBLE);
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mSumIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mSumIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_SUMMARY);
			mSumIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_SUMMARY);
			mSumIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_SUMMARY);
		}
		startService(mSumIntent);
	}

	private void subscribeJobStories() {
		if (mJobIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mJobsRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mBinding.jobstoriesStatusTv.setVisibility(View.VISIBLE);
					if (intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false)) {
						mBinding.jobstoriesStatusTv.setText(R.string.lbl_finished);
						updateSubscribing();
					} else {
						mBinding.jobstoriesStatusTv.setText(R.string.lbl_failed);
						mBinding.jobstoriesRetryBtn.setVisibility(View.VISIBLE);
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mJobIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mJobIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_JOB_STORIES);
			mJobIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_JOBSTORIES);
			mJobIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_JOB_STORIES);
		}
		startService(mJobIntent);
	}

	private void subscribeShowStories() {
		if (mShowIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mShowRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mBinding.showstoriesStatusTv.setVisibility(View.VISIBLE);
					if (intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false)) {
						mBinding.showstoriesStatusTv.setText(R.string.lbl_finished);
						updateSubscribing();
					} else {
						mBinding.showstoriesStatusTv.setText(R.string.lbl_failed);
						mBinding.showstoriesRetryBtn.setVisibility(View.VISIBLE);
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mShowIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mShowIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_SHOW_STORIES);
			mShowIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_SHOWSTORIES);
			mShowIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_SHOW_STORIES);
		}
		startService(mShowIntent);
	}

	private void subscribeAskStories() {
		if (mAskIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mAskRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mBinding.askstoriesStatusTv.setVisibility(View.VISIBLE);
					if (intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false)) {
						mBinding.askstoriesStatusTv.setText(R.string.lbl_finished);
						updateSubscribing();
					} else {
						mBinding.askstoriesStatusTv.setText(R.string.lbl_failed);
						mBinding.askstoriesRetryBtn.setVisibility(View.VISIBLE);
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mAskIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mAskIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_ASK_STORIES);
			mAskIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_ASKSTORIES);
			mAskIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_ASK_STORIES);
		}
		startService(mAskIntent);
	}

	private void subscribeNewStories() {
		if (mNewIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mNewRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mBinding.newstoriesStatusTv.setVisibility(View.VISIBLE);
					if (intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false)) {
						mBinding.newstoriesStatusTv.setText(R.string.lbl_finished);
						updateSubscribing();
					} else {
						mBinding.newstoriesStatusTv.setText(R.string.lbl_failed);
						mBinding.newstoriesRetryBtn.setVisibility(View.VISIBLE);
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mNewIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mNewIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_NEW_STORIES);
			mNewIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_NEWSTORIES);
			mNewIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_NEW_STORIES);
		}
		startService(mNewIntent);
	}

	private void subscribeTopStories() {
		if (mTopIntent == null) {
			LocalBroadcastManager.getInstance(this).registerReceiver(mTopsRegRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mBinding.topstoriesStatusTv.setVisibility(View.VISIBLE);
					if (intent.getBooleanExtra(SubscribeIntentService.SUBSCRIBE_RESULT, false)) {
						mBinding.topstoriesStatusTv.setText(R.string.lbl_finished);
						updateSubscribing();
					} else {
						mBinding.topstoriesStatusTv.setText(R.string.lbl_failed);
						mBinding.topstoriesRetryBtn.setVisibility(View.VISIBLE);
					}
				}
			}, new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
			mTopIntent = new Intent(getApplicationContext(), SubscribeIntentService.class);
			mTopIntent.putExtra(SubscribeIntentService.TOPIC, Topics.GET_TOP_STORIES);
			mTopIntent.putExtra(SubscribeIntentService.STORAGE_NAME, Prefs.KEY_PUSH_TOPSTORIES);
			mTopIntent.putExtra(SubscribeIntentService.SUBSCRIBE_NAME, Topics.GET_TOP_STORIES);
		}
		startService(mTopIntent);
	}


	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mTopsRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mNewRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mAskRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mShowRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mJobsRegRecv);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mSumRegRecv);
		super.onDestroy();
	}

	private synchronized void updateSubscribing() {
		if (mTotal == 0) {
			mBinding.closeBtn.setVisibility(View.VISIBLE);
			mBinding.loadPb.setVisibility(View.GONE);
		} else {
			mTotal--;
		}
	}


}
