package com.hpush.app.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.hpush.R;
import com.hpush.app.activities.SettingActivity;
import com.hpush.app.adapters.MessagesListAdapter;
import com.hpush.bus.BookmarkAllEvent;
import com.hpush.bus.BookmarkMessageEvent;
import com.hpush.bus.BookmarkedEvent;
import com.hpush.bus.FloatActionButtonEvent;
import com.hpush.bus.LoadAllEvent;
import com.hpush.bus.RemoveAllEvent;
import com.hpush.bus.RemoveAllEvent.WhichPage;
import com.hpush.bus.SortAllEvent;
import com.hpush.data.Message;
import com.hpush.data.MessageListItem;
import com.hpush.data.SyncList;
import com.hpush.db.DB;
import com.hpush.db.DB.Sort;
import com.hpush.gcm.SyncTask;
import com.hpush.utils.Prefs;

import de.greenrobot.event.EventBus;

/**
 * Show saved messages.
 *
 * @author Xinyue Zhao
 */
public class MessagesListFragment extends BaseFragment {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_messages_list;

	/**
	 * Menu on toolbar.
	 */
	private static final int TOOLBAR_MENU = R.menu.item;

	/**
	 * A list to show all messages.
	 */
	private RecyclerView        mRv;
	/**
	 * {@link android.support.v7.widget.RecyclerView.Adapter} for the {@link #mRv}.
	 */
	private MessagesListAdapter mAdp;
	/**
	 * Application's database.
	 */
	private DB                  mDB;
	/**
	 * Indicator for empty data.
	 */
	private View                mEmptyV;
	/**
	 * Indicator for empty data.
	 */
	private View                mEmpty2V;
	/**
	 * Refresh view.
	 */
	private SwipeRefreshLayout  mSwipeRefreshLayout;
	/**
	 * {@true} if the view can take all data to show.
	 */
	private boolean             mDataCanBeShown;
	/**
	 * {@true} if request is running.
	 */
	private boolean             mInProgress;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------


	/**
	 * Handler for {@link SortAllEvent}.
	 *
	 * @param e
	 * 		Event {@link SortAllEvent}.
	 */
	public void onEvent( SortAllEvent e ) {
		loadMessages();
	}

