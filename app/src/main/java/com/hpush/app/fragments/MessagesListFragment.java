package com.hpush.app.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.hpush.R;
import com.hpush.app.adapters.MessagesListAdapter;
import com.hpush.bus.BookmarkAllEvent;
import com.hpush.bus.BookmarkedEvent;
import com.hpush.bus.RemoveAllEvent;
import com.hpush.data.MessageListItem;
import com.hpush.db.DB;
import com.hpush.db.DB.Sort;
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
	 * A list to show all messages.
	 */
	private RecyclerView mRv;



	/**
	 * {@link android.support.v7.widget.RecyclerView.Adapter} for the {@link #mRv}.
	 */
	private MessagesListAdapter mAdp;


	/**
	 * Application's database.
	 */
	private DB mDB;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link RemoveAllEvent}.
	 *
	 * @param e
	 * 		Event {@link RemoveAllEvent}.
	 */
	public void onEvent(RemoveAllEvent e) {
		if (mAdp == null || mAdp.getMessages() == null || mAdp.getMessages().size() == 0) {
			return;
		}
		removeSelectedItems();

	}

	/**
	 * Handler for {@link BookmarkAllEvent}.
	 *
	 * @param e
	 * 		Event {@link BookmarkAllEvent}.
	 */
	public void onEvent(BookmarkAllEvent e) {
		if (mAdp == null || mAdp.getMessages() == null || mAdp.getMessages().size() == 0) {
			return;
		}
		bookmarkSelectedItems();
	}

	//------------------------------------------------
	public static MessagesListFragment newInstance(Context context) {
		return (MessagesListFragment) MessagesListFragment.instantiate(context, MessagesListFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(getLayoutID(), container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mDB = DB.getInstance(getActivity().getApplication());
		mRv = (RecyclerView) view.findViewById(R.id.msg_rv);
		mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
		loadMessages();
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}

	/**
	 * Load all messages.
	 */
	protected void loadMessages() {
		AsyncTask<Void, LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>> task =
				new AsyncTask<Void, LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>>() {
					@Override
					protected LongSparseArray<MessageListItem> doInBackground(Void... params) {
						return fetchDataFromDB();
					}

					@Override
					protected void onPostExecute(LongSparseArray<MessageListItem> data) {
						super.onPostExecute(data);
						if (mAdp == null) {
							mAdp = new MessagesListAdapter(data);
							mRv.setAdapter(mAdp);
						} else {
							mAdp.setMessages(data);
							mAdp.notifyDataSetChanged();
						}

					}
				};
		AsyncTaskCompat.executeParallel(task);
	}



	/**
	 * Remove items that have been selected.
	 */
	private void removeSelectedItems() {
		AsyncTask<LongSparseArray<MessageListItem>, Void, Void> task =
				new AsyncTask<LongSparseArray<MessageListItem>, Void, Void>() {
					@Override
					protected Void doInBackground(LongSparseArray<MessageListItem>... params) {
						LongSparseArray<MessageListItem> data = params[0];
						LongSparseArray<MessageListItem> rmvData = new LongSparseArray<>();
						long key;
						for (int i = 0; i < data.size(); i++) {
							key = data.keyAt(i);
							MessageListItem obj = data.get(key);
							if (obj.isChecked()) {
								deleteDataOnDB(obj);
								rmvData.put(key, obj);
							}
						}
						for (int i = 0; i < rmvData.size(); i++) {
							key = rmvData.keyAt(i);
							data.remove(key);
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void data) {
						super.onPostExecute(data);
						mAdp.notifyDataSetChanged();
					}
				};
		AsyncTaskCompat.executeParallel(task, mAdp.getMessages());
	}

	/**
	 * Bookmark items that have been selected.
	 */
	private void bookmarkSelectedItems() {
		AsyncTask<LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem> , LongSparseArray<MessageListItem> > task =
				new AsyncTask<LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem> , LongSparseArray<MessageListItem> >() {
					@Override
					protected LongSparseArray<MessageListItem>  doInBackground(LongSparseArray<MessageListItem>... params) {
						LongSparseArray<MessageListItem> data = params[0];
						LongSparseArray<MessageListItem> rmvData = new LongSparseArray<>();
						long key;
						for (int i = 0; i < data.size(); i++) {
							key = data.keyAt(i);
							MessageListItem obj = data.get(key);
							if (obj.isChecked()) {
								deleteDataOnDB(obj);
								rmvData.put(key, obj);
							}
						}
						for (int i = 0; i < rmvData.size(); i++) {
							key = rmvData.keyAt(i);
							data.remove(key);
						}
						return rmvData;
					}

					@Override
					protected void onPostExecute(LongSparseArray<MessageListItem>  rmvData) {
						super.onPostExecute(rmvData);
						mAdp.notifyDataSetChanged();

						long key;
						for (int i = 0; i < rmvData.size(); i++) {
							key = rmvData.keyAt(i);
							MessageListItem obj = rmvData.get(key);
							mDB.addBookmark(obj.getMessage());
						}

						EventBus.getDefault().post(new BookmarkedEvent());
					}
				};
		AsyncTaskCompat.executeParallel(task, mAdp.getMessages());
	}

	/**
	 *
	 * @return Application's database.
	 */
	protected DB getDB() {
		return mDB;
	}

	/**
	 * Get data from application's database.
	 * @return List of all data from DB.
	 */
	protected LongSparseArray<MessageListItem> fetchDataFromDB() {
		return mDB.getMessages(Sort.DESC);
	}

	/**
	 * Delete one item on database.
	 * @param obj The item to delete.
	 */
	protected void deleteDataOnDB(MessageListItem obj) {
		mDB.removeMessage(obj.getMessage());
	}

	/**
	 *
	 * @return  View's layout.
	 */
	protected int getLayoutID() {
		return LAYOUT;
	}

}
