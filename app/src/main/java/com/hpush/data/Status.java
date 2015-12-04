package com.hpush.data;

import com.google.gson.annotations.SerializedName;

/**
 * Response of network-calls.
 *
 * @author Xinyue Zhao
 */
public final class Status {
	@SerializedName("status")
	private boolean mStatus;
	@SerializedName("function")
	private String  mFunction;


	public Status( boolean status, String function ) {
		mStatus = status;
		mFunction = function;
	}

	public String getFunction() {
		return mFunction;
	}

	public boolean status() {
		return mStatus;
	}

}
