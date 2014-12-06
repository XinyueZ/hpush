package com.hpush.app.fragments;

import android.content.Context;
import android.os.Bundle;
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
import com.hpush.data.Message;
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

	private RecyclerView mRv;

	private MessagesListAdapter mAdp;

	public static MessagesListFragment newInstance(Context context) {
		return (MessagesListFragment) MessagesListFragment.instantiate(context, MessagesListFragment.class.getName());
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		//Test data
		LongSparseArray<Message> data = new LongSparseArray<>();
		data.put(1, new Message(null, 0, 0, "Hello 1", 0, "World 1", "", 0));
		data.put(2, new Message(null, 0, 0, "Hello 2", 0, "World 2", "", 0));
		data.put(3, new Message(null, 0, 0, "Hello 3", 0, "World 3", "", 0));
		data.put(4, new Message(null, 0, 0, "Hello 4", 0, "World 4", "", 0));
		data.put(5, new Message(null, 0, 0, "Hello 5", 0, "World 5", "", 0));
		data.put(6, new Message(null, 0, 0, "Hello 6", 0, "World 6", "", 0));
		data.put(7, new Message(null, 0, 0, "Hello 7", 0, "World 7", "", 0));
		data.put(8, new Message(null, 0, 0, "Hello 8", 0, "World 8", "", 0));
		data.put(9, new Message(null, 0, 0, "Hello 9", 0, "World 9", "", 0));
		data.put(10, new Message(null, 0, 0, "Hello 10", 0, "World 10", "", 0));
		data.put(11, new Message(null, 0, 0, "Hello 11", 0, "World 11", "", 0));
		data.put(12, new Message(null, 0, 0, "Hello 12", 0, "World 12", "", 0));
		data.put(13, new Message(null, 0, 0, "Hello 13", 0, "World 13", "", 0));
		data.put(14, new Message(null, 0, 0, "Hello 14", 0, "World 14", "", 0));
		data.put(15, new Message(null, 0, 0, "Hello 15", 0, "World 15", "", 0));
		data.put(16, new Message(null, 0, 0, "Hello 16", 0, "World 16", "", 0));
		data.put(17, new Message(null, 0, 0, "Hello 17", 0, "World 17", "", 0));


		mRv = (RecyclerView) view.findViewById(R.id.msg_rv);
		mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
		mAdp = new MessagesListAdapter(data);
		mRv.setAdapter(mAdp);
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}
}
