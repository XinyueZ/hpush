package com.hpush.app.fragments;

import android.content.Context;
import android.support.v4.util.LongSparseArray;

import com.hpush.R;
import com.hpush.bus.BookmarkedEvent;
import com.hpush.data.MessageListItem;
import com.hpush.db.DB.Sort;

/**
 * List of all bookmarks.
 *
 * @author Xinyue Zhao
 */
public final class BookmarksListFragment extends MessagesListFragment{
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_bookmarks_list;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link BookmarkedEvent}.
	 *
	 * @param e
	 * 		Event {@link}.
	 */
	public void onEvent(BookmarkedEvent e) {
		loadMessages();
	}

	//------------------------------------------------

	public static BookmarksListFragment newInstance(Context context) {
		return (BookmarksListFragment) BookmarksListFragment.instantiate(context, BookmarksListFragment.class.getName());
	}

	/**
	 * Get data from application's database.
	 * @return List of all data from DB.
	 */
	protected LongSparseArray<MessageListItem> fetchDataFromDB() {
		return getDB().getBookmarks(Sort.DESC);
	}

	/**
	 * Delete one item on database.
	 * @param obj The item to delete.
	 */
	protected void deleteDataOnDB(MessageListItem obj) {
		getDB().removeBookmark(obj.getMessage());
	}


	@Override
	protected int getLayoutID() {
		return LAYOUT;
	}


}
