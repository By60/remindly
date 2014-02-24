package com.remindly.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import com.remindly.service.database.QueryResult;
import com.remindly.service.dispatcher.Message;
import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.DatabaseUtils;
import com.remindly.service.utils.Log;
import com.remindly.service.utils.TimeUtils;

public class Scheduler {
	
	private Context context;
	private long frequency;
	private int cycleCounter = 1;

	public Scheduler(Context context) {
		this.context = context;
		
		int minutes = Configuration.getInteger("scheduler_frequency");
		frequency = minutes * 60000;
	}
	
	public void init() {
		Log.i("Scheduler initialized, frequency set to " + (frequency / 60000) + " minutes.");
		Thread scheduler = new Thread(schedulerThread);
		scheduler.start();
	}
	
	private Runnable schedulerThread = new Runnable() {
		public void run() {
			while(true) {
				Log.i("SCHEDULER CYCLE #" + cycleCounter + " at " + TimeUtils.currentReadableTime());
				validateEpochTimes();
				collectAndDispatch();
				
				try {
					Thread.sleep(frequency);
				} catch(InterruptedException e) { }
				cycleCounter++;
			}
		}
	};
	
	private void collectAndDispatch() {
		long currentTime = TimeUtils.currentEpochTime();
		String selectQuery = "SELECT * FROM Messages WHERE epoch_time <= ? AND status = ?";
		PreparedStatement selectionPreparedStatement = context.getDatabase().prepareStatement(selectQuery);
		try {
			selectionPreparedStatement.setLong(1, currentTime);
			selectionPreparedStatement.setInt(2, Message.STATUS_PENDING);
		} catch (SQLException e) { }
		
		QueryResult results = context.getDatabase().executeQuery(selectionPreparedStatement);
		ArrayList<Message> messages = new ArrayList<Message>();
		while(results.next()) {
			int messageId = results.getInt("message_id");
			int userId = results.getInt("user_id");
			String recipients = results.getString("recipients");
			String message = results.getString("message");
			
			messages.add(new Message(messageId, userId, recipients, message));
			Log.i("Queuing to send message " + results.getInt("message_id") + " for " + results.getString("time"));
		}
		
		context.getSMSDispatcher().queueMessages(messages);
		results.finish();
	}
	
	private void validateEpochTimes() {
		String selectQuery = "SELECT * FROM Messages WHERE epoch_time = 0";
		PreparedStatement selectionPreparedStatement = context.getDatabase().prepareStatement(selectQuery);
		QueryResult results = context.getDatabase().executeQuery(selectionPreparedStatement);
		while(results.next()) {
			long epochTime = TimeUtils.convertToEpoch(results.getString("time"));
			int messageId = results.getInt("message_id");
			if(epochTime == -1) {
				DatabaseUtils.updateMessageStatus(context, messageId, Message.STATUS_ERROR);
			} else {
				String updateQuery = "UPDATE Messages SET epoch_time = ? WHERE message_id = ?";
				PreparedStatement updatePreparedStatement = context.getDatabase().prepareStatement(updateQuery);
				try {
					updatePreparedStatement.setLong(1, epochTime);
					updatePreparedStatement.setInt(2, messageId);
				} catch (SQLException e) { }
				context.getDatabase().executeUpdate(updatePreparedStatement);
			}
		}
		results.finish();
	}
}
