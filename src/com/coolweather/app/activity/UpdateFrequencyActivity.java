package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateFrequencyActivity extends Activity {
	
	/**
	 * EditText�������ƣ���������С��5λ������
	 */
	private EditText updateFrequency;
	private Button setFrequency;
	/**
	 * ��ǰ����Ƶ��
	 */
	private TextView currentFrequency;
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.frequence_setting);
		
		currentFrequency = (TextView) findViewById(R.id.current_frequency);	
		updateFrequency = (EditText) findViewById(R.id.update_frequence);
		
		/**
		 * ��SharedPerferences�����ڶ�����֮�乫��
		 */
		pref = getSharedPreferences("user", Context.MODE_PRIVATE);
		editor = pref.edit();
		
		//�˷������ڵ�һ�ΰ�װʱִ��һ�Σ����ҵ��û��޸ĸ���Ƶ�ʺ󣬼�ʹ�ٴΰ�װҲ����ִ�У������޸Ļ�Ĭ��ֵ
		if (WeatherActivity.onlyOne == 0 && pref.getInt("frequency", 8*60) == 480) {
			//�洢Ĭ��ֵ��ÿ8*60���Ӹ���һ��
			editor.putInt("frequency", 8*60);
			//�ύֵ
			editor.commit();
		}
			//ȡ��Ĭ��ֵ����ʾ
			int lastFrequency = pref.getInt("frequency", 8*60);
			currentFrequency.setText(lastFrequency + "");
			WeatherActivity.onlyOne =  1;
		
				
		setFrequency = (Button) findViewById(R.id.set_frequency);
		setFrequency.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String value = updateFrequency.getText().toString();
				int frequency = Integer.parseInt(value);
				editor.putInt("frequency", frequency);
				editor.commit();
				currentFrequency.setText(frequency + "");
				
				//�������óɹ���ʾ
				Toast.makeText(UpdateFrequencyActivity.this, R.string.setting_succeed, 
						Toast.LENGTH_SHORT).show();
				
				//�����������������Ը�����������Ƶ��
				Intent i = new Intent(UpdateFrequencyActivity.this,
						AutoUpdateService.class);
				startService(i);
				//������������
				finish();
			}
			
		});
	}
}
