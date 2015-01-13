package com.hpush.bus;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Event after when Google+ has been logined.
 *
 * @author Xinyue Zhao
 */
public final class LoginedGPlusEvent {
	/**
	 * The validate logined client.
	 */
	private GoogleApiClient mPlusClient;

	/**
	 * Constructor  {@link LoginedGPlusEvent}
	 *
	 * @param plusClient
	 * 		The validate logined client.
	 */
	public LoginedGPlusEvent(GoogleApiClient plusClient) {
		mPlusClient = plusClient;
	}

	/**
	 * @return The validate logined client.
	 */
	public GoogleApiClient getPlusClient() {
		return mPlusClient;
	}
}
