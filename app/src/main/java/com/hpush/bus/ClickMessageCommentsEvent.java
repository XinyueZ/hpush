package com.hpush.bus;

import android.view.View;

import com.hpush.data.Message;

/**
 * Event for clicking on comments-button  of a message.
 *
 * @author Xinyue Zhao
 */
public final class ClickMessageCommentsEvent {
	/**
	 * The message to click.
	 */
	private Message mMessage;
	/**
	 * The sender {@link android.view.View}.
	 */
	private View mSenderV;
	/**
	 * Constructor of {@link com.hpush.bus.ClickMessageCommentsEvent}.
	 *
	 * @param message
	 * 		The message to click.
	 * @param senderV The sender {@link android.view.View}.
	 */
	public ClickMessageCommentsEvent(Message message, View senderV) {
		mMessage = message;
		mSenderV = senderV;
	}

	/**
	 * @return The message to click.
	 */
	public Message getMessage() {
		return mMessage;
	}

	/**
	 *
	 * @return The sender {@link android.view.View}.
	 */
	public View getSenderV() {
		return mSenderV;
	}
}
