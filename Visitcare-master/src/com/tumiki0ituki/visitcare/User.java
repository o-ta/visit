/*
 * 被介護者名取得
 * サーバーから被介護者名をjsonで取得するためのクラス
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

public class User {
	/**被介護者番号のメンバ変数*/
	private String user_id;

	/**被介護者名のメンバ変数*/
	private String user_name;

	//コンストラクタ省略

	/**被介護者番号のゲッター*/
	public String getUser_id() {
		return user_id;
	}

	/**被介護者名のゲッター*/
	public String getUser_name() {
		return user_name;
	}

	/**被介護者番号のセッター*/
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	/**被介護者名のセッター*/
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	
}