	/**
	 * Handler for {@link SyncList}.
	 *
	 * @param e
	 * 		Event {@link SyncList}.
	 */
	public void onEvent( SyncList e ) {
		AsyncTask<SyncList, Void, Void> task = new AsyncTask<SyncList, Void, Void>() {
			@Override
			protected Void doInBackground( SyncList... params ) {
				Activity activity = getActivity();
				if( activity != null ) {
					SyncList      syncList = params[ 0 ];
					List<Message> msgs     = syncList.getSyncList();
					DB            db       = DB.getInstance( activity.getApplication() );
					for( Message msg : msgs ) {
						syncDB(
								db,
								msg
						);
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute( Void aVoid ) {
				super.onPostExecute( aVoid );
				loadMessages();


				if( getWhichPage() == WhichPage.Messages ) {
					mSwipeRefreshLayout.setRefreshing( false );
				}

				mInProgress = false;
			}
		};
		AsyncTaskCompat.executeParallel(
				task,
				e
		);
	}


	/**
	 * Handler for {@link LoadAllEvent}.
	 *
	 * @param e
	 * 		Event {@link LoadAllEvent}.
	 */
	public void onEvent( LoadAllEvent e ) {
		loadMessages();
	}

	/**
	 * Handler for {@link RemoveAllEvent}.
	 *
	 * @param e
	 * 		Event {@link RemoveAllEvent}.
	 */
	public void onEvent( RemoveAllEvent e ) {
		if( mAdp == null || mAdp.getMessages() == null || mAdp.getMessages()
															  .size() == 0 ) {
			return;
		}
		if( getWhichPage() != e.getWhichPage() ) {
			return;
		}
		List<MessageListItem> items       = mAdp.getMessages();
		boolean               hasSelected = false;


		for( MessageListItem obj : items ) {
			if( obj.isChecked() ) {
				hasSelected = true;
				break;
			}
		}
		if( hasSelected ) {
			removeSelectedItems();
		} else {
			Activity activity = getActivity();
			if( activity != null ) {
				new android.support.v7.app.AlertDialog.Builder( activity ).setTitle( R.string.application_name )
																		  .setMessage( R.string.msg_remove_all )
																		  .setCancelable( false )
																		  .setPositiveButton(
																				  R.string.lbl_yes,
																				  new DialogInterface.OnClickListener() {
																					  @Override
																					  public void onClick( DialogInterface dialog,
																										   int which
																					  ) {
																						  removeAllItems();
																					  }
																				  }
																		  )
																		  .setNegativeButton(
																				  R.string.lbl_no,
																				  null
																		  )
																		  .create()
																		  .show();
			}
		}
	}

	/**
	 * Handler for {@link BookmarkAllEvent}.
	 *
	 * @param e
	 * 		Event {@link BookmarkAllEvent}.
	 */
	public void onEvent( BookmarkAllEvent e ) {
		if( mAdp == null || mAdp.getMessages() == null || mAdp.getMessages()
															  .size() == 0 ) {
			return;
		}
		bookmarkSelectedItems();
	}

	/**
	 * Handler for {@link BookmarkMessageEvent}.
	 *
	 * @param e
	 * 		Event {@link BookmarkMessageEvent}.
	 */
	public void onEvent( BookmarkMessageEvent e ) {
		if( mAdp == null || mAdp.getMessages() == null || mAdp.getMessages()
															  .size() == 0 ) {
			return;
		}
		final MessageListItem itemToBookmark = e.getMessageListItem();
		bookmarkOneItem( itemToBookmark );
		EventBus.getDefault()
				.removeAllStickyEvents();
	}

	/**
	 * Handler for {@link VolleyError}.
	 *
	 * @param e
	 * 		Event {@link VolleyError}.
	 */
	public void onEvent( VolleyError e ) {
		if( mSwipeRefreshLayout != null ) {
			mSwipeRefreshLayout.setRefreshing( false );
		}
		mInProgress = false;
	}

	//------------------------------------------------
	public static MessagesListFragment newInstance( Context context ) {
		return (MessagesListFragment) MessagesListFragment.instantiate(
				context,
				MessagesListFragment.class.getName()
		);
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		return inflater.inflate(
				getLayoutResId(),
				container,
				false
		);
	}

	@Override
	public void onViewCreated( View view, Bundle savedInstanceState ) {
		super.onViewCreated(
				view,
				savedInstanceState
		);
		mDataCanBeShown = true;
		mEmptyV = view.findViewById( R.id.empty_ll );
		mEmpty2V = view.findViewById( R.id.empty_ll_2 );
		if( getWhichPage() == WhichPage.Messages ) {
			mEmpty2V.findViewById( R.id.open_setting_btn )
					.setOnClickListener( new OnClickListener() {
						@Override
						public void onClick( View v ) {
							SettingActivity.showInstance(
									getActivity(),
									v
							);
						}
					} );
			mEmpty2V.findViewById( R.id.sync_ii_btn )
					.setOnClickListener( new OnClickListener() {
						@Override
						public void onClick( View v ) {
							mSwipeRefreshLayout.setRefreshing( true );
							sync( false );
						}
					} );
			mEmptyV.findViewById( R.id.sync_i_btn )
				   .setOnClickListener( new OnClickListener() {
					   @Override
					   public void onClick( View v ) {
						   mSwipeRefreshLayout.setRefreshing( true );
						   sync( false );
					   }
				   } );
		}
		mDB = DB.getInstance( getActivity().getApplication() );
		mRv = (RecyclerView) view.findViewById( R.id.msg_rv );
		if( getResources().getBoolean( R.bool.landscape ) ) {
			mRv.setLayoutManager( new StaggeredGridLayoutManager(
					4,
					StaggeredGridLayoutManager.VERTICAL
			) );
		} else {
			mRv.setLayoutManager( new LinearLayoutManager( getActivity() ) );
		}
		mRv.setHasFixedSize( false );
		mRv.addOnScrollListener( new OnScrollListener() {
			@Override
			public void onScrolled( RecyclerView recyclerView, int dx, int dy ) {
				float y = ViewCompat.getY( recyclerView );
				if( y < dy ) {
					EventBus.getDefault()
							.post( new FloatActionButtonEvent( true ) );
				} else {
					EventBus.getDefault()
							.post( new FloatActionButtonEvent( false ) );
				}
			}
		} );
		if( getWhichPage() == WhichPage.Messages ) {
			mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById( R.id.content_srl );
			mSwipeRefreshLayout.setColorSchemeResources( R.color.hacker_orange,
														 R.color.hacker_orange_mid_deep,
														 R.color.hacker_orange_deep,
														 R.color.hacker_orange
			);

			mSwipeRefreshLayout.setOnRefreshListener( new OnRefreshListener() {
				@Override
				public void onRefresh() {
					sync( true );
				}
			} );
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		loadMessages();
		testEmpty();
	}

	@Override
	public void onDestroyView() {
		mDataCanBeShown = false;
		super.onDestroyView();
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance( getActivity().getApplication() );
	}

	/**
	 * Test whether data is empty not, then shows a message on UI.
	 */
	private void testEmpty() {
		if( getWhichPage() == WhichPage.Messages ) {
			mEmptyV.setVisibility( View.GONE );
			mEmpty2V.setVisibility( View.GONE );
			Activity activity = getActivity();
			if( activity != null ) {
				if( !TextUtils.isEmpty( Prefs.getInstance( activity.getApplication() )
											 .getPushRegId() ) ) {
					mEmptyV.setVisibility( mAdp == null || mAdp.getItemCount() == 0 ? View.VISIBLE : View.GONE );
				} else {
					mEmpty2V.setVisibility( mAdp == null || mAdp.getItemCount() == 0 ? View.VISIBLE : View.GONE );
				}
			}
		}
	}

	/**
	 * Load all messages.
	 */
	protected void loadMessages() {
		AsyncTask<Void, List<MessageListItem>, List<MessageListItem>> task = new AsyncTask<Void, List<MessageListItem>, List<MessageListItem>>() {
			@Override
			protected List<MessageListItem> doInBackground( Void... params ) {
				return fetchDataFromDB();
			}

			@Override
			protected void onPostExecute( List<MessageListItem> data ) {
				super.onPostExecute( data );
				if( mDataCanBeShown ) {
					if( mAdp == null ) {
						mAdp = new MessagesListAdapter(
								data,
								getToolbarMenuId()
						);
						mRv.setAdapter( mAdp );
					} else {
						mAdp.setMessages( data );
						mAdp.notifyDataSetChanged();
					}
					testEmpty();
				}
			}
		};
		AsyncTaskCompat.executeParallel( task );
	}


	/**
	 * Remove items that have been selected.
	 */
	private void removeSelectedItems() {
		AsyncTask<List<MessageListItem>, Void, Void> task = new AsyncTask<List<MessageListItem>, Void, Void>() {
			@Override
			protected Void doInBackground( List<MessageListItem>... params ) {
				List<MessageListItem> data    = params[ 0 ];
				List<MessageListItem> rmvData = new ArrayList<>();

				for( MessageListItem obj : data ) {
					if( obj.isChecked() ) {
						deleteDataOnDB( obj );
						rmvData.add( obj );
					}
				}
				for( MessageListItem rd : rmvData ) {
					data.remove( rd );
				}
				return null;
			}

			@Override
			protected void onPostExecute( Void data ) {
				super.onPostExecute( data );
				mAdp.notifyDataSetChanged();
				testEmpty();
			}
		};
		AsyncTaskCompat.executeParallel(
				task,
				mAdp.getMessages()
		);
	}


	/**
	 * Remove all items.
	 */
	private void removeAllItems() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground( Void... params ) {
				deleteDataOnDB( null );
				mAdp.setMessages( null );
				return null;
			}

			@Override
			protected void onPostExecute( Void data ) {
				super.onPostExecute( data );
				mAdp.notifyDataSetChanged();
				testEmpty();
			}
		};
		AsyncTaskCompat.executeParallel( task );
	}


	/**
	 * Bookmark items that have been selected.
	 */
	private void bookmarkSelectedItems() {
		AsyncTask<List<MessageListItem>, List<MessageListItem>, List<MessageListItem>> task
				= new AsyncTask<List<MessageListItem>, List<MessageListItem>, List<MessageListItem>>() {
			@Override
			protected List<MessageListItem> doInBackground( List<MessageListItem>... params ) {
				List<MessageListItem> data    = params[ 0 ];
				List<MessageListItem> rmvData = new ArrayList<>();

				for( MessageListItem obj : data ) {
					if( obj.isChecked() ) {
						deleteDataOnDB( obj );
						rmvData.add( obj );
					}
				}
				for( MessageListItem rd : rmvData ) {
					data.remove( rd );
				}
				return rmvData;
			}

			@Override
			protected void onPostExecute( List<MessageListItem> rmvData ) {
				super.onPostExecute( rmvData );
				mAdp.notifyDataSetChanged();

				for( MessageListItem obj : rmvData ) {
					mDB.addBookmark( obj.getMessage() );
				}

				EventBus.getDefault()
						.post( new BookmarkedEvent() );
			}
		};
		AsyncTaskCompat.executeParallel(
				task,
				mAdp.getMessages()
		);
	}

	/**
	 * Bookmark one item.
	 *
	 * @param itemToBookmark
	 * 		The item to bookmark.
	 */
	private void bookmarkOneItem( final MessageListItem itemToBookmark ) {
		AsyncTask<List<MessageListItem>, List<MessageListItem>, List<MessageListItem>> task
				= new AsyncTask<List<MessageListItem>, List<MessageListItem>, List<MessageListItem>>() {
			@Override
			protected List<MessageListItem> doInBackground( List<MessageListItem>... params ) {
				List<MessageListItem> data    = params[ 0 ];
				List<MessageListItem> rmvData = new ArrayList<>();
				for( MessageListItem obj : data ) {
					if( obj.getId() == itemToBookmark.getId() ) {
						deleteDataOnDB( obj );
						rmvData.add( obj );
					}
				}
				for( MessageListItem rd : rmvData ) {
					data.remove( rd );
				}
				return rmvData;
			}

			@Override
			protected void onPostExecute( List<MessageListItem> rmvData ) {
				super.onPostExecute( rmvData );
				mAdp.notifyDataSetChanged();

				for( MessageListItem obj : rmvData ) {
					mDB.addBookmark( obj.getMessage() );
				}

				EventBus.getDefault()
						.post( new BookmarkedEvent() );
			}
		};
		AsyncTaskCompat.executeParallel(
				task,
				mAdp.getMessages()
		);
	}

	/**
	 * @return Application's database.
	 */
	protected DB getDB() {
		return mDB;
	}

	/**
	 * Get data from application's database.
	 *
	 * @return List of all data from DB.
	 */
	protected List<MessageListItem> fetchDataFromDB() {
		return mDB.getMessages( Sort.DESC );
	}

	/**
	 * Delete one item on database.
	 *
	 * @param obj
	 * 		The item to delete.
	 */
	protected void deleteDataOnDB( MessageListItem obj ) {
		mDB.removeMessage( obj == null ? null : obj.getMessage() );
	}


	/**
	 * @return Menu on toolbar.
	 */
	protected int getToolbarMenuId() {
		return TOOLBAR_MENU;
	}

	/**
	 * @return Define the command whom to do remove.
	 */
	protected WhichPage getWhichPage() {
		return WhichPage.Messages;
	}

	/**
	 * @return Layout id.
	 */
	protected int getLayoutResId() {
		return LAYOUT;
	}

	/**
	 * Sync DB when sync has been fired, see {@link #sync(boolean)}.
	 *
	 * @param db
	 * 		The instance of {@link com.hpush.db.DB}.
	 * @param message
	 * 		The msg one of the sync-list.
	 */
	protected void syncDB( DB db, Message message ) {
		boolean foundMsg      = db.findMessage( message );
		boolean foundBookmark = db.findBookmark( message );
		if( !foundMsg && !foundBookmark ) {//To test whether in our local database or not.
			//Save in database.
			db.addMessage( message );
		} else {
			if( foundMsg ) {
				db.updateMessage( message );
			} else {
				db.updateBookmark( message );
			}
		}
	}


	/**
	 * Sync data from backend and refresh DB, see {@link #syncDB(com.hpush.db.DB , com.hpush.data.Message)}.
	 *
	 * @param handlingDelayIndicator
	 * 		{@true} if the request too delay and dismiss indicator automatically.
	 */
	private void sync( boolean handlingDelayIndicator ) {
		Activity activity = getActivity();
		if( activity != null && !mInProgress ) {
			mInProgress = true;
			SyncTask.sync( activity.getApplication() );
			if( handlingDelayIndicator ) {
				Prefs prefs = (Prefs) getPrefs();
				mHandler.postDelayed(
						new Runnable() {
							@Override
							public void run() {
								if( mSwipeRefreshLayout != null ) {
									mSwipeRefreshLayout.setRefreshing( false );
								}
							}
						},
						prefs.getSyncRetry() * 1000
				);
			}
		}

	}

	private android.os.Handler mHandler = new android.os.Handler();

	@Override
	protected void onReload() {
		if( getWhichPage() == WhichPage.Messages ) {
			sync( true );
		}
	}
}
