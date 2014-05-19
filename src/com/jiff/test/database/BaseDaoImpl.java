package com.jiff.test.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.jiff.test.util.Util;

/**
 * @author saranyashanmugam
 *
 *Class used to get the database connection.
 */
public class BaseDaoImpl implements BaseDao{
	
	  private static Properties properties;
	  
	  /**
	   * Method used to get the database connection.
	 * @return Connection object.
	 */
	public Connection getConnection()
	  {
		 Connection conn = null;
		 properties = Util.getProperty();
	  try {
	   Class.forName(properties.getProperty("driver")).newInstance();
	   System.out.println();
	   System.out.println(properties.getProperty("password"));
	   conn = (Connection) DriverManager.getConnection(properties.getProperty("url")+properties.getProperty("dbname"),properties.getProperty("username"),properties.getProperty("password"));
	  } catch (Exception e) {
	  e.printStackTrace();
	  }
	   return conn;
	  }
}


