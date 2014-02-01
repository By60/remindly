package com.remindly.service.framework;

import com.remindly.service.framework.dispatcher.SMSDispatcher;
import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.Log;

public class Main {

	public static final String VERSION = "v1.0_alpha";
	
	public static void main(String[] args) {
		init();
		
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
		
		// Initialize SMS Dispatcher
		SMSDispatcher smsDispatcher = new SMSDispatcher();
		smsDispatcher.init();
	}
}
