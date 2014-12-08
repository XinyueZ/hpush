package com.hpush.bus;

import com.hpush.data.MessageListItem;

/**
 * To bookmark one message.
 *
 * @author Xinyue Zhao
 */
public final class BookmarkMessageEvent {
	/**
	 * The list item that has been selected to bookmark.
	 */
	private MessageListItem mMessageListItem;

	/**
	 * Constructor of {@link com.hpush.bus.BookmarkMessageEvent}.
	 * @param messageListItem  The list item that has been selected to bookmark.
	 */
	public BookmarkMessageEvent(MessageListItem messageListItem) {
		mMessageListItem = messageListItem;
	}

	/**
	 *
	 * @return  The list item that has been selected to bookmark.
	 */
	public MessageListItem getMessageListItem() {
		return mMessageListItem;
	}
}
