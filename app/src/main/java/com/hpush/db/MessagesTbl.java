package com.hpush.db;

/**
 * Messages table.
 *
 * @author Xinyue Zhao
 */
interface MessagesTbl {
	static final String DB_ID = "_db_id";
	static final String BY = "_by";
	static final String ID = "_id";
	static final String SCORE = "_score";
	static final String COMMENTS_COUNT  = "_comments_count";
	static final String TEXT = "_text";
	static final String TIME = "_time";
	static final String TITLE = "_title";
	static final String URL = "_url";
	static final String PUSHED_TIME = "_pushed_time";
	static final String TABLE_NAME = "messages";



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
			COMMENTS_COUNT + " INTEGER  DEFAULT -1, " +
			TEXT + " TEXT  DEFAULT \"\", " +
			TIME + " INTEGER   DEFAULT -1, " +
			TITLE + " TEXT  DEFAULT \"\", " +
			MessagesTbl.URL + " TEXT  DEFAULT \"\", " +
			PUSHED_TIME + " INTEGER   DEFAULT -1" +
			");";
}
