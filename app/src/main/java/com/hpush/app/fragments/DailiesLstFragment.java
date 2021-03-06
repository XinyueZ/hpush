package com.hpush.app.fragments;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.hpush.R;
import com.hpush.app.adapters.DailiesListAdapter;
import com.hpush.bus.BookmarkMessageEvent;
import com.hpush.bus.BookmarkedEvent;
import com.hpush.bus.DeleteAllDailiesEvent;
import com.hpush.bus.FloatActionButtonEvent;
import com.hpush.bus.LoadedAllDailiesEvent;
import com.hpush.data.RecentListItem;
import com.hpush.db.DB;
import com.hpush.db.DB.Sort;
import com.hpush.utils.Prefs;

import de.greenrobot.event.EventBus;

/**
 * Show all {@link com.hpush.data.Recent}s.
 *
 * @author Xinyue Zhao
 */
public class DailiesLstFragment extends BaseFragment {

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_dailies_list;

	/**
	 * Database.
	 */
	private DB mDB;
	/**
	 * View shows all data.
	 */
	private android.support.v7.widget.RecyclerView mRv;
	/**
	 * {@link android.support.v7.widget.RecyclerView.Adapter} for the {@link #mRv}.
	 */
	private DailiesListAdapter mAdp;
	/**
	 * {@true} if the view can take all data to show.
	 */
	private boolean mDataCanBeShown;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.hpush.bus.BookmarkMessageEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.BookmarkMessageEvent}.
	 */
	public void onEvent( BookmarkMessageEvent e ) {
		if( e.getMessageListItem() instanceof RecentListItem ) {
			//Bookmark on the site self.
			bookmarkOneItem( (RecentListItem) e.getMessageListItem() );
		} else {
			//Bookmark from a webview shows details.
			List<RecentListItem> list = mAdp.getMessages();
			for( RecentListItem item : list ) {
				if( item.getId() == e.getMessageListItem()
									 .getId() ) {
					bookmarkOneItem( item );
					EventBus.getDefault()
							.removeAllStickyEvents();
					break;
				}
			}
		}
	}

	/**
	 * Handler for {@link com.hpush.bus.DeleteAllDailiesEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.DeleteAllDailiesEvent}.
	 */
	public void onEvent( DeleteAllDailiesEvent e ) {
		AsyncTaskCompat.executeParallel( new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground( Object... params ) {
				if( mAdp != null ) {
					mAdp.getMessages()
						.clear();
				}
				mDB.clearDailies();
				return null;
			}

			@Override
			protected void onPostExecute( Object o ) {
				super.onPostExecute( o );
				if( mAdp != null ) {
					mAdp.notifyDataSetChanged();
					ActivityCompat.finishAfterTransition( getActivity() );
				}
			}
		} );
	}

	//------------------------------------------------


	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		return inflater.inflate(
				LAYOUT,
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
		setErrorHandlerAvailable( false );
		mDataCanBeShown = true;

		mDB = DB.getInstance( getActivity().getApplication() );
		mRv = (android.support.v7.widget.RecyclerView) view.findViewById( R.id.daily_rv );
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


		loadDailies();
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance( getActivity().getApplication() );
	}


	/**
	 * Get all data.
	 */
	private void loadDailies() {
		AsyncTaskCompat.executeParallel( new AsyncTask<Void, List<RecentListItem>, List<RecentListItem>>() {
			@Override
			protected List<RecentListItem> doInBackground( Void... params ) {
				return doSearch();
			}

			@Override
			protected void onPostExecute( List<RecentListItem> items ) {
				super.onPostExecute( items );
				if( mDataCanBeShown ) {
					if( mAdp == null ) {
						mAdp = new DailiesListAdapter( items );
						mRv.setAdapter( mAdp );
					} else {
						mAdp.setMessages( items );
						mAdp.notifyDataSetChanged();
					}
				}
				EventBus.getDefault()
						.post( new LoadedAllDailiesEvent( items.size() ) );
			}
		} );
	}

	/**
	 * @return Data on the view.
	 */
	protected List<RecentListItem> doSearch() {
		return mDB.getDailies( Sort.DESC );
	}

	/**
	 * Bookmark one item.
	 *
	 * @param itemToBookmark
	 * 		The item to bookmark.
	 */
	private void bookmarkOneItem( final RecentListItem itemToBookmark ) {
		AsyncTaskCompat.executeParallel(
				new AsyncTask<List<RecentListItem>, Void, Void>() {
					@Override
					protected Void doInBackground( List<RecentListItem>... params ) {
						List<RecentListItem> data = params[ 0 ];
						for( RecentListItem obj : data ) {
							if( obj.getId() == itemToBookmark.getId() ) {
								mDB.removeMessage( obj == null ? null : obj.getMessage() );
								mDB.addBookmark( obj.getMessage() );
							}
						}
						itemToBookmark.setBookmarked( true );
						return null;
					}

					@Override
					protected void onPostExecute( Void aVoid ) {
						super.onPostExecute( aVoid );
						mAdp.notifyDataSetChanged();
						EventBus.getDefault()
								.post( new BookmarkedEvent() );
					}
				},
				mAdp.getMessages()
		);
	}


	/**
	 * @return The database.
	 */
	protected DB getDB() {
		return mDB;
	}

	@Override
	public void onDestroyView() {
		mDataCanBeShown = false;
		super.onDestroyView();
	}
}
