package com.hpush.data;


import java.io.Serializable;

public final class Message implements Serializable{
	private long mDbId;
	private String mBy;
	private long mId;
	private long mScore;
	private long mCommentsCount;
	private String mText;
	private long mTime;
	private String mTitle;
	private String mUrl;
	private long mPushedTime;

	public Message(long dbId, String by, long id, long score, long commentsCount, String text, long time, String title, String url, long pushedTime) {
		mDbId = dbId;
		this.mBy = by;
		this.mId = id;
		this.mScore = score;
		this.mCommentsCount = commentsCount;
		this.mText = text;
		this.mTime = time;
		this.mTitle = title;
		this.mUrl = url;
		mPushedTime = pushedTime;
	}

	public Message(String by, long id, long score, long commentsCount, String text, long time, String title, String url, long pushedTime) {
		this.mBy = by;
		this.mId = id;
		this.mScore = score;
		this.mCommentsCount = commentsCount;
		this.mText = text;
		this.mTime = time;
		this.mTitle = title;
		this.mUrl = url;
		mPushedTime = pushedTime;
	}

	public String getBy() {
		return mBy;
	}

	public long getId() {
		return mId;
	}

	public long getScore() {
		return mScore;
	}

	public String getText() {
		return mText;
	}

	public long getTime() {
		return mTime;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getUrl() {
		return mUrl;
	}

	public long getDbId() {
		return mDbId;
	}

	public void setDbId(long dbId) {
		mDbId = dbId;
	}

	public long getPushedTime() {
		return mPushedTime;
	}

	public void setPushedTime(long pushedTime) {
		mPushedTime = pushedTime;
	}

	public long getCommentsCount() {
		return mCommentsCount;
	}

	public void setCommentsCount(long commentsCount) {
		mCommentsCount = commentsCount;
	}
}
