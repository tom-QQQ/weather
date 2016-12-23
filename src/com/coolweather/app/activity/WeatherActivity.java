package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{

	private LinearLayout weatherInfoLayout;
	
	/**
	 * ������ʾ������
	 */
	private TextView cityNameText;
	
	/**
	 * ������ʾͬ��״̬�򷢲�ʱ��
	 */
	private TextView publishText;
	
	/**
	 * ������ʾ����������Ϣ
	 */
	private TextView weatherDespText;
	
	/**
	 * ������ʾ����1
	 */
	private TextView temp1Text;
	
	/**
	 * ������ʾ����2
	 */
	private TextView temp2Text;
	
	/**
	 * ������ʾ��ǰ����
	 */
	private TextView currentDateText;
	
	/**
	 * �л����а�ť
	 */
	private Button switchCity;
	
	/**
	 * ����������ť
	 */
	private Button refreshWeather;
	
	protected static int onlyOne = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
				
		//��ʼ����������
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
		//��ȡChooseAreaActivity��Intent���ݹ������ؼ���������
		String countyCode = getIntent().getStringExtra("county_code");
		//ֻҪcountyCode��Ϊnull��""�ͷ�����
		if (!TextUtils.isEmpty(countyCode)) {
			//���ؼ�����ʱ��ȥ��ѯ����
			String synchronizeing = (String)this.getResources().
					getString(R.string.syncing);
			publishText.setText(synchronizeing);
			//��ʱ������ʾ������Ϣ�Ĳ���
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			//��ʱ���س�����
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			//û���ؼ�����ʱ��ֱ����ʾ��������
			showWeather();
		}
	}
	
	/**
	 * �����˵�
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * ����˵���Ӧ�¼�
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//��ת����������Ƶ�ʻ
		case R.id.action_settings:
			Intent intent = new Intent(this, UpdateFrequencyActivity.class);
			startActivity(intent);
		}
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
			
		case R.id.refresh_weather:
			String syncing = (String) this.getResources().getString(R.string.syncing);
			publishText.setText(syncing);
			SharedPreferences prefs = PreferenceManager.
					getDefaultSharedPreferences(this);
			//��SharedPreferences�ļ��ж�ȡ��������
			String weatherCode = prefs.getString("weather_code", "");
			//��ȡ�Ĵ��Ų�Ϊ�յĻ������²�ѯ����
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
			
		default:
			break;
		}
	}

	/**
	 * ��ѯ�ؼ�����������Ӧ����������
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + 
				".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * ��ѯ������������Ӧ������,�ӿ�Ϊ�й�������,�������ݸ�ʽΪjson
	 */
	protected void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + 
				".html";
		queryFromServer(address, "weatherCode");
	}
	
	/**
	 * ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ
	 */
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, type, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//�ӷ��������ص������н�������������,ע���Ƿ�б��
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					//������������ص�������Ϣ
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					//�漰UI���������߳��н���
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}						
					}); 
				}
			}
			@Override
			public void onError(final Exception e) {
				runOnUiThread(new Runnable () {
					@Override
					public void run() {
						e.printStackTrace();
						String lost = (String) WeatherActivity.this.getResources().
								getString(R.string.sync_lost);
						publishText.setText(lost);
					}					
				});
			}			
		});
	}

	/**
	 * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ��������,��������̨���������ķ���
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText(prefs.getString("publish_time", ""));
		currentDateText.setText(prefs.getString("current_date", ""));
		//��ȡ�ɹ����ݺ�����ʾ���صĲ��ֺ�view
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
}
