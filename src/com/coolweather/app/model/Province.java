package com.coolweather.app.model;

public class Province {
	
	private int id;
	private String provinceName;
	private String provinceCode;
	
	//id
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	//provinceName
	public String getProvinceName() {
		return provinceName;
	}
	
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	
	//provinceCode
	public String getProvinceCode() {
		return provinceCode;
	}
	
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}
}
