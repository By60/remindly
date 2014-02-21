package com.remindly.service;

import com.remindly.service.database.SQLConnector;
import com.remindly.service.dispatcher.SMSDispatcher;
import com.remindly.service.utils.Configuration;

public class Context {

	private SQLConnector database;
	private SMSDispatcher smsDispatcher;
	
	public Context() {
		database = new SQLConnector();
		database.testDatabase();

		smsDispatcher = new SMSDispatcher(this);
		if(Configuration.getBoolean("sms_simulation_mode"))
			smsDispatcher.setSimulationMode(true);
		smsDispatcher.init();
	}
	
	public SQLConnector getDatabase() {
		return database;
	}
	
	public SMSDispatcher getSMSDispatcher() {
		return smsDispatcher;
	}
}
