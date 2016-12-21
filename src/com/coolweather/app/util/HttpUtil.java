package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpUtil {

	public static void sendHttpRequest(final String address, final String type,
			final HttpCallbackListener listener) {
		new Thread (new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					String value = null;					
					//当参数类型为weatherCode时，采用json解析方法
					if ("weatherCode".equals(type)) {
						HttpClient httpClient = new DefaultHttpClient();
						HttpGet httpGet = new HttpGet(address.toString());
						HttpResponse httpResponse = httpClient.execute(httpGet);
						if (httpResponse.getStatusLine().getStatusCode() == 200) {
							HttpEntity entity = httpResponse.getEntity();
							//json文件包含中文，用GB2312
							value = EntityUtils.toString(entity, "utf-8");
						}						
						//当参数类型为其他时，采用普通解析方法
					} else  {
						URL url = new URL(address);
						connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("GET");
						connection.setConnectTimeout(8000);
						connection.setReadTimeout(8000);
						InputStream in = connection.getInputStream();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(in));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						value = response.toString();
					}
					if (listener != null) {
						//回调finish方法
						listener.onFinish(value);						
					}
					
				} catch (Exception e) {
					if (listener != null) {
						//回调onError方法
						listener.onError(e);
					}
					
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
			//start()真正实现多线程
		}).start();
	}
}
