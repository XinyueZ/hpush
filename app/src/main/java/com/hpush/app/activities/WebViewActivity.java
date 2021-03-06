package com.hpush.app.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.FacebookException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.hpush.R;
import com.hpush.bus.BookmarkMessageEvent;
import com.hpush.bus.ShareMessageEvent;
import com.hpush.bus.ShareMessageEvent.Type;
import com.hpush.data.Message;
import com.hpush.data.MessageListItem;
import com.hpush.db.DB;
import com.hpush.utils.ActionProviderTinyUrl4JListener;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;
import com.hpush.views.WebViewEx;
import com.tinyurl4j.Api;

import de.greenrobot.event.EventBus;

/**
 * A WebView  .
 *
 * @author Xinyue Zhao
 */
public final class WebViewActivity extends BasicActivity implements DownloadListener {

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_webview;

	/**
	 * The menu to this view.
	 */
	private static final int    MENU       = R.menu.webview;
	/**
	 * Store link that the {@link android.webkit.WebView} opens.
	 */
	private static final String EXTRAS_URL = "com.hpush.app.activities.EXTRAS.Url";
	/**
	 * The message that contains the information that the {@link #mWebView} uses. It might be null.
	 */
	private static final String EXTRAS_MSG = "com.hpush.app.activities.EXTRAS.Msg";
	/**
	 * {@link android.webkit.WebView} shows homepage.
	 */
	private WebViewEx          mWebView;
	/**
	 * The "ActionBar".
	 */
	private Toolbar            mToolbar;
	/**
	 * Progress indicator.
	 */
	private SwipeRefreshLayout mRefreshLayout;
	/**
	 * The message that contains the information that the {@link #mWebView} uses. It might be null.
	 */
	private Message            msg;

