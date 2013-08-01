package com.whueric.weathernow.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonAPI {
	private static final String BASE_URL = "http://api.douban.com/movie/subject/";
	private static final String EXTENSION = "?alt=json";
	private static final String TAG = "API";
	private static final String USER_AGENT = "Mozilla/4.5";
	private static final String SOURCE_PARAMETER = "fanfou api";

	
	/**
	 * 获取电影的内容
	 * @return JSONArray
	 * @throws JSONException 
	 * @throws ConnectionException 
	 */
	public JSONObject getMovie(String sbj) throws JSONException, Exception {
		//http://api.douban.com/movie/subject/2277018?alt=json
		return new JSONObject(getRequest(sbj));
	}
	
	/**
	 * 向饭否api发送get请求，url需按照饭否api要求写，返回从饭否取得的信息。
	 * 
	 * @param url
	 * @return String
	 */
	protected String getRequest(String url) throws Exception {
		return getRequest(url, new DefaultHttpClient(new BasicHttpParams()));
	}
	
	/**
	 * 向api发送get请求，url需按照api要求写，返回从饭否取得的信息。
	 * 
	 * @param url
	 * @param client
	 * @return String
	 */
	protected String getRequest(String url, DefaultHttpClient client) throws Exception {
		String result = null;
		int statusCode = 0;
		HttpGet getMethod = new HttpGet(url);
		Log.d(TAG, "do the getRequest,url="+url+"");
		try {
			getMethod.setHeader("User-Agent", USER_AGENT);
			//添加用户密码验证信息
//	    	client.getCredentialsProvider().setCredentials(
//	    			new AuthScope(null, -1),
//	    			new UsernamePasswordCredentials(mUsername, mPassword));
			
			HttpResponse httpResponse = client.execute(getMethod);
			//statusCode == 200 正常
			statusCode = httpResponse.getStatusLine().getStatusCode();
			Log.d(TAG, "statuscode = "+statusCode);
			//处理返回的httpResponse信息
			result = retrieveInputStream(httpResponse.getEntity());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			throw new Exception(e);
		} finally {
			getMethod.abort();
		}
		return result;
	}
	
	/**
	 * 处理httpResponse信息,返回String
	 * 
	 * @param httpEntity
	 * @return String
	 */
	protected String retrieveInputStream(HttpEntity httpEntity) {
		Long l = httpEntity.getContentLength();		
		int length = (int) httpEntity.getContentLength();		
		//the number of bytes of the content, or a negative number if unknown. If the content length is known but exceeds Long.MAX_VALUE, a negative number is returned.
		//length==-1，下面这句报错，println needs a message
		if (length < 0) length = 10000;
		StringBuffer stringBuffer = new StringBuffer(length);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(httpEntity.getContent(), HTTP.UTF_8);
			char buffer[] = new char[length];
			int count;
			while ((count = inputStreamReader.read(buffer, 0, length - 1)) > 0) {
				stringBuffer.append(buffer, 0, count);
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		return stringBuffer.toString();
	}
}
