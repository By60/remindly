package com.remindly.service.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.remindly.service.Context;

public class DatabaseUtils {
	
	public static void updateMessageStatus(Context context, int messageId, int status) {
		String updateMessage = "UPDATE Messages SET status = ? WHERE message_id = ?";
		PreparedStatement preparedStatement = context.getDatabase().prepareStatement(updateMessage);
		try {
			preparedStatement.setInt(1, status);
			preparedStatement.setInt(2, messageId);
		} catch (SQLException e) { }
		context.getDatabase().executeUpdate(preparedStatement);
	}
}
