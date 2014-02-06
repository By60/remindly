package com.remindly.service.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.Log;

public class SQLConnector {

	private Connection database;
	
	public SQLConnector() {
		
	}
	
	public void init() {
		String address = Configuration.getString("sql_address");
		String port = Configuration.getString("sql_port");
		String username = Configuration.getString("sql_username");
		String password = Configuration.getString("sql_password");
		String connectionRequest = "jdbc:mysql://" + address + ":" + port + "/mysql?user=" + username + "&password=" + password;
		
		try {
			Log.i("Initializing connection to MySQL database...");
			Class.forName("com.mysql.jdbc.Driver");
			
			database = DriverManager.getConnection(connectionRequest);
			Log.i("Successfully connected to database!");
		} catch(Exception e) {
			Log.e("An error occurred while connecting to MySQL database.");
			Log.stackTrace(e);
		}
	}
	
	public void stop() {
		if(database == null)
			return;
		try {
			database.close();
		} catch (SQLException e) {
			Log.e("An error occurred closing the database.");
			Log.stackTrace(e);
		}
	}
}
