package com.hpush.data;

/**
 * Data-model for the items on the list.
 *
 * @author Xinyue Zhao
 */
public final class MessageListItem {
	/**
	 * The message to show on list.
	 */
	private Message message;
	/**
	 * Is the item checked or not.
	 */
	private boolean mChecked;
	/**
	 * Constructor of {@link com.hpush.data.MessageListItem}.
	 * @param message  The message to show on list.
	 */
	public MessageListItem(Message message) {
		this.message = message;
	}


	public String getBy() {
		return message.getBy();
	}

	public long getId() {
		return message.getId();
	}

	public long getTime() {
		return message.getTime();
	}

	public long getScore() {
		return message.getScore();
	}

	public void setDbId(long dbId) {
		message.setDbId(dbId);
	}

	public String getText() {
		return message.getText();
	}

	public String getTitle() {
		return message.getTitle();
	}

	public void setPushedTime(long pushedTime) {
		message.setPushedTime(pushedTime);
	}

	public long getPushedTime() {
		return message.getPushedTime();
	}

	public String getUrl() {
		return message.getUrl();
	}

	public long getDbId() {
		return message.getDbId();
	}

	public boolean isChecked() {
		return mChecked;
	}

	public Message getMessage() {
		return message;
	}
}
