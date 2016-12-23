package com.coolweather.app.service;

import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;


public class AutoUpdateService extends Service {
	
	private SharedPreferences pref;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				updateWeather();
			}
			
		}).start();
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		pref = this.getSharedPreferences("user", Context.MODE_PRIVATE);
		int frequence = pref.getInt("frequency", 8*60);
		
		//当更新频率不为0时，更新启动天气更新
		if (frequence != 0) {
			
			long triggerAtTime = SystemClock.elapsedRealtime() + frequence*60*1000;
			Intent i = new Intent(this, AutoUpdateReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
			manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);	
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 更新天气信息
	 */
	private void updateWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + 
				".html";
		String type = "weatherCode";
		HttpUtil.sendHttpRequest(address, type, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}
			
		});
	}
}
