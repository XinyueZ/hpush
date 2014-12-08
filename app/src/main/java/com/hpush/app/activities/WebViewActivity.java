package com.hpush.app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.hpush.R;
import com.hpush.utils.Prefs;
import com.hpush.views.WebViewEx;
import com.hpush.views.WebViewEx.OnWebViewExScrolledListener;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * A WebView to the homepage of SchautUp.
 *
 * @author Xinyue Zhao
 */
public final class WebViewActivity extends BaseActivity implements DownloadListener {

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_webview;

	/**
	 * The menu to this view.
	 */
	private static final int MENU = R.menu.webview;

	/**
	 * {@link android.webkit.WebView} shows homepage.
	 */
	private WebViewEx mWebView;
	/**
	 * The "ActionBar".
	 */
	private Toolbar mToolbar;
	/**
	 * Progress indicator.
	 */
	private SwipeRefreshLayout mRefreshLayout;
	/**
	 * The height of actionbar.
	 */
	private int mActionBarHeight;
	/**
	 * Show single instance of {@link WebViewActivity}.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	public static void showInstance(Context cxt) {
		Intent intent = new Intent(cxt, WebViewActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getApplication());
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);
		calcActionBarHeight();
		//Progress-indicator.
		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.hacker_orange, R.color.hacker_orange_mid_deep, R.color.hacker_orange_deep, R.color.hacker_orange);
		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				animToolActionBar(-mActionBarHeight * 4);
				mWebView.reload();
			}
		});

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mWebView = (WebViewEx) findViewById(R.id.home_wv);
		mWebView.setOnWebViewExScrolledListener(new OnWebViewExScrolledListener() {
			@Override
			public void onScrollChanged(boolean isUp) {
				if(isUp) {
					animToolActionBar(0);
				} else {
					animToolActionBar(-mActionBarHeight * 4);
				}
			}

			@Override
			public void onScrolledTop() {
				animToolActionBar(-mActionBarHeight * 4);
			}
		});
		mWebView.setDownloadListener(this);
		WebSettings settings = mWebView.getSettings();
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setCacheMode(WebSettings.LOAD_NORMAL);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(false);
		settings.setDomStorageEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
				mRefreshLayout.setRefreshing(true);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				mRefreshLayout.setRefreshing(false);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		mWebView.loadUrl("http://g.cn");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(MENU, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forward:
			if(mWebView.canGoForward()) mWebView.goForward();
			break;
		case R.id.action_backward:
			if(mWebView.canGoBack()) mWebView.goBack();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
			long contentLength) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}


	/**
	 * Animation and moving actionbar(toolbar).
	 *
	 * @param value
	 * 		The property value of animation.
	 */
	private void animToolActionBar(float value) {
		ViewPropertyAnimator animator = ViewPropertyAnimator.animate(mToolbar);
		animator.translationY(value).setDuration(400);
	}


	/**
	 * Calculate height of actionbar.
	 */
	private void calcActionBarHeight() {
		int[] abSzAttr;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			abSzAttr = new int[] { android.R.attr.actionBarSize };
		} else {
			abSzAttr = new int[] { R.attr.actionBarSize };
		}
		TypedArray a = obtainStyledAttributes(abSzAttr);
		mActionBarHeight = a.getDimensionPixelSize(0, -1);
	}

}
