package com.hpush.data;

/**
 * Data-model for the items on the list.
 *
 * @author Xinyue Zhao
 */
public final class RecentListItem extends MessageListItem {
	/**
	 * The daily to show on list.
	 */
	private Recent mRecent;

	public RecentListItem( Recent recent ) {
		super( recent );
		mRecent = recent;
	}

	public boolean isBookmarked() {
		return mRecent.isBookmarked();
	}
	public void setBookmarked( boolean bookmarked ) {
		mRecent.setBookmarked( bookmarked );
	}
	@Override
	public String getText() {
		return mRecent.getText();
	}
	@Override
	public long getScore() {
		return mRecent.getScore();
	}

	@Override
	public String getUrl() {
		return mRecent.getUrl();
	}

	@Override
	public long getId() {
		return mRecent.getId();
	}

	@Override
	public long getDbId() {
		return mRecent.getDbId();
	}
	@Override
	public void setDbId( long dbId ) {
		mRecent.setDbId( dbId );
	}
	@Override
	public long getPushedTime() {
		return mRecent.getPushedTime();
	}
	@Override
	public void setPushedTime( long pushedTime ) {
		mRecent.setPushedTime( pushedTime );
	}
	@Override
	public long getTime() {
		return mRecent.getTime();
	}
	@Override
	public String getTitle() {
		return mRecent.getTitle();
	}
	@Override
	public String getBy() {
		return mRecent.getBy();
	}
}
