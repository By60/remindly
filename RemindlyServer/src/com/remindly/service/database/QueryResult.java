package com.remindly.service.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.remindly.service.utils.Log;

public class QueryResult {

	private Statement queryStatement;
	private ResultSet result;
	
	public QueryResult(Statement queryStatement, ResultSet result) {
		this.queryStatement = queryStatement;
		this.result = result;
		
		try {
			if(result != null)
				result.first();
		} catch(SQLException e) { }
	}
	
	public void finish() {
		if(queryStatement == null)
			return;
		
		try {
			result.close();
			queryStatement.getConnection().close();
			queryStatement.close();
		} catch(SQLException e) { }
	}
	
	public boolean next() {
		if(result == null)
			return false;
		
		try {
			return result.next();
		} catch(SQLException e) {
			return false;
		}
	}
	
	public String getString(String columnIndex) {
		if(result == null)
			return null;
		
		try {
			return result.getString(columnIndex);
		} catch(SQLException e) {
			return null;
		}
	}
	
	public int getInt(String columnIndex) {
		if(result == null)
			return -1;
		
		try {
			return result.getInt(columnIndex);
		} catch(SQLException e) {
			return -1;
		}
	}
}
