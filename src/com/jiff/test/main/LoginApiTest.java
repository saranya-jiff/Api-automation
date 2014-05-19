package com.jiff.test.main;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jiff.test.login.Login;
import com.jiff.test.vo.TestCaseDetails;
import com.jiff.test.vo.TestCaseResults;

/**
 * @author saranyashanmugam
 *
 *Class used to test positive scenario for Login Api.
 */
public class LoginApiTest {
	
	
	private static Map<String,String> output;
	private static List<TestCaseDetails> testCases;
	private static Login login;
	private static TestCaseResults testCaseResults;
	private static List<TestCaseResults> testCaseResultsList = new ArrayList<TestCaseResults>();
	
	
	/**
	 * Method used to initialize database connection.
	 * 
	 */
	@BeforeClass
	public static void initialize()
	{
		login = new Login();
		try {
			testCases = login.getTestCaseDetails();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method used to test positive response of login api.
	 */
	@Test
	public void test() {
		
		for(TestCaseDetails testCaseDetails : testCases)
		{
			output = Login.verifyLoginApi(testCaseDetails);
			String[] expected_Results= processExpectedResults(testCaseDetails.getExpected_result());
			validateResults(expected_Results);
			testCaseResults = new TestCaseResults();
			testCaseResults.setActual_result(output.keySet().toString());
			testCaseResults.setExpected_result(testCaseDetails.getExpected_result());
			testCaseResults.setTest_case_name(testCaseDetails.getTest_case_name());
			testCaseResults.setTest_status("Pass");
			testCaseResultsList.add(testCaseResults);
			
		}
		
		login.insertTestCaseResults(testCaseResultsList);
		
		
	}
	
	private String[] processExpectedResults(String expected_result)

	{
		String[] processedResults = expected_result.split(",");
		return processedResults;
	}
	
	
	private void validateResults(String[] expectedResults)
	{
		
		for(String value : expectedResults)
		{
			assertTrue(output.containsKey(value));
		}
	}

}
