package com.remindly.service.dispatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.nexmo.messaging.sdk.NexmoSmsClientSSL;
import com.nexmo.messaging.sdk.SmsSubmissionResult;
import com.nexmo.messaging.sdk.messages.TextMessage;
import com.remindly.service.Context;
import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.DatabaseUtils;
import com.remindly.service.utils.Log;

public class SMSDispatcher {

	private static final int SMS_CHARACTER_LIMIT = 160;
	private static final int BATCH_FREQUENCY = 1000;
	
	private Context context;
	private String apiKey, apiSecret, senderNumber;
	private NexmoSmsClientSSL nexmoClient;
	private Thread dispatcher;
	private ArrayList<Message> queue;
	private boolean simulationMode = false;
	
	public SMSDispatcher(Context context) {
		this.context = context;
		
		apiKey = Configuration.getString("api_key");
		apiSecret = Configuration.getString("api_secret");
		senderNumber = Configuration.getString("sender_number");
		
		queue = new ArrayList<Message>();
		dispatcher = new Thread(dispatcherThread);
	}
	
	private Runnable dispatcherThread = new Runnable() {
		public void run() {
			while(true) {
				synchronized(queue) {
					if(!queue.isEmpty()) {
						ArrayList<Message> batch = new ArrayList<Message>();
						for(int i = queue.size() - 1; i >= 0; i--)
							batch.add(queue.remove(i));
						dispatch(batch);
					}
				}
				try {
					Thread.sleep(BATCH_FREQUENCY);
				} catch (InterruptedException e) { }
			}
		}
	};
	
	public void setSimulationMode(boolean simulationMode) {
		if(simulationMode)
			Log.i("SMS Dispatcher set to simulation mode, no texts will actually be sent.");
		this.simulationMode = simulationMode;
	}
	
	public void init() {
		if(login())
			dispatcher.start();
	}
	
	private boolean login() {
		if(nexmoClient != null)
			return true;
		
		Log.i("Connecting to Nexmo...");
		try {
			nexmoClient = new NexmoSmsClientSSL(apiKey, apiSecret);
			Log.i("Successfully connected to Nexmo!");
			return true;
		} catch (Exception e) {
			Log.e("SMS Dispatcher unable to connect or log in to Nexmo account.");
			Log.stackTrace(e);
			return false;
		}
	}
	
	public void queueMessage(Message message) {
		synchronized(queue) {
			queue.add(message);
		}
	}
	
	public void queueMessages(ArrayList<Message> messages) {
		synchronized(queue) {
			queue.addAll(messages);
		}
	}

	private void dispatch(ArrayList<Message> batch) {
		if(batch == null)
			return;
		
		for(int i = 0; i < batch.size(); i++) {
			Message message = batch.get(i);
			
			// Remove duplicate phone numbers
			Set<String> recipients = toSet(message.getRecipients().split(","));
			String formattedMessage = fitText(message.getMessage());
			
			// Send message for each phone number
			for(String phoneNumber : recipients) {
				// Check for valid 10-digit phone number before sending
				if(phoneNumber.matches("\\d{10}")) {
					boolean success = sendMessage(phoneNumber, formattedMessage);
					if(!success) {
						Log.w("A message could not be sent."
							+ "\n     Phone: " + phoneNumber 
							+ "\n     Message: " + formattedMessage
							+ "\n     ID: " + message.getMessageId());
						DatabaseUtils.updateMessageStatus(context, message.getMessageId(), Message.STATUS_ERROR);
					} else {
						DatabaseUtils.updateMessageStatus(context, message.getMessageId(), Message.STATUS_SENT);
					}
				} else {
					Log.w("A malformed phone number (" + phoneNumber + ") was detected in message (id: " + message.getMessageId() + ").");
					DatabaseUtils.updateMessageStatus(context, message.getMessageId(), Message.STATUS_ERROR);
				}
			}
		}
	}
	
	private boolean sendMessage(String phoneNumber, String message) {
		try {
			// Nexmo requires 1 in front of US phone numbers
			phoneNumber = "1" + phoneNumber;
			
			SmsSubmissionResult[] results = null;
			if(!simulationMode) {
				TextMessage textMessage = new TextMessage(senderNumber, phoneNumber, message);
				results = nexmoClient.submitMessage(textMessage);
			} else {
				Log.p("[SIMULATION] Simulating message sent."
						+ "\n     Phone: " + phoneNumber
						+ "\n     Message: " + message);
				long now = System.currentTimeMillis();
				while(System.currentTimeMillis() - now < 1000);
			}
			
			if(results.length < 1) {
				Log.w("Nexmo client did not return any results.");
				return false;
			} else {
				SmsSubmissionResult result = results[0];
				if(result.getStatus() == SmsSubmissionResult.STATUS_OK) {
					Log.i("A message was successfully sent to " + phoneNumber + ".");
					return true;
				} else {
					Log.w("Sending message did not recieve success result, result: " + result.getStatus());
					return false;
				}
			}
		} catch(Exception e) {
			Log.e("An error occurred while sending a message.");
			Log.stackTrace(e);
			return false;
		}
	}
	
	private String fitText(String message) {
		if(message.length() <= SMS_CHARACTER_LIMIT)
			return message;
		return message.substring(0, SMS_CHARACTER_LIMIT);
	}
	
	private Set<String> toSet(String[] array) {
		Set<String> set = new HashSet<String>();
		for(int i = 0; i < array.length; i++)
			set.add(array[i]);
		return set;
	}
}
