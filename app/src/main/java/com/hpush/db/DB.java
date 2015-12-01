package com.hpush.db;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hpush.data.Message;
import com.hpush.data.MessageListItem;
import com.hpush.data.Recent;
import com.hpush.data.RecentListItem;
import com.hpush.utils.Prefs;


/**
 * Defines methods that operate on database.
 * <p/>
 * <b>Singleton pattern.</b>
 * <p/>
 * <p/>
 *
 * @author Xinyue Zhao
 */
public final class DB {
	/**
	 * {@link android.content.Context}.
	 */
	private        Context        mContext;
	/**
	 * Impl singleton pattern.
	 */
	private static DB             sInstance;
	/**
	 * Helper class that create, delete, update tables of database.
	 */
	private        DatabaseHelper mDatabaseHelper;
	/**
	 * The database object.
	 */
	private        SQLiteDatabase mDB;

	/**
	 * Constructor of {@link DB}. Impl singleton pattern so that it is private.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	private DB( Context cxt ) {
		mContext = cxt;
	}

	/**
	 * Get instance of  {@link  DB} singleton.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 *
	 * @return The {@link DB} singleton.
	 */
	public static DB getInstance( Context cxt ) {
		if( sInstance == null ) {
			sInstance = new DB( cxt );
		}
		return sInstance;
	}

	/**
	 * Open database.
	 */
	public synchronized void open() {
		mDatabaseHelper = new DatabaseHelper( mContext );
		mDB = mDatabaseHelper.getWritableDatabase();
	}

	/**
	 * Close database.
	 */
	public synchronized void close() {
		mDatabaseHelper.close();
	}


