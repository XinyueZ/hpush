package com.hpush.bus;

/**
 * Event to show action-bar or NOT.
 *
 * @author Xinyue Zhao
 */
public final class ShowActionBar {
	/**
	 * {@code true} if show.
	 */
	private boolean mShow;

	/**
	 * Constructor of {@link com.hpush.bus.ShowActionBar}
	 * @param show {@code true} if show.
	 */
	public ShowActionBar(boolean show) {
		mShow = show;
	}

	/**
	 *
	 * @return {@code true} if show.
	 */
	public boolean isShow() {
		return mShow;
	}
}
