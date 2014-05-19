package com.jiff.test.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author saranyashanmugam
 *
 *Class acts as a utility class
 */
public class Util {
	
	private static Properties properties;
	
	/**
	 * Method used to get the property file.
	 * 
	 * @return property object.
	 */
	public static Properties getProperty()
	{
		properties = new Properties();
		try {
			properties.load(new FileInputStream("main.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
		
	}
	
	
	

}
