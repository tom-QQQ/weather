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
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	
	/**
	 * ���б�
	 */
	private List<City> cityList;
	
	/**
	 * ���б�
	 */
	private List<Country> countryList;
	
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	
	/**
	 * ��ǰѡ�еļ���
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
					//��ȡ������Ķ���
					selectedProvince = provinceList.get(index);
					//����ʡ���б��ٽ����м��б�ʱ���Ử�ص�����
					listViewCity.smoothScrollToPosition(0);
					//��ȫ����ʡ���б�
					listViewProvince.setVisibility(View.GONE);
					//��ʾ�м��б�
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
					//��ȫ�����м��б�
					listViewCity.setVisibility(View.GONE);
					//��ʾ���ؼ��б�
					listViewCounty.setVisibility(View.VISIBLE);
					queryCounties();
				}
			}
			
		});
		queryProvinces();		
 	}

	/**
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ������ȥ�������ϲ�ѯ
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			//������Ϊ���ã���clear()������ֹ�������ݸ���
			dataList.clear();
			for (Province province: provinceList) {
				dataList.add(province.getProvinceName());
			}
			//���ݸı�ʱǿ��ˢ��
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
	 * ��ѯѡ��ʡ�ڵ������У����ȴ����ݿ��ѯ�����û�в�ѯ��
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
	 * ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
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
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ��������
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
					//ͨ��runOnUiThread()�����ص����̴߳����߼�
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
				//ͨ��runOnUiThread()�����ص����̴߳����߼�
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
	 * ��ʾ���ȶԻ���
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
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳�
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			//��ȫ�������ؼ��б�
			listViewCounty.setVisibility(View.GONE);
			//��ʾ�м��б�
			listViewCity.setVisibility(View.VISIBLE);			
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {			
			//��ȫ�����м��б�
			listViewCity.setVisibility(View.GONE);
			//��ʾʡ���б�
			listViewProvince.setVisibility(View.VISIBLE);
			queryProvinces();
		} else {
			finish();
		}
	}
	
}
