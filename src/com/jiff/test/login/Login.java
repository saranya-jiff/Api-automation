package com.jiff.test.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;

import com.jiff.test.database.LoginDao;
import com.jiff.test.database.LoginDaoImpl;
import com.jiff.test.vo.TestCaseDetails;
import com.jiff.test.vo.TestCaseResults;

public class Login
{

	LoginDao loginDao;
	
	public Login()
	{
		loginDao = new LoginDaoImpl();
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> verifyLoginApi(TestCaseDetails testCaseDetails)
	{
		int statuscode;
		String url = testCaseDetails.getUrl();
		Map<String, String> oauthLoginResponse = new LinkedHashMap<String,String>();
		
		
		HttpClient httpclient = HttpClientBuilder.create().build();
	
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Accept", testCaseDetails.getAccept());
		httpPost.setHeader("Content-type",testCaseDetails.getContent_type());
		httpPost.setHeader("Accept-Language", testCaseDetails.getAccept_language());
		httpPost.setHeader("Connection", testCaseDetails.getConnection());
		httpPost.setHeader("Authorization" ,testCaseDetails.getAuthorization());
		httpPost.setEntity(getPostParameters(testCaseDetails.getUsername(),testCaseDetails.getPassword()));
		
		try {
			HttpResponse responseVal = httpclient.execute(httpPost);
			JSONParser parser=new JSONParser();
			
			
			statuscode = responseVal.getStatusLine().getStatusCode();
			
			
			if(statuscode == 200)
			{
			oauthLoginResponse = (Map<String, String>)
					parser.parse(EntityUtils.toString(responseVal.getEntity()));

			}
			
			
			oauthLoginResponse.put(String.valueOf(statuscode),"statuscode");
			
		    for (Map.Entry<String, String> entry : oauthLoginResponse.entrySet()) 
		    {
		        System.out.println(String.format("  %s = %s", entry.getKey(), entry.getValue()));
		    }
		    System.out.println("");
		    
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
		
		
		return oauthLoginResponse;
	}
	
	public List<TestCaseDetails> getTestCaseDetails() throws SQLException
	{
		return loginDao.getTestCaseDetails();
	}
	
	public void insertTestCaseResults(List<TestCaseResults> testCaseResults)
	{
		try {
			loginDao.insertTestCaseResults(testCaseResults);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static UrlEncodedFormEntity getPostParameters(String username,String password)
	{
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();

	   formparams.add(new BasicNameValuePair("username", username));
	   formparams.add(new BasicNameValuePair("password", password));
	    
	    UrlEncodedFormEntity entity = null;
	    try {
	      entity = new UrlEncodedFormEntity(formparams, "UTF-8");
	    } catch (UnsupportedEncodingException exception) {
	      
	    }
	    return entity;
	}
	

}
