package com.hpush.db;

/**
 * Messages table.
 *
 * @author Xinyue Zhao
 */
interface BookmarksTbl extends  MessagesTbl{
	static final String TABLE_NAME = "bookmarks";
	//We use rowId as key for each row.
	//See. http://www.sqlite.org/autoinc.html
	/**
	 * Init new table since {@link com.hpush.db.DatabaseHelper#DATABASE_VERSION} = {@code 1}.
	 */
	static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
			DB_ID + " INTEGER PRIMARY KEY, " +
			BY + " TEXT  DEFAULT \"\", " +
			ID + " INTEGER  DEFAULT -1, " +
			SCORE + " INTEGER  DEFAULT -1, " +
			TEXT + " TEXT  DEFAULT \"\", " +
			TIME + " INTEGER   DEFAULT -1, " +
			TITLE + " TEXT  DEFAULT \"\", " +
			MessagesTbl.URL + " TEXT  DEFAULT \"\", " +
			PUSHED_TIME + " INTEGER   DEFAULT -1" +
			");";
}
