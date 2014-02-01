package com.remindly.service.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

	public static final String CONFIGURATION_FILE = "remindly_config.properties";
	private static Properties configuration;
	
	public static boolean loadConfiguration() {
		String path = Configuration.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.substring(0, path.lastIndexOf("/")) + "/" + CONFIGURATION_FILE;
		try {
			configuration = new Properties();
			configuration.load(new BufferedReader(new FileReader(path)));
			return true;
		} catch(FileNotFoundException e) {
			Log.e("Configuration file not found at " + path + ".");
		} catch(IOException e) {
			Log.e("An error occurred while loading the properites file.");
			Log.stackTrace(e);
		}
		return false;
	}
	
	public static String getString(String key) {
		if(!validateConfiguration(key))
			return null;
		
		return configuration.getProperty(key);
	}
	
	public static int getInteger(String key) {
		if(!validateConfiguration(key))
			return -1;
		
		String value = configuration.getProperty(key);
		try {
			return Integer.parseInt(value);
		} catch(Exception e) {
			Log.w("Unable to parse \"" + value + "\" to int from configuration file.");
			return -1;
		}
	}
	
	public static boolean getBoolean(String key) {
		if(!validateConfiguration(key))
			return false;
		
		String value = configuration.getProperty(key);
		try {
			return Boolean.parseBoolean(value);
		} catch(Exception e) {
			Log.w("Unable to parse \"" + value + "\" to boolean from configuration file.");
			return false;
		}
	}
	
	private static boolean validateConfiguration(String key) {
		if(configuration == null) {
			Log.w("Configuration file not loaded, attempting to load configuration.");
			if(!loadConfiguration())
				return false;
		}
		if(!configuration.containsKey(key)) {
			Log.w("Configuration file does not contain key \"" + key + "\".");
		}
		return true;
	}
}
