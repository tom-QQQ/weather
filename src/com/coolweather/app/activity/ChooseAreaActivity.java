package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listViewProvince;
	private ListView listViewCity;
	private ListView listViewCounty;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	
	/**
	 * 市列表
	 */
	private List<City> cityList;
	
	/**
	 * 县列表
	 */
	private List<Country> countryList;
	
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	
	/**
	 * 选中的城市
	 */
	private City selectedCity;
	
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		titleText = (TextView) findViewById(R.id.title_text);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		listViewProvince = (ListView) findViewById(R.id.list_view_province);
		listViewCity = (ListView) findViewById(R.id.list_view_city);
		listViewCounty = (ListView) findViewById(R.id.list_view_county);
		adapter = new ArrayAdapter<String> (this, 
				android.R.layout.simple_list_item_1,dataList);
		listViewProvince.setAdapter(adapter);
		listViewCity.setAdapter(adapter);
		listViewCounty.setAdapter(adapter);
		
		
		listViewProvince.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, 
					long arg3) {
				if (currentLevel == LEVEL_PROVINCE) {
					//获取被点击的对象
					selectedProvince = provinceList.get(index);
					//返回省级列表再进入市级列表时，会滑回到顶端
					listViewCity.smoothScrollToPosition(0);
					//完全隐藏省级列表
					listViewProvince.setVisibility(View.GONE);
					//显示市级列表
					listViewCity.setVisibility(View.VISIBLE);
					queryCities();
				} 
			}			
		});
		
		listViewCity.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, 
					long id) {
				if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					//完全隐藏市级列表
					listViewCity.setVisibility(View.GONE);
					//显示区县级列表
					listViewCounty.setVisibility(View.VISIBLE);
					queryCounties();
				}
			}
			
		});
		queryProvinces();		
 	}

	/**
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到，再去服务器上查询
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			//此数据为公用，用clear()方法防止其他数据干扰
			dataList.clear();
			for (Province province: provinceList) {
				dataList.add(province.getProvinceName());
			}
			//内容改变时强制刷新
			adapter.notifyDataSetChanged();
			listViewCity.setSelection(0);
			
			String China = (String) this.getResources().getString(R.string.China);
			titleText.setText(China);
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/**
	 * 查询选中省内的所有市，优先从数据库查询，如果没有查询到
	 */
	protected void queryCities() {
		cityList = coolWeatherDB.loadCity(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city. getCityName());
			}
			adapter.notifyDataSetChanged();
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	protected void queryCounties() {
		countryList = coolWeatherDB.loadCountry(selectedCity.getId());
		if (countryList.size() > 0) {
			dataList.clear();
			for (Country country: countryList) {
				dataList.add(country.getCountryName());
			}
			adapter.notifyDataSetChanged();
			listViewCounty.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvinceResponse(coolWeatherDB, response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolWeatherDB, response, 
							selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(coolWeatherDB, response, 
							selectedCity.getId());
				}
				
				if (result) {
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					}); 
				}
			}

			@Override
			public void onError(Exception e) {
				//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeProgressDialog();
						String lost = (String) ChooseAreaActivity.
								this.getResources().getString(R.string.load_lost);
						Toast.makeText(ChooseAreaActivity.this, lost, 
								Toast.LENGTH_SHORT).show();
					}
					
				});
			}
			
		});
	}

	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			String loading = (String) ChooseAreaActivity.
					this.getResources().getString(R.string.loading);
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(loading);
			progressDialog.setCanceledOnTouchOutside(false);
		}
	}
	
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 捕获Back按键，根据当前的级别来判断，此时应该返回市列表，省列表，还是直接退出
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			//完全隐藏区县级列表
			listViewCounty.setVisibility(View.GONE);
			//显示市级列表
			listViewCity.setVisibility(View.VISIBLE);			
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {			
			//完全隐藏市级列表
			listViewCity.setVisibility(View.GONE);
			//显示省级列表
			listViewProvince.setVisibility(View.VISIBLE);
			queryProvinces();
		} else {
			finish();
		}
	}
	
}
