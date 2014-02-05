package com.remindly.service.dispatcher;

import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.Log;
import com.techventus.server.voice.Voice;

public class SMSDispatcher {

	private String gvUsername, gvPassword;
	private Voice gvAccount;
	private Thread dispatcher;
	
	public SMSDispatcher() {
		gvUsername = Configuration.getString("sms_gv_username");
		gvPassword = Configuration.getString("sms_gv_password");
	}
	
	public void init() {
		try {
			Log.i("Connecting to Google Voice...");
			gvAccount = new Voice(gvUsername, gvPassword);
			if(gvAccount.isLoggedIn())
				Log.i("Connected to Google Voice!");
			else
				Log.w("Unable to log in to Google Voice account.");
		} catch(Exception e) {
			Log.e("SMS Dispatcher unable to connect or log in to Google Voice account.");
			Log.stackTrace(e);
		}
	}
}
