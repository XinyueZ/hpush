package com.hpush.app.fragments;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.hpush.R;
import com.hpush.app.adapters.DailiesListAdapter;
import com.hpush.bus.BookmarkMessageEvent;
import com.hpush.bus.BookmarkedEvent;
import com.hpush.bus.ShowActionBar;
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
public final class DailiesLstFragment extends BaseFragment implements ObservableScrollViewCallbacks {

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
	private ObservableRecyclerView mRv;
	/**
	 * {@link android.support.v7.widget.RecyclerView.Adapter} for the {@link #mRv}.
	 */
	private DailiesListAdapter mAdp;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.hpush.bus.BookmarkMessageEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.BookmarkMessageEvent}.
	 */
	public void onEvent(BookmarkMessageEvent e) {
		if(e.getMessageListItem() instanceof RecentListItem) {
			//Bookmark on the site self.
			bookmarkOneItem((RecentListItem) e.getMessageListItem());
		} else {
			//Bookmark from a webview shows details.
			List<RecentListItem> list = mAdp.getMessages();
			for(RecentListItem item : list) {
				if(item.getId() == e.getMessageListItem().getId()) {
					bookmarkOneItem(item);
					EventBus.getDefault().removeAllStickyEvents();
					break;
				}
			}
		}
	}

	//------------------------------------------------
	public static DailiesLstFragment newInstance(Context context) {
		return (DailiesLstFragment) DailiesLstFragment.instantiate(context, DailiesLstFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setErrorHandlerAvailable(false);

		mDB = DB.getInstance(getActivity().getApplication());
		mRv = (ObservableRecyclerView) view.findViewById(R.id.daily_rv);
		if(getResources().getBoolean(R.bool.landscape)) {
			mRv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
		} else {
			mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
		}
		mRv.setHasFixedSize(false);
		mRv.setScrollViewCallbacks(this);


		loadDailies();
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}



	/**
	 * Get all data.
	 */
	private void loadDailies() {
		AsyncTask<Void, List<RecentListItem>, List<RecentListItem>> task = new AsyncTask<Void, List<RecentListItem>, List<RecentListItem>>() {
			@Override
			protected List<RecentListItem> doInBackground(Void... params) {
				return doSearch();
			}

			@Override
			protected void onPostExecute(List<RecentListItem> dailies) {
				super.onPostExecute(dailies);
				if (mAdp == null) {
					mAdp = new DailiesListAdapter(dailies );
					mRv.setAdapter(mAdp);
				} else {
					mAdp.setMessages(dailies);
					mAdp.notifyDataSetChanged();
				}
			}
		};
		AsyncTaskCompat.executeParallel(task);
	}

	/**
	 *
	 * @return Data on the view.
	 */
	protected List<RecentListItem> doSearch() {
		return mDB.getDailies(Sort.DESC);
	}

	/**
	 * Bookmark one item.
	 *
	 * @param itemToBookmark
	 * 		The item to bookmark.
	 */
	private void bookmarkOneItem(final RecentListItem itemToBookmark) {
		AsyncTaskCompat.executeParallel(new AsyncTask<List<RecentListItem>, Void, Void>() {
			@Override
			protected Void doInBackground(
					List<RecentListItem>... params) {
				List<RecentListItem> data = params[0];
				for (RecentListItem obj:data) {
					if (obj.getId() == itemToBookmark.getId()) {
						mDB.removeMessage(obj == null ? null : obj.getMessage());
						mDB.addBookmark(obj.getMessage());
					}
				}
				itemToBookmark.setBookmarked(true);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				mAdp.notifyDataSetChanged();
				EventBus.getDefault().post(new BookmarkedEvent());
			}
		}, mAdp.getMessages());
	}

	@Override
	public void onScrollChanged(int i, boolean b, boolean b2) {

	}

	@Override
	public void onDownMotionEvent() {

	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		if (scrollState == ScrollState.UP) {
			EventBus.getDefault().post(new ShowActionBar(false));
		} else if (scrollState == ScrollState.DOWN) {
			EventBus.getDefault().post(new ShowActionBar(true));
		}
	}
}
