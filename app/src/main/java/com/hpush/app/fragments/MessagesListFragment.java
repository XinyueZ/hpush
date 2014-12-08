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
import com.hpush.data.MessageListItem;
import com.hpush.db.DB;
import com.hpush.db.DB.Sort;
import com.hpush.utils.Prefs;

/**
 * Show saved messages.
 *
 * @author Xinyue Zhao
 */
public final class MessagesListFragment extends BaseFragment {
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

	public static MessagesListFragment newInstance(Context context) {
		return (MessagesListFragment) MessagesListFragment.instantiate(context, MessagesListFragment.class.getName());
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
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
	private void loadMessages() {
		AsyncTask<Void, LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>> task =
				new AsyncTask<Void, LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>>() {
					@Override
					protected LongSparseArray<MessageListItem> doInBackground(Void... params) {
						return mDB.getMessages(Sort.DESC);
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
}
