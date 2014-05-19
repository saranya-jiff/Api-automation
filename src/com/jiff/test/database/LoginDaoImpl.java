package com.jiff.test.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jiff.test.vo.TestCaseDetails;
import com.jiff.test.vo.TestCaseResults;

public class LoginDaoImpl extends BaseDaoImpl implements LoginDao{
	
	private static Connection conn;
	private static PreparedStatement stmt;
	private static ResultSet rs;

	@Override
	public Map<String, String> getTestData() throws SQLException {
		
		String sql = "select cp.url,cp.authorization,c.username,c.password,cp.content_type,cp.accept_language,cp.accept,cp.connection from credential c,connection_properties cp where c.id = cp.id";
		int colIndex = 1;
		String colName;
		Map<String,String> credentials = new LinkedHashMap<String,String>();
		conn = getConnection();
		try {
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			java.sql.ResultSetMetaData rsm = rs.getMetaData();
			while(rs.next())
			{
				while (colIndex < 8)
				{
					colName = rsm.getColumnName(colIndex);
				    credentials.put(colName, rs.getString(colName));
				    colIndex++;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally
		{
			rs.close();
			stmt.close();
			conn.close();
		}
		
		return credentials;
	}

	@Override
	public List<TestCaseDetails> getTestCaseDetails() throws SQLException {


		String sql = "select cp.url,cp.authorization,c.username,c.password,cp.content_type,cp.accept_language,cp.accept,cp.connection,ltc.test_case_name,ltc.expected_result from credential c,connection_properties cp,login_test_case ltc where c.id = cp.id and ltc.id = cp.id";
		TestCaseDetails testCaseDetails;
		List<TestCaseDetails> testCaseDetailsList = new ArrayList<TestCaseDetails>();

		conn = getConnection();
		try {
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next())
			{
				testCaseDetails = new TestCaseDetails();
				testCaseDetails.setUrl(rs.getString(1));
				testCaseDetails.setAuthorization(rs.getString(2));
				testCaseDetails.setUsername(rs.getString(3));
				testCaseDetails.setPassword(rs.getString(4));
				testCaseDetails.setContent_type(rs.getString(5));
				testCaseDetails.setAccept_language(rs.getString(6));
				testCaseDetails.setAccept(rs.getString(7));
				testCaseDetails.setConnection(rs.getString(8));
				testCaseDetails.setTest_case_name(rs.getString(9));
				testCaseDetails.setExpected_result(rs.getString(10));
				testCaseDetailsList.add(testCaseDetails);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally
		{
			rs.close();
			stmt.close();
			conn.close();
		}
		
		return testCaseDetailsList;
	}

	@Override
	public void insertTestCaseResults(List<TestCaseResults> testCaseResults) throws SQLException {
		String sql = "insert into login_test_case_execution_results values(?,?,?,?,?)";

		conn = getConnection();
		try {
			stmt = conn.prepareStatement(sql);
			for(TestCaseResults testCaseResult : testCaseResults)
			{
				stmt.setDate(1, testCaseResult.getDate());
				stmt.setString(2, testCaseResult.getTest_case_name());
				stmt.setString(3, testCaseResult.getExpected_result());
				stmt.setString(4, testCaseResult.getActual_result());
				stmt.setString(5, testCaseResult.getTest_status());
				stmt.addBatch();
			}
			stmt.executeBatch();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally
		{
			stmt.close();
			conn.close();
		}
		
	}

}
