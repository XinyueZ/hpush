package com.hpush.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.hpush.R;
import com.hpush.app.adapters.MessagesListAdapter;
import com.hpush.bus.BookmarkAllEvent;
import com.hpush.bus.BookmarkMessageEvent;
import com.hpush.bus.BookmarkedEvent;
import com.hpush.bus.LoadAllEvent;
import com.hpush.bus.RemoveAllEvent;
import com.hpush.bus.RemoveAllEvent.WhichPage;
import com.hpush.bus.UpdateCurrentTotalMessagesEvent;
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
	 * Menu on toolbar.
	 */
	private static final int TOOLBAR_MENU = R.menu.item;

	/**
	 * A list to show all messages.
	 */
	private ObservableRecyclerView mRv;
	/**
	 * {@link android.support.v7.widget.RecyclerView.Adapter} for the {@link #mRv}.
	 */
	private MessagesListAdapter mAdp;
	/**
	 * Application's database.
	 */
	private DB mDB;
	/**
	 * Indicator for empty data.
	 */
	private View mEmptyV;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link LoadAllEvent}.
	 *
	 * @param e
	 * 		Event {@link LoadAllEvent}.
	 */
	public void onEvent(LoadAllEvent e) {
		loadMessages();
	}

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
		if(getWhichPage() != e.getWhichPage()) {
			return;
		}
		LongSparseArray<MessageListItem> items = mAdp.getMessages();
		boolean hasSelected = false;

		long key;
		MessageListItem obj;
		for (int i = 0; i < items.size(); i++) {
			key = items.keyAt(i);
			obj = items.get(key);
			if (obj.isChecked()) {
				hasSelected = true;
				break;
			}
		}
		if (hasSelected) {
			removeSelectedItems();
		} else {
			new AlertDialog.Builder(getActivity()).setTitle(R.string.application_name).setMessage(
					R.string.msg_remall_all).setCancelable(false).setPositiveButton(R.string.lbl_yes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeAllItems();
						}
					}).setNeutralButton(R.string.lbl_no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Do nothing at moment.
				}
			}).create().show();
		}
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

	/**
	 * Handler for {@link BookmarkMessageEvent}.
	 *
	 * @param e
	 * 		Event {@link BookmarkMessageEvent}.
	 */
	public void onEvent(BookmarkMessageEvent e) {
		if (mAdp == null || mAdp.getMessages() == null || mAdp.getMessages().size() == 0) {
			return;
		}
		final MessageListItem itemToBookmark = e.getMessageListItem();
		bookmarkOneItem(itemToBookmark);
		EventBus.getDefault().removeAllStickyEvents();
	}


	//------------------------------------------------
	public static MessagesListFragment newInstance(Context context) {
		return (MessagesListFragment) MessagesListFragment.instantiate(context, MessagesListFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mEmptyV = view.findViewById(R.id.empty_ll);
		mDB = DB.getInstance(getActivity().getApplication());
		mRv = (ObservableRecyclerView) view.findViewById(R.id.msg_rv);
		mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRv.setHasFixedSize(false);
		Activity parentActivity = getActivity();
		if (parentActivity instanceof ObservableScrollViewCallbacks) {
			final int initialPosition = 0;
			ViewTreeObserver vto = mRv.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
						mRv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					} else {
						mRv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					mRv.scrollVerticallyToPosition(initialPosition);
				}
			});

			mRv.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		loadMessages();
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}

	/**
	 * Test whether data is empty not, then shows a message on UI.
	 */
	private void testEmpty() {
		if(getWhichPage() == WhichPage.Messages) {
			mEmptyV.setVisibility(mAdp.getItemCount() == 0 ? View.VISIBLE : View.GONE);
		}
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
							mAdp = new MessagesListAdapter(data, getToolbarMenuId());
							mRv.setAdapter(mAdp);
						} else {
							mAdp.setMessages(data);
							mAdp.notifyDataSetChanged();
						}
						testEmpty();
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
						testEmpty();
						EventBus.getDefault().post(new UpdateCurrentTotalMessagesEvent());
					}
				};
		AsyncTaskCompat.executeParallel(task, mAdp.getMessages());
	}


	/**
	 * Remove all items.
	 */
	private void removeAllItems() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				deleteDataOnDB(null);
				mAdp.setMessages(null);
				return null;
			}

			@Override
			protected void onPostExecute(Void data) {
				super.onPostExecute(data);
				mAdp.notifyDataSetChanged();
				testEmpty();
				EventBus.getDefault().post(new UpdateCurrentTotalMessagesEvent());
			}
		};
		AsyncTaskCompat.executeParallel(task);
	}


	/**
	 * Bookmark items that have been selected.
	 */
	private void bookmarkSelectedItems() {
		AsyncTask<LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>>
				task =
				new AsyncTask<LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>>() {
					@Override
					protected LongSparseArray<MessageListItem> doInBackground(
							LongSparseArray<MessageListItem>... params) {
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
					protected void onPostExecute(LongSparseArray<MessageListItem> rmvData) {
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
	 * Bookmark one item.
	 *
	 * @param itemToBookmark
	 * 		The item to bookmark.
	 */
	private void bookmarkOneItem(final MessageListItem itemToBookmark) {
		AsyncTask<LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>>
				task =
				new AsyncTask<LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>, LongSparseArray<MessageListItem>>() {
					@Override
					protected LongSparseArray<MessageListItem> doInBackground(
							LongSparseArray<MessageListItem>... params) {
						LongSparseArray<MessageListItem> data = params[0];
						LongSparseArray<MessageListItem> rmvData = new LongSparseArray<>();
						long key;
						for (int i = 0; i < data.size(); i++) {
							key = data.keyAt(i);
							MessageListItem obj = data.get(key);
							if (obj.getId() == itemToBookmark.getId()) {
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
					protected void onPostExecute(LongSparseArray<MessageListItem> rmvData) {
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
	protected LongSparseArray<MessageListItem> fetchDataFromDB() {
		return mDB.getMessages(Sort.DESC);
	}

	/**
	 * Delete one item on database.
	 *
	 * @param obj
	 * 		The item to delete.
	 */
	protected void deleteDataOnDB(MessageListItem obj) {
		mDB.removeMessage(obj == null ? null : obj.getMessage());
	}


	/**
	 * @return Menu on toolbar.
	 */
	protected int getToolbarMenuId() {
		return TOOLBAR_MENU;
	}

	/**
	 *
	 * @return Define the command whom to do remove.
	 */
	protected WhichPage getWhichPage() {
		return WhichPage.Messages;
	}

}
