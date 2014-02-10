package com.remindly.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.remindly.service.database.QueryResult;
import com.remindly.service.dispatcher.Message;
import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.Log;

public class Main {

	public static final String VERSION = "v1.0_alpha";
	
	private static Context context;
	
	public static void main(String[] args) {
		init();
		
		Message test = new Message(1, 2, "5104732960", "Testing");
		context.getSMSDispatcher().queueMessage(test);
		
		String updateMessage = "UPDATE Messages SET status = ? WHERE message_id = ?";
		PreparedStatement preparedStatement = context.getDatabase().prepareStatement(updateMessage);
		try {
			preparedStatement.setInt(1, (int)(Math.random()*100));
			preparedStatement.setInt(2, 1);
		} catch (SQLException e) { }
		context.getDatabase().executeUpdate(preparedStatement);
		Log.i("Executed query!");
		
		String queryMessage = "SELECT status FROM Messages WHERE message_id = ?";
		PreparedStatement preparedStatement2 = context.getDatabase().prepareStatement(queryMessage);
		try {
			preparedStatement2.setInt(1, 1);
		} catch(SQLException e) { }
		QueryResult result = context.getDatabase().executeQuery(preparedStatement2);
		Log.i("Testing selection for status = " + result.getInt("status"));
		result.finish();
		
		while(true);
	}

	private static void init() {
		// Print header
		String header = "Remindly Service (" + VERSION + ")";
		Log.p(header);
		String divider = "";
		for(int i = 0; i < header.length(); i++)
			divider += "=";
		Log.p(divider);
		
		// Load configuration file
		if(!Configuration.loadConfiguration()) {
			Log.w("Cannot continue initializing server without configuration file.");
			System.exit(0);
		}
		
		context = new Context();
		
		Log.i("Initialization complete!");
	}
}
