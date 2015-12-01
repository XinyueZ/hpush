package com.hpush.db;

/**
 * Remarks top3 news daily. The table will be cleared by being over to new day.
 *
 * @author Xinyue Zhao
 */
public interface DailyTbl {
	static final String DB_ID     = "_db_id";
	static final String ID        = "_id";
	static final String EDIT_TIME = "_edit_time";

	static final String TABLE_NAME = "daily";

	//We use rowId as key for each row.
	//See. http://www.sqlite.org/autoinc.html
	/**
	 * Init new table since {@link com.hpush.db.DatabaseHelper#DATABASE_VERSION} = {@code 2}.
	 */
	static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
									 DB_ID + " INTEGER PRIMARY KEY, " +
									 ID + " INTEGER  DEFAULT -1, " +
									 EDIT_TIME + " INTEGER DEFAULT -1" +
									 ");";
}
