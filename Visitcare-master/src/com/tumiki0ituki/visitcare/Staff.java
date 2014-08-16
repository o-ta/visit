/*
 * スタッフ名取得
 * サーバーからスタッフ名をjsonで取得するためのクラス
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

public class Staff {
	/** スタッフID */
	private int staff_id;
	
	/** スタッフの氏名 */
	private String staff_name;
	
	/** 緯度*/
	private double latitude;
	
	/** 経度 */
	private double longitude;
	
	/** 最終更新日 */
	private String last_update_time;

	/**
	 * スタッフIDを取得します
	 */
	public int getStaff_id() {
		return staff_id;
	}

	/**
	 * スタッフIDを設定します
	 */
	public void setStaff_id(int staff_id) {
		this.staff_id = staff_id;
	}

	/**
	 * スタッフ名を取得します
	 */
	public String getStaff_name() {
		return staff_name;
	}

	/**
	 * スタッフ名を設定します
	 */
	public void setStaff_name(String staff_name) {
		this.staff_name = staff_name;
	}

	/**
	 * 緯度を取得します
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * 緯度を設定します
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * 経度を取得します
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * 経度を設定します
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * 最終更新時刻を取得します
	 */
	public String getLast_update_time() {
		return last_update_time;
	}

	/**
	 * 最終更新時刻を設定します
	 */
	public void setLast_update_time(String last_update_time) {
		this.last_update_time = last_update_time;
	}

}
