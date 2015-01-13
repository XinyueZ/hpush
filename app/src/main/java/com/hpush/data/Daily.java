package com.hpush.data;


public final class Daily extends Message {
	private boolean mBookmarked;

	public Daily(Message msg, boolean bookmarked) {
		super(msg.getDbId(),msg.getBy(), msg.getId(), msg.getScore(), msg.getCommentsCount(),
				msg.getText(), msg.getTime(), msg.getTitle(), msg.getUrl(), msg.getPushedTime());
		mBookmarked = bookmarked;
	}



	public boolean isBookmarked() {
		return mBookmarked;
	}

	public void setBookmarked(boolean bookmarked) {
		mBookmarked = bookmarked;
	}
}
