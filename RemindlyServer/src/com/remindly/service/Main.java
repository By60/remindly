package com.remindly.service;

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
		
		String query = "INSERT INTO Messages VALUES(NULL,'0','5104732960','2/9/2014 14:35PM', 'Hey this is a test.', '0');";
		context.getDatabase().executeUpdate(query);
		
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
