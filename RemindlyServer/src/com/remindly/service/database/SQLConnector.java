package com.remindly.service.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;

import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.Log;

public class SQLConnector {

	private BasicDataSource dataSource;
	private Connection database;
	
	public SQLConnector() {
		dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUsername(Configuration.getString("sql_username"));
		dataSource.setPassword(Configuration.getString("sql_password"));

		String databaseName = Configuration.getString("sql_database");
		String address = Configuration.getString("sql_address");
		String port = Configuration.getString("sql_port");
		dataSource.setUrl("jdbc:mysql://" + address + ":" + port + "/" + databaseName);
	}
	
	public void init() {
		try {
			Log.i("Initializing connection to MySQL database...");
			database = dataSource.getConnection();
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
	
	public QueryResult executeQuery(String query) {
		if(database == null)
			return null;
		
		try {
			Statement statement = database.createStatement();
			ResultSet result = statement.executeQuery(query);
			return new QueryResult(statement, result);
		} catch(Exception e) {
			Log.e("An error occurred while executing a SQL query.");
			Log.stackTrace(e);
			return null;
		}
	}
	
	// Returns the number of rows affected by the the update, or -1 if an error occurred
	public int executeUpdate(String query) {
		if(database == null)
			return -1;
		
		try {
			Statement statement = database.createStatement();
			int rowsAffected = statement.executeUpdate(query);
			statement.close();
			return rowsAffected;
		} catch(Exception e) {
			Log.e("An error occurred while executing a SQL update query.");
			Log.stackTrace(e);
			return -1;
		}
	}
}