	public synchronized boolean addMessage( Message item ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success = false;
		try {
			long          rowId = -1;
			ContentValues v     = new ContentValues();
			v.put( MessagesTbl.BY, item.getBy() );
			v.put( MessagesTbl.ID, item.getId() );
			v.put( MessagesTbl.SCORE, item.getScore() );
			v.put( MessagesTbl.COMMENTS_COUNT, item.getCommentsCount() );
			v.put( MessagesTbl.TEXT, item.getText() );
			v.put( MessagesTbl.TIME, item.getTime() );
			v.put( MessagesTbl.TITLE, item.getTitle() );
			v.put( MessagesTbl.URL, item.getUrl() );
			v.put( MessagesTbl.PUSHED_TIME, item.getPushedTime() );

			rowId = mDB.insert( MessagesTbl.TABLE_NAME, null, v );
			item.setDbId( rowId );
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	public synchronized boolean addBookmark( Message item ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success = false;
		try {
			long          rowId = -1;
			ContentValues v     = new ContentValues();
			v.put( BookmarksTbl.BY, item.getBy() );
			v.put( BookmarksTbl.ID, item.getId() );
			v.put( BookmarksTbl.SCORE, item.getScore() );
			v.put( BookmarksTbl.COMMENTS_COUNT, item.getCommentsCount() );
			v.put( BookmarksTbl.TEXT, item.getText() );
			v.put( BookmarksTbl.TIME, item.getTime() );
			v.put( BookmarksTbl.TITLE, item.getTitle() );
			v.put( BookmarksTbl.URL, item.getUrl() );
			v.put( BookmarksTbl.PUSHED_TIME, item.getPushedTime() );

			rowId = mDB.insert( BookmarksTbl.TABLE_NAME, null, v );
			item.setDbId( rowId );
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	public synchronized boolean updateMessage( Message item ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success = false;
		try {
			long          rowId;
			ContentValues v = new ContentValues();
			v.put( MessagesTbl.BY, item.getBy() );
			v.put( MessagesTbl.SCORE, item.getScore() );
			v.put( MessagesTbl.COMMENTS_COUNT, item.getCommentsCount() );
			v.put( MessagesTbl.TEXT, item.getText() );
			v.put( MessagesTbl.TIME, item.getTime() );
			v.put( MessagesTbl.TITLE, item.getTitle() );
			v.put( MessagesTbl.URL, item.getUrl() );
			v.put( MessagesTbl.PUSHED_TIME, item.getPushedTime() );
			String[] args = new String[] { item.getId() + "" };
			rowId = mDB.update( MessagesTbl.TABLE_NAME, v, MessagesTbl.ID + " = ?", args );
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}


	public synchronized boolean updateBookmark( Message item ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success = false;
		try {
			long          rowId;
			ContentValues v = new ContentValues();
			v.put( BookmarksTbl.BY, item.getBy() );
			v.put( BookmarksTbl.SCORE, item.getScore() );
			v.put( BookmarksTbl.COMMENTS_COUNT, item.getCommentsCount() );
			v.put( BookmarksTbl.TEXT, item.getText() );
			v.put( BookmarksTbl.TIME, item.getTime() );
			v.put( BookmarksTbl.TITLE, item.getTitle() );
			v.put( BookmarksTbl.URL, item.getUrl() );
			v.put( BookmarksTbl.PUSHED_TIME, item.getPushedTime() );
			String[] args = new String[] { item.getId() + "" };
			rowId = mDB.update( BookmarksTbl.TABLE_NAME, v, BookmarksTbl.ID + " = ?", args );
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}


	public synchronized int removeMessage( Message item ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		int     rowsRemain = -1;
		boolean success;
		Cursor  c          = null;
		try {
			long rowId;
			if( item != null ) {
				String   whereClause = MessagesTbl.ID + "=?";
				String[] whereArgs   = new String[] { String.valueOf( item.getId() ) };
				rowId = mDB.delete( MessagesTbl.TABLE_NAME, whereClause, whereArgs );
			} else {
				rowId = mDB.delete( MessagesTbl.TABLE_NAME, null, null );
			}
			success = rowId > 0;
			if( success ) {
				c = mDB.query( MessagesTbl.TABLE_NAME, new String[] { MessagesTbl.ID }, null, null, null, null, null );
				rowsRemain = c.getCount();
			} else {
				rowsRemain = -1;
			}
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return rowsRemain;
	}


	public synchronized int removeBookmark( Message item ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		int     rowsRemain = -1;
		boolean success;
		Cursor  c          = null;
		try {
			long rowId;
			if( item != null ) {
				String   whereClause = MessagesTbl.ID + "=?";
				String[] whereArgs   = new String[] { String.valueOf( item.getId() ) };
				rowId = mDB.delete( BookmarksTbl.TABLE_NAME, whereClause, whereArgs );
			} else {
				rowId = mDB.delete( BookmarksTbl.TABLE_NAME, null, null );
			}
			success = rowId > 0;
			if( success ) {
				c = mDB.query( BookmarksTbl.TABLE_NAME, new String[] { BookmarksTbl.ID }, null, null, null, null, null );
				rowsRemain = c.getCount();
			} else {
				rowsRemain = -1;
			}
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return rowsRemain;
	}


	/**
	 * Sort direction.
	 */
	public enum Sort {
		DESC( "DESC" ), ASC( "ASC" );
		private String nm;
		Sort( String nm ) {
			this.nm = nm;
		}

		@Override
		public String toString() {
			return nm;
		}
	}


	public synchronized List<MessageListItem> getMessages( Sort sort ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		Cursor                c    = null;
		List<MessageListItem> list = new ArrayList<>();
		try {
			c = mDB.query( MessagesTbl.TABLE_NAME, null, null, null, null, null, getSortBy( mContext ) + " " + sort.toString() );
			Message item;
			while( c.moveToNext() ) {
				item = new Message( c.getLong( c.getColumnIndex( MessagesTbl.DB_ID ) ), c.getString( c.getColumnIndex( MessagesTbl.BY ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.ID ) ), c.getLong( c.getColumnIndex( MessagesTbl.SCORE ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.COMMENTS_COUNT ) ), c.getString( c.getColumnIndex( MessagesTbl.TEXT ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.TIME ) ), c.getString( c.getColumnIndex( MessagesTbl.TITLE ) ),
									c.getString( c.getColumnIndex( MessagesTbl.URL ) ), c.getLong( c.getColumnIndex( MessagesTbl.PUSHED_TIME ) )
				);
				list.add( new MessageListItem( item ) );
			}
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return list;
	}


	public synchronized List<MessageListItem> getBookmarks( Sort sort ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		Cursor                c    = null;
		Message               item;
		List<MessageListItem> list = new ArrayList<>();
		try {
			c = mDB.query( BookmarksTbl.TABLE_NAME, null, null, null, null, null, getSortBy( mContext ) + " " + sort.toString() );
			while( c.moveToNext() ) {
				item = new Message( c.getLong( c.getColumnIndex( BookmarksTbl.DB_ID ) ), c.getString( c.getColumnIndex( BookmarksTbl.BY ) ),
									c.getLong( c.getColumnIndex( BookmarksTbl.ID ) ), c.getLong( c.getColumnIndex( BookmarksTbl.SCORE ) ),
									c.getLong( c.getColumnIndex( BookmarksTbl.COMMENTS_COUNT ) ),
									c.getString( c.getColumnIndex( BookmarksTbl.TEXT ) ), c.getLong( c.getColumnIndex( BookmarksTbl.TIME ) ),
									c.getString( c.getColumnIndex( BookmarksTbl.TITLE ) ), c.getString( c.getColumnIndex( BookmarksTbl.URL ) ),
									c.getLong( c.getColumnIndex( BookmarksTbl.PUSHED_TIME ) )
				);
				list.add( new MessageListItem( item ) );
			}
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return list;
	}


	public synchronized boolean findMessage( Message item ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success;
		Cursor  c = null;
		try {
			String   whereClause = MessagesTbl.ID + "=?";
			String[] whereArgs   = new String[] { String.valueOf( item.getId() ) };
			c = mDB.query( MessagesTbl.TABLE_NAME, new String[] { MessagesTbl.ID }, whereClause, whereArgs, null, null, null );
			success = c.getCount() >= 1;
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return success;
	}

	public synchronized boolean findBookmark( Message item ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success;
		Cursor  c = null;
		try {
			String   whereClause = BookmarksTbl.ID + "=?";
			String[] whereArgs   = new String[] { String.valueOf( item.getId() ) };
			c = mDB.query( BookmarksTbl.TABLE_NAME, new String[] { BookmarksTbl.ID }, whereClause, whereArgs, null, null, null );
			success = c.getCount() >= 1;
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return success;
	}


	/**
	 * Get column name to sort data.
	 * <p/>
	 * <code> <p/> <item>Scores</item> <p/> <item>Arrival</item> <p/> <item>Creation</item> <p/> <item>Comments</item> <p/> </code>
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 *
	 * @return Name of column.
	 */
	private static String getSortBy( Context cxt ) {
		String sortTypeValue = Prefs.getInstance( cxt.getApplicationContext() ).getSortTypeValue();
		switch( sortTypeValue ) {
			case "0":
				return MessagesTbl.SCORE;
			case "1":
				return MessagesTbl.PUSHED_TIME;
			case "2":
				return MessagesTbl.TIME;
			case "3":
				return MessagesTbl.COMMENTS_COUNT;
			default:
				return MessagesTbl.TIME;
		}
	}

	public synchronized boolean addDaily( String id ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success = false;
		try {
			long          rowId = -1;
			ContentValues v     = new ContentValues();
			v.put( DailyTbl.ID, Long.valueOf( id ) );
			v.put( DailyTbl.EDIT_TIME, System.currentTimeMillis() );
			rowId = mDB.insert( DailyTbl.TABLE_NAME, null, v );
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}


	public synchronized boolean updateDaily( String id ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success = false;
		try {
			long          rowId;
			ContentValues v = new ContentValues();
			v.put( DailyTbl.EDIT_TIME, System.currentTimeMillis() );
			String[] args = new String[] { id };
			rowId = mDB.update( DailyTbl.TABLE_NAME, v, DailyTbl.ID + " = ?", args );
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	public synchronized void clearDailies() {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		try {
			mDB.delete( DailyTbl.TABLE_NAME, null, null );
		} finally {
			close();
		}
	}

	public synchronized boolean findDaily( String id ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		boolean success;
		Cursor  c = null;
		try {
			String   whereClause = DailyTbl.ID + "=?";
			String[] whereArgs   = new String[] { id };
			c = mDB.query( DailyTbl.TABLE_NAME, new String[] { DailyTbl.ID }, whereClause, whereArgs, null, null, null );
			success = c.getCount() >= 1;
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return success;
	}

	public synchronized List<RecentListItem> getDailies( Sort sort ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		Cursor               c    = null;
		List<RecentListItem> list = new ArrayList<>();
		try {
			c = mDB.query( DailyTbl.TABLE_NAME, null, null, null, null, null, DailyTbl.EDIT_TIME + " " + sort.toString() );
			long    id;
			Message msg;
			while( c.moveToNext() ) {
				id = c.getLong( c.getColumnIndex( DailyTbl.ID ) );
				msg = getBookmark( id );
				if( msg != null ) {
					list.add( new RecentListItem( new Recent( msg, true ) ) );
				} else {
					msg = getMessage( id );
					if( msg != null ) {
						list.add( new RecentListItem( new Recent( msg, false ) ) );
					}
				}
			}
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return list;
	}

	public synchronized Message getMessage( long id ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		Message msg = null;
		Cursor  c   = null;
		try {
			String   whereClause = MessagesTbl.ID + "=?";
			String[] whereArgs   = new String[] { String.valueOf( id ) };
			c = mDB.query( MessagesTbl.TABLE_NAME, null, whereClause, whereArgs, null, null, null );
			while( c.moveToNext() ) {
				msg = new Message( c.getLong( c.getColumnIndex( MessagesTbl.DB_ID ) ), c.getString( c.getColumnIndex( MessagesTbl.BY ) ),
								   c.getLong( c.getColumnIndex( MessagesTbl.ID ) ), c.getLong( c.getColumnIndex( MessagesTbl.SCORE ) ),
								   c.getLong( c.getColumnIndex( MessagesTbl.COMMENTS_COUNT ) ), c.getString( c.getColumnIndex( MessagesTbl.TEXT ) ),
								   c.getLong( c.getColumnIndex( MessagesTbl.TIME ) ), c.getString( c.getColumnIndex( MessagesTbl.TITLE ) ),
								   c.getString( c.getColumnIndex( MessagesTbl.URL ) ), c.getLong( c.getColumnIndex( MessagesTbl.PUSHED_TIME ) )
				);
			}
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return msg;
	}

	public synchronized Message getBookmark( long id ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		Message msg = null;
		Cursor  c   = null;
		try {
			String   whereClause = BookmarksTbl.ID + "=?";
			String[] whereArgs   = new String[] { String.valueOf( id ) };
			c = mDB.query( BookmarksTbl.TABLE_NAME, null, whereClause, whereArgs, null, null, null );
			while( c.moveToNext() ) {
				msg = new Message( c.getLong( c.getColumnIndex( BookmarksTbl.DB_ID ) ), c.getString( c.getColumnIndex( BookmarksTbl.BY ) ),
								   c.getLong( c.getColumnIndex( BookmarksTbl.ID ) ), c.getLong( c.getColumnIndex( BookmarksTbl.SCORE ) ),
								   c.getLong( c.getColumnIndex( BookmarksTbl.COMMENTS_COUNT ) ), c.getString( c.getColumnIndex( BookmarksTbl.TEXT ) ),
								   c.getLong( c.getColumnIndex( BookmarksTbl.TIME ) ), c.getString( c.getColumnIndex( BookmarksTbl.TITLE ) ),
								   c.getString( c.getColumnIndex( BookmarksTbl.URL ) ), c.getLong( c.getColumnIndex( BookmarksTbl.PUSHED_TIME ) )
				);
			}
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return msg;
	}

	public List<RecentListItem> search( String keyword ) {
		if( mDB == null || !mDB.isOpen() ) {
			open();
		}
		String               whereClause = MessagesTbl.TITLE + " LIKE '%" + keyword + "%'" + " OR " + MessagesTbl.TEXT + " LIKE '%" + keyword + "%'";
		Cursor               c           = null;
		List<RecentListItem> list        = new ArrayList<>();
		try {
			c = mDB.query( MessagesTbl.TABLE_NAME, null, whereClause, null, null, null, getSortBy( mContext ) + " " +
																						Sort.DESC.toString() );
			Message item;
			while( c.moveToNext() ) {
				item = new Message( c.getLong( c.getColumnIndex( MessagesTbl.DB_ID ) ), c.getString( c.getColumnIndex( MessagesTbl.BY ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.ID ) ), c.getLong( c.getColumnIndex( MessagesTbl.SCORE ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.COMMENTS_COUNT ) ), c.getString( c.getColumnIndex( MessagesTbl.TEXT ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.TIME ) ), c.getString( c.getColumnIndex( MessagesTbl.TITLE ) ),
									c.getString( c.getColumnIndex( MessagesTbl.URL ) ), c.getLong( c.getColumnIndex( MessagesTbl.PUSHED_TIME ) )
				);
				list.add( new RecentListItem( new Recent( item, false ) ) );
			}
			c = mDB.query( BookmarksTbl.TABLE_NAME, null, whereClause, null, null, null, getSortBy( mContext ) + " " +
																						 Sort.DESC.toString() );
			while( c.moveToNext() ) {
				item = new Message( c.getLong( c.getColumnIndex( MessagesTbl.DB_ID ) ), c.getString( c.getColumnIndex( MessagesTbl.BY ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.ID ) ), c.getLong( c.getColumnIndex( MessagesTbl.SCORE ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.COMMENTS_COUNT ) ), c.getString( c.getColumnIndex( MessagesTbl.TEXT ) ),
									c.getLong( c.getColumnIndex( MessagesTbl.TIME ) ), c.getString( c.getColumnIndex( MessagesTbl.TITLE ) ),
									c.getString( c.getColumnIndex( MessagesTbl.URL ) ), c.getLong( c.getColumnIndex( MessagesTbl.PUSHED_TIME ) )
				);
				list.add( new RecentListItem( new Recent( item, true ) ) );
			}
		} finally {
			if( c != null && !c.isClosed() ) {
				c.close();
			}
			close();
		}
		return list;
	}
}
