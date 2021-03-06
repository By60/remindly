package com.remindly.service.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtils {
	
	private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("M/d/yy'_'h:mmaaa");
	private static SimpleDateFormat READABLE_TIME_FORMAT = new SimpleDateFormat("M/d/yy' 'h:mmaaa");
	
	public static long convertToEpoch(String time) {
		if(time == null)
			return -1;
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(TIME_FORMAT.parse(time));
			return calendar.getTimeInMillis();
		} catch(ParseException e) {
			Log.stackTrace(e);
			return -1;
		}
	}
	
	public static String currentReadableTime() {
		Calendar calendar = Calendar.getInstance();
		return READABLE_TIME_FORMAT.format(calendar.getTime());
	}
	
	public static long currentEpochTime() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTimeInMillis();
	}
}
