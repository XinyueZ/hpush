package com.hpush.data;

/**
 * Data-model for the items on the list.
 *
 * @author Xinyue Zhao
 */
public final class DailyListItem extends MessageListItem {
	/**
	 * The daily to show on list.
	 */
	private Daily mDaily;

	public DailyListItem(Daily daily) {
		super(daily);
		mDaily = daily;
	}

	public boolean isBookmarked() {
		return mDaily.isBookmarked();
	}

	@Override
	public String getText() {
		return mDaily.getText();
	}

	@Override
	public void setPushedTime(long pushedTime) {
		mDaily.setPushedTime(pushedTime);
	}

	@Override
	public long getScore() {
		return mDaily.getScore();
	}

	@Override
	public String getUrl() {
		return mDaily.getUrl();
	}

	@Override
	public long getId() {
		return mDaily.getId();
	}

	@Override
	public long getDbId() {
		return mDaily.getDbId();
	}

	@Override
	public long getPushedTime() {
		return mDaily.getPushedTime();
	}

	@Override
	public long getTime() {
		return mDaily.getTime();
	}

	@Override
	public void setDbId(long dbId) {
		mDaily.setDbId(dbId);
	}

	@Override
	public String getTitle() {
		return mDaily.getTitle();
	}

	@Override
	public String getBy() {
		return mDaily.getBy();
	}


	public void setBookmarked( boolean bookmarked) {
		mDaily.setBookmarked(bookmarked);
	}
}
