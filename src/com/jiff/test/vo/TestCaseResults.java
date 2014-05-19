package com.jiff.test.vo;

import java.sql.Date;

public class TestCaseResults {
	
	private String test_case_name;
	private String expected_result;
	private String actual_result;
	private String test_status;

	
	

	public Date getDate() {
		return new Date(System.currentTimeMillis());
	}
	public String getTest_case_name() {
		return test_case_name;
	}
	public void setTest_case_name(String test_case_name) {
		this.test_case_name = test_case_name;
	}
	public String getExpected_result() {
		return expected_result;
	}
	public void setExpected_result(String expected_result) {
		this.expected_result = expected_result;
	}
	public String getActual_result() {
		return actual_result;
	}
	public void setActual_result(String actual_result) {
		this.actual_result = actual_result;
	}
	public String getTest_status() {
		return test_status;
	}
	public void setTest_status(String test_status) {
		this.test_status = test_status;
	}
	
	
}
