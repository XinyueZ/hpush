package com.hpush.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;

import com.hpush.data.Message;
import com.hpush.data.MessageListItem;


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
	private Context mContext;
	/**
	 * Impl singleton pattern.
	 */
	private static DB sInstance;
	/**
	 * Helper class that create, delete, update tables of database.
	 */
	private  DatabaseHelper mDatabaseHelper;
	/**
	 * The database object.
	 */
	private SQLiteDatabase mDB;

	/**
	 * Constructor of {@link DB}. Impl singleton pattern so that it is private.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	private DB(Context cxt) {
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
	public static DB getInstance(Context cxt) {
		if (sInstance == null) {
			sInstance = new DB(cxt);
		}
		return sInstance;
	}

	/**
	 * Open database.
	 */
	public synchronized void open() {
		mDatabaseHelper = new  DatabaseHelper(mContext);
		mDB = mDatabaseHelper.getWritableDatabase();
	}

	/**
	 * Close database.
	 */
	public synchronized void close() {
		mDatabaseHelper.close();
	}


	public synchronized boolean addMessage(Message item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId = -1;
			ContentValues v = new ContentValues();
			v.put(MessagesTbl.BY, item.getBy());
			v.put(MessagesTbl.ID, item.getId());
			v.put(MessagesTbl.SCORE, item.getScore());
			v.put(MessagesTbl.COMMENTS_COUNT, item.getCommentsCount());
			v.put(MessagesTbl.TEXT, item.getText());
			v.put(MessagesTbl.TIME, item.getTime());
			v.put(MessagesTbl.TITLE, item.getTitle());
			v.put(MessagesTbl.URL, item.getUrl());

			rowId = mDB.insert(MessagesTbl.TABLE_NAME, null, v);
			item.setDbId(rowId);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	public synchronized boolean addBookmark(Message item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId = -1;
			ContentValues v = new ContentValues();
			v.put(MessagesTbl.BY, item.getBy());
			v.put(MessagesTbl.ID, item.getId());
			v.put(MessagesTbl.SCORE, item.getScore());
			v.put(MessagesTbl.COMMENTS_COUNT, item.getCommentsCount());
			v.put(MessagesTbl.TEXT, item.getText());
			v.put(MessagesTbl.TIME, item.getTime());
			v.put(MessagesTbl.TITLE, item.getTitle());
			v.put(MessagesTbl.URL, item.getUrl());

			rowId = mDB.insert(BookmarksTbl.TABLE_NAME, null, v);
			item.setDbId(rowId);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	public synchronized boolean updateMessage(Message item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId;
			ContentValues v = new ContentValues();
			v.put(MessagesTbl.BY, item.getBy());
			v.put(MessagesTbl.SCORE, item.getScore());
			v.put(MessagesTbl.COMMENTS_COUNT, item.getCommentsCount());
			v.put(MessagesTbl.TEXT, item.getText());
			v.put(MessagesTbl.TIME, item.getTime());
			v.put(MessagesTbl.TITLE, item.getTitle());
			v.put(MessagesTbl.URL, item.getUrl());
			String[] args = new String[] { item.getId() + "" };
			rowId = mDB.update(MessagesTbl.TABLE_NAME, v, MessagesTbl.ID + " = ?", args);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}


	public synchronized boolean updateBookmark(Message item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId;
			ContentValues v = new ContentValues();
			v.put(MessagesTbl.BY, item.getBy());
			v.put(MessagesTbl.SCORE, item.getScore());
			v.put(MessagesTbl.COMMENTS_COUNT, item.getCommentsCount());
			v.put(MessagesTbl.TEXT, item.getText());
			v.put(MessagesTbl.TIME, item.getTime());
			v.put(MessagesTbl.TITLE, item.getTitle());
			v.put(MessagesTbl.URL, item.getUrl());
			String[] args = new String[] { item.getId() + "" };
			rowId = mDB.update(BookmarksTbl.TABLE_NAME, v, MessagesTbl.ID + " = ?", args);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}




	public synchronized int removeMessage(Message item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		int rowsRemain = -1;
		boolean success;
		try {
			long rowId;
			if(item != null) {
				String whereClause = MessagesTbl.ID + "=?";
				String[] whereArgs = new String[] { String.valueOf(item.getId()) };
				rowId = mDB.delete(MessagesTbl.TABLE_NAME, whereClause, whereArgs);
			} else {
				rowId = mDB.delete(MessagesTbl.TABLE_NAME, null, null);
			}
			success = rowId > 0;
			if (success) {
				Cursor c = mDB.query(MessagesTbl.TABLE_NAME, new String[] { MessagesTbl.ID }, null, null, null, null, null);
				rowsRemain = c.getCount();
			} else {
				rowsRemain = -1;
			}
		} finally {
			close();
		}
		return rowsRemain;
	}


	public synchronized int removeBookmark(Message item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		int rowsRemain = -1;
		boolean success;
		try {
			long rowId;
			if(item != null) {
				String whereClause = MessagesTbl.ID + "=?";
				String[] whereArgs = new String[] { String.valueOf(item.getId()) };
				rowId = mDB.delete(BookmarksTbl.TABLE_NAME, whereClause, whereArgs);
			} else {
				rowId = mDB.delete(BookmarksTbl.TABLE_NAME, null, null);
			}
			success = rowId > 0;
			if (success) {
				Cursor c = mDB.query(BookmarksTbl.TABLE_NAME, new String[] { MessagesTbl.ID }, null, null, null, null, null);
				rowsRemain = c.getCount();
			} else {
				rowsRemain = -1;
			}
		} finally {
			close();
		}
		return rowsRemain;
	}



	/**
	 * Sort direction.
	 */
	public enum Sort {
		DESC("DESC"), ASC("ASC");
		private String nm;
		Sort(String nm) {
			this.nm = nm;
		}

		@Override
		public String toString() {
			return nm;
		}
	}


	public synchronized LongSparseArray<MessageListItem> getMessages(Sort sort) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		Cursor c = mDB.query(MessagesTbl.TABLE_NAME, null, null, null, null, null,
				MessagesTbl.PUSHED_TIME + " " + sort.toString());
		Message item    ;
		LongSparseArray<MessageListItem>  list = new LongSparseArray<>();
		try {
			while (c.moveToNext()) {
				item = new Message(
						c.getLong(c.getColumnIndex(MessagesTbl.DB_ID)),
						c.getString(c.getColumnIndex(MessagesTbl.BY)),
						c.getLong(c.getColumnIndex(MessagesTbl.ID)),
						c.getLong(c.getColumnIndex(MessagesTbl.SCORE)),
						c.getLong(c.getColumnIndex(MessagesTbl.COMMENTS_COUNT)),
						c.getString(c.getColumnIndex(MessagesTbl.TEXT)),
						c.getLong(c.getColumnIndex(MessagesTbl.TIME)),
						c.getString(c.getColumnIndex(MessagesTbl.TITLE)),
						c.getString(c.getColumnIndex(MessagesTbl.URL)),
						c.getLong(c.getColumnIndex(MessagesTbl.PUSHED_TIME))
				);
				list.put(c.getLong(c.getColumnIndex(MessagesTbl.DB_ID)), new MessageListItem(item));
			}
		} finally {
			if (c != null) {
				c.close();
			}
			close();
			return list;
		}
	}


	public synchronized LongSparseArray<MessageListItem> getBookmarks(Sort sort) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		Cursor c = mDB.query(BookmarksTbl.TABLE_NAME, null, null, null, null, null,
				MessagesTbl.PUSHED_TIME + " " + sort.toString());
		Message item   ;
		LongSparseArray<MessageListItem>  list = new LongSparseArray<>();
		try {
			while (c.moveToNext()) {
				item = new Message(
						c.getLong(c.getColumnIndex(MessagesTbl.DB_ID)),
						c.getString(c.getColumnIndex(MessagesTbl.BY)),
						c.getLong(c.getColumnIndex(MessagesTbl.ID)),
						c.getLong(c.getColumnIndex(MessagesTbl.SCORE)),
						c.getLong(c.getColumnIndex(MessagesTbl.COMMENTS_COUNT)),
						c.getString(c.getColumnIndex(MessagesTbl.TEXT)),
						c.getLong(c.getColumnIndex(MessagesTbl.TIME)),
						c.getString(c.getColumnIndex(MessagesTbl.TITLE)),
						c.getString(c.getColumnIndex(MessagesTbl.URL)),
						c.getLong(c.getColumnIndex(MessagesTbl.PUSHED_TIME))
				);
				list.put(c.getLong(c.getColumnIndex(MessagesTbl.DB_ID)),  new MessageListItem(item));
			}
		} finally {
			if (c != null) {
				c.close();
			}
			close();
			return list;
		}
	}


	public synchronized  boolean findMessage(Message item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success;
		try {
			String whereClause =   MessagesTbl.ID + "=?";
			String[] whereArgs = new String[] {   String.valueOf(item.getId()) };
			Cursor c = mDB.query(MessagesTbl.TABLE_NAME, new String[] { MessagesTbl.ID }, whereClause, whereArgs, null, null, null);
			success = c.getCount() >= 1;
		} finally {
			close();
		}
		return success ;
	}

	public synchronized boolean findBookmark(Message item) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success;
		try {
			String whereClause =   MessagesTbl.ID + "=?";
			String[] whereArgs = new String[] {   String.valueOf(item.getId()) };
			Cursor c = mDB.query(BookmarksTbl.TABLE_NAME, new String[] { MessagesTbl.ID }, whereClause, whereArgs, null, null, null);
			success = c.getCount() >= 1;
		} finally {
			close();
		}
		return success ;
	}
}
