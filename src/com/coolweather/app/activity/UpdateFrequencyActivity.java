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
	 * EditText已做限制，必须输入小于5位的数字
	 */
	private EditText updateFrequency;
	private Button setFrequency;
	/**
	 * 当前更新频率
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
		 * 此SharedPerferences可以在多个组件之间公用
		 */
		pref = getSharedPreferences("user", Context.MODE_PRIVATE);
		editor = pref.edit();
		
		//此方法会在第一次安装时执行一次，并且当用户修改更新频率后，即使再次安装也不会执行，除非修改回默认值
		if (WeatherActivity.onlyOne == 0 && pref.getInt("frequency", 8*60) == 480) {
			//存储默认值，每8*60分钟更新一次
			editor.putInt("frequency", 8*60);
			//提交值
			editor.commit();
		}
			//取出默认值并显示
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
				
				//弹出设置成功提示
				Toast.makeText(UpdateFrequencyActivity.this, R.string.setting_succeed, 
						Toast.LENGTH_SHORT).show();
				
				//启动更新天气服务以更新天气更新频率
				Intent i = new Intent(UpdateFrequencyActivity.this,
						AutoUpdateService.class);
				startService(i);
				//返回天气界面
				finish();
			}
			
		});
	}
}
