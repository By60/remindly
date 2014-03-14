package com.remindly.service.dispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.remindly.service.Context;
import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.DatabaseUtils;
import com.remindly.service.utils.Log;
import com.techventus.server.voice.Voice;

public class SMSDispatcher {

	private static final int SMS_CHARACTER_LIMIT = 160;
	private static final int BATCH_FREQUENCY = 1000;
	
	private Context context;
	private String gvUsername, gvPassword;
	private Voice gvAccount;
	private Thread dispatcher;
	private ArrayList<Message> queue;
	private boolean simulationMode = false;
	
	public SMSDispatcher(Context context) {
		this.context = context;
		
		gvUsername = Configuration.getString("sms_gv_username");
		gvPassword = Configuration.getString("sms_gv_password");
		
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
		if(gvLogin())
			dispatcher.start();
	}
	
	private boolean gvLogin() {
		if(gvAccount != null && gvAccount.isLoggedIn())
			return true;
		
		Log.i("Connecting to Google Voice...");
		try {
			gvAccount = new Voice(gvUsername, gvPassword);
		} catch (IOException e) {
			Log.e("SMS Dispatcher unable to connect or log in to Google Voice account.");
			Log.stackTrace(e);
			return false;
		}
		
		if(gvAccount.isLoggedIn()) {
			Log.i("Connected to Google Voice!");
			return true;
		} else {
			Log.w("Unable to log in to Google Voice account.");
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

		if(!gvAccount.isLoggedIn()) {
			Log.i("Reconnecting to Google Voice...");
			if(!gvLogin()) {
				Log.e("Batch of size " + batch.size() + " was canceled because unable to connect to Google Voice.");
				return;
			}
		}
		
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
							+ "\n     ID: " + message.getMessageId()
							+ "\n     GV Logged In: " + gvAccount.isLoggedIn());
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
			String result;
			if(!simulationMode) {
				result = gvAccount.sendSMS(phoneNumber, message);
			} else { 
				result = "\"ok\":true";
				Log.p("[SIMULATION] Simulating message sent."
						+ "\n     Phone: " + phoneNumber
						+ "\n     Message: " + message);
				long now = System.currentTimeMillis();
				while(System.currentTimeMillis() - now < 1000);
			}
			
			// Result returns a JSON with "ok" value set to true
			if(result.contains("\"ok\":true")) {
				Log.i("A message was successfully sent to " + phoneNumber + ".");
				return true;
			} else {
				Log.w("Sending message did not recieve success result, result: " + result);
				return false;
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
