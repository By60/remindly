package com.remindly.service;

import com.remindly.service.database.SQLConnector;
import com.remindly.service.dispatcher.Message;
import com.remindly.service.dispatcher.SMSDispatcher;
import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.Log;

public class Main {

	public static final String VERSION = "v1.0_alpha";
	
	private static SMSDispatcher smsDispatcher;
	private static SQLConnector database;
	
	public static void main(String[] args) {
		init();
		
		Message test = new Message(1, 2, "5104732960,8052676424", "Testing");
		smsDispatcher.queueMessage(test);
		
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
		Configuration.loadConfiguration();
		
		// Initialize MySQL connection
		database = new SQLConnector();
		database.init();
		
		// Initialize SMS Dispatcher
		smsDispatcher = new SMSDispatcher();
		smsDispatcher.init();
		if(Configuration.getBoolean("sms_simulation_mode"))
			smsDispatcher.setSimulationMode(true);
		
		Log.i("Initialization complete!");
	}
}
