/*
 * 訪問先リスト取得
 * サーバーから訪問先リストをjsonで受け取るためのクラス
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

public class Schedule {
	
	/**業務IDのメンバ変数*/
	private int work_id;
	
	/**訪問順のメンバ変数*/
	private int turn;
	
	/**被介護者番号のメンバ変数*/
	private int user_id;
	
	/**被介護者のメンバ変数*/
	private String user_name;
	
	/**訪問先緯度のメンバ変数*/
	private double latitude;
	
	/**訪問先経度のメンバ変数*/
	private double longitude;
	
	/**訪問予定時間のメンバ変数*/
	private String schedule_time;
	
	/**訪問先住所のメンバ変数*/
	private String address;
	
	/**ステータス（訪問実績入力有無）のメンバ変数*/
	private int status;
	
	/**画像登録有無フラグのメンバ変数*/
	private int imagefix;
	
	//コンストラクタ省略

	/**業務IDのゲッター*/
	public int getwork_id() {
		return work_id;
	}

	/**業務IDのセッター*/
	public void setwork_id(int work_id) {
		this.work_id = work_id;
	}

	/**訪問順のゲッター*/
	public int getturn() {
		return turn;
	}

	/**訪問順のセッター*/
	public void setturn(int turn) {
		this.turn = turn;
	}

	/**被介護者番号のゲッター*/
	public int getuser_id() {
		return user_id;
	}

	/**被介護者番号のセッター*/
	public void setuser_id(int user_id) {
		this.user_id = user_id;
	}

	/**被介護者名のゲッター*/
	public String getuser_name() {
		return user_name;
	}

	/**被介護者名のセッター*/
	public void setuser_name(String user_name) {
		this.user_name = user_name;
	}

	/**訪問先の緯度のゲッター*/
	public double getlatitude() {
		return latitude;
	}

	/**訪問先の緯度のセッター*/
	public void setlatitude(double latitude) {
		this.latitude = latitude;
	}

	/**訪問先の経度のゲッター*/
	public double getlongitude() {
		return longitude;
	}

	/**訪問先の経度のセッター*/
	public void setlongitude(double longitude) {
		this.longitude = longitude;
	}

	/**訪問予定時間のゲッター*/
	public String getschedule_time() {
		return schedule_time;
	}

	/**訪問予定時間のセッター*/
	public void setschedule_time(String schedule_time) {
		this.schedule_time = schedule_time;
	}

	/**訪問先住所のゲッター*/
	public String getaddress() {
		return address;
	}

	/**訪問先住所のセッター*/
	public void setaddress(String address) {
		this.address = address;
	}

	/**ステータス（訪問実績入力有無）のゲッター*/
	public int getstatus() {
		return status;
	}

	/**ステータス（訪問実績入力有無）のセッター*/
	public void setstatus(int status) {
		this.status = status;
	}

	/**画像登録有無フラグのゲッター*/
	public int getImagefix() {
		return imagefix;
	}

	/**画像登録有無フラグのセッター*/
	public void setImagefix(int imagefix) {
		this.imagefix = imagefix;
	}
}

