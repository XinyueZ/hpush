package com.hpush.bus;

/**
 * Event after loading all dailies.
 *
 * @author Xinyue Zhao
 */
public final class LoadedAllDailiesEvent {
	/**
	 * Total count of loaded data.
	 */
	private int mCount;

	/**
	 * Constructor of {@link com.hpush.bus.LoadedAllDailiesEvent}.
	 *
	 * @param count
	 * 		Total count of loaded data.
	 */
	public LoadedAllDailiesEvent(int count) {
		mCount = count;
	}

	/**
	 * @return Total count of loaded data.
	 */
	public int getCount() {
		return mCount;
	}
}
