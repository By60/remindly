package com.remindly.service.utils;

public class Log {
	
	/* Log
	 * ---
	 * This class is a utility for outputting logs and debug information to the console in a consistent
	 * manner. Use Log.i for printing info/debug messages, Log.w for printing warning messages, and
	 * Log.e for printing error messages.
	 */
	
	public static void i(String message) {
		System.out.println("[INFO] " + message);
	}
	
	public static void w(String message) {
		System.out.println("[WARN] " + message);
	}
	
	public static void e(String message) {
		System.err.println("[ERROR] " + message);
	}
	
	public static void p(String message) {
		System.out.println(message);
	}
	
	public static void stackTrace(Exception e) {
		String exception = e.toString();
		StackTraceElement[] stackTrace = e.getStackTrace();
		for(int i = 0; i < stackTrace.length; i++)
			exception += "\n     at " + stackTrace[i].toString();
		System.err.println(exception);
	}
}
