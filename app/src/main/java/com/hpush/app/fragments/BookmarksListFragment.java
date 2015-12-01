package com.hpush.app.fragments;

import java.util.List;

import android.content.Context;

import com.hpush.R;
import com.hpush.app.App;
import com.hpush.bus.BookmarkAllEvent;
import com.hpush.bus.BookmarkMessageEvent;
import com.hpush.bus.BookmarkedEvent;
import com.hpush.bus.RemoveAllEvent.WhichPage;
import com.hpush.data.Bookmark;
import com.hpush.data.MessageListItem;
import com.hpush.data.SyncList;
import com.hpush.db.DB.Sort;
import com.hpush.utils.Prefs;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * List of all bookmarks.
 *
 * @author Xinyue Zhao
 */
public final class BookmarksListFragment extends MessagesListFragment {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT       = R.layout.fragment_bookmarks_list;
	/**
	 * Menu on toolbar.
	 */
	private static final int TOOLBAR_MENU = R.menu.item2;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------


	/**
	 * Handler for {@link SyncList}.
	 *
	 * @param e
	 * 		Event {@link}.
	 */
	public void onEvent( SyncList e ) {
		//We don't sync in bookmark.
	}
	/**
	 * Handler for {@link BookmarkedEvent}.
	 *
	 * @param e
	 * 		Event {@link}.
	 */
	public void onEvent( BookmarkedEvent e ) {
		loadMessages();
	}


	/**
	 * Handler for {@link com.hpush.bus.BookmarkAllEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.BookmarkAllEvent}.
	 */
	public void onEvent( BookmarkAllEvent e ) {
		//We don't bookmark anything in "bookmark-list".
	}


	/**
	 * Handler for {@link com.hpush.bus.BookmarkMessageEvent}.
	 *
	 * @param e
	 * 		Event {@link com.hpush.bus.BookmarkMessageEvent}.
	 */
	public void onEvent( BookmarkMessageEvent e ) {
		//We don't bookmark anything in "bookmark-list".
	}

	//------------------------------------------------

	public static BookmarksListFragment newInstance( Context context ) {
		return (BookmarksListFragment) BookmarksListFragment.instantiate( context, BookmarksListFragment.class.getName() );
	}

	/**
	 * Get data from application's database.
	 *
	 * @return List of all data from DB.
	 */
	protected List<MessageListItem> fetchDataFromDB() {
		return getDB().getBookmarks( Sort.DESC );
	}

	/**
	 * Delete one item on database.
	 *
	 * @param obj
	 * 		The item to delete.
	 */
	protected void deleteDataOnDB( final MessageListItem obj ) {
		if( getWhichPage() == WhichPage.Bookmarks ) {
			getDB().removeBookmark( obj == null ? null : obj.getMessage() );
			Prefs prefs = Prefs.getInstance( App.Instance );
			if( obj == null ) {
				//Remove all on backend.
				BmobQuery<Bookmark> query = new BmobQuery<>();
				query.addWhereEqualTo( "mUID", prefs.getGoogleAccount() );
				query.findObjects( App.Instance, new FindListener<Bookmark>() {
					@Override
					public void onSuccess( List<Bookmark> list ) {
						for( Bookmark bookmark : list ) {
							Bookmark delBookmark = new Bookmark();
							delBookmark.setObjectId( bookmark.getObjectId() );
							delBookmark.delete( App.Instance );
						}
					}

					@Override
					public void onError( int i, String s ) {
						//Ignore.
					}
				} );
			} else {
				BmobQuery<Bookmark> query = new BmobQuery<>();
				query.addWhereEqualTo( "mUID", prefs.getGoogleAccount() );
				query.findObjects( App.Instance, new FindListener<Bookmark>() {
					@Override
					public void onSuccess( List<Bookmark> list ) {
						for( Bookmark bookmark : list ) {
							if( bookmark.equals( obj.getMessage() ) ) {
								Bookmark delBookmark = new Bookmark( obj.getMessage() );
								delBookmark.setObjectId( bookmark.getObjectId() );
								delBookmark.delete( App.Instance );
								break;
							}
						}
					}

					@Override
					public void onError( int i, String s ) {
						//Ignore.
					}
				} );
			}
		}
	}


	/**
	 * @return Define the command whom to do remove.
	 */
	protected WhichPage getWhichPage() {
		return WhichPage.Bookmarks;
	}

	/**
	 * @return Menu on toolbar.
	 */
	protected int getToolbarMenuId() {
		return TOOLBAR_MENU;
	}

	@Override
	protected int getLayoutResId() {
		return LAYOUT;
	}

}
