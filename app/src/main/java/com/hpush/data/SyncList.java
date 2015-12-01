package com.hpush.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * A sync-list of messages.
 *
 * @author Xinyue Zhao
 */
public final class SyncList {
	@SerializedName("SyncList")
	private List<Message> mSyncList;

	public SyncList( List<Message> syncList ) {
		mSyncList = syncList;
	}

	public List<Message> getSyncList() {
		return mSyncList;
	}

	public void setSyncList( List<Message> syncList ) {
		mSyncList = syncList;
	}
}