	/**
	 * Show single instance of {@link WebViewActivity}.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param url
	 * 		Target to open.
	 * @param openWevViewV
	 * 		The view that open the {@link com.hpush.app.activities.WebViewActivity}.
	 * @param msg
	 * 		The message that contains the information that the {@link #mWebView} uses. It might be null.
	 */
	public static void showInstance( Activity cxt, String url, View openWevViewV, Message msg ) {
		Intent intent = new Intent(
				cxt,
				WebViewActivity.class
		);
		intent.putExtra(
				EXTRAS_URL,
				url
		);
		intent.putExtra(
				EXTRAS_MSG,
				msg
		);
		intent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP );
		//		if (openWevViewV != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		//			ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(cxt,
		//					Pair.create(openWevViewV, "openWevViewV"));
		//			cxt.startActivity(intent, transitionActivityOptions.toBundle());
		//		} else {
		cxt.startActivity( intent );
		//		}
	}


	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( LAYOUT );
		if( getResources().getBoolean( R.bool.landscape ) ) {
			setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
		}
		calcActionBarHeight();
		//Progress-indicator.
		mRefreshLayout = (SwipeRefreshLayout) findViewById( R.id.content_srl );
		mRefreshLayout.setColorSchemeResources( R.color.hacker_orange,
												R.color.hacker_orange_mid_deep,
												R.color.hacker_orange_deep,
												R.color.hacker_orange
		);

		mRefreshLayout.setOnRefreshListener( new OnRefreshListener() {
			@Override
			public void onRefresh() {

				mWebView.reload();
			}
		} );
		mToolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( mToolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );
		mWebView = (WebViewEx) findViewById( R.id.home_wv );

		mWebView.setDownloadListener( this );
		WebSettings settings = mWebView.getSettings();
		settings.setLoadWithOverviewMode( true );
		settings.setJavaScriptEnabled( true );
		settings.setLoadsImagesAutomatically( true );
		settings.setJavaScriptCanOpenWindowsAutomatically( true );
		settings.setSupportZoom( true );
		settings.setBuiltInZoomControls( false );
		settings.setDomStorageEnabled( true );
		settings.setRenderPriority( RenderPriority.HIGH );
		settings.setCacheMode( WebSettings.LOAD_NO_CACHE );
		mWebView.setWebViewClient( new WebViewClient() {
			@Override
			public void onPageStarted( WebView view, String url, android.graphics.Bitmap favicon ) {
				mRefreshLayout.setRefreshing( true );
			}

			@Override
			public void onPageFinished( WebView view, String url ) {
				mRefreshLayout.setRefreshing( false );
			}

			@Override
			public boolean shouldOverrideUrlLoading( WebView view, String url ) {
				view.loadUrl( url );
				return true;
			}
		} );
		handleIntent();

	}

	@Override
	protected void onNewIntent( Intent intent ) {
		super.onNewIntent( intent );
		setIntent( intent );
		handleIntent();
	}

	/**
	 * Handle the {@link android.content.Intent} of the {@link com.hpush.app.activities.WebViewActivity}.
	 */
	private void handleIntent() {
		Intent intent = getIntent();
		String url    = intent.getStringExtra( EXTRAS_URL );
		msg = (Message) intent.getSerializableExtra( EXTRAS_MSG );
		if( !TextUtils.isEmpty( url ) ) {
			mWebView.loadUrl( url );
		} else {
			mWebView.loadUrl( Prefs.getInstance( getApplication() )
								   .getHackerNewsHomeUrl() );
		}

	}

	@Override
	public boolean onCreateOptionsMenu( final Menu menu ) {
		getMenuInflater().inflate(
				MENU,
				menu
		);
		if( msg == null ) {
			menu.findItem( R.id.action_item_comment )
				.setVisible( false );
			menu.findItem( R.id.action_item_bookmark )
				.setVisible( false );
		}

		if( msg != null ) {
			AsyncTaskCompat.executeParallel( new AsyncTask<Void, Boolean, Boolean>() {
				@Override
				protected Boolean doInBackground( Void... params ) {
					msg = (Message) getIntent().getSerializableExtra( EXTRAS_MSG );
					DB db = DB.getInstance( getApplication() );
					return db.findBookmark( msg );
				}

				@Override
				protected void onPostExecute( Boolean found ) {
					super.onPostExecute( found );
					if( found ) {
						menu.findItem( R.id.action_item_bookmark )
							.setVisible( false );
					}
				}
			} );
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu( Menu menu ) {
		MenuItem menuShare = menu.findItem( R.id.action_item_share );
		android.support.v7.widget.ShareActionProvider provider
				= (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider( menuShare );
		if( msg != null ) {
			//Setting a share intent.
			String url = msg.getUrl();
			if( TextUtils.isEmpty( url ) ) {
				url = Prefs.getInstance( getApplication() )
						   .getHackerNewsCommentsUrl() + msg.getId();
			}
			Api.call(
					url,
					new ActionProviderTinyUrl4JListener( this,
														 provider,
														 R.string.lbl_share_item_title,
														 R.string.lbl_share_item_content,
														 msg,
														 url
					)
			);
		} else {
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
		}
		return super.onPrepareOptionsMenu( menu );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		String name, caption, desc, link;
		switch( item.getItemId() ) {
			case android.R.id.home:
				ActivityCompat.finishAfterTransition( this );
				break;
			case R.id.action_search:
				onSearchRequested();
				break;
			case R.id.action_forward:
				if( mWebView.canGoForward() ) {
					mWebView.goForward();
				}
				break;
			case R.id.action_backward:
				if( mWebView.canGoBack() ) {
					mWebView.goBack();
				}
				break;
			case R.id.action_item_comment:
				showInstance(
						this,
						Prefs.getInstance( getApplication() )
							 .getHackerNewsCommentsUrl() + msg.getId(),
						null,
						msg
				);
				break;
			case R.id.action_item_bookmark:
				EventBus.getDefault()
						.postSticky( new BookmarkMessageEvent( new MessageListItem( msg ) ) );
				Snackbar.make(
						mRefreshLayout,
						R.string.lbl_has_been_bookmarked,
						Snackbar.LENGTH_LONG
				)
						.show();
				item.setVisible( false );
				break;
			case R.id.action_facebook:
				if( msg == null ) {
					name = getString( R.string.lbl_share_item_title );
					caption = String.format(
							getString( R.string.lbl_share_app_title ),
							getString( R.string.lbl_share_item_title )
					);
					desc = getString( R.string.lbl_share_app_content );
					link = getString( R.string.lbl_app_link );

					Bundle postParams = new Bundle();
					final WebDialog fbDlg = new WebDialog.FeedDialogBuilder(
							this,
							getString( R.string.applicationId ),
							postParams
					).setCaption( caption )
					 .setName( name )
					 .setDescription( desc )
					 .setLink( link )
					 .build();
					fbDlg.setOnCompleteListener( new OnCompleteListener() {
						@Override
						public void onComplete( Bundle bundle, FacebookException e ) {
							fbDlg.dismiss();
						}
					} );
					fbDlg.show();
				} else {
					if( TextUtils.isEmpty( msg.getUrl() ) ) {
						msg.setUrl( Prefs.getInstance( getApplication() )
										 .getHackerNewsCommentsUrl() + msg.getId() );
					}
					EventBus.getDefault()
							.post( new ShareMessageEvent(
									new MessageListItem( msg ),
									Type.Facebook
							) );
				}
				break;
			case R.id.action_tweet:
				break;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	public void onDownloadStart( String url, String userAgent, String contentDisposition, String mimetype, long contentLength ) {
		Uri    uri    = Uri.parse( url );
		Intent intent = new Intent(
				Intent.ACTION_VIEW,
				uri
		);
		startActivity( intent );
	}


	@Override
	public void onBackPressed() {
		backPressed();
	}

	private void backPressed() {
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
			finishAfterTransition();
		} else {
			finish();
		}
	}
}
