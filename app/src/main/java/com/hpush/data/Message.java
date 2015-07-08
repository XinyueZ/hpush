package com.hpush.data;


import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

import cn.bmob.v3.BmobObject;

public class Message extends BmobObject implements Serializable {
	private long mDbId;
	@SerializedName("By")
	private String mBy;
	@SerializedName("Id")
	private long mId;
	@SerializedName("Score")
	private long mScore;
	@SerializedName("Kids")
	private long mCommentsCount;
	@SerializedName("Text")
	private String mText;
	@SerializedName("Time")
	private long mTime;
	@SerializedName("Title")
	private String mTitle;
	@SerializedName("Url")
	private String mUrl;
	@SerializedName("Pushed_Time")
	private long mPushedTime;

	protected Message() {
	}

	public Message(long dbId, String by, long id, long score, long commentsCount, String text, long time, String title,
			String url, long pushedTime) {
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

	public Message(String by, long id, long score, long commentsCount, String text, long time, String title, String url,
			long pushedTime) {
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

	public void setUrl(String url) {
		mUrl = url;
	}
}
