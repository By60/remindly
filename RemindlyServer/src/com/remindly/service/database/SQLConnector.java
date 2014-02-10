package com.remindly.service.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;

import com.remindly.service.utils.Configuration;
import com.remindly.service.utils.Log;

public class SQLConnector {

	private BasicDataSource dataSource;
	private String databaseName;

	public SQLConnector() {
		dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUsername(Configuration.getString("sql_username"));
		dataSource.setPassword(Configuration.getString("sql_password"));

		databaseName = Configuration.getString("sql_database");
		String address = Configuration.getString("sql_address");
		String port = Configuration.getString("sql_port");
		dataSource.setUrl("jdbc:mysql://" + address + ":" + port + "/" + databaseName);
	}

	public void testDatabase() {
		Log.i("Testing database connection...");
		Connection database = obtainDatabase();
		if(database != null) {
			Log.i("Database successfully connected.");
			finishDatabase(database);
		} else {
			Log.w("Database connection test failed.");
		}
	}
	
	public String getDatabaseName() {
		return databaseName;
	}

	private Connection obtainDatabase() {
		Connection database = null;
		try {
			database = dataSource.getConnection();
		} catch(SQLException e) {
			Log.e("An error occurred while connecting to MySQL database.");
			Log.stackTrace(e);
		}
		return database;
	}

	private void finishDatabase(Connection database) {
		if(database == null)
			return;
		try {
			database.close();
		} catch(SQLException e) { }
	}
	
	public PreparedStatement prepareStatement(String sql) {
		Connection database = obtainDatabase();
		if(database == null)
			return null;
		
		try {
			PreparedStatement preparedStatement = database.prepareStatement(sql);
			return preparedStatement;
		} catch (SQLException e) {
			return null;
		}
	}
	
	public QueryResult executeQuery(PreparedStatement preparedStatement) {
		if(preparedStatement == null)
			return null;
		
		try {
			ResultSet resultSet = preparedStatement.executeQuery();
			return new QueryResult(preparedStatement, resultSet);
		} catch(SQLException e) {
			Log.e("An error occurred while executing a SQL query.");
			Log.stackTrace(e);
			return null;
		}
	}
	
	public int executeUpdate(PreparedStatement preparedStatement) {
		if(preparedStatement == null)
			return -1;
		
		try {
			int rowsAffected = preparedStatement.executeUpdate();
			finishDatabase(preparedStatement.getConnection());
			preparedStatement.close();
			return rowsAffected;
		} catch(SQLException e) {
			Log.e("An error occurred while executing a SQL update query.");
			Log.stackTrace(e);
			return -1;
		}
	}
}
