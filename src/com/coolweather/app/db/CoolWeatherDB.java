package com.coolweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	
	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "cool_weather";
	
	/**
	 * 数据库版本
	 */
	public static final int VERSION = 1;
	
	private static CoolWeatherDB coolWeatherDB;
	private SQLiteDatabase db;
	
	/**
	 * 将构造方法私有化
	 */
	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, 
				null, VERSION);
		db = dbHelper.getWritableDatabase();
	}
	
	/**
	 * 获取CoolWeatherDB的实例
	 */
	//线程锁，仅允许一个线程执行，单例类
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB != null) {
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
	public List<Province> loadProvince() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor =db.query("province_name", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				
				//遍历Cursor对象，取出数据，放入province实例中
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.
						getColumnIndex("provinceName")));
				province.setProvinceCode(cursor.getString(cursor.
						getColumnIndex("provinceCode")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		return list;		
	}
}
