package com.hpush.data;


public final class Bookmark extends Message {
	private String mUID;

	public Bookmark() {

	}

	public Bookmark( String uid, Message msg ) {
		super( msg.getDbId(),
			   msg.getBy(),
			   msg.getId(),
			   msg.getScore(),
			   msg.getCommentsCount(),
			   msg.getText(),
			   msg.getTime(),
			   msg.getTitle(),
			   msg.getUrl(),
			   msg.getPushedTime()
		);
		mUID = uid;
	}

	public Bookmark( Message msg ) {
		super( msg.getDbId(),
			   msg.getBy(),
			   msg.getId(),
			   msg.getScore(),
			   msg.getCommentsCount(),
			   msg.getText(),
			   msg.getTime(),
			   msg.getTitle(),
			   msg.getUrl(),
			   msg.getPushedTime()
		);

	}

	public String getUID() {
		return mUID;
	}


	@Override
	public boolean equals( Object o ) {
		Message other = (Message) o;
		return other.getId() == getId();
	}
}
