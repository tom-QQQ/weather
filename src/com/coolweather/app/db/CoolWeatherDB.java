package com.coolweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	
	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "CoolWeather.db";
	
	/**
	 * 数据库版本
	 */
	public static final int VERSION = 1;
	
	private static CoolWeatherDB coolWeatherDB;
	private SQLiteDatabase db;
	
	/**
	 * 将构造方法私有化,创建数据库
	 */
	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, 
				null, VERSION);
		db = dbHelper.getWritableDatabase();
	}
	
	/**
	 * 获取CoolWeatherDB的实例
	 */
	//线程锁，同时间仅允许一个线程执行，单例类
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	
	/**
	 * 将Province实例存储到数据库
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			//使用ContentValues来对要添加的数据进行组装
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	
	/**
	 * 从数据库读取全国所有的省份信息
	 */
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor =db.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				
				//遍历Cursor对象，取出数据，放入province实例中
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.
						getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.
						getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;		
	}
	
	/**
	 * 将City实例存储到数据库
	 */
	public void saveCity(City city) {
		if (city != null) {
			//使用ContentValues来对要添加的数据进行组装
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}
	
	/**
	 * 从数据库读取指定省所有的城市信息
	 */
	public List<City> loadCity(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor =db.query("City", null, "province_id = ?", 
				new String[] {String.valueOf(provinceId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.
						getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.
						getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;		
	}
	
	/**
	 * 将Country实例存储到数据库
	 */
	public void saveCountry(Country country) {
		if (country != null) {
			//使用ContentValues来对要添加的数据进行组装
			ContentValues values = new ContentValues();
			values.put("country_name", country.getCountryName());
			values.put("country_code", country.getCountryCode());
			values.put("city_id", country.getCityId());
			db.insert("Country", null, values);
		}
	}
	
	/**
	 * 从数据库读取全国所有区县信息
	 */
	public List<Country> loadCountry(int cityId) {
		List<Country> list = new ArrayList<Country>();
		Cursor cursor =db.query("Country", null, "city_id = ?", 
				new String[] {String.valueOf(cityId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Country country = new Country();
				country.setId(cursor.getInt(cursor.getColumnIndex("id")));
				country.setCountryName(cursor.getString(cursor.
						getColumnIndex("country_name")));
				country.setCountryCode(cursor.getString(cursor.
						getColumnIndex("country_code")));
				country.setCityId(cityId);
				list.add(country);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;		
	}
}
