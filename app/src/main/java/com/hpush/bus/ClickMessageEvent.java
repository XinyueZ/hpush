package com.hpush.bus;

import com.hpush.data.Message;

/**
 * Event for clicking a message.
 *
 * @author Xinyue Zhao
 */
public final class ClickMessageEvent {
	/**
	 * The message to click.
	 */
	private Message mMessage;

	/**
	 * Constructor of {@link com.hpush.bus.ClickMessageEvent}.
	 *
	 * @param message
	 * 		The message to click.
	 */
	public ClickMessageEvent(Message message) {
		mMessage = message;
	}

	/**
	 * @return The message to click.
	 */
	public Message getMessage() {
		return mMessage;
	}
}
