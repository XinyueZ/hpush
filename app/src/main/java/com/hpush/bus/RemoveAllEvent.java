package com.hpush.bus;

/**
 * Event to remove all selected values.
 *
 * @author Xinyue Zhao
 */
public final class RemoveAllEvent {
	/**
	 * Which pager and whose items should be removed.
	 */
	public enum WhichPage {
		Messages, Bookmarks
	}

	/**
	 * Pager type.
	 */
	private WhichPage mWhichPage;

	/**
	 * Constructor of {@link RemoveAllEvent}
	 * @param whichPage Pager type.
	 */
	public RemoveAllEvent(WhichPage whichPage) {
		mWhichPage = whichPage;
	}

	/**
	 *
	 * @return Pager type.
	 */
	public WhichPage getWhichPage() {
		return mWhichPage;
	}
}
