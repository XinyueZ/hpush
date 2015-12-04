package com.hpush.bus;

/**
 * Show or hidden float-action-button.
 *
 * @author Xinyue Zhao
 */
public final class FloatActionButtonEvent {
	private boolean mHide;

	public FloatActionButtonEvent( boolean hide ) {
		mHide = hide;
	}

	public boolean isHide() {
		return mHide;
	}
}
