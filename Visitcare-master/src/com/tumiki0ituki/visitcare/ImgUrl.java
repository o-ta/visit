/*
 * 画像送信用URL取得
 * サーバーから画像送信用のURLをjsonで取得するためのクラス
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

public class ImgUrl {
	
	//コンストラクタ省略
	
	/**画像送信用URLを入れる変数*/
	private String url;

	/**画像送信用URLのゲッター*/
	public String getUrl() {
		return url;
	}

	/**画像送信用URLのセッター（JSONから）*/
	public void setUrl(String url) {
		this.url = url;
	}
}

