package com.remindly.service.dispatcher;

public class Message {
	
	// Message id from the database
	private int messageId;
	
	// User id that created this message
	private int userId;
	
	// A list of comma-delimited 10-digit phone numbers, no user_id's allowed
	private String recipients;
	
	// The message to be sent (message exceeding 160 characters will be truncated)
	private String message;
	
	public Message(int messageId, int userId, String recipients, String message) {
		this.messageId = messageId;
		this.userId = userId;
		this.recipients = recipients;
		this.message = message;
	}

	public int getMessageId() {
		return messageId;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public String getRecipients() {
		return recipients;
	}

	public String getMessage() {
		return message;
	}
}
