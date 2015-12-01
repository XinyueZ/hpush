package com.hpush.data;

/**
 * Data-model for the items on the list.
 *
 * @author Xinyue Zhao
 */
public class MessageListItem {
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
	 *
	 * @param message
	 * 		The message to show on list.
	 */
	public MessageListItem( Message message ) {
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
	public String getText() {
		return message.getText();
	}
	public String getTitle() {
		return message.getTitle();
	}
	public long getPushedTime() {
		return message.getPushedTime();
	}
	public void setPushedTime( long pushedTime ) {
		message.setPushedTime( pushedTime );
	}
	public String getUrl() {
		return message.getUrl();
	}
	public long getDbId() {
		return message.getDbId();
	}
	public void setDbId( long dbId ) {
		message.setDbId( dbId );
	}
	public boolean isChecked() {
		return mChecked;
	}

	public void setChecked( boolean checked ) {
		mChecked = checked;
	}

	public long getCommentCounts() {
		return message.getCommentsCount();
	}
	public Message getMessage() {
		return message;
	}
}
