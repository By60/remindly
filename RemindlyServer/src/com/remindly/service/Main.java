package com.remindly.service;

import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.Log;

public class Main {

	public static final String VERSION = "v1.0_alpha";
	
	private static Context context;
	private static Scheduler scheduler;
	
	public static void main(String[] args) {
		init();
		
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
		
		// Initialize context
		context = new Context();
		
		// Initialize scheduler
		scheduler = new Scheduler(context);
		scheduler.init();
		
		Log.i("Initialization complete!");
	}
}
