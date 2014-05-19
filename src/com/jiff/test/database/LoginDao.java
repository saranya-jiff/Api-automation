package com.jiff.test.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.jiff.test.vo.TestCaseDetails;
import com.jiff.test.vo.TestCaseResults;

/**
 * @author saranyashanmugam
 * Interface for LoginDaoImpl.
 */
public interface LoginDao {
	
	
	/**
	 * Method used to get the login credentials.
	 * @return Map<String,String>
	 * @throws SQLException
	 */
	public Map<String,String> getTestData() throws SQLException;
	
	public List<TestCaseDetails> getTestCaseDetails() throws SQLException;
	
	public void insertTestCaseResults( List<TestCaseResults> testCaseResults) throws SQLException;
	
	

}
