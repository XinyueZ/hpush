package com.hpush.bus;

import com.google.android.gms.plus.PlusClient;

/**
 * Event after when Google+ has been logined.
 *
 * @author Xinyue Zhao
 */
public final class LoginedGPlusEvent {
	/**
	 * The validate logined client.
	 */
	private PlusClient mPlusClient;

	/**
	 * Constructor  {@link LoginedGPlusEvent}
	 *
	 * @param plusClient
	 * 		The validate logined client.
	 */
	public LoginedGPlusEvent(PlusClient plusClient) {
		mPlusClient = plusClient;
	}

	/**
	 * @return The validate logined client.
	 */
	public PlusClient getPlusClient() {
		return mPlusClient;
	}
}
