package com.hpush.bus;

import com.hpush.data.MessageListItem;

/**
 * Share message with Facebook or Tweet.
 *
 * @author Xinyue Zhap
 */
public final class ShareMessageEvent {
	/**
	 * Message to share.
	 */
	private MessageListItem mMessage;

	/**
	 * Facebook or Tweet.
	 */
	public enum Type {
		Facebook, Tweet;
	}

	/**
	 * Facebook or Tweet.
	 */
	private Type mType;

	/**
	 * Constructor of {@link ShareMessageEvent}
	 *
	 * @param message
	 * 		Message to share.
	 * @param type
	 * 		Facebook or Tweet.
	 */
	public ShareMessageEvent(MessageListItem message, Type type) {
		mMessage = message;
		mType = type;
	}

	/**
	 * @return Facebook or Tweet.
	 */
	public Type getType() {
		return mType;
	}

	/**
	 * @return Facebook or Tweet.
	 */
	public MessageListItem getMessage() {
		return mMessage;
	}
}
